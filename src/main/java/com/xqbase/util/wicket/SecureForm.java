package com.xqbase.util.wicket;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.IPageMap;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.Page;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.form.Form;

import com.xqbase.util.servlet.Servlets;

public class SecureForm extends Form<Void> {
	private static final long serialVersionUID = 1L;

	private static final String HTTP_PREFIX = "http://";
	private static final String HTTPS_PREFIX = "https://";

	private int securePort, unsecurePort;

	public SecureForm(String id, int securePort) {
		super(id);
		if (Servlets.getRequest().isSecure()) {
			this.securePort = unsecurePort = 0;
		} else {
			this.securePort = securePort;
			unsecurePort = Servlets.getRequest().getServerPort();
		}
	}

	private String switchProtocol(String url, boolean secure) {
		String prefix = secure ? HTTPS_PREFIX : HTTP_PREFIX;
		if (url.startsWith(HTTP_PREFIX)) {
			return prefix + url.substring(HTTP_PREFIX.length());
		}
		if (url.startsWith(HTTPS_PREFIX)) {
			return prefix + url.substring(HTTPS_PREFIX.length());
		}
		HttpServletRequest req = Servlets.getRequest();
		StringBuilder sb = new StringBuilder();
		int port = secure ? securePort : unsecurePort;
		if (port != (secure ? 443 : 80)) {
			sb.append(":" + port);
		}
		if (!url.startsWith("/")) {
			int index = req.getRequestURI().lastIndexOf('/');
			if (index < 0) {
				sb.append('/');
			} else {
				sb.append(req.getRequestURI().substring(0, index + 1));
			}
		}
		return prefix + req.getServerName() + sb + url;
	}

	@Override
	protected void onSubmit() {
		if (securePort == 0) {
			return;
		}
		Page page = getRequestCycle().getResponsePage();
		String url = page.urlFor(IRedirectListener.INTERFACE).toString();
		IPageMap pageMap = page.getPageMap();
		if (pageMap.get(page.getNumericId(), page.getCurrentVersionNumber()) == null) {
			// Redirect may fail if Page does not exist in PageMap
			pageMap.put(page);
		}
		WicketUtil.redirect(switchProtocol(url, false));
	}

	@Override
	protected void onComponentTag(ComponentTag tag) {
		super.onComponentTag(tag);
		if (securePort == 0) {
			return;
		}
		tag.put("action", switchProtocol(tag.getAttribute("action"), true));
	}
}