package com.xqbase.util.sns;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.http.HttpPool;

public class ChinazIp {
	public static class Exception extends java.lang.Exception {
		private static final long serialVersionUID = 1L;

		public Exception(String message) {
			super(message);
		}
	}

	private static final String SERVICE_URL =
			"http://ip.chinaz.com/ajaxsync.aspx?at=ipbatch&ip=";

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
			String callback = response.toString(StandardCharsets.UTF_8);
			if (!callback.startsWith("([") && !callback.endsWith("])")) {
				throw new Exception("Error Getting " + ip + ": " + callback);
			}
			return new JSONObject(callback.substring(2, callback.length() - 2)).
					optString("location");
		} catch (IOException | JSONException e) {
			throw new Exception("Error Getting " + ip +
					": " + e.getMessage() + ", " + response);
		}
	}
}