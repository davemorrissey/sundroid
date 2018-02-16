package uk.co.sundroid.util.astro.math

import java.util.ArrayList
import java.util.Calendar
import java.util.Collections
import java.util.TimeZone

import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.astro.MoonPhaseEvent

object MoonPhaseCalculator {

    private var lastCalculatedYear = 0
    private var lastCalculatedZone: TimeZone? = null
    private var lastCalculatedEvents: List<MoonPhaseEvent>? = null

    private fun jyear(j: Double): IntArray {
        var td = j
        val z: Double
        val f: Double
        val a: Double
        val alpha: Double
        val b: Double
        val c: Double
        val d: Double
        val e: Double
        val mm: Double
        td += 0.5
        z = Math.floor(td)
        f = td - z
        if (z < 2299161.0) {
            a = z
        } else {
            alpha = Math.floor((z - 1867216.25) / 36524.25)
            a = z + 1.0 + alpha - Math.floor(alpha / 4)
        }
        b = a + 1524
        c = Math.floor((b - 122.1) / 365.25)
        d = Math.floor(365.25 * c)
        e = Math.floor((b - d) / 30.6001)
        mm = Math.floor(if (e < 14) e - 1 else e - 13)
        return intArrayOf(Math.floor(if (mm > 2) c - 4716 else c - 4715).toInt(), mm.toInt(), Math.floor(b - d - Math.floor(30.6001 * e) + f).toInt())
    }

    private fun jhms(j: Double): IntArray {
        val j2 = j + 0.5
        val ij = ((j2 + 0.5) - Math.floor(j2)) * 86400.0
        return intArrayOf(Math.floor(ij / 3600).toInt(), Math.floor(ij / 60 % 60).toInt(), Math.floor(ij % 60).toInt())
    }

    private fun dtr(d: Double): Double {
        return d * Math.PI / 180.0
    }

    private fun dsin(x: Double): Double {
        return Math.sin(dtr(x))
    }

    private fun dcos(x: Double): Double {
        return Math.cos(dtr(x))
    }

    private fun truephase(y: Double, phase: Double): Double {
        var k = y
        val t: Double
        val t2: Double
        val t3: Double
        var pt: Double
        val m: Double
        val mprime: Double
        val f: Double
        val synMonth = 29.53058868
        k += phase
        t = k / 1236.85
        t2 = t * t
        t3 = t2 * t
        pt = 2415020.75933 + synMonth * k + 0.0001178 * t2 - 0.000000155 * t3 + 0.00033 * dsin(166.56 + 132.87 * t - 0.009173 * t2)
        m = 359.2242 + 29.10535608 * k - 0.0000333 * t2 - 0.00000347 * t3
        mprime = 306.0253 + 385.81691806 * k + 0.0107306 * t2 + 0.00001236 * t3
        f = 21.2964 + 390.67050646 * k - 0.0016528 * t2 - 0.00000239 * t3
        if (phase < 0.01 || Math.abs(phase - 0.5) < 0.01) {
            pt += ((((0.1734 - 0.000393 * t) * dsin(m) + 0.0021 * dsin(2 * m) - 0.4068 * dsin(mprime) + 0.0161 * dsin(2 * mprime) - 0.0004 * dsin(3 * mprime) + 0.0104 * dsin(2 * f)
                    - 0.0051 * dsin(m + mprime)
                    - 0.0074 * dsin(m - mprime)) + 0.0004 * dsin(2 * f + m)
                    - 0.0004 * dsin(2 * f - m)
                    - 0.0006 * dsin(2 * f + mprime))
                    + 0.0010 * dsin(2 * f - mprime)
                    + 0.0005 * dsin(m + 2 * mprime))
        } else if (Math.abs(phase - 0.25) < 0.01 || Math.abs(phase - 0.75) < 0.01) {
            pt += ((((0.1721 - 0.0004 * t) * dsin(m) + 0.0021 * dsin(2 * m) - 0.6280 * dsin(mprime) + 0.0089 * dsin(2 * mprime) - 0.0004 * dsin(3 * mprime) + 0.0079 * dsin(2 * f)
                    - 0.0119 * dsin(m + mprime)
                    - 0.0047 * dsin(m - mprime)) + 0.0003 * dsin(2 * f + m)
                    - 0.0004 * dsin(2 * f - m)
                    - 0.0006 * dsin(2 * f + mprime))
                    + 0.0021 * dsin(2 * f - mprime)
                    + 0.0003 * dsin(m + 2 * mprime)
                    + 0.0004 * dsin(m - 2 * mprime)) - 0.0003 * dsin(2 * m + mprime)
            pt += if (phase < 0.5)
                0.0028 - 0.0004 * dcos(m) + 0.0003 * dcos(mprime)
            else
                -0.0028 + 0.0004 * dcos(m) - 0.0003 * dcos(mprime)
        }
        return pt
    }

    private fun calendar(j: Double, zone: TimeZone): Calendar {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getTimeZone("UTC")

        val date = jyear(j)
        val time = jhms(j)

        calendar.set(Calendar.YEAR, date[0])
        calendar.set(Calendar.MONTH, date[1] - 1)
        calendar.set(Calendar.DAY_OF_MONTH, date[2])
        calendar.set(Calendar.HOUR_OF_DAY, time[0])
        calendar.set(Calendar.MINUTE, time[1])
        calendar.set(Calendar.SECOND, time[2])
        calendar.set(Calendar.MILLISECOND, 0)

        val localCal = Calendar.getInstance(zone)
        localCal.timeInMillis = calendar.timeInMillis
        return localCal
    }

    /**
     * Get all events occurring in a given year, with zone adjustment applied.
     * @param year The year.
     * @param zone The local time zone.
     * @return A list of phase events occurring during the year.
     */
    fun getYearEvents(year: Int, zone: TimeZone): List<MoonPhaseEvent> {

        val lastZone = lastCalculatedZone
        val lastEvents = lastCalculatedEvents
        if (lastCalculatedYear == year &&
                lastZone != null && lastZone.id == zone.id &&
                lastEvents != null) {
            return Collections.unmodifiableList(lastEvents)
        } else {
            val events = ArrayList<MoonPhaseEvent>()
            var k1 = Math.floor((year - 1900) * 12.3685) - 4

            while (true) {

                val newTime = truephase(k1, 0.0)
                val newCal = calendar(newTime, zone)
                if (newCal.get(Calendar.YEAR) == year) {
                    events.add(MoonPhaseEvent(MoonPhase.NEW, newCal))
                }

                val fqTime = truephase(k1, 0.25)
                val fqCal = calendar(fqTime, zone)
                if (fqCal.get(Calendar.YEAR) == year) {
                    events.add(MoonPhaseEvent(MoonPhase.FIRST_QUARTER, fqCal))
                }

                val fullTime = truephase(k1, 0.5)
                val fullCal = calendar(fullTime, zone)
                if (fullCal.get(Calendar.YEAR) == year) {
                    events.add(MoonPhaseEvent(MoonPhase.FULL, fullCal))
                }

                val lqTime = truephase(k1, 0.75)
                val lqCal = calendar(lqTime, zone)
                if (lqCal.get(Calendar.YEAR) == year) {
                    events.add(MoonPhaseEvent(MoonPhase.LAST_QUARTER, lqCal))
                }

                if (newCal.get(Calendar.YEAR) > year && fqCal.get(Calendar.YEAR) > year && fullCal.get(Calendar.YEAR) > year && lqCal.get(Calendar.YEAR) > year) {
                    break
                }

                k1++
            }

            lastCalculatedYear = year
            lastCalculatedZone = zone
            lastCalculatedEvents = events
            return Collections.unmodifiableList(events)
        }
    }

    /**
     * Check for event occurring on a particular day using pre-calculated list to save repetition
     * while rendering calendars.
     * @param dateMidnight Date.
     * @param events Pre-calculated events for the year.
     * @return An event, if any occurs.
     */
    fun getDayEvent(dateMidnight: Calendar, events: List<MoonPhaseEvent>): MoonPhaseEvent? {
        return events.firstOrNull {
                it.time.get(Calendar.YEAR) == dateMidnight.get(Calendar.YEAR) &&
                it.time.get(Calendar.MONTH) == dateMidnight.get(Calendar.MONTH) &&
                it.time.get(Calendar.DAY_OF_MONTH) == dateMidnight.get(Calendar.DAY_OF_MONTH)
        }
    }

    /**
     * Check for an event occurring on a given day. Able to use last calculated year events result to save repeated calculations.
     * @param dateMidnight The day calendar.
     * @return An event, if any occurs.
     */
    @Synchronized
    fun getDayEvent(dateMidnight: Calendar): MoonPhaseEvent? {
        val events: List<MoonPhaseEvent>
        val lastZone = lastCalculatedZone
        val lastEvents = lastCalculatedEvents
        if (lastCalculatedYear == dateMidnight.get(Calendar.YEAR) &&
                lastZone != null && lastZone.id == dateMidnight.timeZone.id &&
                lastEvents != null) {
            events = lastEvents
        } else {
            events = getYearEvents(dateMidnight.get(Calendar.YEAR), dateMidnight.timeZone)
            lastCalculatedYear = dateMidnight.get(Calendar.YEAR)
            lastCalculatedZone = dateMidnight.timeZone
            this.lastCalculatedEvents = events
        }
        return getDayEvent(dateMidnight, events)
    }

    /**
     * Calculate intermediate phase based on events occurring either side. This should
     * allow calendars to show phases that exactly match the event icons.
     */
    @Synchronized
    fun getNoonPhase(dateMidnight: Calendar): Double {

        val events = getYearEvents(dateMidnight.get(Calendar.YEAR), dateMidnight.timeZone)

        var before: MoonPhaseEvent? = null
        var after: MoonPhaseEvent? = null

        val dateNoon = Calendar.getInstance(dateMidnight.timeZone)
        dateNoon.timeInMillis = dateMidnight.timeInMillis
        dateNoon.set(Calendar.HOUR_OF_DAY, 12)
        dateNoon.set(Calendar.MINUTE, 0)
        dateNoon.set(Calendar.SECOND, 0)

        // Get the last event before and first event after the specified date, or return
        // the event if one happens on the day.
        for (event in events) {
            if (event.time.timeInMillis < dateNoon.timeInMillis) {
                before = event
            } else if (event.time.timeInMillis > dateNoon.timeInMillis && after == null) {
                after = event
            }
        }

        // Length of a default phase in milliseconds.
        val defaultPhaseMs: Long = 637860715

        val msNoon = dateNoon.timeInMillis.toFloat()
        var msBefore = 0f
        var msAfter = 0f
        var phaseBefore = 0.0
        val phaseAfter: Double

        // At start or end of year, before or after can be null, but never both.
        if (before == null && after != null) {
            msAfter = after.time.timeInMillis.toFloat()
            phaseAfter = after.phaseDouble
            msBefore = msAfter - defaultPhaseMs
            phaseBefore = if (phaseAfter == 0.0) 0.75 else phaseAfter - 0.25
        } else if (after == null && before != null) {
            msBefore = before.time.timeInMillis.toFloat()
            phaseBefore = before.phaseDouble
            msAfter = msBefore + defaultPhaseMs
        } else if (after != null && before != null) {
            msBefore = before.time.timeInMillis.toFloat()
            phaseBefore = before.phaseDouble
            msAfter = after.time.timeInMillis.toFloat()
        }

        return phaseBefore + (msNoon - msBefore) / (msAfter - msBefore) * 0.25

    }

    fun getIlluminatedPercent(phase: Double): Int {
        val angleD = phase * 360
        val angleR = Math.toRadians(angleD)
        val cos = (Math.cos(angleR) + 1) / 2.0
        val percent = (1 - cos) * 100
        return Math.round(percent).toInt()
    }

}
