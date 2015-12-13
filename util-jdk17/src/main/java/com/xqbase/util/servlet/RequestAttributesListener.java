package com.xqbase.util.servlet;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;

public class RequestAttributesListener implements ServletRequestListener {
	public static final String REQUEST_ATTRIBUTES =
			RequestAttributesListener.class.getName() + ".requestAttributes";

	private volatile LinkedHashMap<String, String> attrMap = null;

	private void init(ServletRequestEvent event) {
		if (attrMap != null) {
			return;
		}
		synchronized (this) {
			if (attrMap != null) {
				return;
			}
			attrMap = new LinkedHashMap<>();
			String requestAttributes = event.getServletContext().
					getInitParameter(REQUEST_ATTRIBUTES);
			if (requestAttributes == null) {
				return;
			}
			String[] ss = requestAttributes.split(",");
			for (String s : ss) {
				int eq = s.indexOf('=');
				if (eq >= 0) {
					attrMap.put(s.substring(0, eq), s.substring(eq + 1));
				}
			}
		}
	}

	@Override
	public void requestInitialized(ServletRequestEvent event) {
		init(event);
		ServletRequest req = event.getServletRequest();
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			req.setAttribute(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void requestDestroyed(ServletRequestEvent event) {/**/}
}