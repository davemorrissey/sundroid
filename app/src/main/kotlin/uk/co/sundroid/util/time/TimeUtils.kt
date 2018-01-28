@file:JvmName("TimeUtils")
package uk.co.sundroid.util.time

import android.content.Context
import uk.co.sundroid.util.SharedPrefsHelper
import uk.co.sundroid.util.zeroPad
import java.util.Calendar
import java.util.Calendar.*

val shortMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

fun shortDateAndMonth(calendar: Calendar): String {
    val dom = calendar.get(Calendar.DAY_OF_MONTH)
    var date = dom.toString()
    date += when {
        arrayOf(1, 21, 31).contains(dom) -> "st"
        arrayOf(2, 22).contains(dom) -> "nd"
        arrayOf(3, 2).contains(dom) -> "rd"
        else -> "th"
    }
    return "$date ${getShortMonth(calendar)}"
}

fun getShortMonth(calendar: Calendar): String {
    return shortMonths[calendar.get(Calendar.MONTH)]
}

fun clone(calendar: Calendar): Calendar {
    val clone = Calendar.getInstance(calendar.timeZone)
    clone.timeInMillis = calendar.timeInMillis
    return clone
}

fun isSameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(YEAR) == calendar2.get(YEAR)
            && calendar1.get(MONTH) == calendar2.get(MONTH)
            && calendar1.get(DAY_OF_MONTH) == calendar2.get(DAY_OF_MONTH)
}

fun formatTime(context: Context, calendar: Calendar, allowSeconds: Boolean, allowRounding: Boolean): Time {
    val clone = Calendar.getInstance(calendar.timeZone)
    clone.timeInMillis = calendar.timeInMillis

    val showSeconds = SharedPrefsHelper.getShowSeconds(context) && allowSeconds
    val is24 = SharedPrefsHelper.getClockType24(context)

    // If more than half way through a minute, roll forward into next minute.
    if (allowRounding && !showSeconds && clone.get(Calendar.SECOND) >= 30) {
        clone.add(SECOND, 30)
    }

    var time = ""
    if (!is24) {
        val hour = clone.get(HOUR)
        time += if (hour == 0) "12" else hour
    } else {
        time += zeroPad(clone.get(HOUR_OF_DAY), 2)
    }
    time += ":" + zeroPad(clone.get(MINUTE), 2)
    if (showSeconds) {
        time += ":" + zeroPad(clone.get(SECOND), 2)
    }
    val marker = if (clone.get(AM_PM) == AM) "am" else "pm"

    return Time(time, if (is24) "" else marker)
}

fun formatTime(context: Context, calendar: Calendar, allowSeconds: Boolean = false): Time {
    return formatTime(context, calendar, allowSeconds, true)
}

fun formatDuration(context: Context, durationHours: Double, allowSeconds: Boolean = false): String {
    return Clock(durationHours, SharedPrefsHelper.getShowSeconds(context) && allowSeconds).toClock()
}

fun formatDurationHMS(context: Context, durationHours: Double, allowSeconds: Boolean = false): String {
    return Clock(durationHours, SharedPrefsHelper.getShowSeconds(context) && allowSeconds).toHMS()
}

fun formatDiff(context: Context, diffHours: Double, allowSeconds: Boolean = false): String {
    var diff = diffHours
    val sign = if (diff == 0.0) "\u00b1" else if (diff < 0) "-" else "+"
    diff = Math.abs(diff)

    val clock = Clock(diff, SharedPrefsHelper.getShowSeconds(context) && allowSeconds)
    return sign + clock.toClock()
}

fun formatDiff(context: Context, current: Calendar, previous: Calendar, allowSeconds: Boolean = false): String {
    var diffMs = current.timeInMillis - previous.timeInMillis
    diffMs -= 24 * 60 * 60 * 1000
    return formatDiff(context, diffMs/(1000.0*60.0*60.0), allowSeconds)
}
