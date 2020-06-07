package uk.co.sundroid.util.astro.smc

import uk.co.sundroid.util.astro.BodyDayEvent
import uk.co.sundroid.util.astro.smc.SmcDateUtils.calendarToJulianDay
import uk.co.sundroid.util.astro.smc.SmcDateUtils.calendarToTTMinusUT
import java.util.*

/**
 * Input values.
 */
class SmcParams(dateTime: Calendar, val obsLat: Double, val obsLon: Double, val event: BodyDayEvent.Event) {

    private val ttMinusUt: Double = calendarToTTMinusUT(dateTime)
    private val jd: Double = calendarToJulianDay(dateTime)

    fun jd(): Double {
        return jd
    }

    fun t(): Double {
        return t(jd)
    }

    fun t(jd: Double): Double {
        return (jd + ttMinusUt / SECONDS_PER_DAY - J2000) / JULIAN_DAYS_PER_CENTURY
    }

    fun time(): DoubleArray {
        return time(jd)
    }

    fun time(jd: Double): DoubleArray {
        return doubleArrayOf(jd, t(jd))
    }

    companion object {
        /** Julian century conversion constant = 100 * days per year.  */
        private const val JULIAN_DAYS_PER_CENTURY = 36525.0

        /** Seconds in one day.  */
        private const val SECONDS_PER_DAY = 86400.0

        /** Our default epoch. The Julian Day which represents noon on 2000-01-01.  */
        private const val J2000 = 2451545.0
    }
}