package com.xqbase.util.servlet;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

@WebListener
public class Listener implements ServletContextListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();
		sc.setInitParameter("com.xqbase.util.servlet.ForwardedWrapper.trustedIPs", "127.0.0.1");
		sc.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.SSL));
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {/**/}
}