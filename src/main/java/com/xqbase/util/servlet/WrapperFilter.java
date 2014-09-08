package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.TreeMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Log;

public class WrapperFilter implements Filter {
	private ArrayList<WrapperFactory> wrappers;
	private ArrayList<AutoCloseable> closeables;

	private static void closeAll(ArrayList<AutoCloseable> closeables) {
		for (AutoCloseable closeable : closeables) {
			try {
				closeable.close();
			} catch (Exception e) {
				Log.e(e);
			}
		}
	}

	@Override
	public void init(FilterConfig conf) throws ServletException {
		ServletContext sc = conf.getServletContext();
		TreeMap<String, String> classMap = new TreeMap<>();
		Enumeration<?> en = conf.getInitParameterNames();
		while (en.hasMoreElements()) {
			String name = (String) en.nextElement();
			String value = conf.getInitParameter(name);
			classMap.put(name, value);
		}
		wrappers = new ArrayList<>();
		closeables = new ArrayList<>();
		try {
			for (String className : classMap.values()) {
				Class<?> clazz = Class.forName(className);
				Object o;
				try {
					o = clazz.getConstructor(ServletContext.class).newInstance(sc);
				} catch (NoSuchMethodException e) {
					o = clazz.newInstance();
				}
				if (o instanceof WrapperFactory) {
					wrappers.add((WrapperFactory) o);
				}
				if (o instanceof AutoCloseable) {
					closeables.add((AutoCloseable) o);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new ServletException(e);
		}
	}

	@Override
	public void destroy() {
		closeAll(closeables);
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
		if (!(req instanceof HttpServletRequest &&
				resp instanceof HttpServletResponse)) {
			chain.doFilter(req, resp);
			return;
		}
		ArrayList<AutoCloseable> closeables_ = new ArrayList<>();
		try {
			HttpServletRequest newReq = (HttpServletRequest) req;
			HttpServletResponse newResp = (HttpServletResponse) resp;
			for (WrapperFactory wrapperFactory : wrappers) {
				Object wrapper = wrapperFactory.getWrapper(newReq, newResp);
				if (wrapper instanceof HttpServletRequest) {
					newReq = (HttpServletRequest) wrapper;
				}
				if (wrapper instanceof HttpServletResponse) {
					newResp = (HttpServletResponse) wrapper;
				}
				if (wrapper instanceof AutoCloseable) {
					closeables_.add((AutoCloseable) wrapper);
				}
			}
			chain.doFilter(newReq, newResp);
		} finally {
			closeAll(closeables_);
		}
	}
}