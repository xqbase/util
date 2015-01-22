package com.xqbase.util.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.SocketPool;

public class Pipeline {
	public static class Request {
		private Object id;
		private String path;
		private boolean head;
		private ByteArrayQueue body = null;
		private HashMap<String, List<String>> headers = new HashMap<>();

		public Request(Object id, String path) {
			this(id, path, false);
		}

		public Request(Object id, String path, boolean head) {
			this.id = id;
			this.path = path;
			this.head = head;
		}

		public Object getId() {
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

		public HashMap<String, List<String>> getHeaders() {
			return headers;
		}
	}

	public static class Response {
		private Object id;
		private int status = 0;
		private ByteArrayQueue body = new ByteArrayQueue();
		private LinkedHashMap<String, List<String>> headers = new LinkedHashMap<>();

		public Response(Object id) {
			this.id = id;
		}

		public Object getId() {
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

		public HashMap<String, List<String>> getHeaders() {
			return headers;
		}
	}

	static void pipeline(Socket socket, String host, List<Request> requests,
			List<Response> responses, boolean[] connectionClose) throws IOException {
		OutputStream out = socket.getOutputStream();
		for (Request request : requests) {
			HttpUtil.send(out, request.getPath(), host, null,
					request.getBody(), request.getHeaders(), request.isHead());
		}
		InputStream in = socket.getInputStream();
		for (Request request : requests) {
			boolean[] connectionClose_ = connectionClose == null ?
					new boolean[1] : connectionClose;
			Response response = new Response(request.getId());
			response.setStatus(HttpUtil.recv(in, response.getBody(),
					response.getHeaders(), request.isHead(), false, connectionClose_));
			responses.add(response);
			if (connectionClose_[0]) {
				return;
			}
		}
	}

	public static void pipeline(String url, List<Request> requests,
			List<Response> responses, int timeout) throws IOException {
		pipeline(null, url, requests, responses, timeout);
	}

	public static void pipeline(HttpProxy httpProxy, String url, List<Request> requests,
			List<Response> responses, int timeout) throws IOException {
		HttpParam param = new HttpParam(httpProxy, url);
		try (Socket socket = SocketPool.createSocket(param.secure)) {
			socket.connect(new InetSocketAddress(param.socketHost, param.socketPort), timeout);
			socket.setSoTimeout(timeout);
			pipeline(socket, param.host, requests, responses, null);
		}
	}
}