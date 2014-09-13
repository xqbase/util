package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Base64;

public class AuthFilter implements Filter {
	private String auth, realm;

	@Override
	public void init(FilterConfig config) {
		auth = config.getInitParameter("auth");
		if (auth != null) {
			auth = Base64.encode(auth.getBytes());
		}
		realm = config.getInitParameter("realm");
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest) ||
				!(response instanceof HttpServletResponse)) {
			chain.doFilter(request, response);
			return;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		String authorization = req.getHeader("Authorization");
		if (auth == null || (authorization != null &&
				authorization.toUpperCase().startsWith("BASIC ") &&
				authorization.substring(6).equals(auth))) {
			chain.doFilter(request, response);
		} else {
			resp.setHeader("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}