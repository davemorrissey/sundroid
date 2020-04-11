package uk.co.sundroid.util.astro

import java.util.Calendar

open class BodyDayEvent(val event: BodyDayEventType, val time: Calendar, val azimuth: Double? = null): Comparable<BodyDayEvent> {

    override fun compareTo(other: BodyDayEvent): Int {
        return time.compareTo(other.time)
    }

}
