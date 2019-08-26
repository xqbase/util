package com.xqbase.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.SocketPool;

public class Pipeline {
	public static class Request<T> {
		private T id;
		private String path;
		private boolean head;
		private ByteArrayQueue body = null;
		private Map<String, List<String>> headers = new HashMap<>();

		public Request(T id, String path) {
			this(id, path, false);
		}

		public Request(T id, String path, boolean head) {
			this.id = id;
			this.path = path;
			this.head = head;
		}

		public T getId() {
			return id;
		}

		public String getPath() {
			return path;
		}

		public boolean isHead() {
			return head;
		}

		public ByteArrayQueue getBody() {
			return body;
		}

		public void setBody(ByteArrayQueue body) {
			this.body = body;
		}

		public Map<String, List<String>> getHeaders() {
			return headers;
		}
	}

	public static class Response<T> {
		private T id;
		private int status = 0;
		private ByteArrayQueue body = new ByteArrayQueue();
		private Map<String, List<String>> headers = new LinkedHashMap<>();

		public Response(T id) {
			this.id = id;
		}

		public T getId() {
			return id;
		}

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public ByteArrayQueue getBody() {
			return body;
		}

		public Map<String, List<String>> getHeaders() {
			return headers;
		}
	}

	static <T> void pipeline(Socket socket, String path, String host,
			List<Request<T>> requests, List<Response<T>> responses,
			boolean[] connectionClose) throws IOException {
		OutputStream out = socket.getOutputStream();
		for (Request<T> request : requests) {
			HttpUtil.send(out, path + request.getPath(), host, null,
					request.getBody(), request.getHeaders(), request.isHead());
		}
		InputStream in = socket.getInputStream();
		for (Request<T> request : requests) {
			boolean[] connectionClose_ = connectionClose == null ?
					new boolean[1] : connectionClose;
			Response<T> response = new Response<>(request.getId());
			response.setStatus(HttpUtil.recv(in, response.getBody(),
					response.getHeaders(), request.isHead(), false, connectionClose_));
			responses.add(response);
			if (connectionClose_[0]) {
				return;
			}
		}
	}

	public static <T> void pipeline(String url, List<Request<T>> requests,
			List<Response<T>> responses, int timeout) throws IOException {
		pipeline(null, url, requests, responses, timeout);
	}

	public static <T> void pipeline(HttpProxy httpProxy, String url,
			List<Request<T>> requests, List<Response<T>> responses,
			int timeout) throws IOException {
		HttpParam param = new HttpParam(httpProxy, url);
		if (param.connect) {
			try (Socket socket = httpProxy.createSocket(param.host, param.secure, timeout)) {
				pipeline(socket, param.path, param.host, requests, responses, null);
			}
		} else {
			try (Socket socket = SocketPool.createSocket(param.secure)) {
				socket.connect(new InetSocketAddress(param.socketHost, param.socketPort), timeout);
				socket.setSoTimeout(timeout);
				pipeline(socket, param.path, param.host, requests, responses, null);
			}
		}
	}
}