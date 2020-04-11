package uk.co.sundroid.util.time

import android.content.Context
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.zeroPad
import java.util.*
import java.util.Calendar.*
import kotlin.math.abs

val shortMonths = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

fun shortDateAndMonth(calendar: Calendar, html: Boolean = false, upperCase: Boolean = false): String {
    val dom = calendar.get(DAY_OF_MONTH)
    var date = dom.toString()
    var month = getShortMonth(calendar)
    if (upperCase) {
        month = month.toUpperCase(Locale.getDefault())
    }
    date += when {
        arrayOf(1, 21, 31).contains(dom) -> if (html) "<small>ST</small>" else "st"
        arrayOf(2, 22).contains(dom) -> if (html) "<small>ND</small>" else "nd"
        arrayOf(3, 23).contains(dom) -> if (html) "<small>RD</small>" else "rd"
        else -> if (html) "<small>TH</small>" else "th"
    }
    return "$date $month"
}

fun getShortMonth(calendar: Calendar): String {
    return shortMonths[calendar.get(MONTH)]
}

fun clone(calendar: Calendar): Calendar {
    val clone = getInstance(calendar.timeZone)
    clone.timeInMillis = calendar.timeInMillis
    return clone
}

fun isSameDay(calendar1: Calendar, calendar2: Calendar): Boolean {
    return calendar1.get(YEAR) == calendar2.get(YEAR)
            && calendar1.get(MONTH) == calendar2.get(MONTH)
            && calendar1.get(DAY_OF_MONTH) == calendar2.get(DAY_OF_MONTH)
}

fun formatTime(context: Context, calendar: Calendar, allowSeconds: Boolean, allowRounding: Boolean, html: Boolean = false): Time {
    val clone = getInstance(calendar.timeZone)
    clone.timeInMillis = calendar.timeInMillis

    val showSeconds = Prefs.showSeconds(context) && allowSeconds
    val is24 = Prefs.clockType24(context)

    // If more than half way through a minute, roll forward into next minute.
    if (allowRounding && !showSeconds && clone.get(SECOND) >= 30) {
        clone.add(SECOND, 30)
    }

    var time = if (!is24) {
        val hour = clone.get(HOUR)
        if (hour == 0) "12" else hour.toString()
    } else {
        zeroPad(clone.get(HOUR_OF_DAY), 2)
    }
    time += ":" + zeroPad(clone.get(MINUTE), 2)
    if (showSeconds) {
        time += ":" + zeroPad(clone.get(SECOND), 2)
    }
    var marker = if (clone.get(AM_PM) == AM) "am" else "pm"
    if (html) {
        marker = if (clone.get(AM_PM) == AM) "<small>AM</small>" else "<small>PM</small>"
    }

    return Time(time, if (is24) "" else marker)
}

fun formatTimeStr(context: Context, calendar: Calendar, allowSeconds: Boolean = false, allowRounding: Boolean = true, html: Boolean = false): String {
    val time = formatTime(context, calendar, allowSeconds = allowSeconds, allowRounding = allowRounding, html = html)
    return time.toString()
}

fun formatDuration(context: Context, durationHours: Double, allowSeconds: Boolean = false): String {
    return Clock(durationHours, Prefs.showSeconds(context) && allowSeconds).toClock()
}

fun formatDurationHMS(context: Context, durationHours: Double, allowSeconds: Boolean = false, html: Boolean = false): String {
    return Clock(durationHours, Prefs.showSeconds(context) && allowSeconds).toHMS(html = html)
}

fun formatDiff(context: Context, diffHours: Double, allowSeconds: Boolean = false): String {
    val sign = if (diffHours == 0.0) "\u00b1" else if (diffHours < 0) "-" else "+"
    val clock = Clock(abs(diffHours), Prefs.showSeconds(context) && allowSeconds)
    return sign + clock.toClock()
}

fun formatDiff(context: Context, current: Calendar, previous: Calendar, allowSeconds: Boolean = false): String {
    var diffMs = current.timeInMillis - previous.timeInMillis
    diffMs -= 24 * 60 * 60 * 1000
    return formatDiff(context, diffMs/(1000.0*60.0*60.0), allowSeconds)
}
