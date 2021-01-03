package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Strings;

public class HttpsFilter extends HttpFilter {
	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (req.isSecure()) {
			chain.doFilter(req, resp);
			return;
		}
		switch (req.getMethod()) {
		case "GET":
		case "HEAD":
			break;
		default:
			chain.doFilter(req, resp);
			return;
		}
		// "getRequestURI()" is available for HttpServlet, but not Filter
		// String prefix = req.getContextPath() + req.getServletPath();
		// String path = req.getRequestURI();
		String prefix = req.getContextPath();
		String path = req.getServletPath();
		if (path.startsWith(prefix)) {
			path = path.substring(prefix.length());
		}
		String query = req.getQueryString();
		String suffix = (path == null ? "" : path) +
				(Strings.isEmpty(query) ? "" : "?" + query);
		resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		resp.setHeader("Location",
				"https://" + req.getServerName() + prefix + suffix);
	}
}