package com.xqbase.util.http;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.SocketPool;
import com.xqbase.util.function.SupplierEx;

public class HttpPool extends SocketPool {
	private String path_, host, proxyAuth;

	private static SupplierEx<Socket, IOException>
			getSocketSupplier(HttpParam param, int timeout) {
		return () -> {
			Socket socket = SocketPool.createSocket(param.socketHost,
					param.socketPort, param.connect ? false : param.secure, timeout);
			return param.connect ? HttpUtil.connect(socket,
					param.host, param.proxyAuth, param.secure) : socket;
		};
	}

	private HttpPool(HttpParam param, int soTimeout, int kaTimeout) {
		super(getSocketSupplier(param, soTimeout), kaTimeout);
		path_ = param.path;
		host = param.host;
		proxyAuth = param.proxyAuth;
	}

	public HttpPool(String url, int timeout) {
		this(url, timeout, timeout);
	}

	public HttpPool(String url, int soTimeout, int kaTimeout) {
		this(null, url, soTimeout, kaTimeout);
	}

	public HttpPool(HttpProxy httpProxy, String url, int timeout) {
		this(httpProxy, url, timeout, timeout);
	}

	public HttpPool(HttpProxy httpProxy, String url, int soTimeout, int kaTimeout) {
		this(new HttpParam(httpProxy, url), soTimeout, kaTimeout);
	}

	private int request(String path, ByteArrayQueue requestBody,
			Map<String, List<String>> requestHeaders, boolean head, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders) throws IOException {
		while (true) {
			try (Entry entry = borrow()) {
				try {
					boolean[] connectionClose = {false};
					int status = HttpUtil.request(entry.getObject(), path_ + path,
							host, proxyAuth, requestBody, requestHeaders, head,
							responseBody, responseHeaders, connectionClose);
					entry.setValid(!connectionClose[0]);
					return status;
				} catch (IOException e) {
					if (e instanceof SocketTimeoutException || entry.getBorrows() == 0) {
						throw e;
					}
					continue;
				}
			}
		}
	}

	public int head(String path, Map<String, List<String>> requestHeaders,
			Map<String, List<String>> responseHeaders) throws IOException {
		return request(path, null, requestHeaders, true, null, responseHeaders);
	}

	public int get(String path,
			Map<String, List<String>> requestHeaders, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders) throws IOException {
		return request(path, null, requestHeaders, false, responseBody, responseHeaders);
	}

	public int post(String path, ByteArrayQueue requestBody,
			Map<String, List<String>> requestHeaders, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders) throws IOException {
		return request(path, requestBody, requestHeaders, false, responseBody, responseHeaders);
	}

	public void pipeline(List<Pipeline.Request> requests,
			List<Pipeline.Response> responses) throws IOException {
		try (Entry entry = borrow()) {
			boolean[] connectionClose = {false};
			Pipeline.pipeline(entry.getObject(), host, requests, responses, connectionClose);
			entry.setValid(!connectionClose[0]);
		}
	}
}