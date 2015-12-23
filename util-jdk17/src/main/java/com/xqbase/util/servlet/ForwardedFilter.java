package com.xqbase.util.servlet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.xqbase.util.Base64;
import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Numbers;

public class ForwardedFilter implements Filter {
	private static final X509Certificate[] EMPTY_X509CERTS = {};

	private HashSet<String> trustedIPs = new HashSet<>();

	@Override
	public void init(FilterConfig conf) throws ServletException {
		for (String ip : conf.getInitParameter("trustedIPs").split(",")) {
			trustedIPs.add(ip);
		}
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) ||
				!trustedIPs.contains(request.getRemoteAddr())) {
			chain.doFilter(request, response);
			return;
		}

		HttpServletRequest req = (HttpServletRequest) request;
		String forwardedFor = req.getHeader("X-Forwarded-For");
		if (forwardedFor == null) {
			forwardedFor = req.getRemoteAddr();
		}

		String proto = req.getHeader("X-Forwarded-Proto");
		if (proto == null) {
			proto = req.getScheme();
		}

		String sslSessionId = req.getHeader("X-Forwarded-SSL-Session-ID");
		if (sslSessionId != null) {
			req.setAttribute("javax.servlet.request.ssl_session_id", sslSessionId);
		}

		String sslCipher = req.getHeader("X-Forwarded-SSL-Cipher");
		if (sslCipher != null) {
			req.setAttribute("javax.servlet.request.cipher_suite", sslCipher);
		}

		String certificates = req.getHeader("X-Forwarded-Certificates");
		if (certificates != null) {
			ByteArrayQueue baq = new ByteArrayQueue();
			baq.add(Base64.decode(certificates));
			try {
				Collection<? extends Certificate> certs = CertificateFactory.
						getInstance("X509").generateCertificates(baq.getInputStream());
				req.setAttribute("javax.servlet.request.X509Certificate",
						certs.toArray(EMPTY_X509CERTS));
			} catch (GeneralSecurityException e) {/**/}
		}

		final boolean secure = "https".equalsIgnoreCase(proto);
		final int serverPort;
		String host = req.getHeader("Host");
		if (host == null) {
			serverPort = secure ? 443 : 80;
		} else {
			int colon = host.indexOf(':');
			if (colon < 0) {
				serverPort = secure ? 443 : 80;
			} else {
				serverPort = Numbers.parseInt(host.substring(colon + 1));
			}
		}
		final String scheme = proto;
		final String remoteAddr = forwardedFor;
		HttpServletRequestWrapper req_ = new HttpServletRequestWrapper(req) {
			@Override
			public String getScheme() {
				return scheme;
			}

			@Override
			public boolean isSecure() {
				return secure;
			}

			@Override
			public int getServerPort() {
				return serverPort;
			}

			@Override
			public String getRemoteAddr() {
				return remoteAddr;
			}

			@Override
			public String getRemoteHost() {
				return remoteAddr;
			}
		};
		chain.doFilter(req_, response);
	}
}