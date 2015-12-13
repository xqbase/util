package com.xqbase.util.servlet;

import java.util.Collections;
import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionTrackingMode;
import javax.servlet.annotation.WebListener;

import com.xqbase.util.Log;

@WebListener
public class TestListener implements ServletContextListener {
	public static final String QUERY_ENCODING =
			"org.eclipse.jetty.server.Request.queryEncoding";

	@Override
	public void contextInitialized(ServletContextEvent event) {
		EnumSet<DispatcherType> all = EnumSet.allOf(DispatcherType.class);
		ServletContext sc = event.getServletContext();
		sc.setSessionTrackingModes(Collections.singleton(SessionTrackingMode.SSL));

		sc.setInitParameter(RequestAttributesListener.REQUEST_ATTRIBUTES,
				QUERY_ENCODING + "=ISO-8859-1");
		sc.addListener(new RequestAttributesListener());

		FilterRegistration.Dynamic filterReg;
		filterReg = sc.addFilter("Pre-ThreadLocal", new TestFilter());
		filterReg.addMappingForUrlPatterns(all, false, "/*");
		filterReg = sc.addFilter("ThreadLocal", new ThreadLocalFilter());
		filterReg.addMappingForUrlPatterns(all, false, "/*");
		filterReg = sc.addFilter("Bandwidth", new BandwidthFilter());
		filterReg.setInitParameter("limit", "1024");
		filterReg.addMappingForUrlPatterns(all, false, "/*");
		filterReg = sc.addFilter("Post-ThreadLocal", new TestFilter());
		filterReg.setInitParameter("key", "value");
		filterReg.addMappingForUrlPatterns(all, false, "/*");
		filterReg = sc.addFilter("Forwarded", new ForwardedFilter());
		filterReg.setInitParameter("trustedIPs", "127.0.0.1");
		filterReg.addMappingForUrlPatterns(all, false, "/plain/*");

		ServletRegistration.Dynamic servletReg;
		servletReg = sc.addServlet("Plain", new TestServlet());
		servletReg.addMapping("/plain/*");
		servletReg = sc.addServlet("Secure", new ProxyPassServlet());
		servletReg.setInitParameter("proxyPass", "http://localhost/plain");
		servletReg.setInitParameter("timeout", "15000");
		servletReg.addMapping("/secure/*");

		Log.i(sc.getServletContextName() + " Started");
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		Log.i(event.getServletContext().getServletContextName() + " Stopped");
	}
}