package com.xqbase.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Numbers;
import com.xqbase.util.SocketPool;
import com.xqbase.util.Streams;

class HttpParam {
	String socketHost, path, host, proxyAuth;
	int socketPort;
	boolean secure, connect;

	HttpParam(HttpProxy httpProxy, String url) {
		try {
			URL url_ = new URL(url);
			int port = url_.getPort();
			String query = url_.getQuery();
			secure = url_.getProtocol().equals("https");
			socketHost = url_.getHost();
			socketPort = port == -1 ? url_.getDefaultPort() : port;
			path = url_.getPath() + (query == null ? "" : "?" + query);
			host = url_.getHost() + (port == -1 ? "" : ":" + port);
		} catch (IOException e) {
			secure = false;
			socketHost = "localhost";
			socketPort = 80;
			path = "/";
			host = "localhost";
		}
		connect = false;
		if (httpProxy == null) {
			proxyAuth = null;
			return;
		}
		socketHost = httpProxy.getHost();
		socketPort = httpProxy.getPort();
		if (secure) {
			connect = true;
		} else {
			path = "http://" + host + path;
		}
		proxyAuth = httpProxy.getProxyAuth();
	}
}

public class HttpUtil {
	private static final int RESP_MAX_SIZE = 65536;
	private static final String HEX_DIGITS = "0123456789ABCDEF";

	private static void copyResponse(InputStream in, ByteArrayQueue baq,
			byte[] buffer, int length) throws IOException {
		int bytesToRead = length;
		while (bytesToRead > 0) {
			int bytesRead = in.read(buffer, 0,
					Math.min(RESP_MAX_SIZE, bytesToRead));
			if (bytesRead < 0) {
				throw new IOException("Connection Lost");
			}
			if (bytesRead == 0) {
				throw new IOException("Zero Bytes Read");
			}
			bytesToRead -= bytesRead;
			if (baq != null) {
				baq.add(buffer, 0, bytesRead);
			}
		}
	}

	private static final byte[] CRLF = {'\r', '\n'};
	private static final byte[] COLON = {':', ' '};
	private static final byte[] GET = "GET ".getBytes();
	private static final byte[] HEAD = "HEAD ".getBytes();
	private static final byte[] POST = "POST ".getBytes();
	private static final byte[] CONNECT = "CONNECT ".getBytes();
	private static final byte[] HTTP11 = " HTTP/1.1\r\n".getBytes();
	private static final byte[] HTTP10 = " HTTP/1.0\r\n".getBytes();
	private static final byte[] HOST = "Host: ".getBytes();
	private static final byte[] PROXY_AUTH = "Proxy-Authorization: ".getBytes();
	private static final byte[] CONTENT_LENGTH = "Content-Length: ".getBytes();
	private static final byte[] HEAD_END =
			("Accept-Encoding: gzip\r\n" +
			"Connection: Keep-Alive\r\n\r\n").getBytes();

	private static final HashSet<String> SKIP_HEADERS = new HashSet<>(Arrays.asList(
		"ACCEPT-ENCODING", "CONNECTION", "CONTENT-LENGTH", "PROXY_AUTH"
	));

	static void send(OutputStream out, String path, String host,
			String proxyAuth, ByteArrayQueue requestBody, Map<String, List<String>>
			requestHeaders, boolean head) throws IOException {
		ByteArrayQueue headerBaq = new ByteArrayQueue();
		if (requestBody == null) {
			headerBaq.add(head ? HEAD : GET).
					add(path.getBytes(StandardCharsets.ISO_8859_1)).add(HTTP11);
		} else {
			String length = "" + requestBody.length();
			headerBaq.add(POST).
					add(path.getBytes(StandardCharsets.ISO_8859_1)).add(HTTP11).
					add(CONTENT_LENGTH).add(length.getBytes()).add(CRLF);
		}
		if (requestHeaders == null || !requestHeaders.containsKey("Host")) {
			headerBaq.add(HOST).
					add(host.getBytes(StandardCharsets.ISO_8859_1)).add(CRLF);
		}
		if (proxyAuth != null) {
			headerBaq.add(PROXY_AUTH).
					add(proxyAuth.getBytes(StandardCharsets.ISO_8859_1)).add(CRLF);
		}
		if (requestHeaders != null) {
			requestHeaders.forEach((key, values) -> {
				if (SKIP_HEADERS.contains(key.toUpperCase())) {
					return;
				}
				byte[] key_ = key.getBytes(StandardCharsets.ISO_8859_1);
				for (String value : values) {
					headerBaq.add(key_).add(COLON).
							add(value.getBytes(StandardCharsets.ISO_8859_1)).add(CRLF);
				}
			});
		}
		headerBaq.add(HEAD_END);

		Streams.copy(headerBaq.getInputStream(), out);
		if (requestBody != null) {
			Streams.copy(requestBody.getInputStream(), out);
		}
	}

	static int recv(InputStream in, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders, boolean head,
			boolean connect, boolean[] connectionClose) throws IOException {
		// Response Header
		boolean http10 = false, close = false, gzip = false;
		int status = 0, contentLength = 0;
		StringBuilder sb = new StringBuilder();
		while (true) {
			int b = in.read();
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
				if (status == 100) {
					status = 0;
					continue;
				}
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
			} else if (status >= 200) {
				int index = sb.indexOf(": ");
				if (index >= 0) {
					String key = sb.substring(0, index).toUpperCase();
					String value = sb.substring(index + 2);
					switch (key) {
					case "CONNECTION":
						close = value.equalsIgnoreCase("close");
						break;
					case "CONTENT-LENGTH":
						contentLength = Numbers.parseInt(value);
						break;
					case "CONTENT-ENCODING":
						gzip = value.equalsIgnoreCase("gzip");
						break;
					case "TRANSFER-ENCODING":
						contentLength = value.equalsIgnoreCase("chunked") ?
								-1 : contentLength;
						break;
					default:
						if (responseHeaders == null) {
							break;
						}
						List<String> values = responseHeaders.get(key);
						if (values == null) {
							values = new ArrayList<>();
							responseHeaders.put(key, values);
						}
						values.add(value);
					}
				}
			}
			sb.setLength(0);
		}
		if (connectionClose != null && connectionClose.length > 0) {
			connectionClose[0] = http10 || close;
		}

		// Response Body
		if (http10 && !connect) {
			OutputStream out = responseBody == null ? new ByteArrayQueue().getOutputStream() :
					responseBody.getOutputStream();
			// For HTTP/1.0 response, read from stream until connection lost
			Streams.copy(gzip ? new GZIPInputStream(in) : in, out);
			return status;
		}
		if (head || contentLength == 0) {
			return status;
		}
		byte[] buffer = new byte[RESP_MAX_SIZE];
		if (contentLength > 0) {
			if (gzip) {
				ByteArrayQueue gzipBody = new ByteArrayQueue();
				copyResponse(in, gzipBody, buffer, contentLength);
				if (responseBody != null) {
					try (GZIPInputStream gzipis = new GZIPInputStream(gzipBody.getInputStream())) {
						Streams.copy(gzipis, responseBody.getOutputStream());
					} catch (IOException e) {
						// Ignored
					}
				}
			} else {
				copyResponse(in, responseBody, buffer, contentLength);
			}
			return status;
		}

		/* Response Body (Chunked)
		 * >=0: waiting size and CRLF
		 * -1: waiting next block (CRLF)
		 * -2: waiting terminator (CRLF)
		 */
		int chunkSize = 0;
		ByteArrayQueue gzipBody = gzip ? new ByteArrayQueue() : responseBody;
		while (true) {
			int b = in.read();
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
					copyResponse(in, gzipBody, buffer, chunkSize);
					chunkSize = -1;
				}
			} else {
				b = HEX_DIGITS.indexOf(Character.toUpperCase(b));
				if (b >= 0) {
					chunkSize = chunkSize * 16 + b;
				}
			}
		}
		if (gzip && responseBody != null) {
			try (GZIPInputStream gzipis = new GZIPInputStream(gzipBody.getInputStream())) {
				Streams.copy(gzipis, responseBody.getOutputStream());
			} catch (IOException e) {
				// Ignored
			}
		}
		return status;
	}

	static int request(Socket socket, String path, String host, String proxyAuth,
			ByteArrayQueue requestBody, Map<String, List<String>> requestHeaders, boolean head,
			ByteArrayQueue responseBody, Map<String, List<String>> responseHeaders,
			boolean[] connectionClose) throws IOException {
		send(socket.getOutputStream(), path, host,
				proxyAuth, requestBody, requestHeaders, head);
		return recv(socket.getInputStream(), responseBody,
				responseHeaders, head, false, connectionClose);
	}

	static Socket connect(Socket socket, String host,
			String proxyAuth, boolean secure) throws IOException {
		int colon = host.indexOf(':');
		int port;
		String host_;
		if (colon < 0) {
			host_ = host;
			port = secure ? 443 : 80;
		} else {
			host_ = host.substring(0, colon);
			port = Numbers.parseInt(host.substring(colon + 1));
		}
		return connect(socket, host_, port, proxyAuth, secure);
	}

	static Socket connect(Socket socket, String host, int port,
			String proxyAuth, boolean secure) throws IOException {
		ByteArrayQueue headerBaq = new ByteArrayQueue();
		headerBaq.add(CONNECT).
				add(host.getBytes(StandardCharsets.ISO_8859_1)).add(COLON, 0, 1).
				add(("" + port).getBytes()).add(HTTP10);
		if (proxyAuth != null) {
			headerBaq.add(PROXY_AUTH).
					add(proxyAuth.getBytes(StandardCharsets.ISO_8859_1)).add(CRLF);
		}
		headerBaq.add(CRLF);
		Streams.copy(headerBaq.getInputStream(), socket.getOutputStream());
		int status = recv(socket.getInputStream(), null, null, false, true, null);
		if (status != 200) {
			throw new IOException("HTTP/1.0 " + status + " Connection NOT established");
		}
		return secure ? SocketPool.createSocket(socket, host, port) : socket;
	}

	private static int request(HttpProxy httpProxy, String url,
			ByteArrayQueue requestBody, Map<String, List<String>> requestHeaders, boolean head,
			ByteArrayQueue responseBody, Map<String, List<String>> responseHeaders,
			int timeout) throws IOException {
		HttpParam param = new HttpParam(httpProxy, url);
		if (param.connect) {
			try (Socket socket = connect(SocketPool.createSocket(param.socketHost,
					param.socketPort, false, timeout),
					param.host, param.proxyAuth, param.secure)) {
				return request(socket, param.path, param.host, null, requestBody,
						requestHeaders, head, responseBody, responseHeaders, null);			
			}
		}
		try (Socket socket = SocketPool.createSocket(param.secure)) {
			socket.connect(new InetSocketAddress(param.socketHost, param.socketPort), timeout);
			socket.setSoTimeout(timeout);
			return request(socket, param.path, param.host, param.proxyAuth, requestBody,
					requestHeaders, head, responseBody, responseHeaders, null);
		}
	}

	public static int head(String url, Map<String, List<String>> requestHeaders,
			Map<String, List<String>> responseHeaders, int timeout) throws IOException {
		return head(null, url, requestHeaders, responseHeaders, timeout);
	}

	public static int get(String url,
			Map<String, List<String>> requestHeaders, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders, int timeout) throws IOException {
		return get(null, url, requestHeaders, responseBody, responseHeaders, timeout);
	}

	public static int post(String url,
			ByteArrayQueue requestBody, Map<String, List<String>> requestHeaders,
			ByteArrayQueue responseBody, Map<String, List<String>> responseHeaders,
			int timeout) throws IOException {
		return post(null, url, requestBody, requestHeaders,
				responseBody, responseHeaders, timeout);
	}

	public static int head(HttpProxy httpProxy, String url, Map<String, List<String>>
			requestHeaders, Map<String, List<String>> responseHeaders,
			int timeout) throws IOException {
		return request(httpProxy, url, null, requestHeaders, true,
				null, responseHeaders, timeout);
	}

	public static int get(HttpProxy httpProxy, String url,
			Map<String, List<String>> requestHeaders, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders, int timeout) throws IOException {
		return request(httpProxy, url, null, requestHeaders, false,
				responseBody, responseHeaders, timeout);
	}

	public static int post(HttpProxy httpProxy, String url,
			ByteArrayQueue requestBody, Map<String, List<String>> requestHeaders,
			ByteArrayQueue responseBody, Map<String, List<String>> responseHeaders,
			int timeout) throws IOException {
		return request(httpProxy, url, requestBody, requestHeaders, false,
				responseBody, responseHeaders, timeout);
	}
}