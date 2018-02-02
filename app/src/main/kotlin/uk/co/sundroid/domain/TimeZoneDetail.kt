package uk.co.sundroid.domain

import uk.co.sundroid.util.*
import java.io.Serializable
import java.util.TimeZone

class TimeZoneDetail(val zone: TimeZone, val cities: String?) : Comparable<TimeZoneDetail>, Serializable {

    val id: String
        get() = zone.id

    private val currentOffset: Long = zone.getOffset(System.currentTimeMillis()).toLong()

    fun getOffset(time: Long): String {
        if (zone.id == "UTC") {
            return "UTC"
        }

        val offset = zone.getOffset(time)
        if (offset == 0) {
            return "GMT"
        }

        var absOffset = Math.abs(offset)
        val hours = absOffset / (1000 * 60 * 60)
        absOffset -= hours * (1000 * 60 * 60)
        val minutes = absOffset / (1000 * 60)

        val hoursStr = zeroPad(hours.toString(), 2)
        val minutesStr = zeroPad(minutes.toString(), 2)
        val sign = if (offset < 0) "-" else "+"
        return "GMT$sign$hoursStr:$minutesStr"
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
