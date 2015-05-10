package com.xqbase.util.servlet;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

@WebFilter(urlPatterns="/plain/*", initParams=@WebInitParam(name="request.1",
		value="com.xqbase.util.servlet.ForwardedWrapper"))
public class PlainFilter extends WrapperFilter {/**/}