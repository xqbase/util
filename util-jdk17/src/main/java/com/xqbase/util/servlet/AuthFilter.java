package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.xqbase.util.Base64;
import com.xqbase.util.Conf;

public class AuthFilter extends HttpFilter {
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

	private static final String AUTHORIZED =
			AuthFilter.class.getName() + ".authorized";

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		String authorization = req.getHeader("Authorization");
		if (auth == null) {
			chain.doFilter(req, resp);
			return;
		}
		HttpSession session = null;
		if (useSession) {
			session = req.getSession();
			if (session.getAttribute(AUTHORIZED) != null) {
				chain.doFilter(req, resp);
				return;
			}
		}
		if (authorization != null &&
				authorization.toUpperCase().startsWith("BASIC ") &&
				authorization.substring(6).equals(auth)) {
			if (session != null) {
				session.setAttribute(AUTHORIZED, Boolean.TRUE);
			}
			chain.doFilter(req, resp);
		} else {
			resp.setHeader("WWW-Authenticate", realm == null ? "Basic" :
					"Basic realm=\"" + realm + "\"");
			resp.sendError(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}
}