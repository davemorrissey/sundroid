package uk.co.sundroid.util.astro

import java.util.Calendar
import kotlin.math.abs

open class BodyDayEvent(val event: Event, val direction: Direction, val time: Calendar, val azimuth: Double? = null, val elevation: Double? = null): Comparable<BodyDayEvent> {

    override fun compareTo(other: BodyDayEvent): Int {
        if (equals(other)) {
            return 0
        }
        return time.compareTo(other.time)
    }

    override fun equals(other: Any?): Boolean {
        return if (other !is BodyDayEvent) {
            false
        } else {
            other.event == event && other.direction == direction && abs(other.time.timeInMillis - time.timeInMillis) < 60000
        }
    }

    override fun hashCode(): Int {
        var result = event.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + time.hashCode()
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
