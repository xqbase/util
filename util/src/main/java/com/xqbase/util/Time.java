package com.xqbase.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/** Convert date or time string to unix time (in milliseconds), and vice versa. */
public class Time {
	/** Milliseconds in a <b>Second</b> */
	public static final long SECOND = 1000;
	/** Milliseconds in a <b>Minute</b> */
	public static final long MINUTE = SECOND * 60;
	/** Milliseconds in an <b>Hour</b> */
	public static final long HOUR = MINUTE * 60;
	/** Milliseconds in a <b>Day</b> */
	public static final long DAY = HOUR * 24;
	/** Milliseconds in a <b>Week</b> */
	public static final long WEEK = DAY * 7;

	private static long timeZoneOffset = TimeZone.getDefault().getRawOffset();

	private static long parseTime(String time) {
		if (time == null) {
			return -timeZoneOffset;
		}
		String[] hms = time.split(":");
		if (hms.length < 2) {
			return -timeZoneOffset;
		}
		return Numbers.parseInt(hms[0]) * HOUR + Numbers.parseInt(hms[1]) * MINUTE +
				(hms.length == 2 ? 0 : Numbers.parseInt(hms[2])) * SECOND - timeZoneOffset;
	}

	private static long parseDate(String date) {
		if (date == null) {
			return -timeZoneOffset;
		}
		String[] ymd = date.split("-");
		if (ymd.length < 3) {
			return -timeZoneOffset;
		}
		GregorianCalendar cal = new GregorianCalendar(Numbers.parseInt(ymd[0]),
				Numbers.parseInt(ymd[1]) - 1, Numbers.parseInt(ymd[2]));
		return cal.getTimeInMillis();
	}

	/**
	 * Convert a date and a time string to a Unix time (in milliseconds).
	 * Either date or time can be null.
	 */
	public static long parse(String date, String time) {
		return parseDate(date) + parseTime(time) + timeZoneOffset;
	}

	private static String toDateString(GregorianCalendar cal) {
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH) + 1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return String.format("%04d-%02d-%02d", Integer.valueOf(year),
				Integer.valueOf(month), Integer.valueOf(day));
	}

	/**
	 * Convert a Unix time (in milliseconds) to a date string
	 */
	public static String toDateString(long time) {
		GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
		cal.setTimeInMillis(time);
		return toDateString(cal);
	}

	private static String toTimeString(GregorianCalendar cal, boolean millis) {
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int minute = cal.get(Calendar.MINUTE);
		int second = cal.get(Calendar.SECOND);
		String s = String.format("%02d:%02d:%02d", Integer.valueOf(hour),
				Integer.valueOf(minute), Integer.valueOf(second));
		if (millis) {
			s += String.format(".%03d",
					Integer.valueOf(cal.get(Calendar.MILLISECOND)));
		}
		return s;
	}

	/**
	 * Convert a Unix time (in milliseconds) to a time string,
	 * rounding to the second
	 */
	public static String toTimeString(long time) {
		return toTimeString(time, false);
	}

	/**
	 * Convert a Unix time (in milliseconds) to a time string
	 *
	 * @param millis <code>true</code> to show milliseconds in decimal and
	 *				 <code>false</code> to round to the second
	 */
	public static String toTimeString(long time, boolean millis) {
		GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
		cal.setTimeInMillis(time);
		return toTimeString(cal, millis);
	}

	/**
	 * Convert a Unix time (in milliseconds) to a date time string,
	 * rounding to the second
	 */
	public static String toString(long time) {
		return toString(time, false);
	}

	/**
	 * Convert a Unix time (in milliseconds) to a date time string
	 *
	 * @param millis <code>true</code> to show milliseconds in decimal and
	 *				 <code>false</code> to round to the second
	 */
	public static String toString(long time, boolean millis) {
		GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
		cal.setTimeInMillis(time);
		return toDateString(cal) + " " + toTimeString(cal, millis);
	}

	/**
	 * Get next time (Unix time in milliseconds) in a day<p>
	 * E.g. xqbase.com will backup data every 4:00am, so the next backup will happen at:<p>
	 * <code>nextMidnightPlus(now, 4 * HOUR)</code>
	 *
	 * @param now current time (Unix time in milliseconds)
	 * @param plus milliseconds after midnight (12:00am)
	 */
	public static long nextMidnightPlus(long now, long plus) {
		long next = (now + timeZoneOffset) / DAY * DAY - timeZoneOffset;
		next += plus;
		if (next < now) {
			next += DAY;
		}
		return next;
	}

	/**
	 * Get Unix time (in milliseconds) of last midnight
	 *
	 * @param now current time (Unix time in milliseconds)
	 */
	public static long lastMidnight(long now) {
		return nextMidnightPlus(now, 0) - DAY;
	}

	/**
	 * Get next time (Unix time in milliseconds) in a week<p>
	 * E.g. xqbase.com will restart website every Monday's 4:00am, so the next restart will happen at:<p>
	 * <code>nextThursdayMidnightPlus(now, 4 * DAY + 4 * HOUR)</code> (Monday is the 4th day after Thursday)
	 *
	 * @param now current time (Unix time in milliseconds)
	 * @param plus milliseconds after Thursday midnight
	 */
	public static long nextThursdayMidnightPlus(long now, long plus) {
		long next = (now + timeZoneOffset) / WEEK * WEEK - timeZoneOffset;
		next += plus;
		if (next < now) {
			next += WEEK;
		}
		return next;
	}

	/**
	 * Sleep for some milliseconds like {@link Thread#sleep(long)} but ignore interruption<p>
	 * The <i>interrupted status</i> will not be cleared if current thread is interrupted during sleep
	 */
	public static void sleep(int millis) {
		long end = System.currentTimeMillis() + millis;
		boolean interrupted = Thread.interrupted();
		int remain = millis;
		while (remain > 0) {
			try {
				Thread.sleep(remain);
				break;
			} catch (InterruptedException e) {
				interrupted = true;
				remain = (int) (end - System.currentTimeMillis());
			}
		}
		if (interrupted) {
			Thread.currentThread().interrupt();
		}
	}
}