package com.xqbase.util.sns;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.json.JSONException;
import org.json.JSONObject;

import com.xqbase.util.ByteArrayQueue;
import com.xqbase.util.http.HttpPool;
import com.xqbase.util.http.HttpProxy;

public class TaobaoIp {
	public static class Exception extends java.lang.Exception {
		private static final long serialVersionUID = 1L;

		public Exception(String message) {
			super(message);
		}
	}

	public static class Location {
		private String countryId, country, regionId, region, cityId, city, ispId, isp;

		public String getCountryId() {
			return countryId;
		}

		public void setCountryId(String countryId) {
			this.countryId = countryId;
		}

		public String getCountry() {
			return country;
		}

		public void setCountry(String country) {
			this.country = country;
		}

		public String getRegionId() {
			return regionId;
		}

		public void setRegionId(String regionId) {
			this.regionId = regionId;
		}

		public String getRegion() {
			return region;
		}

		public void setRegion(String region) {
			this.region = region;
		}

		public String getCityId() {
			return cityId;
		}

		public void setCityId(String cityId) {
			this.cityId = cityId;
		}

		public String getCity() {
			return city;
		}

		public void setCity(String city) {
			this.city = city;
		}

		public String getIspId() {
			return ispId;
		}

		public void setIspId(String ispId) {
			this.ispId = ispId;
		}

		public String getIsp() {
			return isp;
		}

		public void setIsp(String isp) {
			this.isp = isp;
		}
	}

	private static final String SERVICE_URL =
			"http://ip.taobao.com/service/getIpInfo.php?ip=";
	private static final int SO_TIMEOUT = 30000;
	private static final int KA_TIMEOUT = 30000;

	private static HttpPool httpPool =
			new HttpPool(SERVICE_URL, SO_TIMEOUT, KA_TIMEOUT);

	public static synchronized void setProxy(HttpProxy proxy) {
		httpPool.close();
		httpPool = new HttpPool(proxy, SERVICE_URL, SO_TIMEOUT, KA_TIMEOUT);
	}

	public static synchronized HttpPool getHttpPool() {
		return httpPool;
	}

	public synchronized static Location getIpInfo(String ip) throws Exception {
		ByteArrayQueue response = new ByteArrayQueue();
		JSONObject json;
		try {
			int status = httpPool.get(ip, null, response, null);
			if (status != 200) {
				throw new Exception("Error Calling " + SERVICE_URL + ip +
						": " + status + ", " + response);
			}
			json = new JSONObject(response.toString(StandardCharsets.UTF_8));
			int code = json.optInt("code");
			if (code != 0) {
				throw new Exception("Error Calling " + SERVICE_URL + ip +
						": " + response);
			}
		} catch (IOException | JSONException e) {
			throw new Exception("Error Calling " + SERVICE_URL + ip +
					": " + e.getMessage() + ", " + response);
		}
		JSONObject data = json.optJSONObject("data");
		if (data == null) {
			throw new Exception("Error Calling " + SERVICE_URL + ip +
					", missing data: " + data);
		}
		Location location = new Location();
		location.setCountryId(data.optString("country_id"));
		location.setCountry(data.optString("country"));
		location.setRegion(data.optString("region"));
		location.setRegionId(data.optString("region_id"));
		location.setCity(data.optString("city"));
		location.setCityId(data.optString("city_id"));
		location.setIsp(data.optString("isp"));
		location.setIspId(data.optString("isp_id"));
		return location;
	}
}