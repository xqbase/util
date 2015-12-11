package com.xqbase.util.servlet;

import java.util.Collections;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

@WebListener
public class Listener extends RequestAttributesListener
		implements ServletContextListener {
	public static final String QUERY_ENCODING =
			"org.eclipse.jetty.server.Request.queryEncoding";

	@Override
	public void contextInitialized(ServletContextEvent event) {
		ServletContext sc = event.getServletContext();
		sc.setInitParameter(REQUEST_ATTRIBUTES, QUERY_ENCODING + "=ISO-8859-1");
		sc.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.SSL));
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {/**/}
}