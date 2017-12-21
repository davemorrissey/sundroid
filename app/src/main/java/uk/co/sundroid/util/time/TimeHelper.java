package uk.co.sundroid.util.time;

import java.util.Calendar;

import android.content.Context;

import uk.co.sundroid.util.SharedPrefsHelper;

/**
 * Utilities for formatting time according to the user's preferences - 12/24 hour clock and precision.
 */
public class TimeHelper {
	
	public static class Time {

		public String time;
		public String marker;

		public Time(String time, String marker) {
			this.time = time;
			this.marker = marker;
		}

		@Override
		public String toString() {
			return time + marker;
		}

	}
	
	public static Time formatTime(Context context, Calendar calendar, boolean allowSeconds, boolean allowRounding) {
		Calendar clone = Calendar.getInstance(calendar.getTimeZone());
		clone.setTimeInMillis(calendar.getTimeInMillis());
		
		boolean showSeconds = SharedPrefsHelper.getShowSeconds(context) && allowSeconds;
		boolean is24 = SharedPrefsHelper.getClockType24(context);
		
		// If more than half way through a minute, roll forward into next minute.
		if (allowRounding && !showSeconds && clone.get(Calendar.SECOND) >= 30) {
			clone.add(Calendar.SECOND, 30);
		}
		
		String time = "";
		if (!is24) {
			int hour = clone.get(Calendar.HOUR);
			time += (hour == 0) ? "12" : hour;
		} else {
			time += zeroPad(clone.get(Calendar.HOUR_OF_DAY));
		}
		time += ":" + zeroPad(clone.get(Calendar.MINUTE));
		if (showSeconds) {
			time += ":" + zeroPad(clone.get(Calendar.SECOND));
		}
		String marker = clone.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
		
		return new Time(time, is24 ? "" : marker);	
		
		
	}
	
	public static Time formatTime(Context context, Calendar calendar, boolean allowSeconds) {
		return formatTime(context, calendar, allowSeconds, true);
	}
	
	private static String zeroPad(int number) {
		String str = Integer.toString(number);
		if (str.length() == 1) {
			return "0" + str;
		}
		return str;
	}
	
	public static String formatDuration(Context context, double durationHours, boolean allowSeconds) {
		if (SharedPrefsHelper.getShowSeconds(context) && allowSeconds) {
			int hours = (int)Math.floor(durationHours);
			int minutes = (int)Math.floor((durationHours - hours) * 60d);
			int seconds = (int)Math.round((durationHours - hours - (minutes/60d)) * 3600d);
			if (seconds >= 60) {
				seconds -= 60;
				minutes++;
			}
			if (minutes >= 60) {
				minutes -=60;
				hours++;
			}
			return Integer.toString(hours) + ":" + zeroPad(minutes) + ":" + zeroPad(seconds);
		} else {
			int hours = (int)Math.floor(durationHours);
			int minutes = (int)Math.round((durationHours - hours) * 60d);
			if (minutes >= 60) {
				minutes -=60;
				hours++;
			}
			return Integer.toString(hours) + ":" + zeroPad(minutes);
		}
	}
	
	public static String formatDurationHMS(Context context, double durationHours, boolean allowSeconds) {
		if (SharedPrefsHelper.getShowSeconds(context) && allowSeconds) {
			int hours = (int)Math.floor(durationHours);
			int minutes = (int)Math.floor((durationHours - hours) * 60d);
			int seconds = (int)Math.round((durationHours - hours - (minutes/60d)) * 3600d);
			if (seconds >= 60) {
				seconds -= 60;
				minutes++;
			}
			if (minutes >= 60) {
				minutes -=60;
				hours++;
			}
			return Integer.toString(hours) + "h " + zeroPad(minutes) + "m " + zeroPad(seconds) + "s";
		} else {
			int hours = (int)Math.floor(durationHours);
			int minutes = (int)Math.round((durationHours - hours) * 60d);
			if (minutes >= 60) {
				minutes -=60;
				hours++;
			}
			return Integer.toString(hours) + "h " + zeroPad(minutes) + "m";
		}
	}

	public static String formatDiff(Context context, double diffHours, boolean allowSeconds) {
		String sign = diffHours == 0d ? "\u00b1" : diffHours < 0 ? "-" : "+";
		diffHours = Math.abs(diffHours);
		if (SharedPrefsHelper.getShowSeconds(context) && allowSeconds) {
			int hours = (int)Math.floor(diffHours);
			int minutes = (int)Math.floor((diffHours - hours) * 60d);
			int seconds = (int)Math.round((diffHours - hours - (minutes/60d)) * 3600d);
			if (seconds >= 60) {
				seconds -= 60;
				minutes++;
			}
			if (minutes >= 60) {
				minutes -=60;
				hours++;
			}
			return sign + Integer.toString(hours) + ":" + zeroPad(minutes) + ":" + zeroPad(seconds);
		} else {
			int hours = (int)Math.floor(diffHours);
			int minutes = (int)Math.round((diffHours - hours) * 60d);
			if (minutes >= 60) {
				minutes -=60;
				hours++;
			}
			return sign + Integer.toString(hours) + ":" + zeroPad(minutes);
		}
	}
	
	public static String formatDiff(Context context, Calendar current, Calendar previous, boolean allowSeconds) {
		double diffMs = current.getTimeInMillis() - previous.getTimeInMillis();
		diffMs -= (24d*60d*60d*1000d);
		return formatDiff(context, diffMs/(1000d*60d*60d), allowSeconds);
	}

}
