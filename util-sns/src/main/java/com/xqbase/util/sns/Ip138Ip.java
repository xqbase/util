package com.xqbase.util.sns;

import java.io.IOException;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.http.HttpPool;
import com.xqbase.util.http.HttpProxy;

public class Ip138Ip {
	public static class Exception extends java.lang.Exception {
		private static final long serialVersionUID = 1L;

		public Exception(String message) {
			super(message);
		}
	}

	private static final String SERVICE_URL =
			"https://www.ip138.com/iplookup.asp?action=2&ip=";

	private static volatile HttpPool httpPool = new HttpPool(SERVICE_URL, 15000);

	public static HttpPool getHttpPool() {
		return httpPool;
	}

	public static void setProxy(HttpProxy proxy) {
		HttpPool originalPool = httpPool;
		httpPool = new HttpPool(proxy, SERVICE_URL, 15000);
		originalPool.close();
	}

	public static String getIpInfo(String ip) throws Exception {
		ByteArrayQueue response = new ByteArrayQueue();
		try {
			int status = httpPool.get(ip, null, response, null);
			if (status != 200) {
				throw new Exception("Error Getting " + ip +
						": " + status + ", " + response);
			}
		} catch (IOException e) {
			throw new Exception("Error Getting " + ip +
					": " + e.getMessage() + ", " + response);
		}
		String html;
		try {
			html = response.toString("GBK");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		int i = html.indexOf("<font color=\"blue\">ASN归属地：");
		if (i < 0) {
			throw new Exception("Error Getting " + ip + ": " + html);
		}
		i += 26;
		int j = html.indexOf("</font>", i);
		if (j < 0) {
			throw new Exception("Error Getting " + ip + ": " + html);
		}
		return html.substring(i, j).trim();
	}
}