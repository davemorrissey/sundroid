package uk.co.sundroid.activity.data.fragments

import java.util.Calendar

class SummaryEvent(val name: String, val time: Calendar, val azimuth: Double?) : Comparable<SummaryEvent> {

    override fun compareTo(other: SummaryEvent): Int {
        val result = time.compareTo(other.time)
        return if (result == 0) {
            1
        } else result
    }

}
