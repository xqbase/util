package com.xqbase.util.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xqbase.util.Numbers;

public class HostFilter implements Filter {
	private HashMap<String, String> map = new HashMap<>(),
			wildcardMap = new HashMap<>();

	@Override
	public void init(FilterConfig conf) {
		Enumeration<?> e = conf.getInitParameterNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String value = conf.getInitParameter(key);
			if (key.startsWith("*.")) {
				wildcardMap.put(key.substring(2), value);
			} else {
				map.put(key, value);
			}
		}
	}

	@Override
	public void destroy() {/**/}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		if (!(request instanceof HttpServletRequest &&
				response instanceof HttpServletResponse)) {
			return;
		}
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		// "getRequestURI()" is available for HttpServlet, but not Filter
		// String prefix = req.getContextPath() + req.getServletPath();
		// String path = req.getRequestURI();
		String prefix = req.getContextPath();
		String path = req.getServletPath();
		if (path.startsWith(prefix)) {
			path = path.substring(prefix.length());
		}
		String query = req.getQueryString();
		String suffix = (path == null ? "" : path) +
				(query == null || query.isEmpty() ? "" : "?" + query);
		String host = request.getServerName();
		// Full match first, then wildcard, finally default ("_") 
		prefix = map.get(host);
		while (prefix == null) {
			int dot = host.indexOf('.');
			if (dot < 0) {
				break;
			}
			host = host.substring(dot + 1);
			prefix = wildcardMap.get(host);
		}
		if (prefix == null) {
			prefix = map.get("_");
		}
		if (prefix == null || prefix.isEmpty()) {
			chain.doFilter(request, response);
		} else if (prefix.startsWith("forward:")) {
			request.getRequestDispatcher(prefix.substring(8) +
					suffix).forward(request, response);
		} else if (prefix.startsWith("status:")) {
			int status = Numbers.parseInt(prefix.substring(7), 200, 599);
			if (status < 400) {
				resp.setStatus(status);
			} else {
				try {
					resp.sendError(status);
				} catch (IOException e) {/**/}
			}
		} else {
			int hash = prefix.indexOf('#');
			resp.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
			resp.setHeader("Location", hash < 0 ?
					prefix + suffix : prefix.substring(0, hash));
		}
	}
}