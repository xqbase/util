package com.xqbase.util.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Base64;
import com.xqbase.util.Log;
import com.xqbase.util.Numbers;
import com.xqbase.util.SocketPool;
import com.xqbase.util.Streams;
import com.xqbase.util.Strings;

public class ProxyPassServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final int RECV_MAX_CHUNK_SIZE = 4095;
	private static final int RESP_MAX_SIZE = 65536;
	private static final int REQ_MAX_SIZE = 4096;

	private static final String HEX_DIGITS = "0123456789ABCDEF";

	private static final String[] SKIP_HEADERS = {
		"Access-Control-Request-Headers",
		"Access-Control-Request-Method",
		"Connection",
		"Content-Length",
		"Content-Transfer-Encoding",
		// "Host",
		"Keep-Alive",
		// "Origin",
		"Trailer",
		"Transfer-Encoding",
		"Upgrade",
		"Via",
		"X-Forwarded-For",
		"X-Forwarded-Proto",
		"X-Forwarded-SSL-Session-ID",
		"X-Forwarded-SSL-Cipher",
		"X-Forwarded-Certificates",
	};

	static class ClientException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	private String basePath, redirect;
	private SocketPool pool;
	private Map<String, String> headerMap = new LinkedHashMap<>();
	private Set<String> skipHeaders = new HashSet<>();

	@Override
	public void init() throws ServletException {
		String proxyPass = getInitParameter("proxyPass");
		int timeout = Numbers.parseInt(getInitParameter("timeout"), 60000);
		URL url;
		try {
			url = new URL(proxyPass);
		} catch (IOException e) {
			throw new ServletException(e);
		}
		int port = url.getPort();
		port = port < 0 ? url.getDefaultPort() : port;
		pool = new SocketPool(url.getHost(), port, "https".equals(url.getProtocol()), timeout);
		basePath = url.getPath();
		redirect = getInitParameter("redirect");

		Enumeration<?> en = getInitParameterNames();
		while (en.hasMoreElements()) {
			String name = (String) en.nextElement();
			if (name.startsWith("Header-")) {
				headerMap.put(name.substring(7), getInitParameter(name));
			}
		}
		for (String header : SKIP_HEADERS) {
			skipHeaders.add(header.toUpperCase());
		}
		for (String header : headerMap.keySet()) {
			skipHeaders.add(header.toUpperCase());
		}
	}

	@Override
	public void destroy() {
		pool.close();
	}

	private static void write(OutputStream out, String s) throws IOException {
		out.write(s.getBytes(StandardCharsets.ISO_8859_1));
	}

	private static void writeln(OutputStream out) throws IOException {
		out.write('\r');
		out.write('\n');
	}

	private static void writeHeader(OutputStream out, String key,
			String value) throws IOException {
		write(out, key);
		out.write(':');
		out.write(' ');
		write(out, value);
		writeln(out);
	}

	private static void copyResponse(InputStream inSocket, OutputStream outResp,
			byte[] buffer, int length) throws ClientException, IOException {
		int bytesToRead = length;
		while (bytesToRead > 0) {
			int bytesRead = inSocket.read(buffer, 0,
					Math.min(RESP_MAX_SIZE, bytesToRead));
			if (bytesRead < 0) {
				throw new IOException("Connection Lost");
			}
			if (bytesRead == 0) {
				throw new IOException("Zero Bytes Read");
			}
			bytesToRead -= bytesRead;
			try {
				outResp.write(buffer, 0, bytesRead);
			} catch (IOException e) {
				// Close if too much left
				if (bytesToRead > RESP_MAX_SIZE) {
					throw new ClientException();
				}
			}
		}
	}

	private static final int METHOD_HEAD = 1;
	private static final List<String> METHOD_VALUE = Arrays.asList("GET", "HEAD");

	@SuppressWarnings("resource")
	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		String query = req.getQueryString();
		String fullPath = basePath + req.getPathInfo() +
				(Strings.isEmpty(query) ? "" : "?" + query);
		String method = req.getMethod();
		int methodType = METHOD_VALUE.indexOf(method);

		try (SocketPool.Entry socketEntry = pool.borrow()) {
			// Request Head
			Socket socket = socketEntry.getObject();
			BufferedOutputStream outSocket = new
					BufferedOutputStream(socket.getOutputStream());
			write(outSocket, method);
			outSocket.write(' ');
			write(outSocket, fullPath);
			write(outSocket, " HTTP/1.1");
			writeln(outSocket);

			Enumeration<?> en = req.getHeaderNames();
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				if (skipHeaders.contains(key.toUpperCase())) {
					continue;
				}
				Enumeration<?> en2 = req.getHeaders(key);
				while (en2.hasMoreElements()) {
					String value = (String) en2.nextElement();
					if (key.equalsIgnoreCase("Destination")) {
						// See http://httpd.apache.org/docs/current/mod/mod_headers.html, Example 5
						if (req.isSecure() && value.startsWith("https:")) {
							value = "http:" + value.substring(6);
						}
					}
					writeHeader(outSocket, key, value);
				}
			}
			for (Map.Entry<String, String> entry : headerMap.entrySet()) {
				writeHeader(outSocket, entry.getKey(), entry.getValue());
			}
			writeHeader(outSocket, "X-Forwarded-For", req.getRemoteAddr());
			writeHeader(outSocket, "X-Forwarded-Proto", req.getScheme());
			Object sslSessionId = req.getAttribute("javax.servlet.request.ssl_session_id");
			if (sslSessionId instanceof String) {
				writeHeader(outSocket, "X-Forwarded-SSL-Session-ID", (String) sslSessionId);
			}
			Object cipherSuite = req.getAttribute("javax.servlet.request.cipher_suite");
			if (cipherSuite instanceof String) {
				writeHeader(outSocket, "X-Forwarded-SSL-Cipher", (String) cipherSuite);
			}
			Object certificates = req.getAttribute("javax.servlet.request.X509Certificate");
			if (certificates instanceof X509Certificate[]) {
				writeHeader(outSocket, "X-Forwarded-Certificates",
						Base64.encode(CertificateFactory.
						getInstance("X509").generateCertPath(Arrays.
						asList((X509Certificate[]) certificates)).getEncoded("PKCS7")));
			}
			writeHeader(outSocket, "Connection", "Keep-Alive");

			// Request Body
			int contentLength = req.getContentLength();
			if (contentLength > 0) {
				writeHeader(outSocket, "Content-Length", "" + contentLength);
				writeln(outSocket);
				// Streams.copy(req.getInputStream(), outSocket);
				// Should throw ClientException when req.getInputStream() broken
				InputStream in = req.getInputStream();
				byte[] buffer = new byte[REQ_MAX_SIZE];
				while (true) {
					int bytesRead;
					try {
						bytesRead = in.read(buffer);
					} catch (IOException e) {
						throw new ClientException();
					}
					if (bytesRead <= 0) {
						break;
					}
					outSocket.write(buffer, 0, bytesRead);
				}
			} else if (methodType >= 0) {
				writeln(outSocket);
			} else if (contentLength == 0) {
				writeHeader(outSocket, "Content-Length", "0");
				writeln(outSocket);
			} else {
				// Request Body (Chunked)
				writeHeader(outSocket, "Transfer-Encoding", "chunked");
				InputStream inReq = req.getInputStream();
				byte[] buffer = new byte[RECV_MAX_CHUNK_SIZE + 7];
				buffer[3] = '\r';
				buffer[4] = '\n';
				int bytesRead;
				while ((bytesRead = inReq.read(buffer, 5, RECV_MAX_CHUNK_SIZE)) > 0) {
					buffer[0] = (byte) HEX_DIGITS.charAt(bytesRead / 256);
					buffer[1] = (byte) HEX_DIGITS.charAt(bytesRead / 16 % 16);
					buffer[2] = (byte) HEX_DIGITS.charAt(bytesRead % 16);
					buffer[bytesRead + 5] = '\r';
					buffer[bytesRead + 6] = '\n';
					outSocket.write(buffer, 0, bytesRead + 7);
				}
				outSocket.write('0');
				writeln(outSocket);
				writeln(outSocket);
			}
			outSocket.flush();

			// Response Head
			BufferedInputStream inSocket = new
					BufferedInputStream(socket.getInputStream());
			boolean http10 = false, close = false;
			int status = 0;
			StringBuilder sb = new StringBuilder();
			contentLength = Integer.MIN_VALUE;
			while (true) {
				int b = inSocket.read();
				if (b < 0) {
					throw new IOException("Connection Lost");
				}
				if (b == '\r') {
					continue;
				}
				if (b != '\n') {
					sb.append((char) b);
					continue;
				}
				if (sb.length() == 0) {
					if (status == HttpServletResponse.SC_CONTINUE) {
						status = 0;
						continue;
					}
					resp.setStatus(status);
					break;
				}
				if (status == 0) {
					String[] ss = sb.toString().split(" ");
					if (ss.length < 2) {
						throw new IOException("Response Error: [" + sb + "]");
					}
					status = Numbers.parseInt(ss[1]);
					if (ss[0].toUpperCase().equals("HTTP/1.0")) {
						http10 = true;
					}
				} else if (status != HttpServletResponse.SC_CONTINUE) {
					int index = sb.indexOf(": ");
					if (index >= 0) {
						String key = sb.substring(0, index);
						String value = sb.substring(index + 2);
						if (skipHeaders.contains(key.toUpperCase())) {
							if (key.equalsIgnoreCase("Content-Length")) {
								contentLength = Numbers.parseInt(value);
							} else if (key.equalsIgnoreCase("Transfer-Encoding")) {
								if (value.equalsIgnoreCase("chunked")) {
									contentLength = -1;
								} else {
									Log.w("Transfer-Encoding: [" + value + "]");
								}
							} else if (!close) {
								if (key.equalsIgnoreCase("Connection")) {
									close = value.equalsIgnoreCase("close");
								} else if (key.equalsIgnoreCase("Keep-Alive")) {
									close = true;
								}
							}
						} else {
							resp.addHeader(key, value);
						}
					}
				}
				sb.setLength(0);
			}
			if (contentLength == Integer.MIN_VALUE) {
				if (close && methodType != METHOD_HEAD) {
					http10 = true;
				} else {
					contentLength = 0;
				}
			}

			// Response Body
			OutputStream outResp = resp.getOutputStream();
			if (http10) {
				// For HTTP/1.0 response, read from stream until connection lost
				Streams.copy(inSocket, outResp, true);
				socketEntry.setValid(false);
				return;
			}
			if (contentLength == 0 || methodType == METHOD_HEAD) {
				resp.setContentLength(contentLength);
				socketEntry.setValid(!close);
				return;
			}
			byte[] buffer = new byte[RESP_MAX_SIZE];
			if (contentLength > 0) {
				resp.setContentLength(contentLength);
				copyResponse(inSocket, outResp, buffer, contentLength);
				socketEntry.setValid(!close);
				return;
			}

			/* Response Body (Chunked)
			 * >=0: waiting size and CRLF
			 * -1: waiting next block (CRLF)
			 * -2: waiting terminator (CRLF)
			 */
			int chunkSize = 0;
			while (true) {
				int b = inSocket.read();
				if (b < 0) {
					throw new IOException("Connection Lost");
				}
				if (chunkSize < 0) {
					if (b == '\n') {
						if (chunkSize == -2) {
							break;
						}
						chunkSize = 0;
					}
				} else if (b == '\n') {
					if (chunkSize == 0) {
						chunkSize = -2;
					} else {
						copyResponse(inSocket, outResp, buffer, chunkSize);
						chunkSize = -1;
					}
				} else {
					b = HEX_DIGITS.indexOf(Character.toUpperCase(b));
					if (b >= 0) {
						chunkSize = chunkSize * 16 + b;
					}
				}
			}
			socketEntry.setValid(!close);

		} catch (ClientException e) {
			// Ignored
		} catch (IOException | GeneralSecurityException e) {
			try {
				if (redirect == null) {
					resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
				} else {
					resp.sendRedirect(redirect);
				}
			} catch (IOException e_) {/**/}
			Log.w(e.getMessage());
		}
	}
}