package uk.co.sundroid.util.astro.math

import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.MoonPhase.*
import uk.co.sundroid.util.astro.math.SunCalculator.Event.RISESET
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.time.clone
import java.lang.Math.PI
import java.lang.Math.toRadians
import java.lang.Math.toDegrees
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.SECOND
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.tan
import kotlin.math.atan
import kotlin.math.atan2


object BodyPositionCalculator {

    fun calcDay(body: Body, location: LatitudeLongitude, dateMidnight: Calendar, transitAndLength: Boolean): BodyDay {

        if (body === Body.SUN) {
            return SunCalculator.calcDay(location, dateMidnight, RISESET)
        }

        val bodyDay: BodyDay
        if (body === Body.MOON) {
            bodyDay = MoonDay()
            bodyDay.phaseDouble = MoonPhaseCalculator.getNoonPhase(dateMidnight)
            bodyDay.illumination = MoonPhaseCalculator.getIlluminatedPercent(bodyDay.phaseDouble)

            when {
                bodyDay.phaseDouble < 0.25 -> bodyDay.phase = EVENING_CRESCENT
                bodyDay.phaseDouble < 0.5 -> bodyDay.phase = WAXING_GIBBOUS
                bodyDay.phaseDouble < 0.75 -> bodyDay.phase = WANING_GIBBOUS
                else -> bodyDay.phase = MORNING_CRESCENT
            }
            val event = MoonPhaseCalculator.getDayEvent(dateMidnight)
            if (event != null) {
                bodyDay.phase = event.phase
                bodyDay.phaseEvent = event
            }

        } else {
            bodyDay = BodyDay()
        }

        // For each hour, get the elevation. If a rise or set has happened during the time, use the
        // relative elevations to guess a minute, calculate that, then work forward or back one
        // minute at a time to find the minute nearest the event. If the body rises and sets within
        // the same hour, both events will be missed.
        // Uptime is taken to be the time between the first rise of the day, and the next set as long
        // as it occurs within the current or next day, so we need up to 48 hours to calculate it.
        val calendar = clone(dateMidnight)
        val hours = if (transitAndLength) 48 else 24
        val hourEls = DoubleArray(50)
        val hourAzs = DoubleArray(50)
        val radiusCorrection = if (body === Body.MOON) 0.5 else 0.0
        hourLoop@ for (hour in 0..hours) {
            calendar.timeInMillis = dateMidnight.timeInMillis
            calendar.add(HOUR_OF_DAY, hour)
            val hourPosition = calcPosition(body, location, calendar)
            hourEls[hour] = hourPosition.appElevation
            hourAzs[hour] = hourPosition.azimuth

            if (transitAndLength && hour > 0 && hour <= 24 && sector(hourAzs[hour]) != sector(hourAzs[hour - 1])) {
                val noon = binarySearchNoon(body, location, sector(hourAzs[hour]), calendar.timeInMillis, 30 * 60 * 1000, -1, 0)
                if (bodyDay.transit == null || noon.appElevation > bodyDay.transitAppElevation) {
                    val noonCal = Calendar.getInstance(dateMidnight.timeZone)
                    noonCal.timeInMillis = noon.timestamp
                    bodyDay.transit = noonCal
                    bodyDay.transitAppElevation = noon.appElevation
                }
            }

            if (hour > 0 && sign(hourEls[hour], radiusCorrection) != sign(hourEls[hour - 1], radiusCorrection)) {

                val diff = hourEls[hour] - hourEls[hour - 1]
                val minuteGuess = round(60 * abs(hourEls[hour - 1] / diff)).toInt()

                calendar.add(HOUR_OF_DAY, -1)
                calendar.set(MINUTE, minuteGuess)

                var initPosition = calcPosition(body, location, calendar)
                var initEl = initPosition.appElevation

                val direction = if (sign(initEl, radiusCorrection) == sign(hourEls[hour - 1], radiusCorrection)) 1 else -1

                var safety = 0
                while (safety < 60) {
                    calendar.add(MINUTE, direction)

                    val thisPosition = calcPosition(body, location, calendar)
                    val thisEl = thisPosition.appElevation

                    if (sign(thisEl, radiusCorrection) != sign(initEl, radiusCorrection)) {
                        var azimuth = thisPosition.azimuth
                        if (abs(thisEl + radiusCorrection) > abs(initEl + radiusCorrection)) {
                            // Previous time was closer. Use previous iteration's values, except when this changes the day of the event.
                            if (direction != -1 || calendar[HOUR_OF_DAY] != 23 || calendar[MINUTE] != 59) {
                                calendar.add(MINUTE, -direction)
                            }
                            azimuth = initPosition.azimuth
                        }
                        if (sign(hourEls[hour - 1], radiusCorrection) < 0) {
                            if (hour <= 24 && calendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
                                val riseCalendar = clone(calendar)
                                if (bodyDay.rise == null) {
                                    bodyDay.rise = riseCalendar
                                    bodyDay.riseAzimuth = azimuth
                                }
                                bodyDay.addEvent(BodyDayEvent(BodyDayEventType.RISE, riseCalendar, azimuth))
                            }
                        } else {
                            if (hour <= 24 && calendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
                                val setCalendar = clone(calendar)
                                if (bodyDay.set == null) {
                                    bodyDay.set = setCalendar
                                    bodyDay.setAzimuth = azimuth
                                }
                                bodyDay.addEvent(BodyDayEvent(BodyDayEventType.SET, setCalendar, azimuth))
                            }
                            if (bodyDay.rise != null) {
                                bodyDay.uptimeHours = (calendar.timeInMillis - bodyDay.rise!!.timeInMillis) / (1000.0 * 60.0 * 60.0)
                            }
                        }
                        break
                    }

                    // Set for next minute.
                    initPosition = thisPosition
                    initEl = thisEl
                    safety++
                }
            }

            // Abort at 24 hours if there has been no rise, an uptime has been calculated already, or
            // the uptime is not needed.
            if (hour > 24 && (bodyDay.rise == null || bodyDay.uptimeHours != 0.0 || !transitAndLength)) {
                break
            }
        }

        if (bodyDay.rise == null && bodyDay.set == null) {
            bodyDay.riseSetType = if (hourEls[12] > 0) RiseSetType.RISEN else RiseSetType.SET
        }

        return bodyDay
    }

    private fun sector(azimuth: Double): Int {
        return if (azimuth >= 0 && azimuth < 180) {
            1
        } else {
            2
        }
    }

    fun calcPosition(body: Body, location: LatitudeLongitude, dateTime: Calendar): Position {
        if (body === Body.SUN) {
            return SunCalculator.calcPosition(location, dateTime)
        }
        val dateTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateTimeUtc.timeInMillis = dateTime.timeInMillis
        dateTimeUtc.timeInMillis
        return if (body === Body.MOON) {
            calcMoonPosition(location, dateTimeUtc)
        } else {
            calcPlanetPositionInternal(body, location, dateTimeUtc)
        }
    }

    private fun calcPosition(body: Body, location: LatitudeLongitude, time: Long): Position {
        val dateTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateTimeUtc.timeInMillis = time
        dateTimeUtc.timeInMillis
        return if (body === Body.MOON) {
            calcMoonPosition(location, dateTimeUtc)
        } else {
            calcPlanetPositionInternal(body, location, dateTimeUtc)
        }
    }

    private fun calcMoonPosition(location: LatitudeLongitude, dateTime: Calendar): Position {

        val day = dayNumber(dateTime)
        val obliquityOfEliptic = obliquityOfEcliptic(julianCent(dateTime))

        // Geocentric orbital components.
        val n = norm360(125.1228 - 0.0529538083 * day) // (Long asc. node)
        val i = norm360(5.1454) // (Inclination)
        val w = norm360(318.0634 + 0.1643573223 * day) // (Arg. of perigee)
        val a = 60.2666 // (Mean distance)
        val e = 0.054900 // (Eccentricity)
        val m = norm360(115.3654 + 13.0649929509 * day) // (Mean anomaly)

        // Calculate eccentric anomaly using iteration to produce accurate value.
        val e0 = m + 180 / PI * e * sin(toRadians(m)) * (1 + e * cos(toRadians(m)))
        var e1 = java.lang.Double.MAX_VALUE
        var loopCount = 0
        while (abs(e1 - e0) > 0.005 && loopCount < 10) {
            e1 = e0 - (e0 - 180 / PI * e * sin(toRadians(e0)) - m) / (1 - e * cos(toRadians(e0)))
            loopCount++
        }

        // Rectangular (x,y) coordinates in the plane of the orbit
        val planarX = a * (cos(toRadians(e1)) - e)
        val planarY = a * sqrt(1 - e * e) * sin(toRadians(e1))

        // Convert rectangular coordinates to distance and true anomaly.
        val geoR = sqrt(planarX * planarX + planarY * planarY) // (Earth radii)
        val trueAnomaly = norm360(toDegrees(atan2(toRadians(planarY), toRadians(planarX)))) // (Degrees)

        // Ecliptic geocentric rectangular coordinates
        var geoRectEclip = doubleArrayOf(geoR * (cos(toRadians(n)) * cos(toRadians(trueAnomaly + w)) - sin(toRadians(n)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), geoR * (sin(toRadians(n)) * cos(toRadians(trueAnomaly + w)) + cos(toRadians(n)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), geoR * sin(toRadians(trueAnomaly + w)) * sin(toRadians(i)))

        val geoRLonLatEclip = rectangularToSpherical(geoRectEclip)

        // Sun values
        val ws = norm360(282.9404 + 4.70935E-5 * day) // (longitude of perihelion)
        val ms = norm360(356.0470 + 0.9856002585 * day) // (mean anomaly)

        val ls = norm360(ws + ms)
        val lm = norm360(n + w + m)
        val d = norm360(lm - ls)
        val f = norm360(lm - n)

        // Apply lunar orbit perturbations
        geoRLonLatEclip[1] = (((geoRLonLatEclip[1] - 1.274 * sin(toRadians(m - 2 * d)) + 0.658 * sin(toRadians(2 * d))
                - 0.186 * sin(toRadians(ms))
                - 0.059 * sin(toRadians(2 * m - 2 * d))
                - 0.057 * sin(toRadians(m - 2 * d + ms)))
                + 0.053 * sin(toRadians(m + 2 * d))
                + 0.046 * sin(toRadians(2 * d - ms))
                + 0.041 * sin(toRadians(m - ms)))
                - 0.035 * sin(toRadians(d))
                - 0.031 * sin(toRadians(m + ms))
                - 0.015 * sin(toRadians(2 * f - 2 * d))) + 0.011 * sin(toRadians(m - 4 * d))
        geoRLonLatEclip[2] = ((geoRLonLatEclip[2]
                - 0.173 * sin(toRadians(f - 2 * d))
                - 0.055 * sin(toRadians(m - f - 2 * d))
                - 0.046 * sin(toRadians(m + f - 2 * d)))
                + 0.033 * sin(toRadians(f + 2 * d))
                + 0.017 * sin(toRadians(2 * m + f)))
        geoRLonLatEclip[0] = (geoRLonLatEclip[0]
                - 0.58 * cos(toRadians(m - 2 * d))
                - 0.46 * cos(toRadians(2 * d)))

        // Convert perturbed ecliptic lat and lon back into geo ecliptic rectangular coords.
        geoRectEclip = sphericalToRectangular(geoRLonLatEclip)

        // Rotate ecliptic rectangular coordinates to equatorial, then convert to spherical for RA and Dec.
        val geoRectEquat = eclipticToEquatorial(geoRectEclip, obliquityOfEliptic)
        val geoRRADec = rectangularToSpherical(geoRectEquat)

        val topoRRADec = geoToTopo(geoRRADec, location, dateTime)
        val topoAzEl = raDecToAzEl(topoRRADec, location, dateTime)

        return Position(dateTime.timeInMillis, topoAzEl[0], refractionCorrection(topoAzEl[1]))
    }

    private fun calcPlanetPositionInternal(body: Body, location: LatitudeLongitude, dateTime: Calendar): Position {

        val day = dayNumber(dateTime)
        val obliquityOfEliptic = obliquityOfEcliptic(julianCent(dateTime))

        val n: Double
        val i: Double
        val w: Double
        val a: Double
        val e: Double
        val m: Double

        // Calculate heliocentric orbital components.
        when (body) {
            Body.MERCURY -> {
                n = norm360(48.3313 + 3.24587E-5 * day)
                i = norm360(7.0047 + 5.00E-8 * day)
                w = norm360(29.1241 + 1.01444E-5 * day)
                a = 0.387098
                e = 0.205635 + 5.59E-10 * day
                m = norm360(168.6562 + 4.0923344368 * day)
            }
            Body.VENUS -> {
                n = norm360(76.6799 + 2.46590E-5 * day)
                i = norm360(3.3946 + 2.75E-8 * day)
                w = norm360(54.8910 + 1.38374E-5 * day)
                a = 0.723330
                e = 0.006773 - 1.302E-9 * day
                m = norm360(48.0052 + 1.6021302244 * day)
            }
            Body.MARS -> {
                n = norm360(49.5574 + 2.11081E-5 * day)
                i = norm360(1.8497 - 1.78E-8 * day)
                w = norm360(286.5016 + 2.92961E-5 * day)
                a = 1.523688
                e = 0.093405 + 2.516E-9 * day
                m = norm360(18.6021 + 0.5240207766 * day)
            }
            Body.JUPITER -> {
                n = norm360(100.4542 + 2.76854E-5 * day)
                i = norm360(1.3030 - 1.557E-7 * day)
                w = norm360(273.8777 + 1.64505E-5 * day)
                a = 5.20256
                e = 0.048498 + 4.469E-9 * day
                m = norm360(19.8950 + 0.0830853001 * day)
            }
            Body.SATURN -> {
                n = norm360(113.6634 + 2.38980E-5 * day)
                i = norm360(2.4886 - 1.081E-7 * day)
                w = norm360(339.3939 + 2.97661E-5 * day)
                a = 9.55475
                e = 0.055546 - 9.499E-9 * day
                m = norm360(316.9670 + 0.0334442282 * day)
            }
            Body.URANUS -> {
                n = norm360(74.0005 + 1.3978E-5 * day)
                i = norm360(0.7733 + 1.9E-8 * day)
                w = norm360(96.6612 + 3.0565E-5 * day)
                a = 19.18171 - 1.55E-8 * day
                e = 0.047318 + 7.45E-9 * day
                m = norm360(142.5905 + 0.011725806 * day)
            }
            Body.NEPTUNE -> {
                n = norm360(131.7806 + 3.0173E-5 * day)
                i = norm360(1.7700 - 2.55E-7 * day)
                w = norm360(272.8461 - 6.027E-6 * day)
                a = 30.05826 + 3.313E-8 * day
                e = 0.008606 + 2.15E-9 * day
                m = norm360(260.2471 + 0.005995147 * day)
            }
            else -> throw IllegalArgumentException("Unrecognised body: $body")
        }

        // Calculate eccentric anomaly using iteration to produce accurate value.
        val e0 = m + 180 / PI * e * sin(toRadians(m)) * (1 + e * cos(toRadians(m)))
        var e1 = java.lang.Double.MAX_VALUE
        var loopCount = 0
        while (abs(e1 - e0) > 0.005 && loopCount < 10) {
            e1 = e0 - (e0 - 180 / PI * e * sin(toRadians(e0)) - m) / (1 - e * cos(toRadians(e0)))
            loopCount++
        }

        // Rectangular (x,y) coordinates in the plane of the orbit
        val planarX = a * (cos(toRadians(e1)) - e)
        val planarY = a * sqrt(1 - e * e) * sin(toRadians(e1))

        // Convert rectangular coordinates to distance and true anomaly.
        val helioR = sqrt(planarX * planarX + planarY * planarY) // (Earth radii)
        val trueAnomaly = norm360(toDegrees(atan2(toRadians(planarY), toRadians(planarX)))) // (Degrees)

        // Ecliptic heliocentric rectangular coordinates
        var helioRectEclip = doubleArrayOf(helioR * (cos(toRadians(n)) * cos(toRadians(trueAnomaly + w)) - sin(toRadians(n)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), helioR * (sin(toRadians(n)) * cos(toRadians(trueAnomaly + w)) + cos(toRadians(n)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), helioR * sin(toRadians(trueAnomaly + w)) * sin(toRadians(i)))

        val helioRLonLatEclip = rectangularToSpherical(helioRectEclip)

        // Apply the planet's perturbations.
        val mju = norm360(19.8950 + 0.0830853001 * day)
        val msa = norm360(316.9670 + 0.0334442282 * day)
        val mur = norm360(142.5905 + 0.011725806 * day)
        when (body) {
            Body.JUPITER -> helioRLonLatEclip[1] = ((helioRLonLatEclip[1]
                    - 0.332 * sin(toRadians(2 * mju - 5 * msa - 67.6))
                    - 0.056 * sin(toRadians(2 * mju - 2 * msa + 21))) + 0.042 * sin(toRadians(3 * mju - 5 * msa + 21)) - 0.036 * sin(toRadians(mju - 2 * msa))
                    + 0.022 * cos(toRadians(mju - msa))
                    + 0.023 * sin(toRadians(2 * mju - 3 * msa + 52))) - 0.016 * sin(toRadians(mju - 5 * msa - 69.0))
            Body.SATURN -> {
                helioRLonLatEclip[1] = (helioRLonLatEclip[1] + 0.812 * sin(toRadians(2 * mju - 5 * msa - 67.6)) - 0.229 * cos(toRadians(2 * mju - 4 * msa - 2.0))
                        + 0.119 * sin(toRadians(mju - 2 * msa - 3.0))
                        + 0.046 * sin(toRadians(2 * mju - 6 * msa - 69.0))
                        + 0.014 * sin(toRadians(mju - 3 * msa + 32)))
                helioRLonLatEclip[2] = helioRLonLatEclip[2] - 0.020 * cos(toRadians(2 * mju - 4 * msa - 2.0)) + 0.018 * sin(toRadians(2 * mju - 6 * msa - 49.0))
            }
            Body.URANUS -> helioRLonLatEclip[1] = (helioRLonLatEclip[1]
                    + 0.040 * sin(toRadians(msa - 2 * mur + 6))
                    + 0.035 * sin(toRadians(msa - 3 * mur + 33))) - 0.015 * sin(toRadians(mju - mur + 20))
            else -> { }
        }

        // Convert perturbed ecliptic lat and lon back into helio ecliptic rectangular coords.
        helioRectEclip = sphericalToRectangular(helioRLonLatEclip)

        val geoRectEclip = helioToGeo(helioRectEclip, day)
        val geoRectEquat = eclipticToEquatorial(geoRectEclip, obliquityOfEliptic)
        val geoRRADec = rectangularToSpherical(geoRectEquat)
        val geoAzEl = raDecToAzEl(geoRRADec, location, dateTime)

        return Position(dateTime.timeInMillis, geoAzEl[0], refractionCorrection(geoAzEl[1]))
    }

    private fun refractionCorrection(elevation: Double): Double {
        var correction: Double
        if (elevation > 85.0) {
            correction = 0.0
        } else {
            val te = tan(toRadians(elevation))
            correction = when {
                elevation > 5.0     -> 58.1 / te - 0.07 / (te * te * te) + 0.000086 / (te * te * te * te * te)
                elevation > -0.575  -> 1735.0 + elevation * (-518.2 + elevation * (103.4 + elevation * (-12.79 + elevation * 0.711)))
                else                -> -20.774 / te
            }
            correction /= 3600.0
        }
        return elevation + correction
    }

    private fun dayNumber(dateTime: Calendar): Double {
        val year = dateTime.get(Calendar.YEAR)
        val month = dateTime.get(Calendar.MONTH) + 1
        val day = dateTime.get(Calendar.DAY_OF_MONTH)
        val fraction = dateTime.get(HOUR_OF_DAY) / 24.0 + dateTime.get(MINUTE) / (60.0 * 24.0) + dateTime.get(SECOND) / (60.0 * 60.0 * 24.0)
        return 367 * year - 7 * (year + (month + 9) / 12) / 4 + 275 * month / 9 + day - 730530 + fraction
    }

    private fun julianDay(dateTime: Calendar): Double {
        return dayNumber(dateTime) + 2451543.5
    }

    private fun julianCent(dateTime: Calendar): Double {
        val jd = julianDay(dateTime)
        return (jd - 2451545.0) / 36525.0
    }

    private fun rectangularToSpherical(xyz: DoubleArray): DoubleArray {
        val r = sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1] + xyz[2] * xyz[2])
        val lon = norm360(toDegrees(atan2(toRadians(xyz[1]), toRadians(xyz[0]))))
        val lat = toDegrees(atan2(toRadians(xyz[2]), toRadians(sqrt(xyz[0] * xyz[0] + xyz[1] * xyz[1]))))
        return doubleArrayOf(r, lon, lat)
    }

    private fun sphericalToRectangular(rLonLat: DoubleArray): DoubleArray {
        val x = rLonLat[0] * cos(toRadians(rLonLat[1])) * cos(toRadians(rLonLat[2]))
        val y = rLonLat[0] * sin(toRadians(rLonLat[1])) * cos(toRadians(rLonLat[2]))
        val z = rLonLat[0] * sin(toRadians(rLonLat[2]))
        return doubleArrayOf(x, y, z)
    }

    private fun eclipticToEquatorial(xyzEclip: DoubleArray, obliquityOfEliptic: Double): DoubleArray {
        val xEquat = xyzEclip[0]
        val yEquat = xyzEclip[1] * cos(toRadians(obliquityOfEliptic)) - xyzEclip[2] * sin(toRadians(obliquityOfEliptic))
        val zEquat = xyzEclip[1] * sin(toRadians(obliquityOfEliptic)) + xyzEclip[2] * cos(toRadians(obliquityOfEliptic))
        return doubleArrayOf(xEquat, yEquat, zEquat)
    }

    private fun raDecToAzEl(rRaDecl: DoubleArray, location: LatitudeLongitude, dateTime: Calendar): DoubleArray {
        val lsth = localSiderealTimeHours(location, dateTime)
        val rah = rRaDecl[1] / 15.0
        val had = 15 * norm24(lsth - rah) // Hour angle
        val x = cos(toRadians(had)) * cos(toRadians(rRaDecl[2]))
        val y = sin(toRadians(had)) * cos(toRadians(rRaDecl[2]))
        val z = sin(toRadians(rRaDecl[2]))
        val xhor = x * sin(toRadians(location.latitude.doubleValue)) - z * cos(toRadians(location.latitude.doubleValue))
        val zhor = x * cos(toRadians(location.latitude.doubleValue)) + z * sin(toRadians(location.latitude.doubleValue))
        val azimuth = norm360(toDegrees(atan2(toRadians(y), toRadians(xhor))) + 180.0)
        val trueElevation = toDegrees(atan2(toRadians(zhor), toRadians(sqrt(xhor * xhor + y * y))))
        return doubleArrayOf(azimuth, trueElevation)
    }

    private fun geoToTopo(rRaDec: DoubleArray, location: LatitudeLongitude, dateTime: Calendar): DoubleArray {
        val lat = location.latitude.doubleValue
        val gclat = lat - 0.1924 * sin(toRadians(2 * lat))
        val rho = 0.99833 + 0.00167 * cos(toRadians(2 * lat))
        val mpar = toDegrees(asin(1 / rRaDec[0]))
        val lst = localSiderealTimeHours(location, dateTime)
        val ha = norm360(lst * 15 - rRaDec[1])
        val g = toDegrees(atan(tan(toRadians(gclat)) / cos(toRadians(ha))))
        val topRA = rRaDec[1] - mpar * rho * cos(toRadians(gclat)) * sin(toRadians(ha)) / cos(toRadians(rRaDec[2]))
        val topDecl = rRaDec[2] - mpar * rho * sin(toRadians(gclat)) * sin(toRadians(g - rRaDec[2])) / sin(toRadians(g))
        return doubleArrayOf(rRaDec[0], topRA, topDecl)
    }

    private fun helioToGeo(helioRectEclip: DoubleArray, day: Double): DoubleArray {
        val w = norm360(282.9404 + 4.70935E-5 * day) // (longitude of perihelion)
        val e = 0.016709 - 1.151E-9 * day // (eccentricity)
        val m = norm360(356.0470 + 0.9856002585 * day) // (mean anomaly)
        val d = m + 180 / PI * e * sin(toRadians(m)) * (1 + e * cos(toRadians(m)))
        var x = cos(toRadians(d)) - e
        var y = sin(toRadians(d)) * sqrt(1 - e * e)
        val r = sqrt(x * x + y * y)
        val v = toDegrees(atan2(y, x))
        val lon = norm360(v + w)
        x = r * cos(toRadians(lon))
        y = r * sin(toRadians(lon))
        val z = 0.0
        return doubleArrayOf(helioRectEclip[0] + x, helioRectEclip[1] + y, helioRectEclip[2] + z)
    }

    private fun localSiderealTimeHours(location: LatitudeLongitude, dateTime: Calendar): Double {
        val day = dayNumber(dateTime)
        val ut = dateTime.get(HOUR_OF_DAY).toDouble() + dateTime.get(MINUTE) / 60.0 + dateTime.get(SECOND) / 3600.0
        val ws = norm360(282.9404 + 4.70935E-5 * day) // (longitude of perihelion)
        val ms = norm360(356.0470 + 0.9856002585 * day) // (mean anomaly)
        val ls = norm360(ws + ms)
        val gmst0 = ls / 15 + 12.0
        return norm24(gmst0 + ut + location.longitude.doubleValue / 15.0)
    }

    private fun obliquityOfEcliptic(julianCent: Double): Double {
        val seconds = 21.448 - julianCent * (46.8150 + julianCent * (0.00059 - julianCent * 0.001813))
        return 23.0 + (26.0 + seconds / 60.0) / 60.0
    }

    private fun norm360(degrees: Double): Double {
        var d = degrees
        while (d < 0.0) {
            d += 360.0
        }
        while (d > 360.0) {
            d -= 360.0
        }
        return d
    }

    private fun norm24(hours: Double): Double {
        var h = hours
        while (h < 0.0) {
            h += 24.0
        }
        while (h > 24.0) {
            h -= 24.0
        }
        return h
    }

    private fun sign(value: Double, plus: Double): Int {
        return if (value + plus < 0.0) -1 else 1
    }

    private fun binarySearchNoon(body: Body, location: LatitudeLongitude, initialSector: Int, initialTimestamp: Long, intervalMs: Int, searchDirection: Int, depth: Int): Position {
        val thisTimestamp = initialTimestamp + searchDirection * intervalMs
        val thisPosition = calcPosition(body, location, thisTimestamp)
        val thisSector = sector(thisPosition.azimuth)

        if (intervalMs < 15000 || depth > 10) {
            return thisPosition
        }

        val directionChange = if (thisSector == initialSector) 1 else -1
        return binarySearchNoon(body, location, thisSector, thisTimestamp, intervalMs / 2, searchDirection * directionChange, depth + 1)
    }

}
