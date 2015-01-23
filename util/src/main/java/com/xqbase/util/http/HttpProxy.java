package com.xqbase.util.http;

import java.io.IOException;
import java.net.Socket;
import java.util.Base64;

import com.xqbase.util.SocketPool;

public class HttpProxy {
	private String host, username, password;
	private int port;

	public HttpProxy(String host, int port) {
		this(host, port, null, null);
	}

	public HttpProxy(String host, int port, String username, String password) {
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getProxyAuth() {
		return username == null ? null :
				"Basic " + Base64.getEncoder().encodeToString((username + ":" +
				(password == null ? "" : password)).getBytes());
	}

	public Socket createSocket(String remoteHost,
			int remotePort, boolean secure, int timeout) throws IOException {
		return HttpUtil.connect(SocketPool.createSocket(host, port, false, timeout),
				remoteHost, remotePort, getProxyAuth(), secure);
	}

	public SocketPool createSocketPool(String remoteHost,
			int remotePort, boolean secure, int timeout) {
		return new SocketPool(() -> createSocket(remoteHost,
				remotePort, secure, timeout), timeout);
	}
}