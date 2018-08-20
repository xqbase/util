package com.xqbase.util.sns;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.http.HttpPool;

public class Ip138Ip {
	public static class Exception extends java.lang.Exception {
		private static final long serialVersionUID = 1L;

		public Exception(String message) {
			super(message);
		}
	}

	private static final String SERVICE_URL =
			"http://m.ip138.com/ip.asp?ip=";

	private static HttpPool httpPool = new HttpPool(SERVICE_URL, 15000);

	public static HttpPool getHttpPool() {
		return httpPool;
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
		String html = response.toString(StandardCharsets.UTF_8);
		int i = html.indexOf("<p class=\"result\">本站主数据：");
		if (i < 0) {
			throw new Exception("Error Getting " + ip + ": " + html);
		}
		i += 24;
		int j = html.indexOf("</p>", i);
		if (j < 0) {
			throw new Exception("Error Getting " + ip + ": " + html);
		}
		return html.substring(i, j);
	}
}