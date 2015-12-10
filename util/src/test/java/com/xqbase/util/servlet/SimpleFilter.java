package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import com.xqbase.util.Log;

public class SimpleFilter implements Filter {
	private String filterName;

	@Override
	public void init(FilterConfig conf) {
		StringBuilder sb = new StringBuilder(conf.getServletContext().
				getServletContextName() + "/" + conf.getFilterName() + ":");
		Enumeration<String> en = conf.getInitParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			sb.append(name + "=" + conf.getInitParameter(name) + ",");
		}
		filterName = sb.substring(0, sb.length() - 1);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		Log.i(filterName);
		chain.doFilter(req, resp);
	}

	@Override
	public void destroy() {/**/}}