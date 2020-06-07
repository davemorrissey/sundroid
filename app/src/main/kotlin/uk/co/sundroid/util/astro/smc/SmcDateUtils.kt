package uk.co.sundroid.util.astro.smc

import java.util.*
import kotlin.math.floor
import java.util.Calendar.YEAR
import java.util.Calendar.MONTH
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND

/**
 * Date utils partly derived from SunMoonCalculator by T. Alonso Albi.
 */
object SmcDateUtils {

    /** Seconds in one day.  */
    const val SECONDS_PER_DAY = 86400.0

    /**
     * Converts a calendar in any time zone to a Julian day.
     */
    fun calendarToJulianDay(dateTime: Calendar): Double {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = dateTime.timeInMillis
        val year = utc.get(YEAR)
        val month = utc.get(MONTH) + 1
        val day = utc.get(DAY_OF_MONTH)
        val h = utc.get(HOUR_OF_DAY)
        val m = utc.get(MINUTE)
        val s = utc.get(SECOND)

        // The conversion formulas are from Meeus, chapter 7.
        var julian = false // Use Gregorian calendar
        if (year < 1582 || year == 1582 && month <= 10 || year == 1582 && month == 10 && day < 15) julian = true
        var ma = month
        var ya = year
        if (ma < 3) {
            ya--
            ma += 12
        }
        val aa = ya / 100
        val ba = if (julian) 0 else 2 - aa + aa / 4
        val dayFraction = (h + (m + s / 60.0) / 60.0) / 24.0
        val jd = dayFraction + (365.25 * (ya + 4716)).toInt() + (30.6001 * (ma + 1)).toInt() + day + ba - 1524.5
        if (jd < 2299160.0 && jd >= 2299150.0) throw IllegalArgumentException("invalid julian day $jd. This date does not exist.")
        return jd
    }

    /**
     * Calculate TT minus UT.
     */
    fun calendarToTTMinusUT(dateTime: Calendar): Double {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.timeInMillis = dateTime.timeInMillis
        val year = utc.get(YEAR)
        val month = utc.get(MONTH) + 1
        val day = utc.get(DAY_OF_MONTH)
        return if (year > -600 && year < 2200) {
            val x = year + (month - 1 + day / 30.0) / 12.0
            val x2 = x * x
            val x3 = x2 * x
            val x4 = x3 * x
            if (year < 1600) {
                10535.328003326353 - 9.995238627481024 * x + 0.003067307630020489 * x2 - 7.76340698361363E-6 * x3 + 3.1331045394223196E-9 * x4 + 8.225530854405553E-12 * x2 * x3 - 7.486164715632051E-15 * x4 * x2 + 1.9362461549678834E-18 * x4 * x3 - 8.489224937827653E-23 * x4 * x4
            } else {
                -1027175.3477559977 + 2523.256625418965 * x - 1.885686849058459 * x2 + 5.869246227888417E-5 * x3 + 3.3379295816475025E-7 * x4 + 1.7758961671447929E-10 * x2 * x3 - 2.7889902806153024E-13 * x2 * x4 + 1.0224295822336825E-16 * x3 * x4 - 1.2528102370680435E-20 * x4 * x4
            }
        } else {
            0.0
        }
    }

    /**
     * Transforms a Julian day (rise/set/transit fields) to a common date.
     * @param jd The Julian day.
     * @return A set of integers: year, month, day, hour, minute, second.
     * @throws Exception If the input date does not exists.
     */
    fun jdToCalendar(jd: Double, tz: TimeZone): Calendar {
        if (jd < 2299160.0 && jd >= 2299150.0) throw IllegalArgumentException("invalid julian day $jd. This date does not exist.")

        // The conversion formulas are from Meeus,
        // Chapter 7.
        val z = floor(jd + 0.5)
        val f = jd + 0.5 - z
        var a = z
        if (z >= 2299161.0) {
            val a1 = ((z - 1867216.25) / 36524.25).toInt()
            a += 1 + a1 - a1 / 4.toDouble()
        }
        val b = a + 1524
        val c = ((b - 122.1) / 365.25).toInt()
        val d = (c * 365.25).toInt()
        val e = ((b - d) / 30.6001).toInt()
        val exactDay = f + b - d - (30.6001 * e).toInt()
        val day = exactDay.toInt()
        val month = if (e < 14) e - 1 else e - 13
        var year = c - 4715
        if (month > 2) year--
        val h = (exactDay - day) * SECONDS_PER_DAY / 3600.0
        val hour = h.toInt()
        val m = (h - hour) * 60.0
        val minute = m.toInt()
        val second = ((m - minute) * 60.0).toInt()
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(year, month - 1, day, hour, minute, second)
        calendar.get(HOUR_OF_DAY)
        calendar.timeZone = tz
        calendar.get(HOUR_OF_DAY)
        return calendar
    }

}