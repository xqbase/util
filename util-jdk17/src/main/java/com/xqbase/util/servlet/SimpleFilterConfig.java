package com.xqbase.util.servlet;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

public class SimpleFilterConfig implements FilterConfig {
	private String filterName;
	private ServletContext servletContext;
	private Map<String, String> initParameters;

	private static LinkedHashMap<String, String> mergeParameters(FilterConfig conf,
			Map<String, String> initParameters) {
		LinkedHashMap<String, String> map = new LinkedHashMap<>();
		Enumeration<String> en = conf.getInitParameterNames();
		while (en.hasMoreElements()) {
			String name = en.nextElement();
			map.put(name, conf.getInitParameter(name));
		}
		map.putAll(initParameters);
		return map;
	}

	public SimpleFilterConfig(String filterName, ServletContext servletContext,
			Map<String, String> initParameters) {
		this.filterName = filterName;
		this.servletContext = servletContext;
		this.initParameters = initParameters;
	}

	public SimpleFilterConfig(FilterConfig filterConfig,
			Map<String, String> initParameters) {
		this(filterConfig.getFilterName(), filterConfig.getServletContext(),
				mergeParameters(filterConfig, initParameters));
	}

	public SimpleFilterConfig(FilterConfig filterConfig) {
		this(filterConfig, Collections.<String, String>emptyMap());
	}

	@Override
	public String getFilterName() {
		return filterName;
	}

	@Override
	public ServletContext getServletContext() {
		return servletContext;
	}

	@Override
	public String getInitParameter(String name) {
		return initParameters.get(name);
	}

	@Override
	public Enumeration<String> getInitParameterNames() {
		return Collections.enumeration(initParameters.keySet());
	}
}