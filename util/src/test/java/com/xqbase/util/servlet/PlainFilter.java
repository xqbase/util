package com.xqbase.util.servlet;

import java.util.Collections;

import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;

@WebFilter("/plain/*")
public class PlainFilter extends ForwardedFilter {
	@Override
	public void init(FilterConfig conf) throws ServletException {
		super.init(new SimpleFilterConfig(conf, Collections.
				singletonMap(ForwardedFilter.class.getName() + ".trustedIPs",
				"127.0.0.1")));
	}
}