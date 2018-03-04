package uk.co.sundroid.activity.data.fragments

import java.util.Calendar

class RiseSetEvent(val name: String, val time: Calendar, val azimuth: Double) : Comparable<RiseSetEvent> {

    override fun compareTo(other: RiseSetEvent): Int {
        val result = time.compareTo(other.time)
        return if (result == 0) 1 else result
    }

}
