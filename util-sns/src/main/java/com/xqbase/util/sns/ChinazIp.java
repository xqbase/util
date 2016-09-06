package com.xqbase.util.sns;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Log;
import com.xqbase.util.http.HttpPool;

public class ChinazIp {
	private static final String SERVICE_URL =
			"http://ip.chinaz.com/ajaxsync.aspx?at=ip&ip=";

	private static HttpPool httpPool = new HttpPool(SERVICE_URL, 15000);

	public static HttpPool getHttpPool() {
		return httpPool;
	}

	public static String getIpInfo(String ip) {
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			int status = httpPool.get(ip, null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			String callback = body.toString(StandardCharsets.UTF_8);
			if (!callback.startsWith("([") && !callback.endsWith("])")) {
				Log.w(callback);
				return null;
			}
			return new JSONObject(callback.substring(2, callback.length() - 2)).
					optString("location");
		} catch (IOException | JSONException e) {
			Log.w(e.getMessage());
			return null;
		}
	}
}