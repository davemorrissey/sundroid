package uk.co.sundroid.util.astro.math

import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MILLISECOND
import java.util.Calendar.MINUTE
import java.util.Calendar.MONTH
import java.util.Calendar.SECOND
import java.util.Calendar.YEAR

import java.util.Calendar
import java.util.TimeZone

import uk.co.sundroid.util.astro.Position
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.TwilightType
import uk.co.sundroid.util.location.LatitudeLongitude

object SunCalculator {
    
    enum class Event {
        RISESET,
        NOON,
        CIVIL,
        NAUTICAL,
        ASTRONOMICAL,
        GOLDENHOUR
    }

    /**
     * Convert radian angle to degrees
     */
    private fun radToDeg(angleRad: Double): Double {
        return (180.0 * angleRad / Math.PI)
    }

    /**
     * Convert degree angle to radians
     */
    private fun degToRad(angleDeg: Double): Double {
        return (Math.PI * angleDeg / 180.0)
    }

    /**
     * Julian day from calendar day.
     * @param year 4-digit year
     * @param month January = 1
     * @param day 1-31
     * @return the Julian day corresponding to the start of the day. Fractional days should be added later.
     */
    private fun calcJD(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val A = Math.floor(y/100.0)
        val B = 2 - A + Math.floor(A/4)
        return Math.floor(365.25*(year + 4716)) + Math.floor(30.6001*(month+1)) + day + B - 1524.5
    }

    /**
     * Convert Julian Day to centuries since J2000.0
     * @param jd Julian day
     * @return the T value corresponding to the Julian Day
     */
    private fun calcTimeJulianCent(jd: Double): Double {
        return (jd - 2451545.0)/36525.0
    }

    /**
     * Convert centuries since J2000.0 to Julian Day
     * @param t number of Julian centuries since J2000.0
     * @return the Julian Day corresponding to the t value
     */
    private fun calcJDFromJulianCent(t: Double): Double {
        return t * 36525.0 + 2451545.0
    }

    /**
     * Calculate the Geometric Mean Longitude of the Sun
     * @param t number of Julian centuries since J2000.0
     * @return the Geometric Mean Longitude of the Sun in degrees
     */
    private fun calcGeomMeanLongSun(t: Double): Double {
        var L0 = 280.46646 + t * (36000.76983 + 0.0003032 * t)
        while (L0 > 360.0) {
            L0 -= 360.0
        }
        while (L0 < 0.0) {
            L0 += 360.0
        }
        return L0        // in degrees
    }

    /**
     * Calculate the Geometric Mean Anomaly of the Sun
     * @param t number of Julian centuries since J2000.0
     * @return the Geometric Mean Anomaly of the Sun in degrees
     */
    private fun calcGeomMeanAnomalySun(t: Double): Double {
        return 357.52911 + t * (35999.05029 - 0.0001537 * t) // in degrees
    }

	/**
     * Calculate the eccentricity of earth's orbit
     * @param t number of Julian centuries since J2000.0
     * @return the unitless eccentricity
     */
    private fun calcEccentricityEarthOrbit(t: Double): Double {
        return 0.016708634 - t * (0.000042037 + 0.0000001267 * t) // unitless
    }

	/**
     * Calculate the equation of center for the sun
     * @param t number of Julian centuries since J2000.0
     * @return in degrees
     */
    private fun calcSunEqOfCenter(t: Double): Double {
        val m = calcGeomMeanAnomalySun(t)

        val mrad = degToRad(m)
        val sinm = Math.sin(mrad)
        val sin2m = Math.sin(mrad+mrad)
        val sin3m = Math.sin(mrad+mrad+mrad)

        return sinm * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin2m * (0.019993 - 0.000101 * t) + sin3m * 0.000289
    }

    /**
     * Calculate the true longitude of the sun
     * @param t number of Julian centuries since J2000.0
     * @return sun's true longitude in degrees
     */
    private fun calcSunTrueLong(t: Double): Double {
        val l0 = calcGeomMeanLongSun(t)
        val c = calcSunEqOfCenter(t)
        return l0 + c
    }

    /**
     * Calculate the apparent longitude of the sun
     * @param t number of Julian centuries since J2000.0
     * @return sun's apparent longitude in degrees
     */
    private fun calcSunApparentLong(t: Double): Double {
        val o = calcSunTrueLong(t)
        val omega = 125.04 - 1934.136 * t
        return o - 0.00569 - 0.00478 * Math.sin(degToRad(omega))
    }

    /**
     * Calculate the mean obliquity of the ecliptic
     * @param t number of Julian centuries since J2000.0
     * @return mean obliquity in degrees
     */
    private fun calcMeanObliquityOfEcliptic(t: Double): Double {
        val seconds = 21.448 - t*(46.8150 + t*(0.00059 - t*(0.001813)))
        return 23.0 + (26.0 + (seconds/60.0))/60.0
    }

	/**
     * Calculate the corrected obliquity of the ecliptic
     * @param t number of Julian centuries since J2000.0
     * @return corrected obliquity in degrees
     */
    private fun calcObliquityCorrection(t: Double): Double {
        val e0 = calcMeanObliquityOfEcliptic(t)

        val omega = 125.04 - 1934.136 * t
        return e0 + 0.00256 * Math.cos(degToRad(omega)) // in degrees
    }

	/**
     * Calculate the declination of the sun
     * @param t number of Julian centuries since J2000.0
     * @return sun's declination in degrees
     */
    private fun calcSunDeclination(t: Double): Double {
        val e = calcObliquityCorrection(t)
        val lambda = calcSunApparentLong(t)

        val sint = Math.sin(degToRad(e)) * Math.sin(degToRad(lambda))
        return radToDeg(Math.asin(sint)) // theta in degrees
    }

	/**
     * Calculate the difference between true solar time and mean solar time
     * @param t number of Julian centuries since J2000.0
     * @return equation of time in minutes of time
     */
    private fun calcEquationOfTime(t: Double): Double {
        val epsilon = calcObliquityCorrection(t)
        val l0 = calcGeomMeanLongSun(t)
        val e = calcEccentricityEarthOrbit(t)
        val m = calcGeomMeanAnomalySun(t)

        var y = Math.tan(degToRad(epsilon)/2.0)
        y *= y

        val sin2l0 = Math.sin(2.0 * degToRad(l0))
        val sinm   = Math.sin(degToRad(m))
        val cos2l0 = Math.cos(2.0 * degToRad(l0))
        val sin4l0 = Math.sin(4.0 * degToRad(l0))
        val sin2m  = Math.sin(2.0 * degToRad(m))

        val Etime = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0
                - 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m

        return radToDeg(Etime)*4.0    // in minutes of time
    }

    private fun calcHourAngleUp(lat: Double, solarDec: Double, elevation: Double): Double {
        val latRad = degToRad(lat)
        val sdRad = degToRad(solarDec)
        return (Math.acos(Math.cos(degToRad(elevation))/(Math.cos(latRad)*Math.cos(sdRad))-Math.tan(latRad) * Math.tan(sdRad)))
    }

    private fun calcHourAngleDown(lat: Double, solarDec: Double, elevation: Double): Double {
        val latRad = degToRad(lat)
        val sdRad  = degToRad(solarDec)
        return -(Math.acos(Math.cos(degToRad(elevation))/(Math.cos(latRad)*Math.cos(sdRad))-Math.tan(latRad) * Math.tan(sdRad)))
    }

    private fun calcUpUTC(JD: Double, latitude: Double, longitude: Double, elevation: Double): Double {
        val t = calcTimeJulianCent(JD)
        val noonmin = calcSolNoonUTC(t, longitude)
        val tnoon = calcTimeJulianCent (JD+noonmin/1440.0)
        var eqTime = calcEquationOfTime(tnoon)
        var solarDec = calcSunDeclination(tnoon)
        var hourAngle = calcHourAngleUp(latitude, solarDec, elevation)
        var delta = longitude - radToDeg(hourAngle)
        var timeDiff = 4 * delta
        var timeUTC = 720 + timeDiff - eqTime
        val newt = calcTimeJulianCent(calcJDFromJulianCent(t) + timeUTC/1440.0)
        eqTime = calcEquationOfTime(newt)
        solarDec = calcSunDeclination(newt)
        hourAngle = calcHourAngleUp(latitude, solarDec, elevation)
        delta = longitude - radToDeg(hourAngle)
        timeDiff = 4 * delta
        timeUTC = 720 + timeDiff - eqTime
        return timeUTC
    }

	/**
     * Calculate the Universal Coordinated Time (UTC) of solar noon for the given day at the given location on earth
     * @oaram t number of Julian centuries since J2000.0
     * @param longitude longitude of observer in degrees
     * @return time in minutes from zero Z
     */
    private fun calcSolNoonUTC(t: Double, longitude: Double): Double {
        // First pass uses approximate solar noon to calculate eqtime
        val tnoon = calcTimeJulianCent(calcJDFromJulianCent(t) + longitude/360.0)
        var eqTime = calcEquationOfTime(tnoon)
        var solNoonUTC = 720 + (longitude * 4) - eqTime // min

        val newt = calcTimeJulianCent(calcJDFromJulianCent(t) -0.5 + solNoonUTC/1440.0)

        eqTime = calcEquationOfTime(newt)
        // var solarNoonDec = calcSunDeclination(newt)
        solNoonUTC = 720 + (longitude * 4) - eqTime // min
        
        return solNoonUTC
    }

	/**
     * calculate the Universal Coordinated Time (UTC) of sunset
     *            for the given day at the given location on earth
     * @param JD julian day
     * @param latitude latitude of observer in degrees
     * @param longitude longitude of observer in degrees
     * @return time in minutes from zero Z
     */
    private fun calcDownUTC(JD: Double, latitude: Double, longitude: Double, elevation: Double): Double {
        val t = calcTimeJulianCent(JD)

        // Find the time of solar noon at the location, and use that declination. This is better than
        // start of the Julian day

        val noonmin = calcSolNoonUTC(t, longitude)
        val tnoon = calcTimeJulianCent (JD+noonmin/1440.0)

        // First calculates sunrise and approx length of day

        var eqTime = calcEquationOfTime(tnoon)
        var solarDec = calcSunDeclination(tnoon)
        var hourAngle = calcHourAngleDown(latitude, solarDec, elevation)

        var delta = longitude - radToDeg(hourAngle)
        var timeDiff = 4 * delta
        var timeUTC = 720 + timeDiff - eqTime

        // first pass used to include fractional day in gamma calc

        val newt = calcTimeJulianCent(calcJDFromJulianCent(t) + timeUTC/1440.0)
        eqTime = calcEquationOfTime(newt)
        solarDec = calcSunDeclination(newt)
        hourAngle = calcHourAngleDown(latitude, solarDec, elevation)

        delta = longitude - radToDeg(hourAngle)
        timeDiff = 4 * delta
        timeUTC = 720 + timeDiff - eqTime // in minutes

        return timeUTC
    }

    /**
     * Create a calendar from midnight with added minutes in a given timezone.
     * @param dateMidnight Calendar for midnight of UTC date. Must be in UTC.
     * @param timeZone Time zone to apply after the calendar is set.
     * @param minutes Minutes to add.
     */
    private fun createCalendar(dateMidnight: Calendar, minutes: Double, timeZone: TimeZone): Calendar {
    
        val floatHour = minutes / 60.0
        var hour = Math.floor(floatHour).toInt()
        val floatMinute = 60.0 * (floatHour - Math.floor(floatHour))
        var minute = Math.floor(floatMinute).toInt()
        val floatSec = 60.0 * (floatMinute - Math.floor(floatMinute))
        val second = Math.floor(floatSec + 0.5).toInt()
        var addDays = 0

        if (minute >= 60) {
            minute -= 60
            hour++
        }
        
        while (hour > 23) {
            hour -= 24
            addDays++
        }
        
        while (hour < 0) {
            hour += 24
            addDays--
        }
    
        val dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateTime.setTimeInMillis(dateMidnight.getTimeInMillis())
        dateTime.set(HOUR_OF_DAY, hour)
        dateTime.set(MINUTE, minute)
        dateTime.set(SECOND, second)
        dateTime.add(DAY_OF_MONTH, addDays)
        dateTime.getTimeInMillis() // Prompt recalc
        dateTime.setTimeZone(timeZone)
        return dateTime
    
    }

    /**
     * Calculate sunrise, sunset, dawns, dusks and durations for a given date.
     */
    fun calcDay(location: LatitudeLongitude, dateMidnight: Calendar, vararg events: Event): SunDay {

        var latitude = location.latitude.doubleValue
        val longitude = -location.longitude.doubleValue
        
        val year = dateMidnight.get(YEAR)
        val month = dateMidnight.get(MONTH) + 1
        val day = dateMidnight.get(DAY_OF_MONTH)
        val dateMidnightUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateMidnightUtc.set(year, month - 1, day, 0, 0, 0)
        dateMidnightUtc.set(MILLISECOND, 0)

        if ((latitude >= -90) && (latitude < -89)) {
            latitude = -89.0
        }
        if ((latitude <= 90) && (latitude > 89)) {
            latitude = 89.0
        }
        
        val julianDay = calcJD(year, month, day)
        val julianCent = calcTimeJulianCent(julianDay)

        val sunDay = SunDay()

        val solarNoon = calcSolNoonUTC(julianCent, longitude)
        val transit = createCalendar(dateMidnightUtc, solarNoon, dateMidnight.getTimeZone())
        val solarNoonPosition = calcPosition(location, transit)
        sunDay.transit = transit
        sunDay.transitAppElevation = solarNoonPosition.appElevation

        if (events.isEmpty() || events.contains(Event.RISESET)) {
            val sunrise = calcUpUTC(julianDay, latitude, longitude, 90.833)
            if (!sunrise.isNaN()) {
                sunDay.rise = createCalendar(dateMidnightUtc, sunrise, dateMidnight.getTimeZone())
            }
            val sunset = calcDownUTC(julianDay, latitude, longitude, 90.833)
            if (!sunset.isNaN()) {
                sunDay.set = createCalendar(dateMidnightUtc, sunset, dateMidnight.getTimeZone())
            }
            val rise = sunDay.rise
            if (rise != null) {
                val risePosition = calcPosition(location, rise)
                sunDay.riseAzimuth = risePosition.azimuth
            }
            val set = sunDay.set
            if (set != null) {
                val setPosition = calcPosition(location, set)
                sunDay.setAzimuth = setPosition.azimuth
            }
            if (sunDay.rise != null && sunDay.set != null) {
                sunDay.uptimeHours = (sunset - sunrise)/60.0
            } else if (sunDay.rise == null || sunDay.set == null) {
                sunDay.uptimeHours = 24.0
            }
            if (sunDay.rise == null && sunDay.set == null) {
                if (solarNoonPosition.appElevation > 0) {
                    sunDay.riseSetType = RiseSetType.RISEN
                    sunDay.uptimeHours = 24.0
                } else {
                    sunDay.riseSetType = RiseSetType.SET
                    sunDay.uptimeHours = 0.0
                }
            }
            
        }

        if (events.isEmpty() || events.contains(Event.CIVIL)) {
            val civDusk = calcDownUTC(julianDay, latitude, longitude, 96.0)
            if (!civDusk.isNaN()) {
                sunDay.civDusk = createCalendar(dateMidnightUtc, civDusk, dateMidnight.getTimeZone())
            }
            val civDawn = calcUpUTC(julianDay, latitude, longitude, 96.0)
            if (!civDawn.isNaN()) {
                sunDay.civDawn = createCalendar(dateMidnightUtc, civDawn, dateMidnight.getTimeZone())
            }
            if (sunDay.civDawn == null && sunDay.civDusk == null) {
                if (solarNoonPosition.appElevation > -5.9) {
                    sunDay.civType = TwilightType.LIGHT
                    sunDay.civHours = 24.0
                } else {
                    sunDay.civType = TwilightType.DARK
                    sunDay.civHours = 0.0
                }
            } else if (sunDay.civDawn == null || sunDay.civDusk == null) {
                sunDay.civHours = 24.0
            } else {
                sunDay.civHours = (civDusk - civDawn) / 60
            }
        }
        
        if (events.isEmpty() || events.contains(Event.NAUTICAL)) {
            val ntcDawn = calcUpUTC(julianDay, latitude, longitude, 102.0)
            if (!ntcDawn.isNaN()) {
                sunDay.ntcDawn = createCalendar(dateMidnightUtc, ntcDawn, dateMidnight.getTimeZone())
            }
            val ntcDusk = calcDownUTC(julianDay, latitude, longitude, 102.0)
            if (!ntcDusk.isNaN()) {
                sunDay.ntcDusk = createCalendar(dateMidnightUtc, ntcDusk, dateMidnight.getTimeZone())
            }
            if (sunDay.ntcDawn == null && sunDay.ntcDusk == null) {
                if (solarNoonPosition.appElevation > -11.9) {
                    sunDay.ntcType = TwilightType.LIGHT
                    sunDay.ntcHours = 24.0
                } else {
                    sunDay.ntcType = TwilightType.DARK
                    sunDay.ntcHours = 0.0
                }
            } else if (sunDay.ntcDawn == null || sunDay.ntcDusk == null) {
                sunDay.ntcHours = 24.0
            } else {
                sunDay.ntcHours = (ntcDusk - ntcDawn) / 60
            }
        }

        if (events.isEmpty() || events.contains(Event.ASTRONOMICAL)) {
            val astDawn = calcUpUTC(julianDay, latitude, longitude, 108.0)
            if (!astDawn.isNaN()) {
                sunDay.astDawn = createCalendar(dateMidnightUtc, astDawn, dateMidnight.getTimeZone())
            }
            val astDusk = calcDownUTC(julianDay, latitude, longitude, 108.0)
            if (!astDusk.isNaN()) {
                sunDay.astDusk = createCalendar(dateMidnightUtc, astDusk, dateMidnight.getTimeZone())
            }
            if (sunDay.astDawn == null && sunDay.astDusk == null) {
                if (solarNoonPosition.appElevation > -17.9) {
                    sunDay.astType = TwilightType.LIGHT
                    sunDay.astHours = 24.0
                } else {
                    sunDay.astType = TwilightType.DARK
                    sunDay.astHours = 0.0
                }
            } else if (sunDay.astDawn == null || sunDay.astDusk == null) {
                sunDay.astHours = 24.0
            } else {
                sunDay.astHours = (astDusk - astDawn) / 60
            }
        }
        
        if (events.isEmpty() || events.contains(Event.GOLDENHOUR)) {
            val ghEnd = calcUpUTC(julianDay, latitude, longitude, 84.0)
            if (!ghEnd.isNaN()) {
                sunDay.ghEnd = createCalendar(dateMidnightUtc, ghEnd, dateMidnight.getTimeZone())
            }
            val ghStart = calcDownUTC(julianDay, latitude, longitude, 84.0)
            if (!ghStart.isNaN()) {
                sunDay.ghStart = createCalendar(dateMidnightUtc, ghStart, dateMidnight.getTimeZone())
            }
            if (sunDay.ghEnd == null && sunDay.ghStart == null) {
                if (solarNoonPosition.appElevation > 6) {
                    sunDay.ghType = TwilightType.LIGHT
                    sunDay.ghHours = 24.0
                } else {
                    sunDay.ghType = TwilightType.DARK
                    sunDay.ghHours = 0.0
                }
            } else if (sunDay.ghEnd == null || sunDay.ghStart == null) {
                sunDay.ghHours = 24.0
            } else {
                sunDay.ghHours = (ghStart - ghEnd) / 60
            }
        }

        return sunDay
        
    }

    /**
     * Calculates the sun's position at a given time.
     */
    fun calcPosition(location: LatitudeLongitude, dateTime: Calendar): Position {
        var latitude = location.latitude.doubleValue
        val longitude = -location.longitude.doubleValue
        
        if ((latitude >= -90) && (latitude < -89.8)) {
            latitude = -89.8
        }
        if ((latitude <= 90) && (latitude > 89.8)) {
            latitude = 89.8
        }
        
        val dateTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateTimeUtc.setTimeInMillis(dateTime.getTimeInMillis())
        

        val timenow = dateTimeUtc.get(HOUR_OF_DAY) + dateTimeUtc.get(MINUTE)/60.0 + dateTimeUtc.get(SECOND)/3600.0
        
        val JD = (calcJD(dateTimeUtc.get(YEAR), dateTimeUtc.get(MONTH) + 1, dateTimeUtc.get(DAY_OF_MONTH)))
        val T = calcTimeJulianCent(JD + timenow/24.0)
        val solarDec = calcSunDeclination(T)
        val eqTime = calcEquationOfTime(T)

        val offsetHours = 0
        val solarTimeFix = eqTime - 4.0 * longitude + 60.0 * offsetHours
        var trueSolarTimeMins = dateTimeUtc.get(HOUR_OF_DAY) * 60.0 + dateTimeUtc.get(MINUTE) + dateTimeUtc.get(SECOND)/60.0 + solarTimeFix

        while (trueSolarTimeMins > 1440) {
            trueSolarTimeMins -= 1440
        }

        var hourAngle = trueSolarTimeMins / 4.0 - 180.0
        if (hourAngle < -180) {
            hourAngle += 360.0
        }

        val haRad = degToRad(hourAngle)

        var csz = Math.sin(degToRad(latitude)) *
            Math.sin(degToRad(solarDec)) +
            Math.cos(degToRad(latitude)) *
            Math.cos(degToRad(solarDec)) * Math.cos(haRad)
        if (csz > 1.0) {
            csz = 1.0
        } else if (csz < -1.0) {
            csz = -1.0
        }
        val zenith = radToDeg(Math.acos(csz))
        var azimuth: Double
        val azDenom = ( Math.cos(degToRad(latitude)) * Math.sin(degToRad(zenith)) )
        if (Math.abs(azDenom) > 0.001) {
            var azRad = (( Math.sin(degToRad(latitude)) *
                Math.cos(degToRad(zenith)) ) -
                Math.sin(degToRad(solarDec))) / azDenom
            if (Math.abs(azRad) > 1.0) {
                if (azRad < 0) {
                    azRad = -1.0
                } else {
                    azRad = 1.0
                }
            }

            azimuth = 180.0 - radToDeg(Math.acos(azRad))
            if (hourAngle > 0.0) {
                azimuth = -azimuth
            }
        } else {
            if (latitude > 0.0) {
                azimuth = 180.0
            } else {
                azimuth = 0.0
            }
        }
        if (azimuth < 0.0) {
            azimuth += 360.0
        }

        val sunPosition = Position()
        sunPosition.azimuth = azimuth
        sunPosition.appElevation = 90.0 - zenith
        return sunPosition
    }

}
