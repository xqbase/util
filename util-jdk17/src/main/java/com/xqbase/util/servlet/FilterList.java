package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public abstract class FilterList implements Filter {
	private static final Filter[] EMPTY_FILTERS = {};

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
		final Filter[] filters = filterMap.keySet().toArray(EMPTY_FILTERS);
		/* A Chain with 3 Filters Looks Like:
		filters[0].doFilter(req, resp, (req_, resp_) -> {
			filters[1].doFilter(req_, resp_, (req__, resp__) -> {
				filters[2].doFilter(req_, resp_, chain);
			});
		}); */
		final FilterChain[] chains = new FilterChain[filters.length];
		chains[filters.length - 1] = chain;
		for (int i = filters.length - 2; i >= 0; i --) {
			final int i_ = i + 1;
			chains[i] = new FilterChain() {
				@Override
				public void doFilter(ServletRequest req_, ServletResponse resp_)
						throws IOException, ServletException {
					filters[i_].doFilter(req_, resp_, chains[i_]); 
				}
			};
		}
		filters[0].doFilter(req, resp, chains[0]);
	}
}