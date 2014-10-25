package com.xqbase.util.servlet;

import java.security.GeneralSecurityException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashSet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Base64;
import com.xqbase.util.ByteArrayQueue;

public class ForwardedWrapper implements WrapperFactory {
	private static final X509Certificate[] EMPTY_X509CERTS = {};

	private HashSet<String> trustedIPs = new HashSet<>();

	public ForwardedWrapper(ServletContext sc) {
		for (String ip : sc.getInitParameter(ForwardedWrapper.
				class.getName() + ".trustedIPs").split(",")) {
			trustedIPs.add(ip);
		}
	}

	@Override
	public HttpServletRequestWrapper getWrapper(HttpServletRequest req,
			HttpServletResponse resp) {
		if (!trustedIPs.contains(req.getRemoteAddr())) {
			return null;
		}

		String scheme = req.getHeader("X-Forwarded-Proto");
		if (scheme == null) {
			scheme = req.getScheme();
		}

		String remote = req.getHeader("X-Forwarded-For");
		if (remote == null) {
			remote = req.getRemoteAddr();
		}

		String pkcs7 = req.getHeader("X-Pkcs7-Certificates-Base64");
		if (pkcs7 != null) {
			ByteArrayQueue baq = new ByteArrayQueue();
			baq.add(Base64.decode(pkcs7));
			try {
				Collection<? extends Certificate> certs = CertificateFactory.
						getInstance("X509").generateCertificates(baq.getInputStream());
				req.setAttribute("javax.servlet.request.X509Certificate",
						certs.toArray(EMPTY_X509CERTS));
			} catch (GeneralSecurityException e) {/**/}
		}

		String scheme_ = scheme;
		String remote_ = remote;
		return new HttpServletRequestWrapper(req) {
			@Override
			public String getScheme() {
				return scheme_;
			}

			@Override
			public boolean isSecure() {
				return super.isSecure() || "https".equalsIgnoreCase(scheme_);
			}

			@Override
			public String getRemoteAddr() {
				return remote_;
			}

			@Override
			public String getRemoteHost() {
				return remote_;
			}
		};
	}
}