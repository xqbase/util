package com.xqbase.util.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Servlets {
	static ThreadLocal<Servlets> local = new ThreadLocal<Servlets>();

	ServletContext context;
	HttpServletRequest request;
	HttpServletResponse response;

	public static ServletContext getContext() {
		Servlets servlets = local.get();
		return servlets == null ? null : servlets.context;
	}

	public static HttpServletRequest getRequest() {
		Servlets servlets = local.get();
		return servlets == null ? null : servlets.request;
	}

	public static HttpServletResponse getResponse() {
		Servlets servlets = local.get();
		return servlets == null ? null : servlets.response;
	}
}