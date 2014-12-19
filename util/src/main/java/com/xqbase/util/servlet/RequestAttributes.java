package com.xqbase.util.servlet;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RequestAttributes implements WrapperFactory {
	private LinkedHashMap<String, String> attrMap = new LinkedHashMap<>();

	public RequestAttributes(ServletContext sc) {
		String[] ss = sc.getInitParameter(RequestAttributes.
				class.getName() + ".requestAttributes").split(",");
		for (String s : ss) {
			String[] keyValue = s.split("=", 2);
			attrMap.put(keyValue[0], keyValue[1]);
		}
	}

	@Override
	public Void getWrapper(HttpServletRequest req, HttpServletResponse resp) {
		for (Map.Entry<String, String> entry : attrMap.entrySet()) {
			req.setAttribute(entry.getKey(), entry.getValue());
		}
		return null;
	}
}