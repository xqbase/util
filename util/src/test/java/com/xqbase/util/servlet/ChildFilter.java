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

public class ChildFilter implements Filter {
	private String filterName;

	@Override
	public void init(FilterConfig conf) {
		StringBuilder sb = new StringBuilder();
		Enumeration<String> en = conf.getInitParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			sb.append("," + name + "=" + conf.getInitParameter(name));
		}
		filterName = conf.getFilterName() + " {" + sb.substring(1) + "}";
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		Log.i("Enter " + filterName);
		try {
			chain.doFilter(req, resp);
		} finally {
			Log.i("Leave " + filterName);
		}
	}

	@Override
	public void destroy() {/**/}}