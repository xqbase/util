package com.xqbase.util.sns;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Log;
import com.xqbase.util.http.HttpPool;

public class TaobaoIp {
	private static final String SERVICE_URL =
			"http://ip.taobao.com/service/getIpInfo.php?ip=";

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
			JSONObject jo = new JSONObject(body.toString());
			int code = jo.optInt("code", 1);
			if (code != 0) {
				Log.w(body.toString());
				return null;
			}
			jo = jo.getJSONObject("data");
			return jo.optString("country") + "/" + jo.optString("region") +
					"/" + jo.optString("city") + "/" + jo.optString("isp");
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static void shutdown() {
		httpPool.close();
	}
}