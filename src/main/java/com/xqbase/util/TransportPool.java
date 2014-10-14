package com.xqbase.util;

import javax.mail.Session;
import javax.mail.internet.InternetAddress;

/** @see com.xqbase.util.mail.TransportPool */
@Deprecated
public class TransportPool extends com.xqbase.util.mail.TransportPool {
	public TransportPool(Session session, String protocol, String user,
			String password, InternetAddress from) {
		super(session, protocol, user, password, from);
	}
}