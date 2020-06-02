package uk.co.sundroid.util.astro

import java.util.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.*


open class BodyDay {

    @Deprecated("Prefer events")
    var rise: Calendar? = null

    @Deprecated("Prefer events")
    var set: Calendar? = null

    var events: MutableSet<BodyDayEvent> = TreeSet()

    @Deprecated("Prefer events")
    var transit: Calendar? = null

    @Deprecated("Prefer events")
    var riseAzimuth: Double = 0.0

    @Deprecated("Prefer events")
    var setAzimuth: Double = 0.0

    @Deprecated("Prefer events")
    var transitAppElevation: Double = 0.0

    var riseSetType: RiseSetType? = null

    var uptimeHours: Double = 0.0

    fun addEvent(event: BodyDayEvent) {
        events.add(event)
        when (event.direction) {
            RISING -> eventUp[event.event] = event
            DESCENDING -> eventDown[event.event] = event
        }
    }

    val eventUp: MutableMap<BodyDayEvent.Event, BodyDayEvent> = EnumMap(BodyDayEvent.Event::class.java)
    val eventDown: MutableMap<BodyDayEvent.Event, BodyDayEvent> = EnumMap(BodyDayEvent.Event::class.java)
    val eventType: MutableMap<BodyDayEvent.Event, RiseSetType> = EnumMap(BodyDayEvent.Event::class.java)

}
