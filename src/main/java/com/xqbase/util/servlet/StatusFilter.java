package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class StatusFilter implements Filter {
	private int status;

	@Override
	public void init(FilterConfig conf) {
		status = Integer.parseInt(conf.getInitParameter("status"));
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) {
		if (resp instanceof HttpServletResponse) {
			try {
				((HttpServletResponse) resp).sendError(status);
			} catch (IOException e) {/**/}
		}
	}
}