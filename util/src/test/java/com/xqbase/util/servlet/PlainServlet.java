package com.xqbase.util.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns="/plain/*")
public class PlainServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req,
			HttpServletResponse resp) throws IOException {
		resp.setContentType("text/plain");
		PrintWriter out = resp.getWriter();
		ServletContext sc = getServletContext();
		out.println(Listener.QUERY_ENCODING + "=" + req.getAttribute(Listener.QUERY_ENCODING));
		out.println("effectiveSessionTrackingModes=" + sc.getEffectiveSessionTrackingModes());
		out.println("defaultSessionTrackingModes=" + sc.getDefaultSessionTrackingModes());
		out.println("sessionId=" + req.getSession(true).getId());
		Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			ArrayList<String> cookieList = new ArrayList<>();
			for (Cookie cookie : cookies) {
				cookieList.add(cookie.getName() + "=" + cookie.getValue());
			}
			out.println("cookies=" + cookieList);
		}
		out.println("sslSessionId=" + req.getAttribute("javax.servlet.request.ssl_session_id"));
		out.println("sslCipher=" + req.getAttribute("javax.servlet.request.cipher_suite"));
		X509Certificate[] certs = (X509Certificate[])
				req.getAttribute("javax.servlet.request.X509Certificate");
		if (certs != null) {
			out.println("certificates=" + Arrays.asList(certs));
		}
	}
}