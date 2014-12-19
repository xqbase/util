package com.xqbase.util.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface WrapperFactory {
	public Object getWrapper(HttpServletRequest req, HttpServletResponse resp);
}