package uk.co.sundroid.util.astro.math;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.asin;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;

import java.util.Calendar;
import java.util.TimeZone;

import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.BodyDay;
import uk.co.sundroid.util.astro.MoonDay;
import uk.co.sundroid.util.astro.MoonPhaseEvent;
import uk.co.sundroid.util.astro.Position;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.geo.LatitudeLongitude;
import uk.co.sundroid.util.time.TimeUtils;

import static uk.co.sundroid.util.astro.math.SunCalculator.Event.*;
import static uk.co.sundroid.util.astro.MoonPhase.*;

public class BodyPositionCalculator {
	
	public static BodyDay calcDay(Body body, LatitudeLongitude location, Calendar dateMidnight, boolean transitAndLength) {
		
		if (body == Body.SUN) {
			return SunCalculator.calcDay(location, dateMidnight, RISESET);
		}
		
		BodyDay bodyDay;
		if (body == Body.MOON) {
			bodyDay = new MoonDay();
			MoonDay moonDay = (MoonDay)bodyDay;
			moonDay.setPhaseDouble(MoonPhaseCalculator.getNoonPhase(dateMidnight));
			moonDay.setIllumination(MoonPhaseCalculator.getIlluminatedPercent(moonDay.getPhaseDouble()));

			if (moonDay.getPhaseDouble() < 0.25) {
				moonDay.setPhase(EVENING_CRESCENT);
			} else if (moonDay.getPhaseDouble() < 0.5) {
				moonDay.setPhase(WAXING_GIBBOUS);
			} else if (moonDay.getPhaseDouble() < 0.75) {
				moonDay.setPhase(WANING_GIBBOUS);
			} else {
				moonDay.setPhase(MORNING_CRESCENT);
			}
			MoonPhaseEvent event = MoonPhaseCalculator.getDayEvent(dateMidnight);
			if (event != null) {
				moonDay.setPhase(event.getPhase());
				moonDay.setPhaseEvent(event);
			}
			
		} else {
			bodyDay = new BodyDay();
		}
		
		// For each hour, get the elevation. If a rise or set has happened during the time,
		// use the relative elevations to guess a minute, calculate that, then work forward
		// or back one minute at a time to find the minute nearest the event. If the body
		// rises and sets within the same hour, both events will be missed.
		Calendar calendar = TimeUtils.clone(dateMidnight);
		int hours = transitAndLength ? 48 : 24;
		double[] hourEls = new double[50];
		double[] hourAzs = new double[50];
		double radiusCorrection = body == Body.MOON ? 0.5 : 0;
		hourLoop:
		for (int hour = 0; hour <= hours; hour++) {
			calendar.setTimeInMillis(dateMidnight.getTimeInMillis());
			calendar.add(HOUR_OF_DAY, hour);
			Position hourPosition = calcPosition(body, location, calendar);
			hourEls[hour] = hourPosition.getAppElevation();
			hourAzs[hour] = hourPosition.getAzimuth();
			
			if (transitAndLength && hour > 0 && hour <= 24 && sector(hourAzs[hour]) != sector(hourAzs[hour - 1])) {
				Position noon = binarySearchNoon(body, location, sector(hourAzs[hour]), calendar.getTimeInMillis(), 30 * 60 * 1000, -1, 0);
				if (bodyDay.getTransit() == null || noon.getAppElevation() > bodyDay.getTransitAppElevation()) {
					Calendar noonCal = Calendar.getInstance(dateMidnight.getTimeZone());
					noonCal.setTimeInMillis(noon.getTimestamp());
					bodyDay.setTransit(noonCal);
					bodyDay.setTransitAppElevation(noon.getAppElevation());
				}
			}
			
			if (hour > 0 && sign(hourEls[hour], radiusCorrection) != sign(hourEls[hour-1], radiusCorrection)) {
				
				double diff = hourEls[hour] - hourEls[hour - 1];
				int minuteGuess = (int)(Math.round(60 * Math.abs(hourEls[hour-1]/diff)));
				
				calendar.add(HOUR_OF_DAY, -1);
				calendar.set(MINUTE, minuteGuess);
				
				Position initPosition = calcPosition(body, location, calendar);
				double initEl = initPosition.getAppElevation();
				
				int direction = sign(initEl, radiusCorrection) == sign(hourEls[hour - 1], radiusCorrection) ? 1 : -1;
				
				int safety = 0;
				while (safety < 60) {
					calendar.add(Calendar.MINUTE, direction);
					
					Position thisPosition = calcPosition(body, location, calendar);
					double thisEl = thisPosition.getAppElevation();
					
				//	System.out.println(TimeHelper.formatTime(calendar, true) + " " + thisEl + " " + direction);
					if (sign(thisEl, radiusCorrection) != sign(initEl, radiusCorrection)) {
						double azimuth = thisPosition.getAzimuth();
						if (Math.abs(thisEl + radiusCorrection) > Math.abs(initEl + radiusCorrection)) {
							// Previous time was closer. Use previous iteration's values.
							calendar.add(Calendar.MINUTE, -direction);
							azimuth = initPosition.getAzimuth();
						}
						if (sign(hourEls[hour - 1], radiusCorrection) < 0) {
							if (hour <= 24 && calendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
								bodyDay.setRise(TimeUtils.clone(calendar));
								bodyDay.setRiseAzimuth(azimuth);
							}
						} else {
							if (hour <= 24 && calendar.get(Calendar.DAY_OF_YEAR) == dateMidnight.get(Calendar.DAY_OF_YEAR)) {
								bodyDay.setSet(TimeUtils.clone(calendar));
								bodyDay.setSetAzimuth(azimuth);
							} else if (hour > 24 && bodyDay.getRise() != null) {
								bodyDay.setUptimeHours((calendar.getTimeInMillis() - bodyDay.getRise().getTimeInMillis())/(1000d * 60 * 60));
								break hourLoop;
							}
						}
						break;
					}
					
					// Set for next minute.
					initPosition = thisPosition;
					initEl = thisEl;
					safety++;
				}
			
				// Set calendar to continue hourly iteration.
				//calendar.set(Calendar.HOUR_OF_DAY, hour);
				//calendar.set(Calendar.MINUTE, 0);
				
			}
			
			
			// If rise and set already calculated and rise before set, use them to calculate uptime.
			// If there is no rise it's a risen or set day.
			if (bodyDay.getRise() != null && bodyDay.getSet() != null && (bodyDay.getRise().getTimeInMillis() < bodyDay.getSet().getTimeInMillis() || !transitAndLength)) {
				bodyDay.setUptimeHours((bodyDay.getSet().getTimeInMillis() - bodyDay.getRise().getTimeInMillis())/(1000d * 60 * 60));
				break;
			} else if (bodyDay.getRise() == null && hour == 24) {
				break;
			}
		}
		
		if (bodyDay.getRise() == null && bodyDay.getSet() == null) {
			bodyDay.setRiseSetType(hourEls[12] > 0 ? RiseSetType.RISEN : RiseSetType.SET);
		}
		
		return bodyDay;
	}
	
	private static int sector(double azimuth) {
		if (azimuth >= 0 && azimuth < 180) {
			return 1;
		} else {
			return 2;
		}
	}
	
	public static Position calcPosition(Body body, LatitudeLongitude location, Calendar dateTime) {
		if (body == Body.SUN) {
			return SunCalculator.calcPosition(location, dateTime);
		}
		Calendar dateTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateTimeUtc.setTimeInMillis(dateTime.getTimeInMillis());
		dateTimeUtc.getTimeInMillis();
		if (body == Body.MOON) {
			return calcMoonPosition(location, dateTimeUtc);
		} else {
			return calcPlanetPositionInternal(body, location, dateTimeUtc);
		}
	}
	
	public static Position calcPosition(Body body, LatitudeLongitude location, long time) {
		Calendar dateTimeUtc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		dateTimeUtc.setTimeInMillis(time);
		dateTimeUtc.getTimeInMillis();
		if (body == Body.MOON) {
			return calcMoonPosition(location, dateTimeUtc);
		} else {
			return calcPlanetPositionInternal(body, location, dateTimeUtc);
		}
	}
	
	private static Position calcMoonPosition(LatitudeLongitude location, Calendar dateTime) {
		
		double d = dayNumber(dateTime);
		double obliquityOfEliptic = obliquityOfEcliptic(julianCent(dateTime));
		
		// Geocentric orbital components.
		double N = norm360(125.1228 - 0.0529538083 * d); // (Long asc. node)
		double i = norm360(5.1454); // (Inclination)
		double w = norm360(318.0634 + 0.1643573223  * d); // (Arg. of perigee)
		double a = 60.2666; // (Mean distance)
		double e = 0.054900; // (Eccentricity)
		double M = norm360(115.3654 + 13.0649929509 * d); // (Mean anomaly)

		// Calculate eccentric anomaly using iteration to produce accurate value.
		double E0 = M + (180/PI) * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)));
		double E1 = Double.MAX_VALUE;
		int loopCount = 0;
		while (abs(E1 - E0) > 0.005 && loopCount < 10) {
			E1 = E0 - (E0 - (180/PI) * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)));
			loopCount++;
		}
		
		// Rectangular (x,y) coordinates in the plane of the orbit
		double planarX = a * (cos(toRadians(E1)) - e);
		double planarY = a * sqrt(1 - e*e) * sin(toRadians(E1));

		// Convert rectangular coordinates to distance and true anomaly.
		double geoR = sqrt( planarX*planarX + planarY*planarY ); // (Earth radii)
		double trueAnomaly = norm360(toDegrees(atan2(toRadians(planarY), toRadians(planarX)))); // (Degrees)
		
		// Ecliptic geocentric rectangular coordinates
		double[] geoRectEclip = {
				geoR * ( cos(toRadians(N)) * cos(toRadians(trueAnomaly+w)) - sin(toRadians(N)) * sin(toRadians(trueAnomaly+w)) * cos(toRadians(i))),
				geoR * ( sin(toRadians(N)) * cos(toRadians(trueAnomaly+w)) + cos(toRadians(N)) * sin(toRadians(trueAnomaly+w)) * cos(toRadians(i))),
				geoR * sin(toRadians(trueAnomaly+w)) * sin(toRadians(i))
		};
		
		double[] geoRLonLatEclip = rectangularToSpherical(geoRectEclip);
		
		// Sun values
		double ws = norm360(282.9404 + 4.70935E-5 * d); // (longitude of perihelion)
		double Ms = norm360(356.0470 + 0.9856002585 * d); // (mean anomaly)

		double Ls = norm360(ws + Ms);
		double Lm = norm360(N + w + M);
		double D =  norm360(Lm - Ls);
		double F =  norm360(Lm - N);
		
		// Apply lunar orbit perturbations
		geoRLonLatEclip[1] = geoRLonLatEclip[1]
			-1.274 * sin(toRadians(M - 2*D))
			+0.658 * sin(toRadians(2*D))
			-0.186 * sin(toRadians(Ms))
			-0.059 * sin(toRadians(2*M - 2*D))
			-0.057 * sin(toRadians(M - 2*D + Ms))
			+0.053 * sin(toRadians(M + 2*D))
			+0.046 * sin(toRadians(2*D - Ms))
			+0.041 * sin(toRadians(M - Ms))
			-0.035 * sin(toRadians(D))
			-0.031 * sin(toRadians(M + Ms))
			-0.015 * sin(toRadians(2*F - 2*D))
			+0.011 * sin(toRadians(M - 4*D));
		geoRLonLatEclip[2] = geoRLonLatEclip[2]
			-0.173 * sin(toRadians(F - 2*D))
			-0.055 * sin(toRadians(M - F - 2*D))
			-0.046 * sin(toRadians(M + F - 2*D))
			+0.033 * sin(toRadians(F + 2*D))
			+0.017 * sin(toRadians(2*M + F));
		geoRLonLatEclip[0] = geoRLonLatEclip[0]
			-0.58 * cos(toRadians(M - 2*D))
			-0.46 * cos(toRadians(2*D));

		// Convert perturbed ecliptic lat and lon back into geo ecliptic rectangular coords.
		geoRectEclip = sphericalToRectangular(geoRLonLatEclip);

		// Rotate ecliptic rectangular coordinates to equatorial, then convert to spherical for RA and Dec.
		double[] geoRectEquat = eclipticToEquatorial(geoRectEclip, obliquityOfEliptic);
		double[] geoRRADec = rectangularToSpherical(geoRectEquat);
		
		double[] topoRRADec = geoToTopo(geoRRADec, location, dateTime);
		double[] topoAzEl = raDecToAzEl(topoRRADec, location, dateTime);
		
		Position position = new Position();
		position.setTimestamp(dateTime.getTimeInMillis());
		position.setAzimuth(topoAzEl[0]);
		position.setAppElevation(refractionCorrection(topoAzEl[1]));
		return position;
	}
	
	private static Position calcPlanetPositionInternal(Body body, LatitudeLongitude location, Calendar dateTime) {
		
		double d = dayNumber(dateTime);
		double obliquityOfEliptic = obliquityOfEcliptic(julianCent(dateTime));
		
		double N, i, w, a, e, M;
		
		// Calculate heliocentric orbital components.
		switch (body) {
			case MERCURY:
				N = norm360(48.3313 + 3.24587E-5 * d);
				i = norm360(7.0047 + 5.00E-8 * d);
				w = norm360(29.1241 + 1.01444E-5 * d);
				a = 0.387098;
				e = 0.205635 + 5.59E-10 * d;
				M = norm360(168.6562 + 4.0923344368 * d);
				break;
			case VENUS:
				N = norm360(76.6799 + 2.46590E-5 * d);
				i = norm360(3.3946 + 2.75E-8 * d);
				w = norm360(54.8910 + 1.38374E-5 * d);
				a = 0.723330;
				e = 0.006773 - 1.302E-9 * d;
				M = norm360(48.0052 + 1.6021302244 * d);
				break;
			case MARS:
				N = norm360(49.5574 + 2.11081E-5 * d);
				i = norm360(1.8497 - 1.78E-8 * d);
				w = norm360(286.5016 + 2.92961E-5 * d);
				a = 1.523688;
				e = 0.093405 + 2.516E-9 * d;
				M = norm360(18.6021 + 0.5240207766 * d);
				break;
			case JUPITER:
				N = norm360(100.4542 + 2.76854E-5 * d);
				i = norm360(1.3030 - 1.557E-7 * d);
				w = norm360(273.8777 + 1.64505E-5 * d);
				a = 5.20256;
				e = 0.048498 + 4.469E-9 * d;
				M = norm360(19.8950 + 0.0830853001 * d);
				break;
			case SATURN:
				N = norm360(113.6634 + 2.38980E-5 * d);
				i = norm360(2.4886 - 1.081E-7 * d);
				w = norm360(339.3939 + 2.97661E-5 * d);
				a = 9.55475;
				e = 0.055546 - 9.499E-9 * d;
				M = norm360(316.9670 + 0.0334442282 * d);
				break;
			case URANUS:
				N = norm360(74.0005 + 1.3978E-5 * d);
				i = norm360(0.7733 + 1.9E-8 * d);
				w = norm360(96.6612 + 3.0565E-5 * d);
				a = 19.18171 - 1.55E-8 * d;
				e = 0.047318 + 7.45E-9 * d;
				M = norm360(142.5905 + 0.011725806 * d);
				break;
			case NEPTUNE:
				N = norm360(131.7806 + 3.0173E-5 * d);
				i = norm360(1.7700 - 2.55E-7 * d);
				w = norm360(272.8461 - 6.027E-6 * d);
				a = 30.05826 + 3.313E-8 * d;
				e = 0.008606 + 2.15E-9 * d;
				M = norm360(260.2471 + 0.005995147 * d);
				break;
			default:
				throw new IllegalArgumentException("Unrecognised body: " + body);
		}

		// Calculate eccentric anomaly using iteration to produce accurate value.
		double E0 = M + (180/PI) * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)));
		double E1 = Double.MAX_VALUE;
		int loopCount = 0;
		while (abs(E1 - E0) > 0.005 && loopCount < 10) {
			E1 = E0 - (E0 - (180/PI) * e * sin(toRadians(E0)) - M) / (1 - e * cos(toRadians(E0)));
			loopCount++;
		}
		
		// Rectangular (x,y) coordinates in the plane of the orbit
		double planarX = a * (cos(toRadians(E1)) - e);
		double planarY = a * sqrt(1 - e*e) * sin(toRadians(E1));

		// Convert rectangular coordinates to distance and true anomaly.
		double helioR = sqrt( planarX*planarX + planarY*planarY ); // (Earth radii)
		double trueAnomaly = norm360(toDegrees(atan2(toRadians(planarY), toRadians(planarX)))); // (Degrees)
		
		// Ecliptic heliocentric rectangular coordinates
		double[] helioRectEclip = {
				helioR * ( cos(toRadians(N)) * cos(toRadians(trueAnomaly+w)) - sin(toRadians(N)) * sin(toRadians(trueAnomaly+w)) * cos(toRadians(i))),
				helioR * ( sin(toRadians(N)) * cos(toRadians(trueAnomaly+w)) + cos(toRadians(N)) * sin(toRadians(trueAnomaly+w)) * cos(toRadians(i))),
				helioR * sin(toRadians(trueAnomaly+w)) * sin(toRadians(i))
		};
		
		double[] helioRLonLatEclip = rectangularToSpherical(helioRectEclip);
		
		// Apply the planet's perturbations.
		double Mju = norm360(19.8950 + 0.0830853001 * d);
		double Msa = norm360(316.9670 + 0.0334442282 * d);
		double Mur = norm360(142.5905 + 0.011725806 * d);
		switch (body) {
			case JUPITER:
				helioRLonLatEclip[1] = helioRLonLatEclip[1]
					-0.332 * sin(toRadians(2*Mju - 5*Msa - 67.6))
					-0.056 * sin(toRadians(2*Mju - 2*Msa + 21))
					+0.042 * sin(toRadians(3*Mju - 5*Msa + 21))
					-0.036 * sin(toRadians(Mju - 2*Msa))
					+0.022 * cos(toRadians(Mju - Msa))
					+0.023 * sin(toRadians(2*Mju - 3*Msa + 52))
					-0.016 * sin(toRadians(Mju - 5*Msa - 69));
				break;
			case SATURN:
				helioRLonLatEclip[1] = helioRLonLatEclip[1]
					+0.812 * sin(toRadians(2*Mju - 5*Msa - 67.6))
					-0.229 * cos(toRadians(2*Mju - 4*Msa - 2))
					+0.119 * sin(toRadians(Mju - 2*Msa - 3))
					+0.046 * sin(toRadians(2*Mju - 6*Msa - 69))
					+0.014 * sin(toRadians(Mju - 3*Msa + 32));
				helioRLonLatEclip[2] = helioRLonLatEclip[2]
					-0.020 * cos(toRadians(2*Mju - 4*Msa - 2))
					+0.018 * sin(toRadians(2*Mju - 6*Msa - 49));
				break;
			case URANUS:
				helioRLonLatEclip[1] = helioRLonLatEclip[1]
					+0.040 * sin(toRadians(Msa - 2*Mur + 6))
					+0.035 * sin(toRadians(Msa - 3*Mur + 33))
					-0.015 * sin(toRadians(Mju - Mur + 20));
				break;
			default:
		}

		// Convert perturbed ecliptic lat and lon back into helio ecliptic rectangular coords.
		helioRectEclip = sphericalToRectangular(helioRLonLatEclip);

		double[] geoRectEclip = helioToGeo(helioRectEclip, d);
		double[] geoRectEquat = eclipticToEquatorial(geoRectEclip, obliquityOfEliptic);
		double[] geoRRADec = rectangularToSpherical(geoRectEquat);
		double[] geoAzEl = raDecToAzEl(geoRRADec, location, dateTime);
		
		Position position = new Position();
		position.setTimestamp(dateTime.getTimeInMillis());
		position.setAzimuth(geoAzEl[0]);
		position.setAppElevation(refractionCorrection(geoAzEl[1]));
		return position;
	}
	
	private static double refractionCorrection(double elevation) {
		
		double refractionCorrection;
		if (elevation > 85.0) {
			refractionCorrection = 0.0;
		} else {
			double te = tan (toRadians(elevation));
			if (elevation > 5.0) {
				refractionCorrection = 58.1 / te - 0.07 / (te*te*te) +
					0.000086 / (te*te*te*te*te);
			} else if (elevation > -0.575) {
				refractionCorrection = 1735.0 + elevation *
					(-518.2 + elevation * (103.4 +
                            elevation * (-12.79 +
                                    elevation * 0.711) ) );
			} else {
				refractionCorrection = -20.774 / te;
			}
			refractionCorrection = refractionCorrection / 3600.0;
		}
		return elevation + refractionCorrection;
		
	}
	
	private static double dayNumber(Calendar dateTime) {
		int year = dateTime.get(Calendar.YEAR);
		int month = dateTime.get(Calendar.MONTH) + 1;
		int day = dateTime.get(Calendar.DAY_OF_MONTH);
		double fraction = (dateTime.get(Calendar.HOUR_OF_DAY)/24d) + (dateTime.get(Calendar.MINUTE)/(60d*24d)) + (dateTime.get(Calendar.SECOND)/(60d*60d*24d));
		return 367*year - (7*(year + ((month+9)/12)))/4 + (275*month)/9 + day - 730530 + fraction;
	}
	
	private static double julianDay(Calendar dateTime) {
		return dayNumber(dateTime) + 2451543.5;
	}
	
	private static double julianCent(Calendar dateTime) {
		double jd = julianDay(dateTime);
		return (jd - 2451545.0)/36525.0;
	}

	private static double[] rectangularToSpherical(double[] xyz) {
		double r = sqrt( xyz[0]*xyz[0] + xyz[1]*xyz[1] + xyz[2]*xyz[2] );
		double lon = norm360(toDegrees(atan2(toRadians(xyz[1]), toRadians(xyz[0]))));
		double lat = toDegrees(atan2(toRadians(xyz[2]), toRadians(sqrt(xyz[0]*xyz[0] + xyz[1]*xyz[1]))));
		return new double[] { r, lon, lat };
	}
	
	private static double[] sphericalToRectangular(double[] rLonLat) {
		double x = rLonLat[0] * cos(toRadians(rLonLat[1])) * cos(toRadians(rLonLat[2]));
		double y = rLonLat[0] * sin(toRadians(rLonLat[1])) * cos(toRadians(rLonLat[2]));
		double z = rLonLat[0] * sin(toRadians(rLonLat[2]));
		return new double[] { x, y, z };
	}
	
	private static double[] eclipticToEquatorial(double[] xyzEclip, double o) {
		double xEquat = xyzEclip[0];
		double yEquat = xyzEclip[1] * cos(toRadians(o)) - xyzEclip[2] * sin(toRadians(o));
		double zEquat = xyzEclip[1] * sin(toRadians(o)) + xyzEclip[2] * cos(toRadians(o));
		return new double[] { xEquat, yEquat, zEquat };
	}
	
	private static double[] raDecToAzEl(double[] rRaDecl, LatitudeLongitude location, Calendar dateTime) {
		
		double LSTh = localSiderealTimeHours(location, dateTime);
		double RAh = rRaDecl[1]/15.0d;
		
		// Hour angle
		double HAd = 15 * norm24(LSTh - RAh);

		double x = cos(toRadians(HAd)) * cos(toRadians(rRaDecl[2]));
		double y = sin(toRadians(HAd)) * cos(toRadians(rRaDecl[2]));
		double z = sin(toRadians(rRaDecl[2]));

		double xhor = x * sin(toRadians(location.getLatitude().getDoubleValue())) - z * cos(toRadians(location.getLatitude().getDoubleValue()));
		double zhor = x * cos(toRadians(location.getLatitude().getDoubleValue())) + z * sin(toRadians(location.getLatitude().getDoubleValue()));

		double azimuth = norm360(toDegrees(atan2(toRadians(y), toRadians(xhor))) + 180.0d);
		double trueElevation = toDegrees(atan2(toRadians(zhor), toRadians(sqrt(xhor*xhor+y*y))));
		
		return new double[] { azimuth, trueElevation };
	}
	
	private static double[] geoToTopo(double[] rRaDec, LatitudeLongitude location, Calendar dateTime) {
		
		double lat = location.getLatitude().getDoubleValue();
		double gclat = lat - 0.1924 * sin(toRadians(2*lat));
		double rho = 0.99833 + 0.00167 * cos(toRadians(2*lat));
		double mpar = toDegrees(asin(1/rRaDec[0]));
		double LST = localSiderealTimeHours(location, dateTime);
		double HA = norm360((LST * 15) - rRaDec[1]);
		double g = toDegrees(atan(tan(toRadians(gclat))/cos(toRadians(HA))));
		
		double topRA   = rRaDec[1] - mpar * rho * cos(toRadians(gclat)) * sin(toRadians(HA)) / cos(toRadians(rRaDec[2]));
		double topDecl = rRaDec[2] - mpar * rho * sin(toRadians(gclat)) * sin(toRadians(g - rRaDec[2])) / sin(toRadians(g));
		
		return new double[] { rRaDec[0], topRA, topDecl };
	}
	
	private static double[] helioToGeo(double[] helioRectEclip, double d) {
		
		double w = norm360(282.9404 + 4.70935E-5 * d); // (longitude of perihelion)
		double e = 0.016709 - 1.151E-9 * d; // (eccentricity)
		double M = norm360(356.0470 + 0.9856002585 * d); // (mean anomaly)
		double E = M + (180/PI) * e * sin(toRadians(M)) * (1 + e * cos(toRadians(M)));
		double x = cos(toRadians(E)) - e;
		double y = sin(toRadians(E)) * sqrt(1 - e*e);
		double r = sqrt(x*x + y*y);
		double v = toDegrees(atan2( y, x ));
		double lon = norm360(v + w);
		x = r * cos(toRadians(lon));
		y = r * sin(toRadians(lon));
		double z = 0.0;

		return new double[] {
				helioRectEclip[0] + x,
				helioRectEclip[1] + y,
				helioRectEclip[2] + z
		};
		
	}
	
	private static double localSiderealTimeHours(LatitudeLongitude location, Calendar dateTime) {
		
		double d = dayNumber(dateTime);
		double UT = dateTime.get(Calendar.HOUR_OF_DAY) + (dateTime.get(Calendar.MINUTE)/60d) + (dateTime.get(Calendar.SECOND)/3600d);
		
		double ws = norm360(282.9404 + 4.70935E-5 * d); // (longitude of perihelion)
		double Ms = norm360(356.0470 + 0.9856002585 * d); // (mean anomaly)
		double Ls = norm360(ws + Ms);
		
		double GMST0 = Ls/15 + 12.0;
		
		return norm24(GMST0 + UT + location.getLongitude().getDoubleValue()/15d);
		
	}
	
	private static double obliquityOfEcliptic(double t) {
		double seconds = 21.448 - t*(46.8150 + t*(0.00059 - t*(0.001813)));
		return 23.0 + (26.0 + (seconds/60.0))/60.0;
	}
	
	private static double norm360(double degrees) {
		while (degrees < 0.0d) {
			degrees += 360.0d;
		}
		while (degrees > 360.0d) {
			degrees -= 360.0d;
		}
		return degrees;
	}
	
	private static double norm24(double hours) {
		while (hours < 0.0d) {
			hours += 24.0d;
		}
		while (hours > 24.0d) {
			hours -= 24.0d;
		}
		return hours;
	}
	
	private static int sign(double value, double plus) {
		return (value + plus) < 0d ? -1 : 1;
	}
	
	private static Position binarySearchNoon(Body body, LatitudeLongitude location, int initialSector, long initialTimestamp, int intervalMs, int searchDirection, int depth) {
		long thisTimestamp = initialTimestamp + (searchDirection * intervalMs);
		Position thisPosition = calcPosition(body, location, thisTimestamp);
		int thisSector = sector(thisPosition.getAzimuth());
		//Calendar thisCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		//thisCal.setTimeInMillis(thisTimestamp);
		//System.out.println("binary " + initialSector + ", " + initialTimestamp + " (" + thisCal.get(Calendar.HOUR_OF_DAY) + ":" + thisCal.get(Calendar.MINUTE) + ":" + thisCal.get(Calendar.SECOND) + ", " + intervalMs + ", " + searchDirection + " : el " + thisPosition.getAzimuth());
		
		if (intervalMs < 15000 || depth > 10) {
			return thisPosition;
		}
		
		if (thisSector == initialSector) {
			return binarySearchNoon(body, location, thisSector, thisTimestamp, intervalMs/2, searchDirection, depth + 1);
		} else {
			return binarySearchNoon(body, location, thisSector, thisTimestamp, intervalMs/2, -searchDirection, depth + 1);
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
