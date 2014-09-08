package com.xqbase.util;

public class Numbers {
	public static int minMax(int min, int mid, int max) {
		return mid < min ? min : mid > max ? max : mid;
	}

	public static int parseInt(String s) {
		return parseInt(s, 0);
	}

	public static int parseInt(String s, int i) {
		if (s == null) {
			return i;
		}
		try {
			return Integer.parseInt(s.trim());
		} catch (NumberFormatException e) {
			return i;
		}
	}

	public static int parseInt(String s, int min, int max) {
		return parseInt(s, 0, min, max);
	}

	public static int parseInt(String s, int i, int min, int max) {
		return minMax(min, parseInt(s, i), max);
	}

	public static long parseLong(String s) {
		return parseLong(s, 0);
	}

	public static long parseLong(String s, long l) {
		if (s == null) {
			return l;
		}
		try {
			return Long.parseLong(s.trim());
		} catch (NumberFormatException e) {
			return l;
		}
	}

	public static float parseFloat(String s) {
		return parseFloat(s, 0);
	}

	public static float parseFloat(String s, float f) {
		if (s == null) {
			return f;
		}
		try {
			return Float.parseFloat(s.trim());
		} catch (NumberFormatException e) {
			return f;
		}
	}

	public static double parseDouble(String s) {
		return parseDouble(s, 0);
	}

	public static double parseDouble(String s, double d) {
		if (s == null) {
			return d;
		}
		try {
			return Double.parseDouble(s.trim());
		} catch (NumberFormatException e) {
			return d;
		}
	}

	public static String toCurrency(int n) {
		return String.format("%.2f", Double.valueOf((double) n / 100));
	}
}