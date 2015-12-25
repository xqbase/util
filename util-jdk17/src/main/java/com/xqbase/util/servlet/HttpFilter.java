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

public abstract class HttpFilter implements Filter {
	public abstract void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException;

	@Override
	public void init(FilterConfig conf) throws ServletException {/**/}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (req instanceof HttpServletRequest &&
				resp instanceof HttpServletResponse) {
			doFilter((HttpServletRequest) req, (HttpServletResponse) resp, chain);
		} else {
			chain.doFilter(req, resp);
		}
	}
}