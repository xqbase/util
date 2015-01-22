package com.xqbase.util.http;

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

	public SocketPool createSocketPool(String remoteHost,
			int remotePort, boolean secure, int timeout) {
		return new SocketPool(() -> HttpUtil.connect(SocketPool.
				createSocket(getHost(), getPort(), false, timeout),
				remoteHost, remotePort, getProxyAuth(), secure), timeout);
	}
}