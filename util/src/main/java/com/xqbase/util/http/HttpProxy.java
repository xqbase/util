package com.xqbase.util.http;

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

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}