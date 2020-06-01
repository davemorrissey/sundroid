package uk.co.sundroid.util.astro

import java.util.Calendar

open class BodyDayEvent(val event: Event, val direction: Direction, val time: Calendar, val azimuth: Double? = null, val elevation: Double? = null): Comparable<BodyDayEvent> {

    override fun compareTo(other: BodyDayEvent): Int {
        val result = time.compareTo(other.time)
        if (result == 0) {
            return 1
        }
        return result
    }

    enum class Direction {
        RISING,
        DESCENDING,
        TRANSIT
    }

    enum class Event {
        RISESET,
        TRANSIT,
        CIVIL,
        NAUTICAL,
        ASTRONOMICAL,
        GOLDENHOUR
    }

}
