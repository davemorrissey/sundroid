package uk.co.sundroid.domain

import java.io.Serializable
import java.util.TimeZone

class TimeZoneDetail(val id: String, val cities: String, val zone: TimeZone) : Comparable<TimeZoneDetail>, Serializable {

    private val currentOffset: Long = zone.getOffset(System.currentTimeMillis()).toLong()

    fun getOffset(time: Long): String {

        if (zone.id == "UTC") {
            return "UTC"
        }

        var offset = Math.abs(zone.getOffset(time))
        if (offset == 0) {
            return "GMT"
        }

        val offsetHours = offset / (1000 * 60 * 60)
        offset -= offsetHours * (1000 * 60 * 60)
        val offsetMinutes = offset / (1000 * 60)

        var offsetHoursStr = "00$offsetHours"
        offsetHoursStr = offsetHoursStr.substring(offsetHoursStr.length - 2)

        var offsetMinutesStr = "00$offsetMinutes"
        offsetMinutesStr = offsetMinutesStr.substring(offsetMinutesStr.length - 2)

        val sign = if (zone.getOffset(time) < 0) "-" else "+"
        return "GMT$sign$offsetHoursStr:$offsetMinutesStr"
    }

    override fun toString(): String {
        return "id=$id, cities=$cities"
    }

    override fun compareTo(other: TimeZoneDetail): Int {
        if (this.id == "UTC") {
            return -1
        } else if (other.id == "UTC") {
            return 1
        }
        return if (this.currentOffset < other.currentOffset) -1 else 1
    }

    companion object {
        private const val serialVersionUID = 8968408131775567729L
    }

}
