package com.xqbase.util.servlet;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Base64;
import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Numbers;
import com.xqbase.util.Strings;

public class ForwardedFilter extends HttpFilter {
	private static final X509Certificate[] EMPTY_X509CERTS = {};

	private Set<String> trustedIPs;

	@Override
	public void init(FilterConfig conf) throws ServletException {
		String trustedIPs_ = conf.getInitParameter("trustedIPs");
		if (Strings.isEmpty(trustedIPs_)) {
			trustedIPs = null;
		} else {
			trustedIPs = new HashSet<>();
			for (String ip : trustedIPs_.split(",")) {
				trustedIPs.add(ip);
			}
		}
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (trustedIPs != null && !trustedIPs.contains(req.getRemoteAddr())) {
			chain.doFilter(req, resp);
			return;
		}

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
		final String host = req.getHeader("Host");
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

			@Override
			public StringBuffer getRequestURL() {
				return new StringBuffer(scheme).append("://").
						append(host).append(getRequestURI());
			}
		};
		chain.doFilter(req_, resp);
	}
}