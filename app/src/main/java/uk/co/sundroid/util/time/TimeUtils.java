package uk.co.sundroid.util.time;

import java.util.Calendar;

public class TimeUtils {

    public static String shortDateAndMonth(Calendar calendar) {
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        String date = Integer.toString(dayOfMonth);
        if (dayOfMonth == 1 || dayOfMonth == 21 || dayOfMonth == 31) {
            date += "st";
        } else if (dayOfMonth == 2 || dayOfMonth == 22) {
            date += "nd";
        } else if (dayOfMonth == 3 || dayOfMonth == 23) {
            date += "rd";
        } else {
            date += "th";
        }
        return date + " " + getShortMonth(calendar);
    }

    private static String getShortMonth(Calendar calendar) {
        switch (calendar.get(Calendar.MONTH)) {
            case 0: return "Jan";
            case 1: return "Feb";
            case 2: return "Mar";
            case 3: return "Apr";
            case 4: return "May";
            case 5: return "Jun";
            case 6: return "Jul";
            case 7: return "Aug";
            case 8: return "Sep";
            case 9: return "Oct";
            case 10: return "Nov";
            case 11: return "Dec";
        }
        return "";

    }

}
