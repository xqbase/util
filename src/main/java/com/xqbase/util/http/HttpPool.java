package com.xqbase.util.http;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.SocketPool;

public class HttpPool extends SocketPool {
	private String path_, host, proxyAuth;

	private HttpPool(HttpParam param, int timeout) {
		super(param.socketHost, param.socketPort, param.secure, timeout);
		path_ = param.path;
		host = param.host;
		proxyAuth = param.proxyAuth;
	}

	public HttpPool(String url, int timeout) {
		this(null, url, timeout);
	}

	public HttpPool(HttpProxy httpProxy, String url, int timeout) {
		this(new HttpParam(httpProxy, url), timeout);
	}

	private int request(String path, ByteArrayQueue requestBody,
			Map<String, List<String>> requestHeaders, boolean head, ByteArrayQueue responseBody,
			Map<String, List<String>> responseHeaders) throws IOException {
		boolean valid = false;
		Entry<Socket> entry = borrow();
		try {
			boolean[] connectionClose = {false};
			int status = HttpUtil.request(entry.obj, path_ + path, host, proxyAuth, requestBody,
					requestHeaders, head, responseBody, responseHeaders, connectionClose);
			valid = !connectionClose[0];
			return status;
		} finally {
			return_(entry, valid);
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
		boolean valid = false;
		Entry<Socket> entry = borrow();
		try {
			boolean[] connectionClose = {false};
			Pipeline.pipeline(entry.obj, host, requests, responses, connectionClose);
			valid = !connectionClose[0];
		} finally {
			return_(entry, valid);
		}
	}
}