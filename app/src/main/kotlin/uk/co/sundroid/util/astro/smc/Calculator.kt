package uk.co.sundroid.util.astro.smc

import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.location.LatitudeLongitude
import java.util.*
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.lang.Math.PI
import uk.co.sundroid.util.astro.smc.SunMoonCalculator.AU
import uk.co.sundroid.util.astro.smc.SunMoonCalculator.EARTH_RADIUS
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction
import uk.co.sundroid.util.astro.BodyDayEvent.Event.TRANSIT
import uk.co.sundroid.util.astro.BodyDayEvent.Event.RISESET
import uk.co.sundroid.util.astro.BodyDayEvent.Event.GOLDENHOUR
import uk.co.sundroid.util.astro.BodyDayEvent.Event.CIVIL
import uk.co.sundroid.util.astro.BodyDayEvent.Event.NAUTICAL
import uk.co.sundroid.util.astro.BodyDayEvent.Event.ASTRONOMICAL
import uk.co.sundroid.util.astro.BodyDayEvent.Event
import uk.co.sundroid.util.astro.smc.SmcDateUtils.jdToCalendar

object Calculator {

    /**
     * Returns Sun, Moon or planet position for a given time and location.
     */
    fun getPosition(body: Body, dateTime: Calendar, location: LatitudeLongitude): Position {
        val params = SmcParams(dateTime, toRadians(location.latitude.doubleValue), toRadians(location.longitude.doubleValue), RISESET)
        val position = SunMoonCalculator.getPosition(body, params.time(), params)
        val topo = SunMoonCalculator.calculateBodyEphemeris(body, params.time(), params)
        val geo = SunMoonCalculator.calculateEphemeris(params.time(), params, position, true)
        return Position(dateTime.timeInMillis, toDegrees(topo.azimuth), toDegrees(topo.elevation)).apply {
            julianDay = params.jd()
            topoRA = toDegrees(topo.rightAscension)
            topoDec = toDegrees(topo.declination)
            topoDistKm = toDegrees(topo.distance)
            topoDistEarthRadii = topo.distance * AU / EARTH_RADIUS
            geoRA = toDegrees(geo.rightAscension)
            geoDec = toDegrees(geo.declination)
            geoDistKm = geo.distance * AU
            geoDistEarthRadii = geo.distance * AU / EARTH_RADIUS
            moonAge = topo.moonAge
            moonPhase = topo.moonPhase / (PI * 2)
            moonIllumination = topo.moonIllumination
        }
    }

    /**
     * Returns Sun, Moon or planet ephemeris for a given date and location.
     */
    fun getDay(body: Body, dateMidnight: Calendar, location: LatitudeLongitude): BodyDay {
        return if (body == Body.SUN) {
            getSunDay(dateMidnight, location)
        } else {
            getBodyDay(body, dateMidnight, location)
        }
    }

    /**
     * Returns the sun ephemeris for a given date and location. Rather than simply finding all events
     * within a 24 hour window, this searches forward and backward from the transit occurring on the
     * given day to find surrounding rise/set events, thus returning a "solar day's" events instead
     * of a calendar day.
     */
    private fun getSunDay(dateMidnight: Calendar, location: LatitudeLongitude): SunDay {
        val eventList = object : ArrayList<BodyDayEvent>() {
            fun findEvent(event: Event, direction: Direction): BodyDayEvent? {
                return firstOrNull { it.event == event && it.direction == direction }
            }
        }
        var transit: BodyDayEvent? = null

        // Run calculations for zone noon requested day and zone noon previous day, which seems to
        // reliably give the full day's events for any time zone, plus some for previous/next day,
        // and without any duplicates.
        arrayOf(previousNoon(dateMidnight), noon(dateMidnight)).forEach { date ->
            BodyDayEvent.Event.values().forEach { event ->
                if (event != TRANSIT) {
                    val params = SmcParams(date, toRadians(location.latitude.doubleValue), toRadians(location.longitude.doubleValue), event)
                    val ephemeris = SunMoonCalculator.calculateBodyEphemeris(Body.SUN, params.time(), params)
                    if (event == RISESET) {
                        ephemeris.transit?.let {
                            val thisTransitCalendar = jdToCalendar(it.jd, date.timeZone)
                            val thisTransit = BodyDayEvent(TRANSIT, Direction.TRANSIT, thisTransitCalendar, azimuth = toDegrees(it.azimuth), elevation = toDegrees(it.elevation))
                            if (thisTransitCalendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
                                transit = thisTransit
                            }
                            eventList.add(thisTransit)
                        }
                    }
                    ephemeris.rise?.let { eventList.add(BodyDayEvent(event, RISING, jdToCalendar(it.jd, date.timeZone), azimuth = toDegrees(it.azimuth), elevation = toDegrees(it.elevation))) }
                    ephemeris.set?.let { eventList.add(BodyDayEvent(event, DESCENDING, jdToCalendar(it.jd, date.timeZone), azimuth = toDegrees(it.azimuth), elevation = toDegrees(it.elevation))) }
                }
            }
        }
        eventList.sort()

        val transitIndex = eventList.indexOf(transit)

        // Remove the next solar day's events.
        for (i in transitIndex + 1 until eventList.size) {
            if (eventList[i].direction != DESCENDING) {
                eventList.subList(i, eventList.size).clear()
                break
            }
        }
        // Remove the previous solar day's events.
        for (i in transitIndex - 1 downTo 0) {
            if (eventList[i].direction != RISING) {
                eventList.subList(0, i + 1).clear()
                break
            }
        }

        val day = SunDay()
        eventList.forEach { day.addEvent(it) }
        day.rise = eventList.findEvent(RISESET, RISING)?.time
        day.riseAzimuth = (eventList.findEvent(RISESET, RISING)?.azimuth ?: 0.0)
        day.set = eventList.findEvent(RISESET, DESCENDING)?.time
        day.setAzimuth = (eventList.findEvent(RISESET, DESCENDING)?.azimuth ?: 0.0)
        day.astDawn = eventList.findEvent(ASTRONOMICAL, RISING)?.time
        day.astDusk = eventList.findEvent(ASTRONOMICAL, DESCENDING)?.time
        day.ntcDawn = eventList.findEvent(NAUTICAL, RISING)?.time
        day.ntcDusk = eventList.findEvent(NAUTICAL, DESCENDING)?.time
        day.civDawn = eventList.findEvent(CIVIL, RISING)?.time
        day.civDusk = eventList.findEvent(CIVIL, DESCENDING)?.time
        day.ghEnd = eventList.findEvent(GOLDENHOUR, RISING)?.time
        day.ghStart = eventList.findEvent(GOLDENHOUR, DESCENDING)?.time
        day.transit = eventList.findEvent(TRANSIT, Direction.TRANSIT)?.time
        day.transitAppElevation = eventList.findEvent(TRANSIT, Direction.TRANSIT)?.elevation ?: 0.0
        return day
    }

    /**
     * Returns rise and set events for the Moon or a planet for a given date and location. All events
     * that occur on the calendar day are returned, with the first rise and set providing the single
     * rise and set fields still used occasionally.
     */
    private fun getBodyDay(body: Body, dateMidnight: Calendar, location: LatitudeLongitude): BodyDay {
        var eventSet = TreeSet<BodyDayEvent>()
        arrayOf(dateMidnight, noon(dateMidnight), nextMidnight(dateMidnight), nextNoon(dateMidnight)).forEach { date ->
            val params = SmcParams(date, toRadians(location.latitude.doubleValue), toRadians(location.longitude.doubleValue), RISESET)
            val ephemeris = SunMoonCalculator.calculateBodyEphemeris(body, params.time(), params)
            ephemeris.transit?.let { eventSet.add(BodyDayEvent(TRANSIT, Direction.TRANSIT, jdToCalendar(it.jd, date.timeZone), azimuth = toDegrees(it.azimuth), elevation = toDegrees(it.elevation))) }
            ephemeris.rise?.let { eventSet.add(BodyDayEvent(RISESET, RISING, jdToCalendar(it.jd, date.timeZone), azimuth = toDegrees(it.azimuth), elevation = toDegrees(it.elevation))) }
            ephemeris.set?.let { eventSet.add(BodyDayEvent(RISESET, DESCENDING, jdToCalendar(it.jd, date.timeZone), azimuth = toDegrees(it.azimuth), elevation = toDegrees(it.elevation))) }
        }

        // Determine uptime by finding the first rise event on the day and the following set event.
        val firstRise = eventSet.firstOrNull { e -> e.direction == RISING && e.time.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR) }
        val followingSet = firstRise?.let { eventSet.firstOrNull { e -> e.direction == DESCENDING && e.time.timeInMillis > it.time.timeInMillis } }

        eventSet = TreeSet(eventSet.filter { e -> e.time.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR) })

        // TODO replace rise, set transit fields with BodyDayEvent to avoid translation
        val day = BodyDay()
        eventSet.firstOrNull { e -> e.direction == RISING }?.let {
            day.rise = it.time
            day.riseAzimuth = it.azimuth ?: 0.0
        }
        eventSet.firstOrNull { e -> e.direction == DESCENDING }?.let {
            day.set = it.time
            day.setAzimuth = it.azimuth ?: 0.0
        }
        eventSet.firstOrNull { e -> e.direction == Direction.TRANSIT }?.let {
            day.transit = it.time
            day.transitAppElevation = it.elevation ?: 0.0
        }
        eventSet.filter { it.direction != Direction.TRANSIT }.forEach { day.addEvent(it) }

        if (firstRise != null && followingSet != null) {
            day.uptimeHours = (followingSet.time.timeInMillis - firstRise.time.timeInMillis) / (1000.0 * 60.0 * 60.0)
        } else if (day.transitAppElevation > 0.0) {
            day.riseSetType = RiseSetType.RISEN
        } else {
            day.riseSetType = RiseSetType.SET
        }
        return day
    }

    private fun noon(dateMidnight: Calendar): Calendar {
        val noon = Calendar.getInstance(dateMidnight.timeZone)
        noon.timeInMillis = dateMidnight.timeInMillis
        noon.add(Calendar.HOUR_OF_DAY, 12)
        return noon
    }

    private fun previousNoon(dateMidnight: Calendar): Calendar {
        val previousNoon = Calendar.getInstance(dateMidnight.timeZone)
        previousNoon.timeInMillis = dateMidnight.timeInMillis
        previousNoon.add(Calendar.HOUR_OF_DAY, 12)
        previousNoon.add(Calendar.DAY_OF_MONTH, -1)
        return previousNoon
    }

    private fun nextMidnight(dateMidnight: Calendar): Calendar {
        val nextNoon = Calendar.getInstance(dateMidnight.timeZone)
        nextNoon.timeInMillis = dateMidnight.timeInMillis
        nextNoon.add(Calendar.DAY_OF_MONTH, 1)
        return nextNoon
    }

    private fun nextNoon(dateMidnight: Calendar): Calendar {
        val nextNoon = Calendar.getInstance(dateMidnight.timeZone)
        nextNoon.timeInMillis = dateMidnight.timeInMillis
        nextNoon.add(Calendar.HOUR_OF_DAY, 12)
        nextNoon.add(Calendar.DAY_OF_MONTH, 1)
        return nextNoon
    }

}