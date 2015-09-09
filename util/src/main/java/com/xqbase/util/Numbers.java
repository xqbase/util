package com.xqbase.util;

/**
 * Help to parse numbers (int, long, float and double)
 * and prevent unchecked {@link NumberFormatException}
 */
public class Numbers {
	/**
	 * @return an <b>int</b> between <b>min</b> and <b>max</b> (inclusive)
	 */
	public static int minMax(int min, int mid, int max) {
		return mid < min ? min : mid > max ? max : mid;
	}

	/**
	 * Parse an <b>int</b> with default value 0
	 *
	 * @return default value if null or not parsable
	 */
	public static int parseInt(String s) {
		return parseInt(s, 0);
	}

	/**
	 * Parse an <b>int</b> with a given default value <b>i</b>
	 *
	 * @return default value if null or not parsable
	 */
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

	/**
	 * Parse an <b>int</b> with a given range
	 * between <b>min</b> and <b>max</b> (inclusive)
	 *
	 * @return default value if null or not parsable
	 */
	public static int parseInt(String s, int min, int max) {
		return parseInt(s, 0, min, max);
	}

	/**
	 * Parse an <b>int</b> with a given default value <b>i</b> and
	 * a given range between <b>min</b> and <b>max</b> (inclusive)
	 *
	 * @return default value if null or not parsable
	 */
	public static int parseInt(String s, int i, int min, int max) {
		return minMax(min, parseInt(s, i), max);
	}

	/**
	 * Parse a <b>long</b> with default value 0
	 *
	 * @return default value if null or not parsable
	 */
	public static long parseLong(String s) {
		return parseLong(s, 0);
	}

	/**
	 * Parse a <b>long</b> with a given default value <b>l</b>
	 *
	 * @return default value if null or not parsable
	 */
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

	/**
	 * Parse a <b>float</b> with default value 0
	 *
	 * @return default value if null or not parsable
	 */
	public static float parseFloat(String s) {
		return parseFloat(s, 0);
	}

	/**
	 * Parse a <b>float</b> with a given default value <b>f</b>
	 *
	 * @return default value if null or not parsable
	 */
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

	/**
	 * Parse a <b>double</b> with default value 0
	 *
	 * @return default value if null or not parsable
	 */
	public static double parseDouble(String s) {
		return parseDouble(s, 0);
	}

	/**
	 * Parse a <b>double</b> with a given default value <b>d</b>
	 *
	 * @return default value if null or not parsable
	 */
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
}