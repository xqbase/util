package com.xqbase.util.wicket;

import org.apache.wicket.Request;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebCodingStrategy;
import org.apache.wicket.protocol.http.request.urlcompressing.UrlCompressingWebRequestProcessor;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.PageReferenceRequestTarget;
import org.apache.wicket.request.target.component.listener.IListenerInterfaceRequestTarget;

class ShortStrategy extends UrlCompressingWebCodingStrategy {
	@Override
	protected void addInterfaceParameters(Request request, RequestParameters parameters) {
		try {
			addInterfaceParameters(request.getParameter("x"), parameters);
		} catch (WicketRuntimeException e) {
			/*
			HttpServletRequest req = Servlets.getRequest();
			Logger.info(req.getRemoteHost() + " << " +
					req.getHeader("User-Agent") + " >> " + e.getMessage());
			*/
		}
	}

	@Override
	protected String encode(RequestCycle requestCycle,
			IListenerInterfaceRequestTarget requestTarget) {
		return super.encode(requestCycle, requestTarget).toString().
				replace(INTERFACE_PARAMETER_NAME, "x");
	}

	@Override
	protected String encode(RequestCycle requestCycle,
			PageReferenceRequestTarget requestTarget) {
		return super.encode(requestCycle, requestTarget).toString().
				replace(INTERFACE_PARAMETER_NAME, "x");
	}
}

public class ShortProcessor extends UrlCompressingWebRequestProcessor {
	@Override
	protected ShortStrategy newRequestCodingStrategy() {
		return new ShortStrategy();
	}
}