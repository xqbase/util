package com.xqbase.util.servlet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import sun.security.pkcs.ContentInfo;
import sun.security.pkcs.PKCS7;
import sun.security.pkcs.SignerInfo;
import sun.security.x509.AlgorithmId;

import com.xqbase.util.Base64;
import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Bytes;
import com.xqbase.util.Log;
import com.xqbase.util.Numbers;
import com.xqbase.util.Pool;
import com.xqbase.util.SocketPool;

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
		"X-Pkcs7-Certificates-Base64",
	};

	private static final IOException CLIENT_EXCEPTION = new IOException(ProxyPassServlet.
			class.getName() + ".CLIENT_EXCEPTION");

	private String basePath, redirect;
	private SocketPool pool;
	private LinkedHashMap<String, String> headerMap = new LinkedHashMap<>();
	private HashSet<String> skipHeaders = new HashSet<>();

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
			byte[] buffer, int length) throws IOException {
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
					throw CLIENT_EXCEPTION;
				}
			}
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		boolean valid = false;
		String query = req.getQueryString();
		String fullPath = basePath + req.getPathInfo() +
				(query == null || query.isEmpty() ? "" : "?" + query);
		String method = req.getMethod();

		Pool.Entry<Socket> socketEntry = null;
		try {
			// Request Head
			socketEntry = pool.borrow();
			Socket socket = socketEntry.obj;
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
			Object certs = req.getAttribute("javax.servlet.request.X509Certificate");
			if (certs instanceof X509Certificate[]) {
				ByteArrayQueue baq = new ByteArrayQueue();
				new PKCS7(new AlgorithmId[0], new ContentInfo(Bytes.EMPTY_BYTES),
						(X509Certificate[]) certs, new SignerInfo[0]).
						encodeSignedData(baq.getOutputStream());
				writeHeader(outSocket, "X-Pkcs7-Certificates-Base64",
						Base64.encode(baq.array(), baq.offset(), baq.length()));
			}
			writeHeader(outSocket, "Connection", "Keep-Alive");

			// Request Body
			int contentLength = req.getContentLength();
			if (contentLength > 0) {
				writeHeader(outSocket, "Content-Length", "" + contentLength);
				writeln(outSocket);
				// Streams.copy(req.getInputStream(), outSocket);
				// Should throw CLIENT_EXCEPTION when req.getInputStream() broken
				InputStream in = req.getInputStream();
				byte[] buffer = new byte[REQ_MAX_SIZE];
				while (true) {
					int bytesRead;
					try {
						bytesRead = in.read(buffer);
					} catch (IOException e) {
						throw CLIENT_EXCEPTION;
					}
					if (bytesRead <= 0) {
						break;
					}
					outSocket.write(buffer, 0, bytesRead);
				}
			} else if (method.equals("GET") || method.equals("HEAD")) {
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
			boolean close = false;
			int status = 0;
			StringBuilder sb = new StringBuilder();
			contentLength = 0;
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

			// Response Body
			if (contentLength == 0 || method.equals("HEAD")) {
				resp.setContentLength(contentLength);
				valid = !close;
				return;
			}
			OutputStream outResp = resp.getOutputStream();
			byte[] buffer = new byte[RESP_MAX_SIZE];
			if (contentLength > 0) {
				resp.setContentLength(contentLength);
				copyResponse(inSocket, outResp, buffer, contentLength);
				valid = !close;
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
			valid = !close;

		} catch (IOException e) {
			if (e != CLIENT_EXCEPTION) {
				try {
					if (redirect == null) {
						resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
					} else {
						resp.sendRedirect(redirect);
					}
				} catch (IOException e_) {/**/}
				Log.w(e.getMessage());
			}
		} finally {
			if (socketEntry != null) {
				pool.return_(socketEntry, valid);
			}
		}
	}
}