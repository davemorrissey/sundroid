package uk.co.sundroid.util.astro.math;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MILLISECOND;
import static java.util.Calendar.MINUTE;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.SECOND;
import static java.util.Calendar.YEAR;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

import uk.co.sundroid.util.astro.Position;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.SunDay;
import uk.co.sundroid.util.astro.TwilightType;
import uk.co.sundroid.util.location.LatitudeLongitude;

public class SunCalculator {
	
	public enum Event {
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
	private static double radToDeg(double angleRad) {
		return (180.0 * angleRad / Math.PI);
	}

    /**
     * Convert degree angle to radians
     */
	private static double degToRad(double angleDeg) {
		return (Math.PI * angleDeg / 180.0);
	}

    /**
     * Julian day from calendar day.
     * @param year 4-digit year
     * @param month January = 1
     * @param day 1-31
     * @return the Julian day corresponding to the start of the day. Fractional days should be added later.
     */
	private static double calcJD(int year, int month, int day) {
		if (month <= 2) {
			year -= 1;
			month += 12;
		}
		double A = Math.floor(year/100);
		double B = 2 - A + Math.floor(A/4);
		return Math.floor(365.25*(year + 4716)) + Math.floor(30.6001*(month+1)) + day + B - 1524.5;
	}

    /**
     * Convert Julian Day to centuries since J2000.0
     * @param jd Julian day
     * @return the T value corresponding to the Julian Day
     */
	private static double calcTimeJulianCent(double jd) {
		return (jd - 2451545.0)/36525.0;
	}

    /**
     * Convert centuries since J2000.0 to Julian Day
     * @param t number of Julian centuries since J2000.0
     * @return the Julian Day corresponding to the t value
     */
    private static double calcJDFromJulianCent(double t) {
		return t * 36525.0 + 2451545.0;
	}

	//***********************************************************************/
	//* Name:    calGeomMeanLongSun							*/
	//* Type:    Function									*/
	//* Purpose: calculate the Geometric Mean Longitude of the Sun		*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   the Geometric Mean Longitude of the Sun in degrees			*/
	//***********************************************************************/

    private static double calcGeomMeanLongSun(double t) {
		double L0 = 280.46646 + t * (36000.76983 + 0.0003032 * t);
		while (L0 > 360.0) {
			L0 -= 360.0;
		}
		while (L0 < 0.0) {
			L0 += 360.0;
		}
		return L0;		// in degrees
	}

	//
	//* Name:    calGeomAnomalySun							*/
	//* Type:    Function									*/
	//* Purpose: calculate the Geometric Mean Anomaly of the Sun		*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   the Geometric Mean Anomaly of the Sun in degrees			*/
	//***********************************************************************/
	private static double calcGeomMeanAnomalySun(double t) {
		return 357.52911 + t * (35999.05029 - 0.0001537 * t); // in degrees
	}

	//***********************************************************************/
	//* Name:    calcEccentricityEarthOrbit						*/
	//* Type:    Function									*/
	//* Purpose: calculate the eccentricity of earth's orbit			*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   the unitless eccentricity							*/
	//***********************************************************************/
	private static double calcEccentricityEarthOrbit(double t) {
		return 0.016708634 - t * (0.000042037 + 0.0000001267 * t); // unitless
	}

	//***********************************************************************/
	//* Name:    calcSunEqOfCenter							*/
	//* Type:    Function									*/
	//* Purpose: calculate the equation of center for the sun			*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   in degrees										*/
	//***********************************************************************/
	private static double calcSunEqOfCenter(double t) {
		double m = calcGeomMeanAnomalySun(t);

		double mrad = degToRad(m);
		double sinm = Math.sin(mrad);
		double sin2m = Math.sin(mrad+mrad);
		double sin3m = Math.sin(mrad+mrad+mrad);

		return sinm * (1.914602 - t * (0.004817 + 0.000014 * t)) + sin2m * (0.019993 - 0.000101 * t) + sin3m * 0.000289;
	}

    /**
     * Calculate the true longitude of the sun
     * @param t number of Julian centuries since J2000.0
     * @return sun's true longitude in degrees
     */
    private static double calcSunTrueLong(double t) {
		double l0 = calcGeomMeanLongSun(t);
		double c = calcSunEqOfCenter(t);
		return l0 + c;
	}

    /**
     * Calculate the apparent longitude of the sun
     * @param t number of Julian centuries since J2000.0
     * @return sun's apparent longitude in degrees
     */
    private static double calcSunApparentLong(double t) {
		double o = calcSunTrueLong(t);
		double omega = 125.04 - 1934.136 * t;
		return o - 0.00569 - 0.00478 * Math.sin(degToRad(omega));
	}

    /**
     * Calculate the mean obliquity of the ecliptic
     * @param t number of Julian centuries since J2000.0
     * @return mean obliquity in degrees
     */
	private static double calcMeanObliquityOfEcliptic(double t) {
		double seconds = 21.448 - t*(46.8150 + t*(0.00059 - t*(0.001813)));
		return 23.0 + (26.0 + (seconds/60.0))/60.0;
	}

	//***********************************************************************/
	//* Name:    calcObliquityCorrection						*/
	//* Type:    Function									*/
	//* Purpose: calculate the corrected obliquity of the ecliptic		*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   corrected obliquity in degrees						*/
	//***********************************************************************/
	private static double calcObliquityCorrection(double t) {
		double e0 = calcMeanObliquityOfEcliptic(t);

		double omega = 125.04 - 1934.136 * t;
		return e0 + 0.00256 * Math.cos(degToRad(omega)); // in degrees
	}

	//***********************************************************************/
	//* Name:    calcSunRtAscension							*/
	//* Type:    Function									*/
	//* Purpose: calculate the right ascension of the sun				*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   sun's right ascension in degrees						*/
	//***********************************************************************/
	private static double calcSunRtAscension(double t) {
		double e = calcObliquityCorrection(t);
		double lambda = calcSunApparentLong(t);
 
		double tananum = (Math.cos(degToRad(e)) * Math.sin(degToRad(lambda)));
		double tanadenom = (Math.cos(degToRad(lambda)));
		return radToDeg(Math.atan2(tananum, tanadenom)); // alpha, in degrees
	}

	//***********************************************************************/
	//* Name:    calcSunDeclination							*/
	//* Type:    Function									*/
	//* Purpose: calculate the declination of the sun				*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   sun's declination in degrees							*/
	//***********************************************************************/
	private static double calcSunDeclination(double t) {
		double e = calcObliquityCorrection(t);
		double lambda = calcSunApparentLong(t);

		double sint = Math.sin(degToRad(e)) * Math.sin(degToRad(lambda));
		return radToDeg(Math.asin(sint)); // theta in degrees
	}

	//***********************************************************************/
	//* Name:    calcEquationOfTime							*/
	//* Type:    Function									*/
	//* Purpose: calculate the difference between true solar time and mean	*/
	//*		solar time									*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//* Return value:										*/
	//*   equation of time in minutes of time						*/
	//***********************************************************************/
	private static double calcEquationOfTime(double t) {
		double epsilon = calcObliquityCorrection(t);
		double l0 = calcGeomMeanLongSun(t);
		double e = calcEccentricityEarthOrbit(t);
		double m = calcGeomMeanAnomalySun(t);

		double y = Math.tan(degToRad(epsilon)/2.0);
		y *= y;

		double sin2l0 = Math.sin(2.0 * degToRad(l0));
		double sinm   = Math.sin(degToRad(m));
		double cos2l0 = Math.cos(2.0 * degToRad(l0));
		double sin4l0 = Math.sin(4.0 * degToRad(l0));
		double sin2m  = Math.sin(2.0 * degToRad(m));

		double Etime = y * sin2l0 - 2.0 * e * sinm + 4.0 * e * y * sinm * cos2l0
				- 0.5 * y * y * sin4l0 - 1.25 * e * e * sin2m;

		return radToDeg(Etime)*4.0;	// in minutes of time
	}

	private static double calcHourAngleUp(double lat, double solarDec, double elevation) {
		double latRad = degToRad(lat);
		double sdRad = degToRad(solarDec);
		return (Math.acos(Math.cos(degToRad(elevation))/(Math.cos(latRad)*Math.cos(sdRad))-Math.tan(latRad) * Math.tan(sdRad)));
	}

	private static double calcHourAngleDown(double lat, double solarDec, double elevation) {
		double latRad = degToRad(lat);
		double sdRad  = degToRad(solarDec);
		return -(Math.acos(Math.cos(degToRad(elevation))/(Math.cos(latRad)*Math.cos(sdRad))-Math.tan(latRad) * Math.tan(sdRad)));
	}

	private static double calcUpUTC(double JD, double latitude, double longitude, double elevation) {
		double t = calcTimeJulianCent(JD);
		double noonmin = calcSolNoonUTC(t, longitude);
		double tnoon = calcTimeJulianCent (JD+noonmin/1440.0);
		double eqTime = calcEquationOfTime(tnoon);
		double solarDec = calcSunDeclination(tnoon);
		double hourAngle = calcHourAngleUp(latitude, solarDec, elevation);
		double delta = longitude - radToDeg(hourAngle);
		double timeDiff = 4 * delta;
		double timeUTC = 720 + timeDiff - eqTime;
		double newt = calcTimeJulianCent(calcJDFromJulianCent(t) + timeUTC/1440.0);
		eqTime = calcEquationOfTime(newt);
		solarDec = calcSunDeclination(newt);
		hourAngle = calcHourAngleUp(latitude, solarDec, elevation);
		delta = longitude - radToDeg(hourAngle);
		timeDiff = 4 * delta;
		timeUTC = 720 + timeDiff - eqTime;
		return timeUTC;
	}

	//***********************************************************************/
	//* Name:    calcSolNoonUTC								*/
	//* Type:    Function									*/
	//* Purpose: calculate the Universal Coordinated Time (UTC) of solar	*/
	//*		noon for the given day at the given location on earth		*/
	//* Arguments:										*/
	//*   t : number of Julian centuries since J2000.0				*/
	//*   longitude : longitude of observer in degrees				*/
	//* Return value:										*/
	//*   time in minutes from zero Z							*/
	//***********************************************************************/
	private static double calcSolNoonUTC(double t, double longitude) {
		// First pass uses approximate solar noon to calculate eqtime
		double tnoon = calcTimeJulianCent(calcJDFromJulianCent(t) + longitude/360.0);
		double eqTime = calcEquationOfTime(tnoon);
		double solNoonUTC = 720 + (longitude * 4) - eqTime; // min

		double newt = calcTimeJulianCent(calcJDFromJulianCent(t) -0.5 + solNoonUTC/1440.0); 

		eqTime = calcEquationOfTime(newt);
		// var solarNoonDec = calcSunDeclination(newt);
		solNoonUTC = 720 + (longitude * 4) - eqTime; // min
		
		return solNoonUTC;
	}

	//***********************************************************************/
	//* Name:    calcSunsetUTC								*/
	//* Type:    Function									*/
	//* Purpose: calculate the Universal Coordinated Time (UTC) of sunset	*/
	//*			for the given day at the given location on earth	*/
	//* Arguments:										*/
	//*   JD  : julian day									*/
	//*   latitude : latitude of observer in degrees				*/
	//*   longitude : longitude of observer in degrees				*/
	//* Return value:										*/
	//*   time in minutes from zero Z							*/
	//***********************************************************************/
	private static double calcDownUTC(double JD, double latitude, double longitude, double elevation) {
		double t = calcTimeJulianCent(JD);

		// Find the time of solar noon at the location, and use that declination. This is better than
        // start of the Julian day

		double noonmin = calcSolNoonUTC(t, longitude);
		double tnoon = calcTimeJulianCent (JD+noonmin/1440.0);

		// First calculates sunrise and approx length of day

		double eqTime = calcEquationOfTime(tnoon);
		double solarDec = calcSunDeclination(tnoon);
		double hourAngle = calcHourAngleDown(latitude, solarDec, elevation);

		double delta = longitude - radToDeg(hourAngle);
		double timeDiff = 4 * delta;
		double timeUTC = 720 + timeDiff - eqTime;

		// first pass used to include fractional day in gamma calc

		double newt = calcTimeJulianCent(calcJDFromJulianCent(t) + timeUTC/1440.0); 
		eqTime = calcEquationOfTime(newt);
		solarDec = calcSunDeclination(newt);
		hourAngle = calcHourAngleDown(latitude, solarDec, elevation);

		delta = longitude - radToDeg(hourAngle);
		timeDiff = 4 * delta;
		timeUTC = 720 + timeDiff - eqTime; // in minutes

		return timeUTC;
	}

	/**
	 * Create a calendar from midnight with added minutes in a given timezone.
	 * @param dateMidnight Calendar for midnight of UTC date. Must be in UTC.
	 * @param timeZone Time zone to apply after the calendar is set.
	 * @param minutes Minutes to add.
	 */
	private static Calendar createCalendar(Calendar dateMidnight, double minutes, TimeZone timeZone) {
	
		double floatHour = minutes / 60.0;
		int hour = (int)Math.floor(floatHour);
		double floatMinute = 60.0 * (floatHour - Math.floor(floatHour));
		int minute = (int)Math.floor(floatMinute);
		double floatSec = 60.0 * (floatMinute - Math.floor(floatMinute));
		int second = (int)Math.floor(floatSec + 0.5);
		int addDays = 0;

		if (minute >= 60) {
			minute -= 60;
			hour++;
		}
		
		while (hour > 23) {
			hour -= 24;
			addDays++;
		}
		
		while (hour < 0) {
			hour += 24;
			addDays--;
		}
	
		Calendar dateTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateTime.setTimeInMillis(dateMidnight.getTimeInMillis());
		dateTime.set(HOUR_OF_DAY, hour);
		dateTime.set(MINUTE, minute);
		dateTime.set(SECOND, second);
		dateTime.add(DAY_OF_MONTH, addDays);
		dateTime.getTimeInMillis(); // Prompt recalc
		dateTime.setTimeZone(timeZone);
		return dateTime;
	
	}

    /**
     * Calculate sunrise, sunset, dawns, dusks and durations for a given date.
     */
	public static SunDay calcDay(LatitudeLongitude location, Calendar dateMidnight, Event... events) {

		double latitude = location.getLatitude().getDoubleValue();
		double longitude = -location.getLongitude().getDoubleValue();
		
		int year = dateMidnight.get(YEAR);
		int month = dateMidnight.get(MONTH) + 1;
		int day = dateMidnight.get(DAY_OF_MONTH);		
		Calendar dateMidnightUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateMidnightUtc.set(year, month - 1, day, 0, 0, 0);
		dateMidnightUtc.set(MILLISECOND, 0);

		if ((latitude >= -90) && (latitude < -89)) {
			latitude = -89;
		}
		if ((latitude <= 90) && (latitude > 89)) {
			latitude = 89;
		}
		
		double julianDay = calcJD(year, month, day);
		double julianCent = calcTimeJulianCent(julianDay);

		SunDay sunDay = new SunDay();

		double solarNoon = calcSolNoonUTC(julianCent, longitude);
		if (!Double.isNaN(solarNoon)) {
			sunDay.setTransit(createCalendar(dateMidnightUtc, solarNoon, dateMidnight.getTimeZone()));
		}
		Position solarNoonPosition = calcPosition(location, sunDay.getTransit());
		sunDay.setTransitAppElevation(solarNoonPosition.getAppElevation());

		if (events == null || events.length == 0 || Arrays.asList(events).contains(Event.RISESET)) {
			double sunrise = calcUpUTC(julianDay, latitude, longitude, 90.833);
			if (!Double.isNaN(sunrise)) {
				sunDay.setRise(createCalendar(dateMidnightUtc, sunrise, dateMidnight.getTimeZone()));
			}
			double sunset = calcDownUTC(julianDay, latitude, longitude, 90.833);
			if (!Double.isNaN(sunset)) {
				sunDay.setSet(createCalendar(dateMidnightUtc, sunset, dateMidnight.getTimeZone()));
			}
			if (sunDay.getRise() != null) {
				Position risePosition = calcPosition(location, sunDay.getRise());
				sunDay.setRiseAzimuth(risePosition.getAzimuth());
			}
			if (sunDay.getSet() != null) {
				Position setPosition = calcPosition(location, sunDay.getSet());
				sunDay.setSetAzimuth(setPosition.getAzimuth());
			}
			if (sunDay.getRise() != null && sunDay.getSet() != null) {
				sunDay.setUptimeHours((sunset - sunrise)/60d);
			} else if (sunDay.getRise() == null || sunDay.getSet() == null) {
				sunDay.setUptimeHours(24d);
			}
			if (sunDay.getRise() == null && sunDay.getSet() == null) {
				if (solarNoonPosition.getAppElevation() > 0) {
					sunDay.setRiseSetType(RiseSetType.RISEN);
					sunDay.setUptimeHours(24d);
				} else {
					sunDay.setRiseSetType(RiseSetType.SET);
					sunDay.setUptimeHours(0d);
				}
			}
			
		}

		if (events == null || events.length == 0 || Arrays.asList(events).contains(Event.CIVIL)) {
			double civDusk = calcDownUTC(julianDay, latitude, longitude, 96);
			if (!Double.isNaN(civDusk)) {
				sunDay.setCivDusk(createCalendar(dateMidnightUtc, civDusk, dateMidnight.getTimeZone()));
			}
			double civDawn = calcUpUTC(julianDay, latitude, longitude, 96);
			if (!Double.isNaN(civDawn)) {
				sunDay.setCivDawn(createCalendar(dateMidnightUtc, civDawn, dateMidnight.getTimeZone()));
			}
			if (sunDay.getCivDawn() == null && sunDay.getCivDusk() == null) {
				if (solarNoonPosition.getAppElevation() > -5.9) {
					sunDay.setCivType(TwilightType.LIGHT);
					sunDay.setCivHours(24d);
				} else {
					sunDay.setCivType(TwilightType.DARK);
					sunDay.setCivHours(0d);
				}
			} else if (sunDay.getCivDawn() == null || sunDay.getCivDusk() == null) {
				sunDay.setCivHours(24d);
			} else {
				sunDay.setCivHours((civDusk - civDawn) / 60);
			}
		}
		
		if (events == null || events.length == 0 || Arrays.asList(events).contains(Event.NAUTICAL)) {
			double ntcDawn = calcUpUTC(julianDay, latitude, longitude, 102);
			if (!Double.isNaN(ntcDawn)) {
				sunDay.setNtcDawn(createCalendar(dateMidnightUtc, ntcDawn, dateMidnight.getTimeZone()));
			}
			double ntcDusk = calcDownUTC(julianDay, latitude, longitude, 102);
			if (!Double.isNaN(ntcDusk)) {
				sunDay.setNtcDusk(createCalendar(dateMidnightUtc, ntcDusk, dateMidnight.getTimeZone()));
			}
			if (sunDay.getNtcDawn() == null && sunDay.getNtcDusk() == null) {
				if (solarNoonPosition.getAppElevation() > -11.9) {
					sunDay.setNtcType(TwilightType.LIGHT);
					sunDay.setNtcHours(24d);
				} else {
					sunDay.setNtcType(TwilightType.DARK);
					sunDay.setNtcHours(0d);
				}
			} else if (sunDay.getNtcDawn() == null || sunDay.getNtcDusk() == null) {
				sunDay.setNtcHours(24d);
			} else {
				sunDay.setNtcHours((ntcDusk - ntcDawn) / 60);
			}
		}

		if (events == null || events.length == 0 || Arrays.asList(events).contains(Event.ASTRONOMICAL)) {
			double astDawn = calcUpUTC(julianDay, latitude, longitude, 108);
			if (!Double.isNaN(astDawn)) {
				sunDay.setAstDawn(createCalendar(dateMidnightUtc, astDawn, dateMidnight.getTimeZone()));
			}
			double astDusk = calcDownUTC(julianDay, latitude, longitude, 108);
			if (!Double.isNaN(astDusk)) {
				sunDay.setAstDusk(createCalendar(dateMidnightUtc, astDusk, dateMidnight.getTimeZone()));
			}
			if (sunDay.getAstDawn() == null && sunDay.getAstDusk() == null) {
				if (solarNoonPosition.getAppElevation() > -17.9) {
					sunDay.setAstType(TwilightType.LIGHT);
					sunDay.setAstHours(24d);
				} else {
					sunDay.setAstType(TwilightType.DARK);
					sunDay.setAstHours(0d);
				}
			} else if (sunDay.getAstDawn() == null || sunDay.getAstDusk() == null) {
				sunDay.setAstHours(24d);
			} else {
				sunDay.setAstHours((astDusk - astDawn) / 60);
			}
		}
		
		if (events == null || events.length == 0 || Arrays.asList(events).contains(Event.GOLDENHOUR)) {
			double ghEnd = calcUpUTC(julianDay, latitude, longitude, 84);
			if (!Double.isNaN(ghEnd)) {
				sunDay.setGhEnd(createCalendar(dateMidnightUtc, ghEnd, dateMidnight.getTimeZone()));
			}
			double ghStart = calcDownUTC(julianDay, latitude, longitude, 84);
			if (!Double.isNaN(ghStart)) {
				sunDay.setGhStart(createCalendar(dateMidnightUtc, ghStart, dateMidnight.getTimeZone()));
			}
			if (sunDay.getGhEnd() == null && sunDay.getGhStart() == null) {
				if (solarNoonPosition.getAppElevation() > 6) {
					sunDay.setGhType(TwilightType.LIGHT);
					sunDay.setGhHours(24d);
				} else {
					sunDay.setGhType(TwilightType.DARK);
					sunDay.setGhHours(0d);
				}
			} else if (sunDay.getGhEnd() == null || sunDay.getGhStart() == null) {
				sunDay.setGhHours(24d);
			} else {
				sunDay.setGhHours((ghStart - ghEnd) / 60);
			}
		}

		return sunDay;
		
	}

    /**
     * Calculates the sun's position at a given time.
     */
	protected static Position calcPosition(LatitudeLongitude location, Calendar dateTime) {
		double latitude = location.getLatitude().getDoubleValue();
		double longitude = -location.getLongitude().getDoubleValue();
		
		if ((latitude >= -90) && (latitude < -89.8)) {
			latitude = -89.8;
		}
		if ((latitude <= 90) && (latitude > 89.8)) {
			latitude = 89.8;
		}
		
		Calendar dateTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateTimeUtc.setTimeInMillis(dateTime.getTimeInMillis());
		

		double timenow = dateTimeUtc.get(HOUR_OF_DAY) + dateTimeUtc.get(MINUTE)/60d + dateTimeUtc.get(SECOND)/3600d;
		
		double JD = (calcJD(dateTimeUtc.get(YEAR), dateTimeUtc.get(MONTH) + 1, dateTimeUtc.get(DAY_OF_MONTH)));
		double T = calcTimeJulianCent(JD + timenow/24.0); 
		double solarDec = calcSunDeclination(T);
		double eqTime = calcEquationOfTime(T);

		double offsetHours = 0;
		double solarTimeFix = eqTime - 4.0 * longitude + 60.0 * offsetHours;
		double trueSolarTimeMins = dateTimeUtc.get(HOUR_OF_DAY) * 60.0 + dateTimeUtc.get(MINUTE) + dateTimeUtc.get(SECOND)/60.0 + solarTimeFix;

		while (trueSolarTimeMins > 1440) {
			trueSolarTimeMins -= 1440;
		}

		double hourAngle = trueSolarTimeMins / 4.0 - 180.0;
		if (hourAngle < -180) {
		    hourAngle += 360.0;
		}

		double haRad = degToRad(hourAngle);

		double csz = Math.sin(degToRad(latitude)) *
			Math.sin(degToRad(solarDec)) +
			Math.cos(degToRad(latitude)) *
			Math.cos(degToRad(solarDec)) * Math.cos(haRad);
		if (csz > 1.0) {
			csz = 1.0;
		} else if (csz < -1.0) {
			csz = -1.0;
		}
		double zenith = radToDeg(Math.acos(csz));
		double azimuth;
		double azDenom = ( Math.cos(degToRad(latitude)) * Math.sin(degToRad(zenith)) );
		if (Math.abs(azDenom) > 0.001) {
			double azRad = (( Math.sin(degToRad(latitude)) *
				Math.cos(degToRad(zenith)) ) -
				Math.sin(degToRad(solarDec))) / azDenom;
			if (Math.abs(azRad) > 1.0) {
				if (azRad < 0) {
					azRad = -1.0;
				} else {
					azRad = 1.0;
				}
			}

			azimuth = 180.0 - radToDeg(Math.acos(azRad));
			if (hourAngle > 0.0) {
				azimuth = -azimuth;
			}
		} else {
			if (latitude > 0.0) {
				azimuth = 180.0;
			} else {
				azimuth = 0.0;
			}
		}
		if (azimuth < 0.0) {
			azimuth += 360.0;
		}

		Position sunPosition = new Position();
		sunPosition.setAzimuth(azimuth);
		sunPosition.setAppElevation(90.0 - zenith);
		return sunPosition;
	}

}
