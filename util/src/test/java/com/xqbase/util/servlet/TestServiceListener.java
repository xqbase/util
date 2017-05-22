package com.xqbase.util.servlet;

import javax.servlet.ServletContextEvent;
// import javax.servlet.annotation.WebListener;

import com.xqbase.util.TestService;

// @WebListener
public class TestServiceListener extends ServiceListener {
	@Override
	public void contextInitialized(ServletContextEvent event) {
		event.getServletContext().setInitParameter(ServiceListener.SERVICES,
				TestService.class.getName());
		super.contextInitialized(event);
	}
}