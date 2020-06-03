package uk.co.sundroid.util.astro

import java.util.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.*
import uk.co.sundroid.util.astro.BodyDayEvent.Event.*


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
            RISING -> { if (eventUp[event.event] == null) eventUp[event.event] = event }
            DESCENDING -> { if (eventDown[event.event] == null) eventDown[event.event] = event }
        }
    }

    fun length(): Double {
        val rise = eventUp[RISESET]
        val set = eventDown[RISESET]
        if (rise != null && set != null) {
            return (set.time.timeInMillis - rise.time.timeInMillis) / (1000.0 * 60.0 * 60.0)
        } else if (rise == null && set == null && transitAppElevation < 0) {
            return 0.0
        }
        return 24.0
    }

    fun type(event: BodyDayEvent.Event): RiseSetType? {
        if (eventUp[event] == null && eventDown[event] == null) {
            return if (transitAppElevation > 0) {
                RiseSetType.RISEN
            } else {
                RiseSetType.SET
            }
        }
        return null
    }

    val eventUp: MutableMap<BodyDayEvent.Event, BodyDayEvent> = EnumMap(BodyDayEvent.Event::class.java)
    val eventDown: MutableMap<BodyDayEvent.Event, BodyDayEvent> = EnumMap(BodyDayEvent.Event::class.java)
    val eventType: MutableMap<BodyDayEvent.Event, RiseSetType> = EnumMap(BodyDayEvent.Event::class.java)

}
