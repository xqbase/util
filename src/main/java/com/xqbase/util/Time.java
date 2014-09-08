package com.xqbase.util;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

public class Time {
	public static final long SECOND = 1000;
	public static final long MINUTE = SECOND * 60;
	public static final long HOUR = MINUTE * 60;
	public static final long DAY = HOUR * 24;
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

	public static String toTimeString(long time) {
		return toTimeString(time, false);
	}

	public static String toTimeString(long time, boolean millis) {
		GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
		cal.setTimeInMillis(time);
		return toTimeString(cal, millis);
	}

	public static String toString(long time) {
		return toString(time, false);
	}

	public static String toString(long time, boolean millis) {
		GregorianCalendar cal = new GregorianCalendar(0, 0, 0);
		cal.setTimeInMillis(time);
		return toDateString(cal) + " " + toTimeString(cal, millis);
	}

	public static long nextMidnightPlus(long now, long plus) {
		long next = (now + timeZoneOffset) / DAY * DAY - timeZoneOffset;
		next += plus;
		if (next < now) {
			next += DAY;
		}
		return next;
	}

	public static long lastMidnight(long now) {
		return nextMidnightPlus(now, 0) - DAY;
	}

	public static long nextThursdayMidnightPlus(long now, long plus) {
		long next = (now + timeZoneOffset) / WEEK * WEEK - timeZoneOffset;
		next += plus;
		if (next < now) {
			next += WEEK;
		}
		return next;
	}

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