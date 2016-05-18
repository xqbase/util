package com.xqbase.util.servlet;

import java.util.LinkedHashMap;

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
			for (String s : requestAttributes.split(",")) {
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
		attrMap.forEach(event.getServletRequest()::setAttribute);
	}

	@Override
	public void requestDestroyed(ServletRequestEvent event) {/**/}
}