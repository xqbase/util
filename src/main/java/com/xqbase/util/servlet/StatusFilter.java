package com.xqbase.util.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Numbers;

/** @see HostFilter */
@Deprecated
public class StatusFilter implements Filter {
	private int status;

	@Override
	public void init(FilterConfig conf) {
		status = Numbers.parseInt(conf.getInitParameter("status"), 200, 599);
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) {
		if (!(resp instanceof HttpServletResponse)) {
			return;
		}
		if (status < 400) {
			((HttpServletResponse) resp).setStatus(status);
		} else {
			try {
				((HttpServletResponse) resp).sendError(status);
			} catch (IOException e) {/**/}
		}
	}
}