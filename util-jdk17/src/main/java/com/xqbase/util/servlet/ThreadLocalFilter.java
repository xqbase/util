package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Log;

public class ThreadLocalFilter extends HttpFilter {
	private ServletContext context;

	@Override
	public void init(FilterConfig conf) {
		context = conf.getServletContext();
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		Servlets servlets = new Servlets();
		servlets.context = context;
		servlets.request = req;
		servlets.response = resp;
		Servlets.local.set(servlets);
		String query = req.getQueryString();
		Log.suffix.set(" [" + req.getRemoteAddr() + ", " + req.getRequestURL() +
				(query == null || query.isEmpty() ? "" : "?" + query) + ", " +
				req.getHeader("Referer") + ", " + req.getHeader("User-Agent") + "]");
		try {
			chain.doFilter(req, resp);
		} finally {
			Log.suffix.remove();
			Servlets.local.remove();
		}
	}
}