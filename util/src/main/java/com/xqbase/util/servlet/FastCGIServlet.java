package com.xqbase.util.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Bytes;
import com.xqbase.util.Log;
import com.xqbase.util.Numbers;
import com.xqbase.util.Time;

class SocketEntry {
	String command;
	InetSocketAddress addr;
	Process process = null;
	Socket socket = null;
	int requests = 0;
	long expire = 0;

	void close() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException ee) {/**/}
			socket = null;
		}
	}

	void destroy() {
		if (process == null) {
			return;
		}
		try {
			// Abort destroying process 1 minute later
			for (int i = 0; i < 60; i ++) {
				process.destroy();
				if (process.waitFor(Time.SECOND, TimeUnit.MILLISECONDS)) {
					break;
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		process = null;
		requests = 0;
	}
}

public class FastCGIServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final int FCGI_BEGIN_REQUEST = 1;
	// private static final int FCGI_ABORT_REQUEST = 2;
	private static final int FCGI_END_REQUEST = 3;
	private static final int FCGI_PARAMS = 4;
	private static final int FCGI_STDIN = 5;
	private static final int FCGI_STDOUT = 6;
	private static final int FCGI_STDERR = 7;

	private static final int RESP_HEADER = 0;
	private static final int RESP_BODY = 1;
	private static final int RESP_END = 2;

	private static void write(OutputStream out, int type, byte[] b) throws IOException {
		write(out, type, b, 0, b.length);
	}

	private static void write(OutputStream out, int type,
			byte[] b, int off, int len) throws IOException {
		byte[] data = new byte[(len + 15) / 8 * 8];
		data[0] = 1; // version
		data[1] = (byte) type;
		data[2] = 0; // requestIdB1
		data[3] = 1; // requestIdB0
		data[4] = (byte) (len >> 8); // contentLengthB1 
		data[5] = (byte) len; // contentLengthB0
		data[6] = (byte) (data.length - 8 - len); // paddingLength
		data[7] = 0; // reserved
		System.arraycopy(b, off, data, 8, len);
		out.write(data);
	}

	private static void addPair(ByteArrayQueue baq, String key, String value) {
		if (value == null) {
			return;
		}
		byte[] keyBytes = key.getBytes();
		byte[] valueBytes = value.getBytes();
		int keyLen = keyBytes.length;
		int valueLen = valueBytes.length;
		int keyLenLen = keyLen < 0x80 ? 1 : 4;
		int valueLenLen = valueLen < 0x80 ? 1 : 4;
		byte[] data = new byte[keyLenLen + valueLenLen];
		if (keyLen < 0x80) {
			data[0] = (byte) keyLen;
		} else {
			Bytes.setInt(keyLen | 0x80000000, data, 0);
		}
		if (valueLen < 0x80) {
			data[keyLenLen] = (byte) valueLen;
		} else {
			Bytes.setInt(valueLen | 0x80000000, data, keyLenLen);
		}
		baq.add(data);
		baq.add(keyBytes);
		baq.add(valueBytes);
	}

	private static byte[] read(InputStream in, int len) throws IOException {
		byte[] data = new byte[len];
		int bytesLeft = len;
		while (bytesLeft > 0) {
			int bytesRead = in.read(data, len - bytesLeft, bytesLeft);
			if (bytesRead <= 0) {
				throw new IOException();
			}
			bytesLeft -= bytesRead;
		}
		return data;
	}

	static class ClientException extends Exception {
		private static final long serialVersionUID = 1L;
	}

	private int timeout, maxRequests;
	private BlockingQueue<SocketEntry> socketQueue;

	@Override
	public void init() throws ServletException {
		timeout = Numbers.parseInt(getInitParameter("timeout"), 15000);
		maxRequests = Numbers.parseInt(getInitParameter("max_requests"), 499);
		String addresses = getInitParameter("addresses");
		if (addresses == null) {
			throw new ServletException("missing param \"addresses\"");
		}
		String command = getInitParameter("command");
		String[] s = addresses.split("[,;]");
		socketQueue = new LinkedBlockingQueue<>();
		for (int i = 0; i < s.length; i ++) {
			String[] ss = s[i].split("[:/]");
			if (ss.length < 2) {
				continue;
			}
			SocketEntry entry = new SocketEntry();
			entry.command = command == null ? null : command + " " + s[i];
			entry.addr = new InetSocketAddress(ss[0], Numbers.parseInt(ss[1]));
			socketQueue.offer(entry);
		}
	}

	@Override
	public void destroy() {
		SocketEntry entry;
		while ((entry = socketQueue.poll()) != null) {
			entry.close();
			entry.destroy();
		}
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		SocketEntry entry = null;
		try {
			entry = socketQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		if (entry == null) {
			try {
				resp.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT);
			} catch (IOException e) {/**/}
			return;
		}
		long begin = System.currentTimeMillis();
		if (begin > entry.expire) {
			entry.close();
		}
		try {
			if (entry.command != null && entry.process == null) {
				entry.process = Runtime.getRuntime().exec(entry.command);
				while (true) {
					entry.socket = new Socket();
					try {
						entry.socket.connect(entry.addr, 100);
						break;
					} catch (IOException e) {
						if (System.currentTimeMillis() > begin + timeout) {
							throw e;
						}
						entry.socket.close();
					}
				}
				entry.socket.setSoTimeout(timeout);
			} else if (entry.socket == null) {
				entry.socket = new Socket();
				entry.socket.connect(entry.addr, timeout);
				entry.socket.setSoTimeout(timeout);
			}
			OutputStream out = entry.socket.getOutputStream();
			write(out, FCGI_BEGIN_REQUEST, new byte[] {0, 1, 1, 0, 0, 0, 0, 0});

			ByteArrayQueue baq = new ByteArrayQueue();
			String method = req.getMethod();
			addPair(baq, "REQUEST_METHOD", method);
			// Many PHP scripts do not check null on $_SERVER['QUERY_STRING']
			String query = req.getQueryString();
			addPair(baq, "QUERY_STRING", query == null ? "" : query);
			addPair(baq, "CONTENT_TYPE", req.getContentType());
			int contentLength = method.equals("GET") || method.equals("HEAD") ?
					0 : req.getContentLength();
			if (contentLength < 0) {
				try {
					resp.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED,
							"Chunked Request Not Supported");
				} catch (IOException e) {/**/}
				throw new ClientException();
			}
			addPair(baq, "CONTENT_LENGTH", "" + contentLength);

			String path = req.getServletPath();
			String realPath = getServletContext().getRealPath(path);
			addPair(baq, "SCRIPT_FILENAME", realPath);
			addPair(baq, "SCRIPT_NAME", path);
			// addPair(baq, "PATH_INFO", req.getContextPath() + path);
			// addPair(baq, "PATH_TRANSLATED", realPath);
			addPair(baq, "REQUEST_URI", req.getRequestURI());
			// addPair(baq, "DOCUMENT_URI", req.getRequestURI());
			addPair(baq, "DOCUMENT_ROOT", getServletContext().getRealPath("/"));

			addPair(baq, "GATEWAY_INTERFACE", "CGI/1.1");
			addPair(baq, "SERVER_SOFTWARE", FastCGIServlet.class.getName());
			addPair(baq, "SERVER_PROTOCOL", req.getProtocol());
			addPair(baq, "SERVER_NAME", req.getServerName());
			addPair(baq, "SERVER_ADDR", req.getLocalAddr());
			addPair(baq, "SERVER_PORT", "" + req.getServerPort());
			addPair(baq, "REMOTE_HOST", req.getRemoteHost());
			addPair(baq, "REMOTE_ADDR", req.getRemoteAddr());
			addPair(baq, "REMOTE_PORT", "" + req.getRemotePort());
			addPair(baq, "REMOTE_USER", req.getRemoteUser());
			addPair(baq, "AUTH_TYPE", req.getAuthType());

			Enumeration<String> e = req.getHeaderNames();
			while (e.hasMoreElements()) {
				String key = e.nextElement();
				String value = req.getHeader(key);
				addPair(baq, "HTTP_" + key.toUpperCase().replace('-', '_'), value);
			}
			write(out, FCGI_PARAMS, baq.array(), baq.offset(), baq.length());
			write(out, FCGI_PARAMS, Bytes.EMPTY_BYTES);

			if (contentLength > 0) {
				InputStream in = req.getInputStream();
				int len;
				byte[] data = new byte[4096];
				while ((len = in.read(data, 0, 4096)) > 0) {
					write(out, FCGI_STDIN, data, 0, len);
				}
			}
			write(out, FCGI_STDIN, Bytes.EMPTY_BYTES);

			InputStream in = entry.socket.getInputStream();
			try {
				out = resp.getOutputStream();
			} catch (IOException ee) {
				throw new ClientException();
			}
			int respStatus = RESP_HEADER;
			StringBuilder sbOut = new StringBuilder();
			StringBuilder sbErr = new StringBuilder();
			while (respStatus < RESP_END) {
				byte[] record = read(in, 8);
				byte[] data = read(in, Bytes.toShort(record, 4) & 0xFFFF);
				read(in, record[6] & 0xFF); // Padding
				switch (record[1]) {
				case FCGI_END_REQUEST:
					respStatus = RESP_END;
					break;
				case FCGI_STDOUT:
					if (respStatus == RESP_BODY) {
						try {
							out.write(data);
							out.flush();
						} catch (IOException ee) {
							throw new ClientException();
						}
						break;
					}

					sbOut.append(new String(data, StandardCharsets.ISO_8859_1));
					int index = sbOut.indexOf("\r\n\r\n");
					if (index >= 0) {
						for (String s : sbOut.substring(0, index).split("\r\n")) {
							String[] ss = s.split(":", 2);
							if (ss.length < 2) {
								continue;
							}
							ss[1] = ss[1].trim();
							if (ss[0].toUpperCase().equals("STATUS")) {
								resp.setStatus(Numbers.parseInt(ss[1].split(" ")[0]));
							} else {
								resp.addHeader(ss[0], ss[1]);
							}
						}

						respStatus = RESP_BODY;
						index += 4;
						if (index < sbOut.length()) {
							try {
								out.write(sbOut.substring(index).
										getBytes(StandardCharsets.ISO_8859_1));
								out.flush();
							} catch (IOException ee) {
								throw new ClientException();
							}
						}
					}
					break;

				case FCGI_STDERR:
					sbErr.append(new String(data, StandardCharsets.ISO_8859_1));
					while ((index = sbErr.indexOf("\n")) >= 0) {
						Log.i(sbErr.substring(0, index > 0 &&
								sbErr.charAt(index - 1) == '\r' ? index - 1 : index));
						sbErr.delete(0, index + 1);
					}
					break;
				}
			}

			int len = sbErr.length();
			if (len > 0 && sbErr.charAt(len - 1) == '\r') {
				len --;
			}
			if (len > 0) {
				Log.i(sbErr.substring(0, len));
			}

			entry.expire = System.currentTimeMillis() + timeout;
		} catch (ClientException e) {
			entry.close();
		} catch (IOException e) {
			entry.close();
			Log.w(entry.addr + " - " + e.getMessage());
			try {
				resp.sendError(HttpServletResponse.SC_BAD_GATEWAY);
			} catch (IOException e_) {/**/}
			entry.destroy();
		} finally {
			if (entry.command != null) {
				entry.requests ++;
				if (entry.requests == maxRequests) {
					entry.close();
					entry.destroy();
				}
			}
			socketQueue.offer(entry);
		}
	}
}