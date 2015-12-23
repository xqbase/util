package com.xqbase.util.servlet;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.xqbase.util.Numbers;

public class SessionTimeoutListener implements HttpSessionListener {
	public static final String SESSION_TIMEOUT =
			SessionTimeoutListener.class.getName() + ".sessionTimeout";

	private volatile int interval = Integer.MIN_VALUE;

	private void init(HttpSessionEvent event) {
		if (interval != Integer.MIN_VALUE) {
			return;
		}
		synchronized (this) {
			if (interval != Integer.MIN_VALUE) {
				return;
			}
			interval = Numbers.parseInt(event.getSession().
					getServletContext().getInitParameter(SESSION_TIMEOUT),
					event.getSession().getMaxInactiveInterval());
		}
	}

	@Override
	public void sessionCreated(HttpSessionEvent event) {
		init(event);
		event.getSession().setMaxInactiveInterval(interval);
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {/**/}
}