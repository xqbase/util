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

import com.xqbase.util.Numbers;
import com.xqbase.util.Strings;

public class HostFilter extends HttpFilter {
	private Map<String, String> map = new HashMap<>();
	private Map<String, String> wildcardMap = new HashMap<>();

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
	public void doFilter(HttpServletRequest req, HttpServletResponse resp,
			FilterChain chain) throws IOException, ServletException {
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
				(Strings.isEmpty(query) ? "" : "?" + query);
		String host = req.getServerName();
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
		if (Strings.isEmpty(prefix)) {
			chain.doFilter(req, resp);
		} else if (prefix.startsWith("forward:")) {
			req.getRequestDispatcher(prefix.substring(8) +
					suffix).forward(req, resp);
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