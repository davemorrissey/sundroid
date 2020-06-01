package uk.co.sundroid.util.log

import android.util.Log
import uk.co.sundroid.BuildConfig
import java.util.*

fun d(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("sundroid.$tag", message)
    }
}
fun i(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.i("sundroid.$tag", message)
    }
}

fun e(tag: String, message: String, tr: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e("sundroid.$tag", message, tr)
    }
}

fun displayCalendar(calendar: Calendar?): String? {
    if (!BuildConfig.DEBUG) {
        return "Disabled"
    }
    if (calendar == null) {
        return "null"
    }
    val zoneDST = calendar.timeZone.inDaylightTime(Date(calendar.timeInMillis + 12 * 60 * 60 * 1000))
    val zoneName = calendar.timeZone.getDisplayName(zoneDST, TimeZone.LONG)
    val zoneCode = calendar.timeZone.getDisplayName(zoneDST, TimeZone.SHORT)
    return calendar[Calendar.YEAR].toString() + "-" + (calendar[Calendar.MONTH] + 1) + "-" + calendar[Calendar.DAY_OF_MONTH] + " " +
            calendar[Calendar.HOUR_OF_DAY] + ":" + calendar[Calendar.MINUTE] + ":" + calendar[Calendar.SECOND] + " " + zoneName + " (" + zoneCode + ")"
}