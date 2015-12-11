package com.xqbase.util.servlet;

import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns="/secure/*", initParams={
	/* @WebInitParam(name="proxyPass", value="http://localhost/plain"), */
	@WebInitParam(name="timeout", value="15000"),
})
public class SecureServlet extends ProxyPassServlet {
	private static final long serialVersionUID = 1L;

	@Override
	public String getInitParameter(String name) {
		switch (name) {
		case "proxyPass":
			return "http://localhost/plain";
		case "timeout":
			return "15000";
		default:
			return super.getInitParameter(name);
		}
	}
}