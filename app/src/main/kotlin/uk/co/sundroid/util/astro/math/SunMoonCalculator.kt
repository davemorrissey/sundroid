package uk.co.sundroid.util.astro.math

import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.OrientationAngles
import uk.co.sundroid.util.astro.Position
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.location.LatitudeLongitude
import java.util.*
import java.util.Calendar.*
import kotlin.math.*


/**
 * This is derived from a free open source class developed by T. Alonso Albi, and available here:
 * http://conga.oan.es/~alonso/doku.php?id=blog:sun_moon_position
 *
 * Currently this is only used to provide orientation angles for rendering the moon. Later versions
 * of Sundroid will gradually migrate other sun and moon calculations to this class if it equals or
 * improves upon the current code.
 *
 * TODO Refactor to remove all state and create a thread safe stateless object.
 *
 * Original documentation follows:
 *
 * A very simple yet accurate Sun/Moon calculator without using JPARSEC library.
 * @author T. Alonso Albi - OAN (Spain), email t.alonso@oan.es
 * @version November 26, 2018 (two new methods getCulminationTime and getAzimuthTime)
 * @version November 6, 2018 (better accuracy for Moon, angular radius in ephemeris, cosmetic improvements)
 * @version July 24, 2018 (new class to hold results, illumination phase, moon phases, equinoxes and solstices)
 * @version May 25, 2017 (fixed nutation correction and moon age, better accuracy in Moon)
 */
@Suppress("unused")
object SunMoonCalculator {

    /** Radians to degrees.  */
    private const val RAD_TO_DEG = 180.0 / Math.PI

    /** Degrees to radians.  */
    private const val DEG_TO_RAD = 1.0 / RAD_TO_DEG

    /** Astronomical Unit in km. As defined by JPL.  */
    private const val AU = 149597870.691

    /** Earth equatorial radius in km. IERS 2003 Conventions.  */
    private const val EARTH_RADIUS = 6378.1366

    /** Two times Pi.  */
    private const val TWO_PI = 2.0 * Math.PI

    /** Pi divided by two.  */
    private const val PI_OVER_TWO = Math.PI / 2.0

    /** Julian century conversion constant = 100 * days per year.  */
    private const val JULIAN_DAYS_PER_CENTURY = 36525.0

    /** Seconds in one day.  */
    private const val SECONDS_PER_DAY = 86400.0

    /** Our default epoch. The Julian Day which represents noon on 2000-01-01.  */
    private const val J2000 = 2451545.0

    // Formulae here is a simplification of the expansion from
    // "Planetary Programs and Tables" by Pierre Bretagnon and
    // Jean-Louis Simon, Willman-Bell, 1986. This source also
    // have expansions for ephemerides of planets
    private val SUN_ELEMENTS = arrayOf(doubleArrayOf(403406.0, 0.0, 4.721964, 1.621043), doubleArrayOf(195207.0, -97597.0, 5.937458, 62830.348067), doubleArrayOf(119433.0, -59715.0, 1.115589, 62830.821524), doubleArrayOf(112392.0, -56188.0, 5.781616, 62829.634302), doubleArrayOf(3891.0, -1556.0, 5.5474, 125660.5691), doubleArrayOf(2819.0, -1126.0, 1.512, 125660.9845), doubleArrayOf(1721.0, -861.0, 4.1897, 62832.4766), doubleArrayOf(0.0, 941.0, 1.163, .813), doubleArrayOf(660.0, -264.0, 5.415, 125659.31), doubleArrayOf(350.0, -163.0, 4.315, 57533.85), doubleArrayOf(334.0, 0.0, 4.553, -33.931), doubleArrayOf(314.0, 309.0, 5.198, 777137.715), doubleArrayOf(268.0, -158.0, 5.989, 78604.191), doubleArrayOf(242.0, 0.0, 2.911, 5.412), doubleArrayOf(234.0, -54.0, 1.423, 39302.098), doubleArrayOf(158.0, 0.0, .061, -34.861), doubleArrayOf(132.0, -93.0, 2.317, 115067.698), doubleArrayOf(129.0, -20.0, 3.193, 15774.337), doubleArrayOf(114.0, 0.0, 2.828, 5296.67), doubleArrayOf(99.0, -47.0, .52, 58849.27), doubleArrayOf(93.0, 0.0, 4.65, 5296.11), doubleArrayOf(86.0, 0.0, 4.35, -3980.7), doubleArrayOf(78.0, -33.0, 2.75, 52237.69), doubleArrayOf(72.0, -32.0, 4.5, 55076.47), doubleArrayOf(68.0, 0.0, 3.23, 261.08), doubleArrayOf(64.0, -10.0, 1.22, 15773.85))

    /**
     * The set of twilights to calculate (types of rise/set events).
     */
    enum class Twilight {

        /**
         * Event ID for calculation of rising and setting times for astronomical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -18 degrees of geometrical elevation below the
         * astronomical horizon. At this time astronomical observations are possible
         * because the sky is dark enough.
         */
        TWILIGHT_ASTRONOMICAL,

        /**
         * Event ID for calculation of rising and setting times for nautical
         * twilight. In this case, the calculated time will be the time when the
         * center of the object is at -12 degrees of geometric elevation below the
         * astronomical horizon.
         */
        TWILIGHT_NAUTICAL,

        /**
         * Event ID for calculation of rising and setting times for civil twilight.
         * In this case, the calculated time will be the time when the center of the
         * object is at -6 degrees of geometric elevation below the astronomical
         * horizon.
         */
        TWILIGHT_CIVIL,

        /**
         * The standard value of 34' for the refraction at the local horizon.
         */
        HORIZON_34ARCMIN

    }

    /**
     * The set of events to calculate (rise/set/transit events).
     */
    enum class Event {
        RISE,
        SET,
        TRANSIT
    }

    /**
     * The set of phases to compute the moon phases.
     * @param phase Phase value where 0 = new and 0.5 = full.
     * */
    enum class MoonPhase(val phase: Double) {

        /** New Moon phase.  */
        NEW_MOON(0.0),

        /** Crescent quarter phase.  */
        CRESCENT_QUARTER(0.25),

        /** Full Moon phase.  */
        FULL_MOON(0.5),

        /** Descent quarter phase.  */
        DESCENT_QUARTER(0.75);

    }

    /**
     * Input values.
     */
    class Params(dateTime: Calendar, val obsLat: Double, val obsLon: Double, val twilight: Twilight) {

        private val ttMinusUt: Double
        val jd: Double

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

        init {
            val utc = getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = dateTime.timeInMillis
            val year = utc.get(YEAR)
            val month = utc.get(MONTH) + 1
            val day = utc.get(DAY_OF_MONTH)
            this.jd = toJulianDay(year, month, day, utc.get(HOUR_OF_DAY), utc.get(MINUTE), utc.get(SECOND))
            if (year > -600 && year < 2200) {
                val x = year + (month - 1 + day / 30.0) / 12.0
                val x2 = x * x
                val x3 = x2 * x
                val x4 = x3 * x
                this.ttMinusUt = if (year < 1600) {
                    10535.328003326353 - 9.995238627481024 * x + 0.003067307630020489 * x2 - 7.76340698361363E-6 * x3 + 3.1331045394223196E-9 * x4 + 8.225530854405553E-12 * x2 * x3 - 7.486164715632051E-15 * x4 * x2 + 1.9362461549678834E-18 * x4 * x3 - 8.489224937827653E-23 * x4 * x4
                } else {
                    -1027175.3477559977 + 2523.256625418965 * x - 1.885686849058459 * x2 + 5.869246227888417E-5 * x3 + 3.3379295816475025E-7 * x4 + 1.7758961671447929E-10 * x2 * x3 - 2.7889902806153024E-13 * x2 * x4 + 1.0224295822336825E-16 * x3 * x4 - 1.2528102370680435E-20 * x4 * x4
                }
            } else {
                this.ttMinusUt = 0.0
            }
        }
    }

    class Position(val eclipticLongitude: Double, val eclipticLatitude: Double, val distance: Double, val angularRadius: Double, val moonAge: Double = 0.0, val moonPhase: Double = 0.0)

    /**
     * Class to hold the results of ephemerides.
     * @author T. Alonso Albi - OAN (Spain)
     */
    class Ephemeris(
            /** Values for azimuth, elevation, rise, set, and transit for the Sun. Angles in radians, rise ...
             * as Julian days in UT. Distance in AU.  */
            var azimuth: Double, var elevation: Double, var rise: Double, var set: Double,
            var transit: Double, var transitElevation: Double, var rightAscension: Double, var declination: Double,
            var distance: Double, var eclipticLongitude: Double, var eclipticLatitude: Double, var angularRadius: Double) {
        var moonIllumination = 100.0
        var moonAge = 0.0
        var moonPhase = 0.0
    }

    private fun toJulianDay(year: Int, month: Int, day: Int, h: Int, m: Int, s: Int): Double {
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
     * Returns the sun's position for a given time and location.
     */
    fun getSunPosition(dateTime: Calendar, location: LatitudeLongitude): uk.co.sundroid.util.astro.Position {
        val params = Params(dateTime, location.latitude.doubleValue * DEG_TO_RAD, location.longitude.doubleValue * DEG_TO_RAD, Twilight.HORIZON_34ARCMIN)
        val sunPosition = getSunPosition(params.t())
        val sunTopo = calculateSunEphemeris(params.time(), params)
        val sunGeo = calculateEphemeris(params.time(), params, sunPosition, true)

        val position = Position(dateTime.timeInMillis, sunTopo.azimuth * RAD_TO_DEG, sunTopo.elevation * RAD_TO_DEG)
        position.julianDay = params.jd
        position.topoRA = sunTopo.rightAscension * RAD_TO_DEG
        position.topoDec = sunTopo.declination * RAD_TO_DEG
        position.topoDistKm = sunTopo.distance * AU
        position.topoDistEarthRadii = sunTopo.distance * AU / EARTH_RADIUS
        position.geoRA = sunGeo.rightAscension * RAD_TO_DEG
        position.geoDec = sunGeo.declination * RAD_TO_DEG
        position.geoDistKm = sunGeo.distance * AU
        position.geoDistEarthRadii = sunGeo.distance * AU / EARTH_RADIUS
        return position
    }

    /**
     * Returns the sun ephemeris for a given date and location.
     */
    fun getSunDay(dateMidnight: Calendar, location: LatitudeLongitude): SunDay {
        val params = Params(dateMidnight, location.latitude.doubleValue * DEG_TO_RAD, location.longitude.doubleValue * DEG_TO_RAD, Twilight.HORIZON_34ARCMIN)
        val ephemeris = calculateSunEphemeris(params.time(), params)
        val day = SunDay()
        day.rise = getCalendar(ephemeris.rise, dateMidnight.timeZone)
        day.set = getCalendar(ephemeris.set, dateMidnight.timeZone)
        return day
    }

    /**
     * Calculates everything for the Sun.
     */
    private fun calculateSunEphemeris(time: DoubleArray, params: Params): Ephemeris {
        val sun = calculateEphemeris(time, params, getSunPosition(time[1]), false)
        val niter = 3 // Number of iterations to get accurate rise/set/transit times
        sun.rise = obtainAccurateRiseSetTransit(sun.rise, params, Event.RISE, niter, true)
        sun.set = obtainAccurateRiseSetTransit(sun.set, params, Event.SET, niter, true)
        sun.transit = obtainAccurateRiseSetTransit(sun.transit, params, Event.TRANSIT, niter, true)
        if (sun.transit == -1.0) {
            sun.transitElevation = 0.0
        } else {
            // Update Sun's maximum elevation
            val transit = params.time(sun.transit)
            sun.transitElevation = calculateEphemeris(transit, params, getSunPosition(transit[1]), false).transitElevation
        }
        return sun
    }

    /**
     * Returns the moon's position for a given time and location.
     */
    fun getMoonPosition(dateTime: Calendar, location: LatitudeLongitude): uk.co.sundroid.util.astro.Position {
        val params = Params(dateTime, location.latitude.doubleValue * DEG_TO_RAD, location.longitude.doubleValue * DEG_TO_RAD, Twilight.HORIZON_34ARCMIN)
        val moonPosition = getMoonPosition(params.t(), params)
        val moonTopo = calculateMoonEphemeris(params.time(), params)
        val moonGeo = calculateEphemeris(params.time(), params, moonPosition, true)

        val position = Position(dateTime.timeInMillis, moonTopo.azimuth * RAD_TO_DEG, moonTopo.elevation * RAD_TO_DEG)
        position.julianDay = params.jd
        position.topoRA = moonTopo.rightAscension * RAD_TO_DEG
        position.topoDec = moonTopo.declination * RAD_TO_DEG
        position.topoDistKm = moonTopo.distance * AU
        position.topoDistEarthRadii = moonTopo.distance * AU / EARTH_RADIUS
        position.geoRA = moonGeo.rightAscension * RAD_TO_DEG
        position.geoDec = moonGeo.declination * RAD_TO_DEG
        position.geoDistKm = moonGeo.distance * AU
        position.geoDistEarthRadii = moonGeo.distance * AU / EARTH_RADIUS
        position.moonAge = moonTopo.moonAge
        position.moonPhase = moonTopo.moonPhase / TWO_PI
        position.moonIllumination = moonTopo.moonIllumination
        return position
    }

    /**
     * Returns the moon ephemeris for a given date and location.
     */
    fun getMoonDay(dateMidnight: Calendar, location: LatitudeLongitude): MoonDay {
        val params = Params(dateMidnight, location.latitude.doubleValue * DEG_TO_RAD, location.longitude.doubleValue * DEG_TO_RAD, Twilight.HORIZON_34ARCMIN)
        val ephemeris = calculateMoonEphemeris(params.time(), params)
        val day = MoonDay()
        day.rise = getCalendar(ephemeris.rise, dateMidnight.timeZone)
        day.set = getCalendar(ephemeris.set, dateMidnight.timeZone)
        return day
    }

    /**
     * Calculates everything for the Moon.
     */
    private fun calculateMoonEphemeris(time: DoubleArray, params: Params): Ephemeris {
        val moonPosition = getMoonPosition(time[1], params)
        val moon = calculateEphemeris(time, params, moonPosition, false)
        val niter = 5 // Number of iterations to get accurate rise/set/transit times
        moon.rise = obtainAccurateRiseSetTransit(moon.rise, params, Event.RISE, niter, false)
        moon.set = obtainAccurateRiseSetTransit(moon.set, params, Event.SET, niter, false)
        moon.transit = obtainAccurateRiseSetTransit(moon.transit, params, Event.TRANSIT, niter, false)
        if (moon.transit == -1.0) {
            moon.transitElevation = 0.0
        } else {
            // Update Moon's maximum elevation
            val transit = params.time(moon.transit)
            moon.transitElevation = calculateEphemeris(transit, params, getMoonPosition(transit[1], params), false).transitElevation
        }

        // Compute illumination phase percentage for the Moon (do not use for other bodies!)
        val sun = calculateSunEphemeris(time, params)
        val dlon = moon.rightAscension - sun.rightAscension
        val elong = acos(sin(sun.declination) * sin(moon.declination) +
                cos(sun.declination) * cos(moon.declination) * cos(dlon))
        moon.moonIllumination = 100 * (1.0 - cos(elong)) * 0.5
        moon.moonAge = moonPosition.moonAge
        moon.moonPhase = moonPosition.moonPhase
        return moon
    }

    private fun getSunPosition(t: Double): Position {
        var l = 0.0
        var r = 0.0
        val t2 = t * 0.01
        for (i in SUN_ELEMENTS.indices) {
            val v = SUN_ELEMENTS[i][2] + SUN_ELEMENTS[i][3] * t2
            val u = normalizeRadians(v)
            l += SUN_ELEMENTS[i][0] * sin(u)
            r += SUN_ELEMENTS[i][1] * cos(u)
        }
        var lon = normalizeRadians(4.9353929 + normalizeRadians(62833.196168 * t2) + l / 10000000.0) * RAD_TO_DEG
        val sdistance = 1.0001026 + r / 10000000.0

        // Now subtract aberration. Note light-time is not corrected, negligible for Sun
        lon += -.00569
        val slongitude = lon // apparent longitude (error<0.001 deg)
        val slatitude = 0.0 // Sun's ecliptic latitude is always negligible
        return Position(slongitude, slatitude, sdistance, atan(696000 / (AU * sdistance)))
    }

    private fun getMoonPosition(t: Double, params: Params): Position {
        // MOON PARAMETERS (Formulae from "Calendrical Calculations")
        val phase = normalizeRadians((297.8502042 + 445267.1115168 * t - 0.00163 * t * t + t * t * t / 538841 - t * t * t * t / 65194000) * DEG_TO_RAD)

        // Anomalistic phase
        var anomaly = 134.9634114 + 477198.8676313 * t + .008997 * t * t + t * t * t / 69699 - t * t * t * t / 14712000
        anomaly *= DEG_TO_RAD

        // Degrees from ascending node
        var node = 93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000 + t * t * t * t / 863310000
        node *= DEG_TO_RAD
        val e = 1.0 - (.002495 + 7.52E-06 * (t + 1.0)) * (t + 1.0)

        // Solar anomaly
        val sanomaly = (357.5291 + 35999.0503 * t - .0001559 * t * t - 4.8E-07 * t * t * t) * DEG_TO_RAD

        // Now longitude, with the three main correcting terms of evection,
        // variation, and equation of year, plus other terms (error<0.01 deg)
        // P. Duffet's MOON program taken as reference
        var l = 218.31664563 + 481267.8811958 * t - .00146639 * t * t + t * t * t / 540135.03 - t * t * t * t / 65193770.4
        l += 6.28875 * sin(anomaly) + 1.274018 * sin(2 * phase - anomaly) + .658309 * sin(2 * phase)
        l += 0.213616 * sin(2 * anomaly) - e * .185596 * sin(sanomaly) - 0.114336 * sin(2 * node)
        l += .058793 * sin(2 * phase - 2 * anomaly) + .057212 * e * sin(2 * phase - anomaly - sanomaly) + .05332 * sin(2 * phase + anomaly)
        l += .045874 * e * sin(2 * phase - sanomaly) + .041024 * e * sin(anomaly - sanomaly) - .034718 * sin(phase) - e * .030465 * sin(sanomaly + anomaly)
        l += .015326 * sin(2 * (phase - node)) - .012528 * sin(2 * node + anomaly) - .01098 * sin(2 * node - anomaly) + .010674 * sin(4 * phase - anomaly)
        l += .010034 * sin(3 * anomaly) + .008548 * sin(4 * phase - 2 * anomaly)
        l += -e * .00791 * sin(sanomaly - anomaly + 2 * phase) - e * .006783 * sin(2 * phase + sanomaly) + .005162 * sin(anomaly - phase) + e * .005 * sin(sanomaly + phase)
        l += .003862 * sin(4 * phase) + e * .004049 * sin(anomaly - sanomaly + 2 * phase) + .003996 * sin(2 * (anomaly + phase)) + .003665 * sin(2 * phase - 3 * anomaly)
        l += e * 2.695E-3 * sin(2 * anomaly - sanomaly) + 2.602E-3 * sin(anomaly - 2 * (node + phase))
        l += e * 2.396E-3 * sin(2 * (phase - anomaly) - sanomaly) - 2.349E-3 * sin(anomaly + phase)
        l += e * e * 2.249E-3 * sin(2 * (phase - sanomaly)) - e * 2.125E-3 * sin(2 * anomaly + sanomaly)
        l += -e * e * 2.079E-3 * sin(2 * sanomaly) + e * e * 2.059E-3 * sin(2 * (phase - sanomaly) - anomaly)
        l += -1.773E-3 * sin(anomaly + 2 * (phase - node)) - 1.595E-3 * sin(2 * (node + phase))
        l += e * 1.22E-3 * sin(4 * phase - sanomaly - anomaly) - 1.11E-3 * sin(2 * (anomaly + node))
        val longitude = l

        // Get accurate Moon age
        val psin = 29.530588853
        val sunEphemeris = calculateSunEphemeris(params.time(), params)
        val moonAge = normalizeRadians(longitude * DEG_TO_RAD - sunEphemeris.eclipticLongitude) * psin / TWO_PI

        // Now Moon parallax
        var parallax = .950724 + .051818 * cos(anomaly) + .009531 * cos(2 * phase - anomaly)
        parallax += .007843 * cos(2 * phase) + .002824 * cos(2 * anomaly)
        parallax += 0.000857 * cos(2 * phase + anomaly) + e * .000533 * cos(2 * phase - sanomaly)
        parallax += e * .000401 * cos(2 * phase - anomaly - sanomaly) + e * .00032 * cos(anomaly - sanomaly) - .000271 * cos(phase)
        parallax += -e * .000264 * cos(sanomaly + anomaly) - .000198 * cos(2 * node - anomaly)
        parallax += 1.73E-4 * cos(3 * anomaly) + 1.67E-4 * cos(4 * phase - anomaly)

        // So Moon distance in Earth radii is, more or less,
        val distance = 1.0 / sin(parallax * DEG_TO_RAD)

        // Ecliptic latitude with nodal phase (error<0.01 deg)
        l = 5.128189 * sin(node) + 0.280606 * sin(node + anomaly) + 0.277693 * sin(anomaly - node)
        l += .173238 * sin(2 * phase - node) + .055413 * sin(2 * phase + node - anomaly)
        l += .046272 * sin(2 * phase - node - anomaly) + .032573 * sin(2 * phase + node)
        l += .017198 * sin(2 * anomaly + node) + .009267 * sin(2 * phase + anomaly - node)
        l += .008823 * sin(2 * anomaly - node) + e * .008247 * sin(2 * phase - sanomaly - node) + .004323 * sin(2 * (phase - anomaly) - node)
        l += .0042 * sin(2 * phase + node + anomaly) + e * .003372 * sin(node - sanomaly - 2 * phase)
        l += e * 2.472E-3 * sin(2 * phase + node - sanomaly - anomaly)
        l += e * 2.222E-3 * sin(2 * phase + node - sanomaly)
        l += e * 2.072E-3 * sin(2 * phase - node - sanomaly - anomaly)
        val latitude = l
        return Position(longitude, latitude, distance * EARTH_RADIUS / AU, atan(1737.4 / (distance * EARTH_RADIUS)), moonAge = moonAge, moonPhase = phase)
    }

    private fun calculateEphemeris(time: DoubleArray, params: Params, pos: Position, geocentric: Boolean): Ephemeris {
        val jd = time[0]
        val t = time[1]
        val obsLat = params.obsLat
        val obsLon = params.obsLon
        var eclipLon = pos.eclipticLongitude
        var eclipLat = pos.eclipticLatitude
        val distance = pos.distance
        val angularRadius = pos.angularRadius

        // Correct for nutation in longitude and obliquity
        val m1 = (124.90 - 1934.134 * t + 0.002063 * t * t) * DEG_TO_RAD
        val m2 = (201.11 + 72001.5377 * t + 0.00057 * t * t) * DEG_TO_RAD
        val dLon = -.0047785 * sin(m1) - .0003667 * sin(m2)
        val dLat = .002558 * cos(m1) - .00015339 * cos(m2)
        eclipLon += dLon
        eclipLat += dLat

        // Ecliptic to equatorial coordinates
        val t2 = t / 100.0
        var tmp = t2 * (27.87 + t2 * (5.79 + t2 * 2.45))
        tmp = t2 * (-249.67 + t2 * (-39.05 + t2 * (7.12 + tmp)))
        tmp = t2 * (-1.55 + t2 * (1999.25 + t2 * (-51.38 + tmp)))
        tmp = t2 * (-4680.93 + tmp) / 3600.0
        val angle = (23.4392911111111 + tmp) * DEG_TO_RAD // mean obliquity
        eclipLon *= DEG_TO_RAD
        eclipLat *= DEG_TO_RAD
        val cl = cos(eclipLat)
        val x = distance * cos(eclipLon) * cl
        var y = distance * sin(eclipLon) * cl
        var z = distance * sin(eclipLat)
        tmp = y * cos(angle) - z * sin(angle)
        z = y * sin(angle) + z * cos(angle)
        y = tmp
        if (geocentric) return Ephemeris(0.0, 0.0, -1.0, -1.0, -1.0, -1.0, normalizeRadians(atan2(y, x)),
                atan2(z / sqrt(x * x + y * y), 1.0), sqrt(x * x + y * y + z * z), eclipLon, eclipLat, angularRadius)

        // Obtain local apparent sidereal time
        val jd0 = floor(jd - 0.5) + 0.5
        val t0 = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY
        val secs = (jd - jd0) * SECONDS_PER_DAY
        var gmst = ((-6.2e-6 * t0 + 9.3104e-2) * t0 + 8640184.812866) * t0 + 24110.54841
        val msday = 1.0 + ((-1.86e-5 * t0 + 0.186208) * t0 + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY)
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD
        val lst = gmst + obsLon

        // Obtain topocentric rectangular coordinates
        val radiusAU = EARTH_RADIUS / AU
        val correction = doubleArrayOf(
                radiusAU * cos(obsLat) * cos(lst),
                radiusAU * cos(obsLat) * sin(lst),
                radiusAU * sin(obsLat))
        val xtopo = x - correction[0]
        val ytopo = y - correction[1]
        val ztopo = z - correction[2]

        // Obtain topocentric equatorial coordinates
        var ra = 0.0
        var dec = PI_OVER_TWO
        if (ztopo < 0.0) dec = -dec
        if (ytopo != 0.0 || xtopo != 0.0) {
            ra = atan2(ytopo, xtopo)
            dec = atan2(ztopo / sqrt(xtopo * xtopo + ytopo * ytopo), 1.0)
        }
        val dist = sqrt(xtopo * xtopo + ytopo * ytopo + ztopo * ztopo)

        // Hour angle
        val angh = lst - ra

        // Obtain azimuth and geometric alt
        val sinlat = sin(obsLat)
        val coslat = cos(obsLat)
        val sindec = sin(dec)
        val cosdec = cos(dec)
        val h = sinlat * sindec + coslat * cosdec * cos(angh)
        var alt = asin(h)
        val azy = sin(angh)
        val azx = cos(angh) * sinlat - sindec * coslat / cosdec
        val azi = Math.PI + atan2(azy, azx) // 0 = north

        // Get apparent elevation
        if (alt > -3 * DEG_TO_RAD) {
            val r = 0.016667 * DEG_TO_RAD * abs(tan(PI_OVER_TWO - (alt * RAD_TO_DEG + 7.31 / (alt * RAD_TO_DEG + 4.4)) * DEG_TO_RAD))
            val refr = r * (0.28 * 1010 / (10 + 273.0)) // Assuming pressure of 1010 mb and T = 10 C
            alt = min(alt + refr, PI_OVER_TWO) // This is not accurate, but acceptable
        }
        tmp = when (params.twilight) {
            Twilight.HORIZON_34ARCMIN ->                 // Rise, set, transit times, taking into account Sun/Moon angular radius (pos[3]).
                // The 34' factor is the standard refraction at horizon.
                // Removing angular radius will do calculations for the center of the disk instead
                // of the upper limb.
                -(34.0 / 60.0) * DEG_TO_RAD - pos.angularRadius
            Twilight.TWILIGHT_CIVIL -> -6 * DEG_TO_RAD
            Twilight.TWILIGHT_NAUTICAL -> -12 * DEG_TO_RAD
            Twilight.TWILIGHT_ASTRONOMICAL -> -18 * DEG_TO_RAD
        }

        // Compute cosine of hour angle
        tmp = (sin(tmp) - sin(obsLat) * sin(dec)) / (cos(obsLat) * cos(dec))
        /** Length of a sidereal day in days according to IERS Conventions.  */
        val siderealDayLength = 1.00273781191135448
        val celestialHoursToEarthTime = 1.0 / (siderealDayLength * TWO_PI)

        // Make calculations for the meridian
        val transitTime1 = celestialHoursToEarthTime * normalizeRadians(ra - lst)
        val transitTime2 = celestialHoursToEarthTime * (normalizeRadians(ra - lst) - TWO_PI)
        var transitAlt = asin(sin(dec) * sin(obsLat) + cos(dec) * cos(obsLat))
        if (transitAlt > -3 * DEG_TO_RAD) {
            val r = 0.016667 * DEG_TO_RAD * abs(tan(PI_OVER_TWO - (transitAlt * RAD_TO_DEG + 7.31 / (transitAlt * RAD_TO_DEG + 4.4)) * DEG_TO_RAD))
            val refr = r * (0.28 * 1010 / (10 + 273.0)) // Assuming pressure of 1010 mb and T = 10 C
            transitAlt = min(transitAlt + refr, PI_OVER_TWO) // This is not accurate, but acceptable
        }

        // Obtain the current event in time
        var transitTime = transitTime1
        val jdToday = floor(jd - 0.5) + 0.5
        val transitToday2 = floor(jd + transitTime2 - 0.5) + 0.5
        // Obtain the transit time. Preference should be given to the closest event
        // in time to the current calculation time
        if (jdToday == transitToday2 && abs(transitTime2) < abs(transitTime1)) transitTime = transitTime2
        val transit = jd + transitTime

        // Make calculations for rise and set
        var rise = -1.0
        var set = -1.0
        if (abs(tmp) <= 1.0) {
            val angHor = abs(acos(tmp))
            val riseTime1 = celestialHoursToEarthTime * normalizeRadians(ra - angHor - lst)
            val setTime1 = celestialHoursToEarthTime * normalizeRadians(ra + angHor - lst)
            val riseTime2 = celestialHoursToEarthTime * (normalizeRadians(ra - angHor - lst) - TWO_PI)
            val setTime2 = celestialHoursToEarthTime * (normalizeRadians(ra + angHor - lst) - TWO_PI)

            // Obtain the current events in time. Preference should be given to the closest event
            // in time to the current calculation time (so that iteration in other method will converge)
            var riseTime = riseTime1
            val riseToday2 = floor(jd + riseTime2 - 0.5) + 0.5
            if (jdToday == riseToday2 && abs(riseTime2) < abs(riseTime1)) riseTime = riseTime2
            var setTime = setTime1
            val setToday2 = floor(jd + setTime2 - 0.5) + 0.5
            if (jdToday == setToday2 && abs(setTime2) < abs(setTime1)) setTime = setTime2
            rise = jd + riseTime
            set = jd + setTime
        }
        return Ephemeris(azi, alt, rise, set, transit, transitAlt,
                normalizeRadians(ra), dec, dist, eclipLon, eclipLat, angularRadius)
    }

    /**
     * Transforms a Julian day (rise/set/transit fields) to a common date.
     * @param jd The Julian day.
     * @return A set of integers: year, month, day, hour, minute, second.
     * @throws Exception If the input date does not exists.
     */
    private fun getCalendar(jd: Double, tz: TimeZone): Calendar {
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
        val calendar = getInstance(TimeZone.getTimeZone("UTC"))
        calendar.set(year, month - 1, day, hour, minute, second)
        calendar.timeZone = tz
        calendar.timeInMillis
        return calendar
    }

    private fun obtainAccurateRiseSetTransit(riseSetJd: Double, params: Params, index: Event, niter: Int, sun: Boolean): Double {
        var step = -1.0
        var riseSet = riseSetJd
        for (i in 0 until niter) {
            if (riseSet == -1.0) return riseSet // -1 means no rise/set from that location
            val time = params.time(riseSet)
            var out: Ephemeris?
            out = if (sun) {
                calculateEphemeris(time, params, getSunPosition(time[1]), false)
            } else {
                calculateEphemeris(time, params, getMoonPosition(time[1], params), false)
            }
            var v = out.rise
            if (index === Event.SET) v = out.set
            if (index === Event.TRANSIT) v = out.transit
            step = abs(riseSet - v)
            riseSet = v
        }
        return if (step > 1.0 / SECONDS_PER_DAY) (-1).toDouble() else riseSet // did not converge => without rise/set/transit in this date
    }

    /**
     * Returns the instant of a given moon phase.
     * @param phase The phase.
     * @return The instant of that phase, accuracy around 1 minute or better.
     */
    fun getMoonPhaseTime(params: Params, phase: MoonPhase): Double {
        val accuracy = 10 / (30 * SECONDS_PER_DAY) // 10s / lunar cycle length in s => 10s accuracy
        val refPhase: Double = phase.phase
        var jd = params.jd
        while (true) {
            val sunLon = getSunPosition(params.t(jd)).eclipticLongitude // Compute Sun before always !
            var age = normalizeRadians((getMoonPosition(params.t(jd), params).eclipticLongitude - sunLon) * DEG_TO_RAD) / TWO_PI - refPhase
            if (age < 0) age += 1.0
            if (age < accuracy || age > 1 - accuracy) break
            if (age < 0.5) {
                jd -= age
            } else {
                jd += 1 - age
            }
        }
        return jd
    }

    // Moon's argument of latitude
    fun moonDiskOrientationAngles(dateTime: Calendar, location: LatitudeLongitude): OrientationAngles {
        val params = Params(dateTime, location.latitude.doubleValue * DEG_TO_RAD, location.longitude.doubleValue * DEG_TO_RAD, Twilight.HORIZON_34ARCMIN)
        val t = params.t()
        val jd = params.jd
        val sun = calculateEphemeris(params.time(), params, getSunPosition(t), false)
        val moon = calculateEphemeris(params.time(), params, getMoonPosition(t, params), false)
        val moonLon = moon.eclipticLongitude
        val moonLat = moon.eclipticLatitude
        val moonRA = moon.rightAscension
        val moonDEC = moon.declination
        val sunRA = sun.rightAscension
        val sunDEC = sun.declination

        // Moon's argument of latitude
        val f = (93.2720993 + 483202.0175273 * t - 0.0034029 * t * t - t * t * t / 3526000.0 + t * t * t * t / 863310000.0) * DEG_TO_RAD
        // Moon's inclination
        val i = 1.54242 * DEG_TO_RAD
        // Moon's mean ascending node longitude
        val omega = (125.0445550 - 1934.1361849 * t + 0.0020762 * t * t + t * t * t / 467410.0 - t * t * t * t / 18999000.0) * DEG_TO_RAD
        // Obliquity of ecliptic (approx, better formulae up)
        val eps = 23.43929 * DEG_TO_RAD

        // Obtain optical librations lp and bp
        val w = moonLon - omega
        val sinA = sin(w) * cos(moonLat) * cos(i) - sin(moonLat) * sin(i)
        val cosA = cos(w) * cos(moonLat)
        val a = atan2(sinA, cosA)
        val lp = normalizeRadians(a - f)
        val sinbp = -sin(w) * cos(moonLat) * sin(i) - sin(moonLat) * cos(i)
        val bp = asin(sinbp)

        // Obtain position angle of axis p
        var posX = sin(i) * sin(omega)
        var posY = sin(i) * cos(omega) * cos(eps) - cos(i) * sin(eps)
        val posW = atan2(posX, posY)
        val sinp = sqrt(posX * posX + posY * posY) * cos(moonRA - posW) / cos(bp)
        val p = asin(sinp)

        // Compute bright limb angle bl
        val bl = Math.PI + atan2(cos(sunDEC) *
                sin(moonRA - sunRA), cos(sunDEC) *
                sin(moonDEC) * cos(moonRA - sunRA) -
                sin(sunDEC) * cos(moonDEC))

        // Paralactic angle par (first obtain local apparent sidereal time)
        val jd0 = floor(jd - 0.5) + 0.5
        val t0 = (jd0 - J2000) / JULIAN_DAYS_PER_CENTURY
        val secs = (jd - jd0) * SECONDS_PER_DAY
        var gmst = ((-6.2e-6 * t0 + 9.3104e-2) * t0 + 8640184.812866) * t0 + 24110.54841
        val msday = 1.0 + ((-1.86e-5 * t0 + 0.186208) * t0 + 8640184.812866) / (SECONDS_PER_DAY * JULIAN_DAYS_PER_CENTURY)
        gmst = (gmst + msday * secs) * (15.0 / 3600.0) * DEG_TO_RAD
        val lst = gmst + params.obsLon
        posY = sin(lst - moonRA)
        posX = tan(params.obsLat) * cos(moonDEC) - sin(moonDEC) * cos(lst - moonRA)
        val par: Double
        par = if (posX != 0.0) {
            atan2(posY, posX)
        } else {
            posY / abs(posY) * PI_OVER_TWO
        }
        val result = OrientationAngles()
        result.brightLimb = Math.toDegrees(bl)
        result.parallactic = Math.toDegrees(par)
        result.axis = Math.toDegrees(p)
        // TODO Confirm which is which if these are ever used
        result.librationLatitude = lp
        result.librationLongitude = bp
        return result
    }

    /**
     * Reduce an angle in radians to the range (0 - 2 Pi).
     * @param rArg Value in radians.
     * @return The reduced radians value.
     */
    private fun normalizeRadians(rArg: Double): Double {
        var r = rArg
        if (r < 0 && r >= -TWO_PI) return r + TWO_PI
        if (r >= TWO_PI && r < 2 * TWO_PI) return r - TWO_PI
        if (r >= 0 && r < TWO_PI) return r
        r -= TWO_PI * floor(r / TWO_PI)
        if (r < 0.0) r += TWO_PI
        return r
    }

}