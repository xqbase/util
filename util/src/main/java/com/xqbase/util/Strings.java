package com.xqbase.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

public class Strings {
	public static boolean isEmpty(String s) {
		return s == null || s.isEmpty();
	}

	public static boolean isBlank(String s) {
		return s == null || s.trim().isEmpty();
	}

	public static String truncate(String s, int len) {
		return s == null ? "" : s.length() > len ? s.substring(0, len) : s;
	}

	public static String join(String separator, String... elements) {
		StringBuilder sb = new StringBuilder();
		for (String element : elements) {
			if (!isEmpty(element)) {
				if (sb.length() > 0) {
					sb.append(separator);
				}
				sb.append(element);
			}
		}
		return sb.toString();
	}

	public static String encodeUrl(String s) {
		try {
			return s == null ? "" : URLEncoder.encode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String decodeUrl(String s) {
		try {
			return s == null ? "" : URLDecoder.decode(s, "UTF-8");
		} catch (IllegalArgumentException e) {
			return "";
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}