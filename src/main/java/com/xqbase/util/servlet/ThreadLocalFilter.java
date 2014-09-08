package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Log;

public class ThreadLocalFilter implements Filter {
	private ServletContext context;

	@Override
	public void init(FilterConfig config) {
		context = config.getServletContext();
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		Servlets servlets = new Servlets();
		servlets.context = context;
		String suffix;
		if (request instanceof HttpServletRequest) {
			HttpServletRequest req = (HttpServletRequest) request;
			servlets.request = req;
			String query = req.getQueryString();
			suffix = " [" + req.getRemoteAddr() + ", " + req.getRequestURL() +
					(query == null || query.isEmpty() ? "" : "?" + query) + ", " +
					req.getHeader("Referer") + ", " + req.getHeader("User-Agent") + "]";
		} else {
			servlets.request = null;
			suffix = null;
		}
		servlets.response = response instanceof HttpServletResponse ?
				(HttpServletResponse) response : null;
		Servlets.local.set(servlets);
		Log.suffix.set(suffix);
		try {
			chain.doFilter(request, response);
		} finally {
			Log.suffix.remove();
			Servlets.local.remove();
		}
	}
}