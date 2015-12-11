package com.xqbase.util.servlet;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(filterName="parent", urlPatterns="/*",
		initParams=@WebInitParam(name="KEY", value="VALUE"))
public class ParentFilter extends FilterList {
	@Override
	protected Map<Filter, FilterConfig> getFilterMap(FilterConfig parentConfig) {
		ServletContext context = parentConfig.getServletContext();
		LinkedHashMap<Filter, FilterConfig> filterMap = new LinkedHashMap<>();
		filterMap.put(new ChildFilter(), new SimpleFilterConfig(parentConfig));
		filterMap.put(new ThreadLocalFilter(), new SimpleFilterConfig(parentConfig));
		filterMap.put(new BandwidthFilter(), new SimpleFilterConfig(null, null,
				Collections.singletonMap("limit", "1024")));
		filterMap.put(new ChildFilter(), new SimpleFilterConfig("Post-ThreadLocalFilter",
				context, Collections.singletonMap("key", "value")));
		return filterMap;
	}
}