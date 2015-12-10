package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public abstract class FilterList implements Filter {
	protected abstract Map<Filter, FilterConfig>
			getFilterMap(FilterConfig parentConfig);

	private Map<Filter, FilterConfig> filterMap;

	@Override
	public void init(FilterConfig conf) throws ServletException {
		filterMap = getFilterMap(conf);
		for (Map.Entry<Filter, FilterConfig> entry : filterMap.entrySet()) {
			entry.getKey().init(entry.getValue());
		}
	}

	@Override
	public void destroy() {
		for (Filter filter : filterMap.keySet()) {
			filter.destroy();
		}
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		ArrayList<Filter> filterList = new ArrayList<>(filterMap.keySet());
		FilterChain[] chain_ = {chain};
		for (int i = filterList.size() - 1; i >= 0; i --) {
			Filter filter = filterList.get(i);
			chain_[0] = (req_, resp_) -> {
				filter.doFilter(req_, resp_, chain_[0]);
			};
		}
	}
}