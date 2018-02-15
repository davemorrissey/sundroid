package uk.co.sundroid.util.astro.math

import java.lang.Math.PI
import java.lang.Math.abs
import java.lang.Math.asin
import java.lang.Math.atan
import java.lang.Math.atan2
import java.lang.Math.cos
import java.lang.Math.sin
import java.lang.Math.sqrt
import java.lang.Math.tan
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE

import java.util.Calendar
import java.util.TimeZone

import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.MoonPhaseEvent
import uk.co.sundroid.util.astro.Position
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.time.*

import uk.co.sundroid.util.astro.math.SunCalculator.Event.*
import uk.co.sundroid.util.astro.MoonPhase.*

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

            if (bodyDay.phaseDouble < 0.25) {
                bodyDay.phase = EVENING_CRESCENT
            } else if (bodyDay.phaseDouble < 0.5) {
                bodyDay.phase = WAXING_GIBBOUS
            } else if (bodyDay.phaseDouble < 0.75) {
                bodyDay.phase = WANING_GIBBOUS
            } else {
                bodyDay.phase = MORNING_CRESCENT
            }
            val event = MoonPhaseCalculator.getDayEvent(dateMidnight)
            if (event != null) {
                bodyDay.phase = event.phase
                bodyDay.phaseEvent = event
            }

        } else {
            bodyDay = BodyDay()
        }

        // For each hour, get the elevation. If a rise or set has happened during the time,
        // use the relative elevations to guess a minute, calculate that, then work forward
        // or back one minute at a time to find the minute nearest the event. If the body
        // rises and sets within the same hour, both events will be missed.
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
                val minuteGuess = Math.round(60 * Math.abs(hourEls[hour - 1] / diff)).toInt()

                calendar.add(HOUR_OF_DAY, -1)
                calendar.set(MINUTE, minuteGuess)

                var initPosition = calcPosition(body, location, calendar)
                var initEl = initPosition.appElevation

                val direction = if (sign(initEl, radiusCorrection) == sign(hourEls[hour - 1], radiusCorrection)) 1 else -1

                var safety = 0
                while (safety < 60) {
                    calendar.add(Calendar.MINUTE, direction)

                    val thisPosition = calcPosition(body, location, calendar)
                    val thisEl = thisPosition.appElevation

                    //	System.out.println(TimeHelper.formatTime(calendar, true) + " " + thisEl + " " + direction);
                    if (sign(thisEl, radiusCorrection) != sign(initEl, radiusCorrection)) {
                        var azimuth = thisPosition.azimuth
                        if (Math.abs(thisEl + radiusCorrection) > Math.abs(initEl + radiusCorrection)) {
                            // Previous time was closer. Use previous iteration's values.
                            calendar.add(Calendar.MINUTE, -direction)
                            azimuth = initPosition.azimuth
                        }
                        if (sign(hourEls[hour - 1], radiusCorrection) < 0) {
                            if (hour <= 24 && calendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
                                bodyDay.rise = clone(calendar)
                                bodyDay.riseAzimuth = azimuth
                            }
                        } else {
                            if (hour <= 24 && calendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
                                bodyDay.set = clone(calendar)
                                bodyDay.setAzimuth = azimuth
                            } else if (hour > 24 && bodyDay.rise != null) {
                                bodyDay.uptimeHours = (calendar.timeInMillis - bodyDay.rise!!.timeInMillis) / (1000.0 * 60.0 * 60.0)
                                break@hourLoop
                            }
                        }
                        break
                    }

                    // Set for next minute.
                    initPosition = thisPosition
                    initEl = thisEl
                    safety++
                }

                // Set calendar to continue hourly iteration.
                //calendar.set(Calendar.HOUR_OF_DAY, hour);
                //calendar.set(Calendar.MINUTE, 0);

            }


            // If rise and set already calculated and rise before set, use them to calculate uptime.
            // If there is no rise it's a risen or set day.
            if (bodyDay.rise != null && bodyDay.set != null && (bodyDay.rise!!.timeInMillis < bodyDay.set!!.timeInMillis || !transitAndLength)) {
                bodyDay.uptimeHours = (bodyDay.set!!.timeInMillis - bodyDay.rise!!.timeInMillis) / (1000.0 * 60.0 * 60.0)
                break
            } else if (bodyDay.rise == null && hour == 24) {
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

    fun calcPosition(body: Body, location: LatitudeLongitude, time: Long): Position {
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

        val d = dayNumber(dateTime)
        val obliquityOfEliptic = obliquityOfEcliptic(julianCent(dateTime))

        // Geocentric orbital components.
        val N = norm360(125.1228 - 0.0529538083 * d) // (Long asc. node)
        val i = norm360(5.1454) // (Inclination)
        val w = norm360(318.0634 + 0.1643573223 * d) // (Arg. of perigee)
        val a = 60.2666 // (Mean distance)
        val e = 0.054900 // (Eccentricity)
        val M = norm360(115.3654 + 13.0649929509 * d) // (Mean anomaly)

        // Calculate eccentric anomaly using iteration to produce accurate value.
        val E0 = M + 180 / PI * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)))
        var E1 = java.lang.Double.MAX_VALUE
        var loopCount = 0
        while (abs(E1 - E0) > 0.005 && loopCount < 10) {
            E1 = E0 - (E0 - 180 / PI * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)))
            loopCount++
        }

        // Rectangular (x,y) coordinates in the plane of the orbit
        val planarX = a * (cos(toRadians(E1)) - e)
        val planarY = a * sqrt(1 - e * e) * sin(toRadians(E1))

        // Convert rectangular coordinates to distance and true anomaly.
        val geoR = sqrt(planarX * planarX + planarY * planarY) // (Earth radii)
        val trueAnomaly = norm360(toDegrees(atan2(toRadians(planarY), toRadians(planarX)))) // (Degrees)

        // Ecliptic geocentric rectangular coordinates
        var geoRectEclip = doubleArrayOf(geoR * (cos(toRadians(N)) * cos(toRadians(trueAnomaly + w)) - sin(toRadians(N)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), geoR * (sin(toRadians(N)) * cos(toRadians(trueAnomaly + w)) + cos(toRadians(N)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), geoR * sin(toRadians(trueAnomaly + w)) * sin(toRadians(i)))

        val geoRLonLatEclip = rectangularToSpherical(geoRectEclip)

        // Sun values
        val ws = norm360(282.9404 + 4.70935E-5 * d) // (longitude of perihelion)
        val Ms = norm360(356.0470 + 0.9856002585 * d) // (mean anomaly)

        val Ls = norm360(ws + Ms)
        val Lm = norm360(N + w + M)
        val D = norm360(Lm - Ls)
        val F = norm360(Lm - N)

        // Apply lunar orbit perturbations
        geoRLonLatEclip[1] = (((geoRLonLatEclip[1] - 1.274 * sin(toRadians(M - 2 * D)) + 0.658 * sin(toRadians(2 * D))
                - 0.186 * sin(toRadians(Ms))
                - 0.059 * sin(toRadians(2 * M - 2 * D))
                - 0.057 * sin(toRadians(M - 2 * D + Ms)))
                + 0.053 * sin(toRadians(M + 2 * D))
                + 0.046 * sin(toRadians(2 * D - Ms))
                + 0.041 * sin(toRadians(M - Ms)))
                - 0.035 * sin(toRadians(D))
                - 0.031 * sin(toRadians(M + Ms))
                - 0.015 * sin(toRadians(2 * F - 2 * D))) + 0.011 * sin(toRadians(M - 4 * D))
        geoRLonLatEclip[2] = ((geoRLonLatEclip[2]
                - 0.173 * sin(toRadians(F - 2 * D))
                - 0.055 * sin(toRadians(M - F - 2 * D))
                - 0.046 * sin(toRadians(M + F - 2 * D)))
                + 0.033 * sin(toRadians(F + 2 * D))
                + 0.017 * sin(toRadians(2 * M + F)))
        geoRLonLatEclip[0] = (geoRLonLatEclip[0]
                - 0.58 * cos(toRadians(M - 2 * D))
                - 0.46 * cos(toRadians(2 * D)))

        // Convert perturbed ecliptic lat and lon back into geo ecliptic rectangular coords.
        geoRectEclip = sphericalToRectangular(geoRLonLatEclip)

        // Rotate ecliptic rectangular coordinates to equatorial, then convert to spherical for RA and Dec.
        val geoRectEquat = eclipticToEquatorial(geoRectEclip, obliquityOfEliptic)
        val geoRRADec = rectangularToSpherical(geoRectEquat)

        val topoRRADec = geoToTopo(geoRRADec, location, dateTime)
        val topoAzEl = raDecToAzEl(topoRRADec, location, dateTime)

        val position = Position()
        position.timestamp = dateTime.timeInMillis
        position.azimuth = topoAzEl[0]
        position.appElevation = refractionCorrection(topoAzEl[1])
        return position
    }

    private fun calcPlanetPositionInternal(body: Body, location: LatitudeLongitude, dateTime: Calendar): Position {

        val d = dayNumber(dateTime)
        val obliquityOfEliptic = obliquityOfEcliptic(julianCent(dateTime))

        val N: Double
        val i: Double
        val w: Double
        val a: Double
        val e: Double
        val M: Double

        // Calculate heliocentric orbital components.
        when (body) {
            Body.MERCURY -> {
                N = norm360(48.3313 + 3.24587E-5 * d)
                i = norm360(7.0047 + 5.00E-8 * d)
                w = norm360(29.1241 + 1.01444E-5 * d)
                a = 0.387098
                e = 0.205635 + 5.59E-10 * d
                M = norm360(168.6562 + 4.0923344368 * d)
            }
            Body.VENUS -> {
                N = norm360(76.6799 + 2.46590E-5 * d)
                i = norm360(3.3946 + 2.75E-8 * d)
                w = norm360(54.8910 + 1.38374E-5 * d)
                a = 0.723330
                e = 0.006773 - 1.302E-9 * d
                M = norm360(48.0052 + 1.6021302244 * d)
            }
            Body.MARS -> {
                N = norm360(49.5574 + 2.11081E-5 * d)
                i = norm360(1.8497 - 1.78E-8 * d)
                w = norm360(286.5016 + 2.92961E-5 * d)
                a = 1.523688
                e = 0.093405 + 2.516E-9 * d
                M = norm360(18.6021 + 0.5240207766 * d)
            }
            Body.JUPITER -> {
                N = norm360(100.4542 + 2.76854E-5 * d)
                i = norm360(1.3030 - 1.557E-7 * d)
                w = norm360(273.8777 + 1.64505E-5 * d)
                a = 5.20256
                e = 0.048498 + 4.469E-9 * d
                M = norm360(19.8950 + 0.0830853001 * d)
            }
            Body.SATURN -> {
                N = norm360(113.6634 + 2.38980E-5 * d)
                i = norm360(2.4886 - 1.081E-7 * d)
                w = norm360(339.3939 + 2.97661E-5 * d)
                a = 9.55475
                e = 0.055546 - 9.499E-9 * d
                M = norm360(316.9670 + 0.0334442282 * d)
            }
            Body.URANUS -> {
                N = norm360(74.0005 + 1.3978E-5 * d)
                i = norm360(0.7733 + 1.9E-8 * d)
                w = norm360(96.6612 + 3.0565E-5 * d)
                a = 19.18171 - 1.55E-8 * d
                e = 0.047318 + 7.45E-9 * d
                M = norm360(142.5905 + 0.011725806 * d)
            }
            Body.NEPTUNE -> {
                N = norm360(131.7806 + 3.0173E-5 * d)
                i = norm360(1.7700 - 2.55E-7 * d)
                w = norm360(272.8461 - 6.027E-6 * d)
                a = 30.05826 + 3.313E-8 * d
                e = 0.008606 + 2.15E-9 * d
                M = norm360(260.2471 + 0.005995147 * d)
            }
            else -> throw IllegalArgumentException("Unrecognised body: " + body)
        }

        // Calculate eccentric anomaly using iteration to produce accurate value.
        val E0 = M + 180 / PI * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)))
        var E1 = java.lang.Double.MAX_VALUE
        var loopCount = 0
        while (abs(E1 - E0) > 0.005 && loopCount < 10) {
            E1 = E0 - (E0 - 180 / PI * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)))
            loopCount++
        }

        // Rectangular (x,y) coordinates in the plane of the orbit
        val planarX = a * (cos(toRadians(E1)) - e)
        val planarY = a * sqrt(1 - e * e) * sin(toRadians(E1))

        // Convert rectangular coordinates to distance and true anomaly.
        val helioR = sqrt(planarX * planarX + planarY * planarY) // (Earth radii)
        val trueAnomaly = norm360(toDegrees(atan2(toRadians(planarY), toRadians(planarX)))) // (Degrees)

        // Ecliptic heliocentric rectangular coordinates
        var helioRectEclip = doubleArrayOf(helioR * (cos(toRadians(N)) * cos(toRadians(trueAnomaly + w)) - sin(toRadians(N)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), helioR * (sin(toRadians(N)) * cos(toRadians(trueAnomaly + w)) + cos(toRadians(N)) * sin(toRadians(trueAnomaly + w)) * cos(toRadians(i))), helioR * sin(toRadians(trueAnomaly + w)) * sin(toRadians(i)))

        val helioRLonLatEclip = rectangularToSpherical(helioRectEclip)

        // Apply the planet's perturbations.
        val Mju = norm360(19.8950 + 0.0830853001 * d)
        val Msa = norm360(316.9670 + 0.0334442282 * d)
        val Mur = norm360(142.5905 + 0.011725806 * d)
        when (body) {
            Body.JUPITER -> helioRLonLatEclip[1] = ((helioRLonLatEclip[1]
                    - 0.332 * sin(toRadians(2 * Mju - 5 * Msa - 67.6))
                    - 0.056 * sin(toRadians(2 * Mju - 2 * Msa + 21))) + 0.042 * sin(toRadians(3 * Mju - 5 * Msa + 21)) - 0.036 * sin(toRadians(Mju - 2 * Msa))
                    + 0.022 * cos(toRadians(Mju - Msa))
                    + 0.023 * sin(toRadians(2 * Mju - 3 * Msa + 52))) - 0.016 * sin(toRadians(Mju - 5 * Msa - 69.0))
            Body.SATURN -> {
                helioRLonLatEclip[1] = (helioRLonLatEclip[1] + 0.812 * sin(toRadians(2 * Mju - 5 * Msa - 67.6)) - 0.229 * cos(toRadians(2 * Mju - 4 * Msa - 2.0))
                        + 0.119 * sin(toRadians(Mju - 2 * Msa - 3.0))
                        + 0.046 * sin(toRadians(2 * Mju - 6 * Msa - 69.0))
                        + 0.014 * sin(toRadians(Mju - 3 * Msa + 32)))
                helioRLonLatEclip[2] = helioRLonLatEclip[2] - 0.020 * cos(toRadians(2 * Mju - 4 * Msa - 2.0)) + 0.018 * sin(toRadians(2 * Mju - 6 * Msa - 49.0))
            }
            Body.URANUS -> helioRLonLatEclip[1] = (helioRLonLatEclip[1]
                    + 0.040 * sin(toRadians(Msa - 2 * Mur + 6))
                    + 0.035 * sin(toRadians(Msa - 3 * Mur + 33))) - 0.015 * sin(toRadians(Mju - Mur + 20))
        }

        // Convert perturbed ecliptic lat and lon back into helio ecliptic rectangular coords.
        helioRectEclip = sphericalToRectangular(helioRLonLatEclip)

        val geoRectEclip = helioToGeo(helioRectEclip, d)
        val geoRectEquat = eclipticToEquatorial(geoRectEclip, obliquityOfEliptic)
        val geoRRADec = rectangularToSpherical(geoRectEquat)
        val geoAzEl = raDecToAzEl(geoRRADec, location, dateTime)

        val position = Position()
        position.timestamp = dateTime.timeInMillis
        position.azimuth = geoAzEl[0]
        position.appElevation = refractionCorrection(geoAzEl[1])
        return position
    }

    private fun refractionCorrection(elevation: Double): Double {

        var refractionCorrection: Double
        if (elevation > 85.0) {
            refractionCorrection = 0.0
        } else {
            val te = tan(toRadians(elevation))
            if (elevation > 5.0) {
                refractionCorrection = 58.1 / te - 0.07 / (te * te * te) + 0.000086 / (te * te * te * te * te)
            } else if (elevation > -0.575) {
                refractionCorrection = 1735.0 + elevation * (-518.2 + elevation * (103.4 + elevation * (-12.79 + elevation * 0.711)))
            } else {
                refractionCorrection = -20.774 / te
            }
            refractionCorrection = refractionCorrection / 3600.0
        }
        return elevation + refractionCorrection

    }

    private fun dayNumber(dateTime: Calendar): Double {
        val year = dateTime.get(Calendar.YEAR)
        val month = dateTime.get(Calendar.MONTH) + 1
        val day = dateTime.get(Calendar.DAY_OF_MONTH)
        val fraction = dateTime.get(Calendar.HOUR_OF_DAY) / 24.0 + dateTime.get(Calendar.MINUTE) / (60.0 * 24.0) + dateTime.get(Calendar.SECOND) / (60.0 * 60.0 * 24.0)
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

    private fun eclipticToEquatorial(xyzEclip: DoubleArray, o: Double): DoubleArray {
        val xEquat = xyzEclip[0]
        val yEquat = xyzEclip[1] * cos(toRadians(o)) - xyzEclip[2] * sin(toRadians(o))
        val zEquat = xyzEclip[1] * sin(toRadians(o)) + xyzEclip[2] * cos(toRadians(o))
        return doubleArrayOf(xEquat, yEquat, zEquat)
    }

    private fun raDecToAzEl(rRaDecl: DoubleArray, location: LatitudeLongitude, dateTime: Calendar): DoubleArray {

        val LSTh = localSiderealTimeHours(location, dateTime)
        val RAh = rRaDecl[1] / 15.0

        // Hour angle
        val HAd = 15 * norm24(LSTh - RAh)

        val x = cos(toRadians(HAd)) * cos(toRadians(rRaDecl[2]))
        val y = sin(toRadians(HAd)) * cos(toRadians(rRaDecl[2]))
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
        val LST = localSiderealTimeHours(location, dateTime)
        val HA = norm360(LST * 15 - rRaDec[1])
        val g = toDegrees(atan(tan(toRadians(gclat)) / cos(toRadians(HA))))

        val topRA = rRaDec[1] - mpar * rho * cos(toRadians(gclat)) * sin(toRadians(HA)) / cos(toRadians(rRaDec[2]))
        val topDecl = rRaDec[2] - mpar * rho * sin(toRadians(gclat)) * sin(toRadians(g - rRaDec[2])) / sin(toRadians(g))

        return doubleArrayOf(rRaDec[0], topRA, topDecl)
    }

    private fun helioToGeo(helioRectEclip: DoubleArray, d: Double): DoubleArray {

        val w = norm360(282.9404 + 4.70935E-5 * d) // (longitude of perihelion)
        val e = 0.016709 - 1.151E-9 * d // (eccentricity)
        val M = norm360(356.0470 + 0.9856002585 * d) // (mean anomaly)
        val E = M + 180 / PI * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)))
        var x = cos(toRadians(E)) - e
        var y = sin(toRadians(E)) * sqrt(1 - e * e)
        val r = sqrt(x * x + y * y)
        val v = toDegrees(atan2(y, x))
        val lon = norm360(v + w)
        x = r * cos(toRadians(lon))
        y = r * sin(toRadians(lon))
        val z = 0.0

        return doubleArrayOf(helioRectEclip[0] + x, helioRectEclip[1] + y, helioRectEclip[2] + z)

    }

    private fun localSiderealTimeHours(location: LatitudeLongitude, dateTime: Calendar): Double {

        val d = dayNumber(dateTime)
        val UT = dateTime.get(Calendar.HOUR_OF_DAY).toDouble() + dateTime.get(Calendar.MINUTE) / 60.0 + dateTime.get(Calendar.SECOND) / 3600.0

        val ws = norm360(282.9404 + 4.70935E-5 * d) // (longitude of perihelion)
        val Ms = norm360(356.0470 + 0.9856002585 * d) // (mean anomaly)
        val Ls = norm360(ws + Ms)

        val GMST0 = Ls / 15 + 12.0

        return norm24(GMST0 + UT + location.longitude.doubleValue / 15.0)

    }

    private fun obliquityOfEcliptic(t: Double): Double {
        val seconds = 21.448 - t * (46.8150 + t * (0.00059 - t * 0.001813))
        return 23.0 + (26.0 + seconds / 60.0) / 60.0
    }

    private fun norm360(degrees: Double): Double {
        var degrees = degrees
        while (degrees < 0.0) {
            degrees += 360.0
        }
        while (degrees > 360.0) {
            degrees -= 360.0
        }
        return degrees
    }

    private fun norm24(hours: Double): Double {
        var hours = hours
        while (hours < 0.0) {
            hours += 24.0
        }
        while (hours > 24.0) {
            hours -= 24.0
        }
        return hours
    }

    private fun sign(value: Double, plus: Double): Int {
        return if (value + plus < 0.0) -1 else 1
    }

    private fun binarySearchNoon(body: Body, location: LatitudeLongitude, initialSector: Int, initialTimestamp: Long, intervalMs: Int, searchDirection: Int, depth: Int): Position {
        val thisTimestamp = initialTimestamp + searchDirection * intervalMs
        val thisPosition = calcPosition(body, location, thisTimestamp)
        val thisSector = sector(thisPosition.azimuth)
        //Calendar thisCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //thisCal.setTimeInMillis(thisTimestamp);
        //System.out.println("binary " + initialSector + ", " + initialTimestamp + " (" + thisCal.get(Calendar.HOUR_OF_DAY) + ":" + thisCal.get(Calendar.MINUTE) + ":" + thisCal.get(Calendar.SECOND) + ", " + intervalMs + ", " + searchDirection + " : el " + thisPosition.getAzimuth());

        if (intervalMs < 15000 || depth > 10) {
            return thisPosition
        }

        return if (thisSector == initialSector) {
            binarySearchNoon(body, location, thisSector, thisTimestamp, intervalMs / 2, searchDirection, depth + 1)
        } else {
            binarySearchNoon(body, location, thisSector, thisTimestamp, intervalMs / 2, -searchDirection, depth + 1)
        }

    }

    //
    //	private static String hms(double degrees) {
    //		double hours = degrees/15d;
    //		int iHours = (int)Math.floor(hours);
    //		hours = hours - iHours;
    //		double minutes = hours*60d;
    //		int iMinutes = (int)Math.floor(minutes);
    //		minutes = minutes - iMinutes;
    //		double seconds = minutes*60d;
    //		int iSeconds = (int)Math.floor(seconds);
    //
    //		return iHours + "h " + iMinutes + "'" + iSeconds + "\"";
    //	}
    //
    //	private static String dms(double degrees) {
    //		int iDegrees = (int)Math.floor(degrees);
    //		degrees = degrees - iDegrees;
    //		double minutes = degrees*60d;
    //		int iMinutes = (int)Math.floor(minutes);
    //		minutes = minutes - iMinutes;
    //		double seconds = minutes*60d;
    //		int iSeconds = (int)Math.floor(seconds);
    //
    //		return iDegrees + "�" + iMinutes + "'" + iSeconds + "\"";
    //	}
    //
    //
    //
    //	public static void main(String[] args) throws Exception {
    //
    //		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Europe/London"));
    //		cal.set(Calendar.YEAR, 1990);
    //		cal.set(Calendar.MONTH, Calendar.APRIL);
    //		cal.set(Calendar.DAY_OF_MONTH, 19);
    //		cal.set(Calendar.HOUR_OF_DAY, 1);
    //		cal.set(Calendar.MINUTE, 0);
    //		cal.set(Calendar.SECOND, 0);
    //
    //		double lastEl = 0;
    //
    //		for (int i = 0; i < 1; i++) {
    //			Position position = calcPosition(Body.MOON, new LatitudeLongitude("600000N 0150000E"), cal);
    //
    //			System.out.println("-------------------");
    //			System.out.println("Topo RA: " + position.getTopoRightAscension());
    //			System.out.println("Topo Dec: " + position.getTopoDeclination());
    //			System.out.println("Geo RA: " + position.getGeoRightAscension() + " (" + hms(position.getGeoRightAscension()) + ")");
    //			System.out.println("Geo Dec: " + position.getGeoDeclination() + " (" + dms(position.getGeoDeclination()));
    //			System.out.println("Az: "+ position.getAzimuth());
    //			System.out.println("El: "+ position.getAppElevation());
    //
    //			System.out.println("geo eclip lon: "+ position.getGeoEclipticLongitude());
    //			System.out.println("geo eclip lat: " + position.getGeoEclipticLatitude());
    //			System.out.println("geo r: " + position.getGeoDistance());
    //			System.out.println("rkm: " + position.getGeoDistanceKm());
    //
    //			System.out.println("Helio r: " + position.getHelioDistance());
    //			System.out.println("Helio lat:" + position.getHelioEclipticLatitude());
    //			System.out.println("Helio lon: " + position.getHelioEclipticLongitude());
    //
    ////			System.out.println("dec: " + position.getDeclination());
    ////
    //			if (position.getAppElevation() > -0.5 && position.getAppElevation() < 0.5) {
    //				System.out.println(cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE) + "  " + position.getAppElevation() + ", " + position.getAzimuth());
    //			}
    ////
    ////
    //	//		if (i > 0 && sign(position.getElevation()) != sign(lastEl)) {
    //	//			System.out.println("event at " + cal.get(Calendar.HOUR_OF_DAY) + ":" + cal.get(Calendar.MINUTE));
    //
    //	//		}
    //
    //
    //		//	lastEl = position.getElevation();
    //			cal.add(Calendar.MINUTE, 1);
    //		}
    //
    //	}


}
