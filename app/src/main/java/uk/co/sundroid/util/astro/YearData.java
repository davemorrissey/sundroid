package uk.co.sundroid.util.astro;

import android.support.annotation.NonNull;

import java.util.Calendar;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import static uk.co.sundroid.util.astro.YearData.EventType.*;

public class YearData {
	
	public static Set<Event> getYearEvents(int year, TimeZone timeZone) {
		
		Set<Event> eventsSet = new TreeSet<>();
		
		switch (year) {
			case 2000:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2000, 5, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2000, 0, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2000, 7, 35));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2000, 17, 28));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2000, 1, 48));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2000, 13, 37));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 0, 21, 2000, 4, 45, null, "http://en.m.wikipedia.org/wiki/January_2000_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 1, 5, 2000, 12, 50, "Magnitude: 0.579", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_February_5,_2000"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 6, 1, 2000, 19, 34, "Magnitude: 0.477", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_July_1,_2000"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 6, 16, 2000, 13, 57, null, "http://en.m.wikipedia.org/wiki/July_2000_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 6, 31, 2000, 2, 14, "Magnitude: 0.603", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_July_31,_2000"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 11, 25, 2000, 17, 36, "Magnitude: 0.723", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_December_25,_2000"));
				break;
			case 2001:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2001, 9, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2001, 14, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2001, 13, 31));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2001, 23, 4));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2001, 7, 38));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2001, 19, 21));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 0, 9, 2001, 20, 22, null, "http://en.m.wikipedia.org/wiki/January_2001_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 5, 21, 2001, 12, 5, "Magnitude: 1.050", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_June_21,_2001"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 6, 5, 2001, 14, 56, null, "http://en.m.wikipedia.org/wiki/July_2001_lunar_eclipse"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 11, 14, 2001, 20, 53, "Magnitude: 0.968", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_December_14,_2001"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 11, 30, 2001, 10, 30, null, "http://en.m.wikipedia.org/wiki/December_2001_lunar_eclipse"));
				break;
			case 2002:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 2, 2002, 14, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 6, 2002, 4, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2002, 19, 16));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2002, 4, 55));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2002, 13, 24));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2002, 1, 14));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 4, 26, 2002, 12, 4, null, "http://en.m.wikipedia.org/wiki/May_2002_lunar_eclipse"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 5, 10, 2002, 23, 45, "Magnitude: 0.996", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_June_10,_2002"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 5, 24, 2002, 21, 28, null, "http://en.m.wikipedia.org/wiki/June_2002_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 10, 20, 2002, 1, 48, null, "http://en.m.wikipedia.org/wiki/November_2002_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 11, 4, 2002, 7, 32, "Magnitude: 1.024", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_December_4,_2002"));
				break;
			case 2003:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2003, 5, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2003, 6, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 21, 2003, 1, 0));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2003, 10, 47));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2003, 19, 10));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2003, 7, 4));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 4, 16, 2003, 3, 41, null, "http://en.m.wikipedia.org/wiki/May_2003_lunar_eclipse"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 4, 31, 2003, 4, 9, "Magnitude: 0.938", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_May_31,_2003"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 10, 9, 2003, 1, 20, null, "http://en.m.wikipedia.org/wiki/November_2003_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 10, 23, 2003, 22, 50, "Magnitude: 1.038", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_November_23,_2003"));
				break;
			case 2004:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2004, 18, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 5, 2004, 11, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2004, 6, 49));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2004, 16, 30));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2004, 0, 57));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2004, 12, 42));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 3, 19, 2004, 13, 35, "Magnitude: 0.737", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_April_19,_2004"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 4, 4, 2004, 20, 31, null, "http://en.m.wikipedia.org/wiki/May_2004_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 9, 14, 2004, 3, 0, "Magnitude: 0.928", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_October_14,_2004"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 9, 28, 2004, 3, 5, null, "http://en.m.wikipedia.org/wiki/October_2004_lunar_eclipse"));
				break;
			case 2005:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 2, 2005, 1, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 5, 2005, 5, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2005, 12, 33));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2005, 22, 23));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2005, 6, 46));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2005, 18, 35));
				eventsSet.add(event(HYBRID_SOLAR, timeZone, 3, 8, 2005, 20, 37, "Magnitude: 1.007", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_April_8,_2005"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 3, 24, 2005, 9, 56, null, "http://en.m.wikipedia.org/wiki/April_2005_lunar_eclipse"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 9, 3, 2005, 10, 33, "Magnitude: 0.958", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_October_3,_2005"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 9, 17, 2005, 12, 4, null, "http://en.m.wikipedia.org/wiki/October_2005_lunar_eclipse"));
				break;
			case 2006:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2006, 16, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 3, 2006, 23, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2006, 18, 26));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2006, 4, 3));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2006, 12, 26));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2006, 0, 22));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 2, 14, 2006, 23, 49, null, "http://en.m.wikipedia.org/wiki/March_2006_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 2, 29, 2006, 10, 12, "Magnitude: 1.052", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_March_29,_2006"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 8, 7, 2006, 18, 52, null, "http://en.m.wikipedia.org/wiki/September_2006_lunar_eclipse"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 8, 22, 2006, 11, 41, "Magnitude: 0.935", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_September_22,_2006"));
				break;
			case 2007:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2007, 20, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 7, 2007, 0, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 21, 2007, 0, 7));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2007, 9, 51));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2007, 18, 6));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2007, 6, 8));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 2, 3, 2007, 23, 22, null, "http://en.m.wikipedia.org/wiki/March_2007_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 2, 19, 2007, 2, 33, "Magnitude: 0.876", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_March_19,_2007"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 7, 28, 2007, 10, 38, null, "http://en.m.wikipedia.org/wiki/August_2007_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 8, 11, 2007, 12, 32, "Magnitude: 0.751", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_September_11,_2007"));
				break;
			case 2008:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2008, 0, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2008, 8, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2008, 5, 48));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2008, 15, 44));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 20, 2008, 23, 59));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2008, 12, 4));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 1, 7, 2008, 3, 56, "Magnitude: 0.965", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_February_7,_2008"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 1, 21, 2008, 3, 27, null, "http://en.m.wikipedia.org/wiki/February_2008_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 7, 1, 2008, 10, 22, "Magnitude: 1.039", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_August_1,_2008"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 7, 16, 2008, 21, 11, null, "http://en.m.wikipedia.org/wiki/August_2008_lunar_eclipse"));
				break;
			case 2009:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2009, 15, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2009, 2, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2009, 11, 44));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2009, 21, 18));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2009, 5, 45));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2009, 17, 47));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 0, 26, 2009, 8, 0, "Magnitude: 0.928", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_January_26,_2009"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 1, 9, 2009, 14, 39, null, "http://en.m.wikipedia.org/wiki/February_2009_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 6, 7, 2009, 9, 40, null, "http://en.m.wikipedia.org/wiki/July_2009_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 6, 22, 2009, 2, 36, "Magnitude: 1.080", "http://en.m.wikipedia.org/wiki/July_2009_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 7, 6, 2009, 0, 40, null, "http://en.m.wikipedia.org/wiki/August_2009_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 11, 31, 2009, 19, 24, null, "http://en.m.wikipedia.org/wiki/December_2009_lunar_eclipse"));
				break;
			case 2010:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2010, 0, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 6, 2010, 12, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2010, 17, 32));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2010, 3, 9));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2010, 11, 28));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2010, 23, 38));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 0, 15, 2010, 7, 8, "Magnitude: 0.919", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_January_15,_2010"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 5, 26, 2010, 11, 40, null, "http://en.m.wikipedia.org/wiki/June_2010_lunar_eclipse"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 6, 11, 2010, 19, 35, "Magnitude: 1.058", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_July_11,_2010"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 11, 21, 2010, 8, 18, null, "http://en.m.wikipedia.org/wiki/December_2010_lunar_eclipse"));
				break;
			case 2011: 
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2011, 19, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2011, 15, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2011, 23, 21));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2011, 9, 4));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2011, 17, 16)); 
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2011, 5, 30));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 0, 4, 2011, 8, 52, "Magnitude: 0.858", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_January_4,_2011"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 5, 1, 2011, 21, 17, "Magnitude: 0.601", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_June_1,_2011"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 5, 15, 2011, 20, 14, null, "http://en.m.wikipedia.org/wiki/June_2011_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 6, 1, 2011, 8, 39, "Magnitude: 0.097", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_July_1,_2011"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 10, 25, 2011, 6, 21, "Magnitude: 0.905", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_November_25,_2011"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 11, 10, 2011, 14, 33, null, "http://en.m.wikipedia.org/wiki/December_2011_lunar_eclipse"));
				break;
			case 2012:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 5, 2012, 1, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 5, 2012, 4, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2012, 5, 14));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2012, 14, 49));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 20, 2012, 23, 9));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2012, 11, 11));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 4, 20, 2012, 23, 54, "Magnitude: 0.944", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_May_20,_2012"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 10, 13, 2012, 22, 13, "Magnitude: 1.050", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_November_13,_2012"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 5, 4, 2012, 11, 4, null, "http://en.m.wikipedia.org/wiki/June_2012_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 10, 28, 2012, 14, 34, null, "http://en.m.wikipedia.org/wiki/November_2012_lunar_eclipse"));
				break;
			case 2013:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 2, 2013, 5, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 5, 2013, 15, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2013, 11, 2));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2013, 20, 44));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2013, 5, 4));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2013, 17, 11));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 4, 10, 2013, 0, 26, "Magnitude: 0.954", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_May_10,_2013"));
				eventsSet.add(event(HYBRID_SOLAR, timeZone, 10, 3, 2013, 12, 48, "Magnitude: 1.016", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_November_3,_2013"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 3, 25, 2013, 20, 9, null, "http://en.m.wikipedia.org/wiki/April_2013_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 4, 25, 2013, 4, 11, null, "http://en.m.wikipedia.org/wiki/May_2013_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 9, 18, 2013, 23, 51, null, "http://en.m.wikipedia.org/wiki/October_2013_lunar_eclipse"));
				break;
			case 2014:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2014, 12, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2014, 0, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2014, 16, 57));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2014, 2, 29));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2014, 10, 51));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2014, 23, 3));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 3, 29, 2014, 6, 5, "Magnitude: 0.987", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_April_29,_2014"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 9, 23, 2014, 21, 46, "Magnitude: 0.811", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_October_23,_2014"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 3, 15, 2014, 7, 47, null, "http://en.m.wikipedia.org/wiki/April_2014_lunar_eclipse"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 9, 8, 2014, 10, 56, null, "http://en.m.wikipedia.org/wiki/October_2014_lunar_eclipse"));
				break;
			case 2015:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2015, 7, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 6, 2015, 20, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2015, 22, 45));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2015, 8, 20));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2015, 16, 38));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2015, 4, 48));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 2, 20, 2015, 9, 47, "Magnitude: 1.045", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_March_20,_2015"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 8, 13, 2015, 6, 55, "Magnitude: 0.788", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_September_13,_2015"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 3, 4, 2015, 12, 1, null, "http://en.m.wikipedia.org/wiki/April_2015_lunar_eclipse"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 8, 28, 2015, 2, 48, null, "http://en.m.wikipedia.org/wiki/September_2015_lunar_eclipse"));
				break;
			case 2016:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 2, 2016, 23, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2016, 16, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2016, 4, 30));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2016, 14, 21));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 20, 2016, 22, 34));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2016, 10, 44));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 2, 9, 2016, 1, 58, "Magnitude: 1.045", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_March_9,_2016"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 8, 1, 2016, 9, 8, "Magnitude: 0.974", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_September_1,_2016"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 2, 23, 2016, 11, 48, null, "http://en.m.wikipedia.org/wiki/March_2016_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 8, 16, 2016, 18, 55, null, "http://en.m.wikipedia.org/wiki/September_2016_lunar_eclipse"));
				break;
			case 2017:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 4, 2017, 14, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 3, 2017, 20, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2017, 10, 28));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2017, 20, 2));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2017, 4, 24));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2017, 16, 28));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 1, 26, 2017, 14, 55, "Magnitude: 0.992", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_February_26,_2017"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 7, 21, 2017, 18, 27, "Magnitude: 1.031", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_August_21,_2017"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 1, 11, 2017, 0, 45, null, "http://en.m.wikipedia.org/wiki/February_2017_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 7, 7, 2017, 18, 22, null, "http://en.m.wikipedia.org/wiki/August_2017_lunar_eclipse"));
				break;
			case 2018:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2018, 6, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 6, 2018, 17, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2018, 16, 15));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2018, 1, 54));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2018, 10, 7));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2018, 22, 22));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 1, 15, 2018, 20, 53, "Magnitude: 0.599", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_February_15,_2018"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 6, 13, 2018, 3, 2, "Magnitude: 0.336", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_July_13,_2018"));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 7, 11, 2018, 9, 47, "Magnitude: 0.737", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_August_11,_2018"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 0, 31, 2018, 13, 31, null, "http://en.m.wikipedia.org/wiki/January_2018_lunar_eclipse"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 6, 27, 2018, 20, 23, null, "http://en.m.wikipedia.org/wiki/July_2018_lunar_eclipse"));
				break;
			case 2019:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 3, 2019, 5, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2019, 22, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2019, 21, 58));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 23, 2019, 7, 50));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 21, 2019, 15, 54));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 22, 2019, 4, 19));
				eventsSet.add(event(PARTIAL_SOLAR, timeZone, 0, 6, 2019, 1, 43, "Magnitude: 0.715", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_January_6,_2019"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 6, 2, 2019, 19, 24, "Magnitude: 1.046", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_July_2,_2019"));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 11, 26, 2019, 5, 19, "Magnitude: 0.970", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_December_26,_2019"));
				eventsSet.add(event(TOTAL_LUNAR, timeZone, 0, 21, 2019, 5, 13, null, "http://en.m.wikipedia.org/wiki/January_2019_lunar_eclipse"));
				eventsSet.add(event(PARTIAL_LUNAR, timeZone, 6, 16, 2019, 21, 32, null, "http://en.m.wikipedia.org/wiki/July_2019_lunar_eclipse"));
				break;
			case 2020:
				eventsSet.add(event(EARTH_PERIHELION, timeZone, 0, 5, 2020, 8, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(EARTH_APHELION, timeZone, 6, 4, 2020, 12, 0, null, "http://en.wikipedia.org/wiki/Apsis#The_perihelion_and_aphelion_of_the_Earth"));
				eventsSet.add(event(MARCH_EQUINOX, timeZone, 2, 20, 2020, 3, 49));
				eventsSet.add(event(SEPTEMBER_EQUINOX, timeZone, 8, 22, 2020, 13, 30));
				eventsSet.add(event(NORTHERN_SOLSTICE, timeZone, 5, 20, 2020, 21, 43));
				eventsSet.add(event(SOUTHERN_SOLSTICE, timeZone, 11, 21, 2020, 10, 2));
				eventsSet.add(event(ANNULAR_SOLAR, timeZone, 5, 21, 2020, 6, 41, "Magnitude: 0.994", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_June_21,_2020"));
				eventsSet.add(event(TOTAL_SOLAR, timeZone, 11, 14, 2020, 16, 15, "Magnitude: 1.025", "http://en.m.wikipedia.org/wiki/Solar_eclipse_of_December_14,_2020"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 0, 10, 2020, 19, 11, null, "http://en.m.wikipedia.org/wiki/January_2020_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 5, 5, 2020, 19, 26, null, "http://en.m.wikipedia.org/wiki/June_2020_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 6, 5, 2020, 4, 31, null, "http://en.m.wikipedia.org/wiki/July_2020_lunar_eclipse"));
				eventsSet.add(event(PENUMBRAL_LUNAR, timeZone, 10, 30, 2020, 9, 44, null, "http://en.m.wikipedia.org/wiki/November_2020_lunar_eclipse"));
				break;
		}
		return eventsSet;
	}
	
    private static Event event(EventType eventType, TimeZone timeZone, int month, int day, int year, int hour, int minute) {
		return event(eventType, timeZone, month, day, year, hour, minute, null, null);
    }
    
    private static Event event(EventType eventType, TimeZone timeZone, int month, int day, int year, int hour, int minute, String extra, String link) {
    	Calendar time = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    	time.set(year, month, day, hour, minute, 0);
    	Calendar localTime = Calendar.getInstance(timeZone);
    	localTime.setTimeInMillis(time.getTimeInMillis());
    	return new Event(eventType, extra, localTime, link);
    }
    
    public static class Event implements Comparable<Event> {

    	public EventType type;
    	public Object extra;
    	public Calendar time;
    	public String link;

    	public Event(EventType type, Object extra, Calendar time, String link) {
    		this.type = type;
    		this.extra = extra;
    		this.time = time;
    		this.link = link;
    	}

		public int compareTo(@NonNull Event other) {
			int result = time.compareTo(other.time);
			if (result == 0) {
				return 1;
			}
			return result;
		}
		
    }
    
    public enum EventType {
    	
    	MARCH_EQUINOX ("March Equinox", Body.SUN),
    	SEPTEMBER_EQUINOX ("September Equinox", Body.SUN),
    	NORTHERN_SOLSTICE ("Northern Solstice", Body.SUN),
    	SOUTHERN_SOLSTICE ("Southern Solstice", Body.SUN),
    	PHASE (null, Body.MOON),
    	PARTIAL_SOLAR ("Partial Solar Eclipse", Body.SUN),
    	TOTAL_SOLAR ("Total Solar Eclipse", Body.SUN),
    	ANNULAR_SOLAR ("Annular Solar Eclipse", Body.SUN),
    	HYBRID_SOLAR ("Hybrid Solar Eclipse", Body.SUN),
    	PARTIAL_LUNAR ("Partial Lunar Eclipse", Body.MOON),
    	TOTAL_LUNAR ("Total Lunar Eclipse", Body.MOON),
    	PENUMBRAL_LUNAR ("Penumbral Lunar Eclipse", Body.MOON),
    	EARTH_APHELION ("Earth Aphelion", Body.SUN),
    	EARTH_PERIHELION ("Earth Perihelion", Body.SUN);
    	
    	public String name;
    	
    	public Body body;
    	
    	EventType(String name, Body body) {
    		this.name = name;
    		this.body = body;
    	}
    	
    }

}
