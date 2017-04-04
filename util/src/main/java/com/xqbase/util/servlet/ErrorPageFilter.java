package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import com.xqbase.util.Numbers;

class ErrorPageResponse extends HttpServletResponseWrapper {
	private HttpServletRequest req;
	private HttpServletResponse resp;
	private Map<Integer, String> map;

	ErrorPageResponse(HttpServletRequest req, HttpServletResponse resp,
			Map<Integer, String> map) {
		super(resp);
		this.req = req;
		this.resp = resp;
		this.map = map;
	}

	private boolean handleError(int sc) throws IOException {
		String location = map.get(Integer.valueOf(sc));
		if (location == null) {
			return false;
		}
		if (location.startsWith("forward:")) {
			try {
				req.getRequestDispatcher(location.substring(8)).forward(req, resp);
			} catch (ServletException e) {
				throw new IOException(e);
			}
		} else {
			resp.sendRedirect(location);
		}
		return true;
	}

	@Override
	public void sendError(int sc, String msg) throws IOException {
		if (!handleError(sc)) {
			super.sendError(sc, msg);
		}
	}

	@Override
	public void sendError(int sc) throws IOException {
		if (!handleError(sc)) {
			super.sendError(sc);
		}
	}
}

public class ErrorPageFilter implements HttpFilter {
	private Map<Integer, String> map = new HashMap<>();

	@Override
	public void init(FilterConfig conf) {
		Enumeration<?> e = conf.getInitParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			map.put(Integer.valueOf(Numbers.parseInt(key)),
					conf.getInitParameter(key));
		}
	}

	@Override
	public void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(req, new ErrorPageResponse(req, resp, map));
	}
}