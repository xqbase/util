package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedHashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class RequestAttributesFilter implements Filter {
	private LinkedHashMap<String, String> attrMap = new LinkedHashMap<>();

	@Override
	public void init(FilterConfig conf) throws ServletException {
		Enumeration<String> en = conf.getInitParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			attrMap.put(name, conf.getInitParameter(name));
		}
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		attrMap.forEach(req::setAttribute);
		chain.doFilter(req, resp);
	}
}