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
import javax.servlet.http.HttpSession;

import com.xqbase.util.Base64;
import com.xqbase.util.Conf;

public class AuthFilter implements Filter {
	private String auth, realm;
	private boolean useSession;

	@Override
	public void init(FilterConfig conf) {
		auth = conf.getInitParameter("auth");
		if (auth != null) {
			auth = Base64.encode(auth.getBytes());
		}
		realm = conf.getInitParameter("realm");
		useSession = Conf.getBoolean(conf.getInitParameter("session"), false);
	}

	@Override
	public void destroy() {/**/}

	private static final String AUTHORIZED =
			AuthFilter.class.getName() + ".authorized";

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
		if (auth == null) {
			chain.doFilter(request, response);
			return;
		}
		HttpSession session = null;
		if (useSession) {
			session = req.getSession();
			if (session.getAttribute(AUTHORIZED) != null) {
				chain.doFilter(request, response);
				return;
			}
		}
		if (authorization != null &&
				authorization.toUpperCase().startsWith("BASIC ") &&
				authorization.substring(6).equals(auth)) {
			if (session != null) {
				session.setAttribute(AUTHORIZED, Boolean.TRUE);
			}
			chain.doFilter(request, response);
		} else {
			resp.setHeader("WWW-Authenticate", realm == null ? "Basic" :
					"Basic realm=\"" + realm + "\"");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}