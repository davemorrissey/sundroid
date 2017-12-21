package uk.co.sundroid.util.astro.math;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import uk.co.sundroid.util.astro.MoonPhase;
import uk.co.sundroid.util.astro.MoonPhaseEvent;

public class MoonPhaseCalculator {

	private static int[] jyear(double td){
		double z,f,a,alpha,b,c,d,e,mm;
		td+=0.5;
		z=Math.floor(td);
		f=td-z;
		if(z < 2299161.0) {
			a=z;
		} else {
			alpha=Math.floor((z-1867216.25)/36524.25);
			a=z+1+alpha-Math.floor(alpha/4);
		}
		b=a+1524;
		c=Math.floor((b-122.1)/365.25);
		d=Math.floor(365.25*c);
		e=Math.floor((b-d)/30.6001);
		mm=Math.floor((e<14)?(e-1):(e-13));
		return new int[] { (int)Math.floor((mm > 2) ? (c-4716) : (c-4715)), (int)mm, (int)Math.floor(b-d-Math.floor(30.6001*e)+f)};
	}
	
	private static int[] jhms(double j) {
		double ij;
		j+=0.5;
		ij=(j-Math.floor(j))*86400.0;
		return new int[] {(int)Math.floor(ij/3600),(int)Math.floor((ij/60)%60),(int)Math.floor(ij%60)};
	}
	private static double dtr(double d) {
		return(d*Math.PI)/180.0;
	}
	
	private static double dsin(double x) {
		return Math.sin(dtr(x));
	}
	
	private static double dcos(double x) {
		return Math.cos(dtr(x));
	}
	
	private static double truephase(double k, double phase) {
		double t,t2,t3,pt,m,mprime,f,SynMonth=29.53058868;
		k+=phase;
		t=k/1236.85;
		t2=t*t;
		t3=t2*t;
		pt=2415020.75933+SynMonth*k+0.0001178*t2-0.000000155*t3+0.00033*dsin(166.56+132.87*t-0.009173*t2);
		m=359.2242+29.10535608*k-0.0000333*t2-0.00000347*t3;
		mprime=306.0253+385.81691806*k+0.0107306*t2+0.00001236*t3;
		f=21.2964+390.67050646*k-0.0016528*t2-0.00000239*t3;
		if((phase<0.01)||(Math.abs(phase-0.5)<0.01)){
			pt+=(0.1734-0.000393*t)*dsin(m)
			+0.0021*dsin(2*m)
			-0.4068*dsin(mprime)
			+0.0161*dsin(2*mprime)
			-0.0004*dsin(3*mprime)
			+0.0104*dsin(2*f)
			-0.0051*dsin(m+mprime)
			-0.0074*dsin(m-mprime)
			+0.0004*dsin(2*f+m)
			-0.0004*dsin(2*f-m)
			-0.0006*dsin(2*f+mprime)
			+0.0010*dsin(2*f-mprime)
			+0.0005*dsin(m+2*mprime);
		} else if((Math.abs(phase-0.25)<0.01||(Math.abs(phase-0.75)<0.01))){
			pt+=(0.1721-0.0004*t)*dsin(m)
			+0.0021*dsin(2*m)
			-0.6280*dsin(mprime)
			+0.0089*dsin(2*mprime)
			-0.0004*dsin(3*mprime)
			+0.0079*dsin(2*f)
			-0.0119*dsin(m+mprime)
			-0.0047*dsin(m-mprime)
			+0.0003*dsin(2*f+m)
			-0.0004*dsin(2*f-m)
			-0.0006*dsin(2*f+mprime)
			+0.0021*dsin(2*f-mprime)
			+0.0003*dsin(m+2*mprime)
			+0.0004*dsin(m-2*mprime)
			-0.0003*dsin(2*m+mprime);
			if(phase<
			0.5)
			pt+=0.0028-0.0004*dcos(m)+0.0003*dcos(mprime);
			else
			pt+=-0.0028+0.0004*dcos(m)-0.0003*dcos(mprime);
		}
		return pt;
	}
	
	private static Calendar calendar(double j, TimeZone zone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeZone(TimeZone.getTimeZone("UTC"));
		
		int[] date = jyear(j);
		int[] time = jhms(j);
		
		calendar.set(Calendar.YEAR, date[0]);
		calendar.set(Calendar.MONTH, date[1] - 1);
		calendar.set(Calendar.DAY_OF_MONTH, date[2]);
		calendar.set(Calendar.HOUR_OF_DAY, time[0]);
		calendar.set(Calendar.MINUTE, time[1]);
		calendar.set(Calendar.SECOND, time[2]);
		calendar.set(Calendar.MILLISECOND, 0);
		
		Calendar localCal = Calendar.getInstance(zone);
		localCal.setTimeInMillis(calendar.getTimeInMillis());
		return localCal;
	}

	/**
	 * Get all events occurring in a given year, with zone adjustment applied.
	 * @param year The year.
	 * @param zone The local time zone.
	 * @return A list of phase events occurring during the year.
	 */
	public static List<MoonPhaseEvent> getYearEvents(int year, TimeZone zone) {
		
		if (lastCalculatedYear == year &&
				lastCalculatedZone != null && lastCalculatedZone.getID().equals(zone.getID()) &&
				lastCalculatedEvents != null) {
			return Collections.unmodifiableList(lastCalculatedEvents);
		} else {
			List<MoonPhaseEvent> events = new ArrayList<>();
			double k1=Math.floor((year-1900)*12.3685)-4;
			
			while (true) {
				
				double newTime = truephase(k1, 0);
				Calendar newCal = calendar(newTime, zone);
				if (newCal.get(Calendar.YEAR) == year) {
					events.add(new MoonPhaseEvent(MoonPhase.NEW, newCal));
				}
				
				double fqTime = truephase(k1, 0.25);
				Calendar fqCal = calendar(fqTime, zone);
				if (fqCal.get(Calendar.YEAR) == year) {
					events.add(new MoonPhaseEvent(MoonPhase.FIRST_QUARTER, fqCal));
				}
				
				double fullTime = truephase(k1, 0.5);
				Calendar fullCal = calendar(fullTime, zone);
				if (fullCal.get(Calendar.YEAR) == year) {
					events.add(new MoonPhaseEvent(MoonPhase.FULL, fullCal));
				}
				
				double lqTime = truephase(k1, 0.75);
				Calendar lqCal = calendar(lqTime, zone);
				if (lqCal.get(Calendar.YEAR) == year) {
					events.add(new MoonPhaseEvent(MoonPhase.LAST_QUARTER, lqCal));
				}
				
				if (newCal.get(Calendar.YEAR) > year && fqCal.get(Calendar.YEAR) > year && fullCal.get(Calendar.YEAR) > year && lqCal.get(Calendar.YEAR) > year) {
					break;
				}
				
				k1++;
			}
			
			lastCalculatedYear = year;
			lastCalculatedZone = zone;
			lastCalculatedEvents = events;
			return Collections.unmodifiableList(events);
		}
	}
	
	/**
	 * Check for event occurring on a particular day using pre-calculated list to save repetition
	 * while rendering calendars.
	 * @param dateMidnight Date.
	 * @param events Pre-calculated events for the year.
	 * @return An event, if any occurs.
	 */
	public static MoonPhaseEvent getDayEvent(Calendar dateMidnight, List<MoonPhaseEvent> events) {
		for (MoonPhaseEvent event : events) {
			if (event.getTime().get(Calendar.YEAR) == dateMidnight.get(Calendar.YEAR) &&
					event.getTime().get(Calendar.MONTH) == dateMidnight.get(Calendar.MONTH) &&
					event.getTime().get(Calendar.DAY_OF_MONTH) == dateMidnight.get(Calendar.DAY_OF_MONTH)) {
				return event;
			}
		}
		return null;
	}
	
	/**
	 * Check for an event occurring on a given day. Able to use last calculated year events result to save repeated calculations.
	 * @param dateMidnight The day calendar.
	 * @return An event, if any occurs.
	 */
	public synchronized static MoonPhaseEvent getDayEvent(Calendar dateMidnight) {
		
		List<MoonPhaseEvent> events;
		if (lastCalculatedYear == dateMidnight.get(Calendar.YEAR) &&
				lastCalculatedZone != null && lastCalculatedZone.getID().equals(dateMidnight.getTimeZone().getID()) &&
				lastCalculatedEvents != null) {
			events = lastCalculatedEvents;
		} else {
			events = getYearEvents(dateMidnight.get(Calendar.YEAR), dateMidnight.getTimeZone());
			lastCalculatedYear = dateMidnight.get(Calendar.YEAR);
			lastCalculatedZone = dateMidnight.getTimeZone();
			lastCalculatedEvents = events;
		}
		return getDayEvent(dateMidnight, events);
		
	}
	
	private static int lastCalculatedYear = 0;
	private static TimeZone lastCalculatedZone = null;
	private static List<MoonPhaseEvent> lastCalculatedEvents = null;
	
	/**
	 * Calculate intermediate phase based on events occurring either side. This should
	 * allow calendars to show phases that exactly match the event icons.
	 */
	public synchronized static double getNoonPhase(Calendar dateMidnight) {
		
		List<MoonPhaseEvent> events = getYearEvents(dateMidnight.get(Calendar.YEAR), dateMidnight.getTimeZone());
		
		MoonPhaseEvent before = null;
		MoonPhaseEvent after = null;

		Calendar dateNoon = Calendar.getInstance(dateMidnight.getTimeZone());
		dateNoon.setTimeInMillis(dateMidnight.getTimeInMillis());
		dateNoon.set(Calendar.HOUR_OF_DAY, 12);
		dateNoon.set(Calendar.MINUTE, 0);
		dateNoon.set(Calendar.SECOND, 0);
		
		// Get the last event before and first event after the specified date, or return
		// the event if one happens on the day.
		for (MoonPhaseEvent event : events) {
			if (event.getTime().getTimeInMillis() < dateNoon.getTimeInMillis()) {
				before = event;
			} else if (event.getTime().getTimeInMillis() > dateNoon.getTimeInMillis() && after == null) {
				after = event;
			}
		}
		
		// Length of a default phase in milliseconds.
		long defaultPhaseMs = 637860715;

		float msNoon = dateNoon.getTimeInMillis();
		float msBefore = 0;
		float msAfter = 0;
		double phaseBefore = 0;
		double phaseAfter;
		
		// At start or end of year, before or after can be null, but never both.
		if (before == null && after != null) {
			msAfter = after.getTime().getTimeInMillis();
			phaseAfter = after.getPhaseDouble();
			msBefore = msAfter - defaultPhaseMs;
			phaseBefore = phaseAfter == 0d ? 0.75d : phaseAfter - 0.25d;
		} else if (after == null && before != null) {
			msBefore = before.getTime().getTimeInMillis();
			phaseBefore = before.getPhaseDouble();
			msAfter = msBefore + defaultPhaseMs;
		} else if (after != null) {
			msBefore = before.getTime().getTimeInMillis();
			phaseBefore = before.getPhaseDouble();
			msAfter = after.getTime().getTimeInMillis();
		}
		
		return phaseBefore + (((msNoon - msBefore)/(msAfter - msBefore)) * 0.25);
		
	}
	
	public static int getIlluminatedPercent(double phase) {
		double angleD = phase * 360;
		double angleR = Math.toRadians(angleD);
		double cos = (Math.cos(angleR) + 1)/2d;
		double percent = (1 - cos) * 100;
		return (int)Math.round(percent);
	}
	
}
