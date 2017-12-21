package uk.co.sundroid.util.time;

import java.util.Calendar;

/**
 * Utilites for working with {@link Calendar}s.
 */
public class CalendarUtils {
	
	public static Calendar clone(Calendar calendar) {
		Calendar clone = Calendar.getInstance(calendar.getTimeZone());
		clone.setTimeInMillis(calendar.getTimeInMillis());
		return clone;
	}
	
	public static boolean isSameDay(Calendar calendar1, Calendar calendar2) {
		calendar1 = clone(calendar1);
		calendar2 = clone(calendar2);
		return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR)
				&& calendar1.get(Calendar.MONTH) == calendar2.get(Calendar.MONTH)
				&& calendar1.get(Calendar.DAY_OF_MONTH) == calendar2.get(Calendar.DAY_OF_MONTH);
	}

}
