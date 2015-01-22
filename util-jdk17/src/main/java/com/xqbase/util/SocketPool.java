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

import com.xqbase.util.function.ConsumerEx;
import com.xqbase.util.function.SupplierEx;

public class SocketPool extends Pool<Socket, IOException> {
	public static Socket createSocket(boolean secure) throws IOException {
		return secure ? sslsf.createSocket() : new Socket();
	}

	public static Socket createSocket(String host, int port,
			boolean secure, int timeout) throws IOException {
		Socket socket = createSocket(secure);
		try {
			socket.connect(new InetSocketAddress(host, port), timeout);
			socket.setSoTimeout(timeout);
			return socket;
		} catch (IOException e) {
			socket.close();
			throw e;
		}
	}

	public static Socket createSocket(Socket socket,
			String host, int port) throws IOException {
		return sslsf.createSocket(socket, host, port, true);
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

	public SocketPool(SupplierEx<Socket, IOException> supplier, int timeout) {
		super(supplier, new ConsumerEx<Socket, IOException>() {
			@Override
			public void accept(Socket socket) throws IOException {
				socket.close();
			}
		}, timeout);
	}

	public SocketPool(String host, int port, int timeout) {
		this(host, port, false, timeout);
	}

	public SocketPool(final String host, final int port,
			final boolean secure, final int timeout) {
		this(new SupplierEx<Socket, IOException>() {
			@Override
			public Socket get() throws IOException {
				return createSocket(host, port, secure, timeout);
			}
		}, timeout);
	}
}