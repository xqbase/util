package com.xqbase.util.sns;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Conf;
import com.xqbase.util.Log;
import com.xqbase.util.http.HttpPool;

public class Weixin {
	public static class UserInfo {
		public String openid, nickname, sex, province, city, country, headimgurl, unionid;
		public Set<String> privilege = new HashSet<>(); 
	}

	private static final String WEIXIN_API_URL = "https://api.weixin.qq.com/sns/";

	private static HttpPool httpPool = new HttpPool(WEIXIN_API_URL, 15000);
	private static String appId, appSecret;

	static {
		Properties p = Conf.load("Weixin");
		appId = p.getProperty("app_id");
		appSecret = p.getProperty("app_secret");
	}

	public static HttpPool getHttpPool() {
		return httpPool;
	}

	public static String getLoginUrl(String redirectUri) {
		try {
			return "https://open.weixin.qq.com/connect/oauth2/authorize?appid=" + appId +
					"&redirect_uri=" + URLEncoder.encode(redirectUri, "UTF-8") +
					"&response_type=code&scope=snsapi_login#wechat_redirect";
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static UserInfo getUserInfo(String code) {
		JSONObject jo;
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			int status = httpPool.get("oauth2/access_token?appid=" +
					appId + "&secret=" + appSecret + "&code=" + code +
					"&grant_type=authorization_code", null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			jo = new JSONObject(body.toString());
			String accessToken = jo.optString("access_token", null);
			String openid = jo.optString("openid", null);
			if (accessToken == null) {
				return null;
			}

			body.clear();
			status = httpPool.get("userinfo?access_token=" +
					accessToken + "&openid=" + openid, null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			jo = new JSONObject(body.toString(StandardCharsets.UTF_8));
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
		UserInfo ui = new UserInfo();
		ui.openid = jo.optString("openid", null);
		if (ui.openid == null) {
			return null;
		}
		ui.nickname = jo.optString("nickname", null);
		ui.sex = jo.optString("sex", null);
		ui.province = jo.optString("province", null);
		ui.city = jo.optString("city", null);
		ui.country = jo.optString("country", null);
		ui.headimgurl = jo.optString("headimgurl", null);
		ui.unionid = jo.optString("unionid", null);
		JSONArray ja = jo.optJSONArray("privilege");
		if (ja == null) {
			return ui;
		}
		int length = ja.length();
		for (int i = 0; i < length; i ++) {
			ui.privilege.add(ja.optString(i));
		}
		return ui;
	}

	/** @deprecated Use <code>getHttpPool().close()</code> */
	@Deprecated
	public static void shutdown() {
		httpPool.close();
	}
}