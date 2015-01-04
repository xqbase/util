package com.xqbase.util.sns;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;

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
		public HashSet<String> privilege = new HashSet<>(); 
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
		ByteArrayQueue body = new ByteArrayQueue();
		JSONObject jo;
		try {
			int status = httpPool.get("oauth2/access_token?appid=" +
					appId + "&secret=" + appSecret + "&code=" + code +
					"&grant_type=authorization_code", null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			jo = new JSONObject(body.toString());
			String accessToken = jo.optString("access_token");
			String openid = jo.optString("openid");
			if (accessToken == null || openid == null) {
				Log.w(body.toString());
				return null;
			}
			status = httpPool.get("userinfo?access_token=" + accessToken +
					"&openid=" + openid, null, body, null);
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
		ui.openid = jo.optString("openid");
		ui.nickname = jo.optString("nickname");
		ui.sex = jo.optString("sex");
		ui.province = jo.optString("province");
		ui.city = jo.optString("city");
		ui.country = jo.optString("country");
		ui.headimgurl = jo.optString("headimgurl");
		ui.unionid = jo.optString("unionid");
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

	public static void shutdown() {
		httpPool.close();
	}
}