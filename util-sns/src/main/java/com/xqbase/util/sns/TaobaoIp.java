package com.xqbase.util.sns;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Log;
import com.xqbase.util.http.HttpUtil;

public class TaobaoIp {
	private static final String SERVICE_URL =
			"http://ip.taobao.com/service/getIpInfo.php?ip=";

	public static String getIpInfo(String ip) {
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			int status = HttpUtil.get(SERVICE_URL + ip, null, body, null, 30000);
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
			Log.w(e.getMessage());
			return null;
		}
	}
}