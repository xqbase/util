package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Log;
import com.xqbase.util.Numbers;
import com.xqbase.util.concurrent.CountMap;

public class DoSFilter implements Filter {
	private CountMap<String> countMap = new CountMap<>();
	private AtomicLong accessed = new AtomicLong(System.currentTimeMillis());
	private int period, requests;

	@Override
	public void init(FilterConfig conf) throws ServletException {
		period = Numbers.parseInt(conf.getInitParameter("period")) * 1000;
		requests = Numbers.parseInt(conf.getInitParameter("requests"));
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		long now = System.currentTimeMillis();
		long accessed_ = accessed.get();
		if (now > accessed_ + period &&
				accessed.compareAndSet(accessed_, now)) {
			countMap.clear();
		}

		String ip = req.getRemoteAddr();
		int count = countMap.acquire(ip).get();
		if (count < requests) {
			chain.doFilter(req, resp);
		} else {
			Log.w("DoS Attack (" + count + ")");
			if (resp instanceof HttpServletResponse) {
				((HttpServletResponse) resp).
						sendError(HttpServletResponse.SC_FORBIDDEN);
			}
		}
	}
}