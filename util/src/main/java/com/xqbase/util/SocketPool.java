package com.xqbase.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

public class SocketPool extends Pool<Socket, IOException> {
	public static Socket createSocket(boolean secure) throws IOException {
		return secure ? sslsf.createSocket() : new Socket();
	}

	private static SSLSocketFactory sslsf;

	static {
		try {
			SSLContext sslc = SSLContext.getInstance("TLS");
			sslc.init(new KeyManager[0], new X509TrustManager[] {
				new X509TrustManager() {
					@Override
					public void checkClientTrusted(X509Certificate[] certs, String s) {/**/}

					@Override
					public void checkServerTrusted(X509Certificate[] certs, String s) {/**/}

					@Override
					public X509Certificate[] getAcceptedIssuers() {
						return new X509Certificate[0];
					}
				}
			}, null);
			sslsf = sslc.getSocketFactory();
		} catch (GeneralSecurityException e) {
			throw new RuntimeException(e);
		}
	}

	public SocketPool(String host, int port, int timeout) {
		this(host, port, false, timeout);
	}

	public SocketPool(String host, int port, boolean secure, int timeout) {
		super(() -> {
			Socket socket = createSocket(secure);
			try {
				socket.connect(new InetSocketAddress(host, port), timeout);
				socket.setSoTimeout(timeout);
				return socket;
			} catch (IOException e) {
				socket.close();
				throw e;
			}
		}, Socket::close, timeout);
	}
}