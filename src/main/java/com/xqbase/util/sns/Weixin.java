package com.xqbase.util.sns;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Conf;
import com.xqbase.util.Log;
import com.xqbase.util.Time;
import com.xqbase.util.http.HttpPool;

public class Weixin {
	private static final String WEIXIN_API_URL = "https://api.weixin.qq.com/cgi-bin/";

	private static HttpPool weixinPool = new HttpPool(WEIXIN_API_URL, 15000);
	private static AtomicLong accessed = new AtomicLong(0);

	private static String appId, appSecret;
	private static volatile String accessToken;

	static {
		Properties p = Conf.load("Weixin");
		appId = p.getProperty("app_id");
		appSecret = p.getProperty("app_secret");
	}

	private static String getAccessToken() {
		long now = System.currentTimeMillis();
		long accessed_ = accessed.get();
		if (now < accessed_ + Time.HOUR ||
				!accessed.compareAndSet(accessed_, now)) {
			return accessToken;
		}

		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status = weixinPool.get("token?grant_type=client_credential&appid=" +
					appId + "&secret=" + appSecret, null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			JSONObject jo = new JSONObject(body.toString());
			accessToken = jo.optString("access_token");
			return accessToken;
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	@Deprecated
	public static String getUserInfo(String openId) {
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status = weixinPool.get("user/info?access_token=" + getAccessToken() +
					"&openid=" + openId + "&lang=zh_CN", null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			return body.toString(StandardCharsets.UTF_8);
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static void shutdown() {
		weixinPool.close();
	}
}