package com.xqbase.util.sns;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.Conf;
import com.xqbase.util.Log;
import com.xqbase.util.Numbers;
import com.xqbase.util.http.HttpForm;
import com.xqbase.util.http.HttpPool;

public class Weibo {
	public static class Status {
		private static final String ENC_TAB =
				"0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

		public long id, uid;
		public String text, pic, uname, upic;
		public Point2D.Float point;

		public String getUrl() {
			String mid = "";
			long rr = id;
			while (true) {
				int r = (int) (rr % 10000000);
				int digits = 4;
				while (r > 0) {
					mid = ENC_TAB.charAt(r % 62) + mid;
					r /= 62;
					digits --;
				}
				rr /= 10000000;
				if (rr == 0) {
					break;
				}
				for (int i = 0; i < digits; i ++) {
					mid = '0' + mid;
				}
			}
			return "http://weibo.com/" + uid + "/" + mid;
		}
	}

	private static final String WEIBO_API_URL = "https://api.weibo.com/";

	private static HttpPool httpPool = new HttpPool(WEIBO_API_URL, 15000);
	private static String appKey, appSecret;
	private static long weiboId;

	static {
		Properties p = Conf.load("Weibo");
		appKey = p.getProperty("app_key");
		appSecret = p.getProperty("app_secret");
		weiboId = Numbers.parseLong(p.getProperty("weibo_id"));
	}

	public static HttpPool getHttpPool() {
		return httpPool;
	}

	public static String getAccessToken() {
		Properties p = Conf.load("Weibo");
		return p.getProperty("access_token");
	}

	private static String encode(String s) {
		try {
			return URLEncoder.encode(s, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getShareUrl(String title, String url, String pic) {
		return "http://service.weibo.com/share/share.php?title=" +
				encode(title) + "&url=" + encode(url) +
				(pic == null ? "" : "&pic=" + encode(pic)) +
				"&appkey=" + appKey;
	}

	public static String getLoginUrl(String redirectUri) {
		return WEIBO_API_URL + "oauth2/authorize?client_id=" +
				appKey + "&redirect_uri=" + encode(redirectUri);
	}

	public static long getUid(String code, String redirectUri, String[] accessToken) {
		HttpForm form = new HttpForm(false, StandardCharsets.UTF_8);
		form.add("client_id", appKey);
		form.add("client_secret", appSecret);
		form.add("grant_type", "authorization_code");
		form.add("code", code);
		form.add("redirect_uri", redirectUri);
		ByteArrayQueue body = new ByteArrayQueue();
		long uid;
		String token;
		try {
			int status = httpPool.post("oauth2/access_token",
					form.getBody(), form.getHeaders(), body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return 0;
			}
			JSONObject jo = new JSONObject(body.toString());
			uid = jo.getLong("uid");
			token = jo.getString("access_token");
		} catch (IOException | JSONException e) {
			Log.e(e);
			return 0;
		}
		if (uid == weiboId) {
			Properties p = new Properties();
			p.setProperty("app_key", appKey);
			p.setProperty("app_secret", appSecret);
			p.setProperty("weibo_id", "" + weiboId);
			p.setProperty("access_token", token);
			Conf.store("Weibo", p);
		}
		if (accessToken != null && accessToken.length > 0) {
			accessToken[0] = token;
		}
		return uid;
	}

	public static long getUid(String accessToken) {
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status = httpPool.get("2/account/get_uid.json" +
					"?access_token=" + accessToken, null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return 0;
			}
			return new JSONObject(body.toString(StandardCharsets.UTF_8)).getLong("uid");
		} catch (IOException | JSONException e) {
			Log.e(e);
			return 0;
		}
	}

	public static String getName() {
		return getName(getAccessToken(), weiboId);
	}

	public static String getName(String accessToken, long uid) {
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status = httpPool.get("2/users/show.json" +
					"?access_token=" + accessToken + "&uid=" + uid, null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			return new JSONObject(body.toString(StandardCharsets.UTF_8)).getString("name");
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static ArrayList<Status> getMentions(String accessToken, long sinceId, int count) {
		try {
			ByteArrayQueue body = new ByteArrayQueue();
			if (httpPool.get("2/statuses/mentions.json?access_token=" +
					accessToken + "&since_id=" + sinceId + "&count=" + count,
					null, body, null) >= 400) {
				Log.w(body.toString());
				return null;
			}

			ArrayList<Status> list = new ArrayList<>();
			JSONArray statuses = new JSONObject(body.toString(StandardCharsets.UTF_8)).
					getJSONArray("statuses");
			int length = statuses.length();
			for (int i = 0; i < length; i ++) {
				Status status = new Status();
				JSONObject json = statuses.getJSONObject(i);
				status.id = json.getLong("id");
				status.text = json.getString("text");
				status.pic = json.optString("original_pic", null);
				JSONObject user = json.getJSONObject("user");
				status.uid = user.getLong("id");
				status.uname = user.getString("name");
				status.upic = user.optString("avatar_large", null);
				JSONObject geo = json.optJSONObject("geo");
				status.point = null;
				if (geo != null) {
					JSONArray coordinates = geo.optJSONArray("coordinates");
					if (coordinates != null) {
						status.point = new Point2D.Float((float) coordinates.
								getDouble(1), (float) coordinates.getDouble(0));
					}
				}
				list.add(status);
			}

			return list;
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static void follow(String accessToken) {
		HttpForm form = new HttpForm(false, StandardCharsets.UTF_8);
		form.add("access_token", accessToken);
		form.add("uid", "" + weiboId);
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status = httpPool.post("2/friendships/create.json",
					form.getBody(), form.getHeaders(), body, null);
			if (status >= 400) {
				Log.w(body.toString());
			}
		} catch (IOException e) {
			Log.e(e);
		}
	}

	public static boolean upload(String accessToken, String status, byte[] pic) {
		HttpForm form = new HttpForm(true, StandardCharsets.UTF_8);
		form.add("access_token", accessToken);
		try {
			form.add("status", URLEncoder.encode(status, "UTF-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		form.next("pic", "pic.gif", "image/gif");
		form.getBody().add(pic);
		form.finish();
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status_ = httpPool.post("2/statuses/upload.json",
					form.getBody(), form.getHeaders(), body, null);
			if (status_ >= 400) {
				Log.w(body.toString());
				return false;
			}
			return true;
		} catch (IOException e) {
			Log.e(e);
			return false;
		}
	}

	public static boolean comment(String accessToken, String comment, long id) {
		HttpForm form = new HttpForm(false, StandardCharsets.UTF_8);
		form.add("access_token", accessToken);
		form.add("comment", comment);
		form.add("id", "" + id);
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			if (httpPool.post("2/comments/create.json", form.getBody(),
					form.getHeaders(), body, null) >= 400) {
				Log.w(body.toString());
				return false;
			}
			return true;
		} catch (IOException e) {
			Log.e(e);
			return false;
		}
	}

	public static LinkedHashMap<String, String> getEmotionMap() {
		try {
			LinkedHashMap<String, String> map = new LinkedHashMap<>();
			ByteArrayQueue body = new ByteArrayQueue();
			if (httpPool.get("2/emotions.json?source=" + appKey,
					null, body, null) >= 400) {
				Log.w(body.toString());
				return null;
			}
			JSONArray emotions = new JSONArray(body.toString(StandardCharsets.UTF_8));
			int length = emotions.length();
			for (int i = 0; i < length; i ++) {
				JSONObject emotion = emotions.getJSONObject(i);
				map.put(emotion.getString("value"), emotion.getString("url"));
			}
			return map;
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static String shortUrl(String url) {
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			int status = httpPool.get("2/short_url/shorten.json" +
					"?source=" + appKey + "&url_long=" +
					URLEncoder.encode(url, "UTF-8"), null, body, null);
			if (status >= 400) {
				Log.w(body.toString());
				return null;
			}
			JSONArray urls = new JSONObject(body.
					toString(StandardCharsets.UTF_8)).getJSONArray("urls");
			if (urls == null || urls.length() == 0) {
				Log.w(body.toString());
				return null;
			}
			JSONObject obj = urls.getJSONObject(0);
			if (obj == null) {
				Log.w(body.toString());
				return null;
			}
			return obj.getString("url_short");
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static String getAddress(Point2D.Float point) {
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			if (httpPool.get("2/location/geo/geo_to_address.json?source=" +
					appKey + "&coordinate=" + point.x + "," + point.y,
					null, body, null) >= 400) {
				Log.w(body.toString());
				return null;
			}
			JSONArray geos = new JSONObject(body.toString(StandardCharsets.UTF_8)).
					getJSONArray("geos");
			if (geos.length() == 0) {
				return null;
			}
			return geos.getJSONObject(0).getString("address");
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static String[] ipLocation(String... ips) {
		if (ips == null) {
			return null;
		}
		if (ips.length == 0) {
			return new String[0];
		}
		StringBuilder sb = new StringBuilder();
		for (String ip : ips) {
			sb.append(ip + ",");
		}
		String ip = sb.substring(0, sb.length() - 1);
		ByteArrayQueue body = new ByteArrayQueue();
		try {
			if (httpPool.get("2/location/geo/ip_to_geo.json?source=" +
					appKey + "&ip=" + ip, null, body, null) >= 400) {
				Log.w(body.toString());
				return null;
			}
			JSONArray geos = new JSONObject(body.toString(StandardCharsets.UTF_8)).
					optJSONArray("geos");
			if (geos == null || geos.length() != ips.length) {
				Log.w("Incorrect \"geos\" length for IP " + ip);
				Log.w(body.toString(StandardCharsets.UTF_8));
				return null;
			}
			String[] locations = new String[ips.length];
			for (int i = 0; i < locations.length; i ++) {
				locations[i] = geos.getJSONObject(i).getString("more");
			}
			return locations;
		} catch (IOException | JSONException e) {
			Log.e(e);
			return null;
		}
	}

	public static void shutdown() {
		httpPool.close();
	}
}