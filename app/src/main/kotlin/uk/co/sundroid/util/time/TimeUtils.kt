package uk.co.sundroid.util.time

import java.util.Calendar

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
