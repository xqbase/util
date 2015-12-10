package com.xqbase.util.servlet;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(urlPatterns="/*", initParams={@WebInitParam(name="NAME", value="VALUE")})
public class TestFilter extends FilterList {
	@Override
	protected Map<Filter, FilterConfig> getFilterMap(FilterConfig parentConfig) {
		ServletContext context = parentConfig.getServletContext();
		LinkedHashMap<Filter, FilterConfig> filterMap = new LinkedHashMap<>();
		filterMap.put(new SimpleFilter(), new SimpleFilterConfig("TEST-1", context, Collections.singletonMap("A", "B")));
		filterMap.put(new SimpleFilter(), new SimpleFilterConfig("TEST-2", context, Collections.singletonMap("C", "D")));
		filterMap.put(new SimpleFilter(), new SimpleFilterConfig("TEST-3", context, Collections.singletonMap("E", "F")));
		return filterMap;
	}
}