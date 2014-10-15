package com.xqbase.util.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** @see HostFilter */
@Deprecated
public class RedirectServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private String location;
	private int hash;

	@Override
	public void init() throws ServletException {
		location = getInitParameter("location");
		hash = location.indexOf('#');
	}

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		String query = req.getQueryString();
		String suffix = req.getPathInfo() +
				(query == null || query.isEmpty() ? "" : "?" + query);
		resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
		resp.setHeader("Location", hash < 0 ?
				location + suffix : location.substring(0, hash));
	}
}