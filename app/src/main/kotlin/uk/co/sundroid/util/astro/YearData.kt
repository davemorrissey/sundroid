package uk.co.sundroid.util.astro

import java.util.Calendar
import java.util.Calendar.*
import java.util.TimeZone
import java.util.TreeSet

import uk.co.sundroid.util.astro.YearData.EventType.*

object YearData {
    
    private const val WIKI = "http://en.m.wikipedia.org/wiki/"

    fun getYearEvents(year: Int, timeZone: TimeZone): MutableSet<Event> {

        val eventsSet = TreeSet<Event>()

        eventsSet.apply {
            when (year) {
                2000 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2000, 5, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2000, 0, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2000, 7, 35))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2000, 17, 28))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2000, 1, 48))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2000, 13, 37))
                    add(event(TOTAL_LUNAR, timeZone, JANUARY, 21, 2000, 4, 45, null, "${WIKI}January_2000_lunar_eclipse"))
                    add(event(PARTIAL_SOLAR, timeZone, FEBRUARY, 5, 2000, 12, 50, "Magnitude: 0.579", "${WIKI}Solar_eclipse_of_February_5,_2000"))
                    add(event(PARTIAL_SOLAR, timeZone, JULY, 1, 2000, 19, 34, "Magnitude: 0.477", "${WIKI}Solar_eclipse_of_July_1,_2000"))
                    add(event(TOTAL_LUNAR, timeZone, JULY, 16, 2000, 13, 57, null, "${WIKI}July_2000_lunar_eclipse"))
                    add(event(PARTIAL_SOLAR, timeZone, JULY, 31, 2000, 2, 14, "Magnitude: 0.603", "${WIKI}Solar_eclipse_of_July_31,_2000"))
                    add(event(PARTIAL_SOLAR, timeZone, DECEMBER, 25, 2000, 17, 36, "Magnitude: 0.723", "${WIKI}Solar_eclipse_of_December_25,_2000"))
                }
                2001 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2001, 9, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2001, 14, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2001, 13, 31))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2001, 23, 4))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2001, 7, 38))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2001, 19, 21))
                    add(event(TOTAL_LUNAR, timeZone, JANUARY, 9, 2001, 20, 22, null, "${WIKI}January_2001_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, JUNE, 21, 2001, 12, 5, "Magnitude: 1.050", "${WIKI}Solar_eclipse_of_June_21,_2001"))
                    add(event(PARTIAL_LUNAR, timeZone, JULY, 5, 2001, 14, 56, null, "${WIKI}July_2001_lunar_eclipse"))
                    add(event(ANNULAR_SOLAR, timeZone, DECEMBER, 14, 2001, 20, 53, "Magnitude: 0.968", "${WIKI}Solar_eclipse_of_December_14,_2001"))
                    add(event(PENUMBRAL_LUNAR, timeZone, DECEMBER, 30, 2001, 10, 30, null, "${WIKI}December_2001_lunar_eclipse"))
                }
                2002 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 2, 2002, 14, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2002, 4, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2002, 19, 16))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2002, 4, 55))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2002, 13, 24))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2002, 1, 14))
                    add(event(PENUMBRAL_LUNAR, timeZone, MAY, 26, 2002, 12, 4, null, "${WIKI}May_2002_lunar_eclipse"))
                    add(event(ANNULAR_SOLAR, timeZone, JUNE, 10, 2002, 23, 45, "Magnitude: 0.996", "${WIKI}Solar_eclipse_of_June_10,_2002"))
                    add(event(PENUMBRAL_LUNAR, timeZone, JUNE, 24, 2002, 21, 28, null, "${WIKI}June_2002_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, NOVEMBER, 20, 2002, 1, 48, null, "${WIKI}November_2002_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, DECEMBER, 4, 2002, 7, 32, "Magnitude: 1.024", "${WIKI}Solar_eclipse_of_December_4,_2002"))
                }
                2003 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2003, 5, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2003, 6, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 21, 2003, 1, 0))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2003, 10, 47))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2003, 19, 10))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2003, 7, 4))
                    add(event(TOTAL_LUNAR, timeZone, MAY, 16, 2003, 3, 41, null, "${WIKI}May_2003_lunar_eclipse"))
                    add(event(ANNULAR_SOLAR, timeZone, MAY, 31, 2003, 4, 9, "Magnitude: 0.938", "${WIKI}Solar_eclipse_of_May_31,_2003"))
                    add(event(TOTAL_LUNAR, timeZone, NOVEMBER, 9, 2003, 1, 20, null, "${WIKI}November_2003_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, NOVEMBER, 23, 2003, 22, 50, "Magnitude: 1.038", "${WIKI}Solar_eclipse_of_November_23,_2003"))
                }
                2004 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2004, 18, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2004, 11, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2004, 6, 49))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2004, 16, 30))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2004, 0, 57))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2004, 12, 42))
                    add(event(PARTIAL_SOLAR, timeZone, APRIL, 19, 2004, 13, 35, "Magnitude: 0.737", "${WIKI}Solar_eclipse_of_April_19,_2004"))
                    add(event(TOTAL_LUNAR, timeZone, MAY, 4, 2004, 20, 31, null, "${WIKI}May_2004_lunar_eclipse"))
                    add(event(PARTIAL_SOLAR, timeZone, OCTOBER, 14, 2004, 3, 0, "Magnitude: 0.928", "${WIKI}Solar_eclipse_of_October_14,_2004"))
                    add(event(TOTAL_LUNAR, timeZone, OCTOBER, 28, 2004, 3, 5, null, "${WIKI}October_2004_lunar_eclipse"))
                }
                2005 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 2, 2005, 1, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2005, 5, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2005, 12, 33))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2005, 22, 23))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2005, 6, 46))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2005, 18, 35))
                    add(event(HYBRID_SOLAR, timeZone, APRIL, 8, 2005, 20, 37, "Magnitude: 1.007", "${WIKI}Solar_eclipse_of_April_8,_2005"))
                    add(event(PENUMBRAL_LUNAR, timeZone, APRIL, 24, 2005, 9, 56, null, "${WIKI}April_2005_lunar_eclipse"))
                    add(event(ANNULAR_SOLAR, timeZone, OCTOBER, 3, 2005, 10, 33, "Magnitude: 0.958", "${WIKI}Solar_eclipse_of_October_3,_2005"))
                    add(event(PARTIAL_LUNAR, timeZone, OCTOBER, 17, 2005, 12, 4, null, "${WIKI}October_2005_lunar_eclipse"))
                }
                2006 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2006, 16, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 3, 2006, 23, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2006, 18, 26))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2006, 4, 3))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2006, 12, 26))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2006, 0, 22))
                    add(event(PENUMBRAL_LUNAR, timeZone, MARCH, 14, 2006, 23, 49, null, "${WIKI}March_2006_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, MARCH, 29, 2006, 10, 12, "Magnitude: 1.052", "${WIKI}Solar_eclipse_of_March_29,_2006"))
                    add(event(PARTIAL_LUNAR, timeZone, SEPTEMBER, 7, 2006, 18, 52, null, "${WIKI}September_2006_lunar_eclipse"))
                    add(event(ANNULAR_SOLAR, timeZone, SEPTEMBER, 22, 2006, 11, 41, "Magnitude: 0.935", "${WIKI}Solar_eclipse_of_September_22,_2006"))
                }
                2007 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2007, 20, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 7, 2007, 0, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 21, 2007, 0, 7))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2007, 9, 51))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2007, 18, 6))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2007, 6, 8))
                    add(event(TOTAL_LUNAR, timeZone, MARCH, 3, 2007, 23, 22, null, "${WIKI}March_2007_lunar_eclipse"))
                    add(event(PARTIAL_SOLAR, timeZone, MARCH, 19, 2007, 2, 33, "Magnitude: 0.876", "${WIKI}Solar_eclipse_of_March_19,_2007"))
                    add(event(TOTAL_LUNAR, timeZone, AUGUST, 28, 2007, 10, 38, null, "${WIKI}August_2007_lunar_eclipse"))
                    add(event(PARTIAL_SOLAR, timeZone, SEPTEMBER, 11, 2007, 12, 32, "Magnitude: 0.751", "${WIKI}Solar_eclipse_of_September_11,_2007"))
                }
                2008 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2008, 0, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2008, 8, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2008, 5, 48))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2008, 15, 44))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 20, 2008, 23, 59))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2008, 12, 4))
                    add(event(ANNULAR_SOLAR, timeZone, FEBRUARY, 7, 2008, 3, 56, "Magnitude: 0.965", "${WIKI}Solar_eclipse_of_February_7,_2008"))
                    add(event(TOTAL_LUNAR, timeZone, FEBRUARY, 21, 2008, 3, 27, null, "${WIKI}February_2008_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, AUGUST, 1, 2008, 10, 22, "Magnitude: 1.039", "${WIKI}Solar_eclipse_of_August_1,_2008"))
                    add(event(PARTIAL_LUNAR, timeZone, AUGUST, 16, 2008, 21, 11, null, "${WIKI}August_2008_lunar_eclipse"))
                }
                2009 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2009, 15, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2009, 2, 0, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2009, 11, 44))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2009, 21, 18))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2009, 5, 45))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2009, 17, 47))
                    add(event(ANNULAR_SOLAR, timeZone, JANUARY, 26, 2009, 8, 0, "Magnitude: 0.928", "${WIKI}Solar_eclipse_of_January_26,_2009"))
                    add(event(PENUMBRAL_LUNAR, timeZone, FEBRUARY, 9, 2009, 14, 39, null, "${WIKI}February_2009_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, JULY, 7, 2009, 9, 40, null, "${WIKI}July_2009_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, JULY, 22, 2009, 2, 36, "Magnitude: 1.080", "${WIKI}July_2009_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, AUGUST, 6, 2009, 0, 40, null, "${WIKI}August_2009_lunar_eclipse"))
                    add(event(PARTIAL_LUNAR, timeZone, DECEMBER, 31, 2009, 19, 24, null, "${WIKI}December_2009_lunar_eclipse"))
                }
                2010 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2010, 0, 9, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2010, 11, 30, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2010, 17, 32))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2010, 3, 9))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2010, 11, 28))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2010, 23, 38))
                    add(event(ANNULAR_SOLAR, timeZone, JANUARY, 15, 2010, 7, 8, "Magnitude: 0.919", "${WIKI}Solar_eclipse_of_January_15,_2010"))
                    add(event(PARTIAL_LUNAR, timeZone, JUNE, 26, 2010, 11, 40, null, "${WIKI}June_2010_lunar_eclipse"))
                    add(event(TOTAL_SOLAR, timeZone, JULY, 11, 2010, 19, 35, "Magnitude: 1.058", "${WIKI}Solar_eclipse_of_July_11,_2010"))
                    add(event(TOTAL_LUNAR, timeZone, DECEMBER, 21, 2010, 8, 18, null, "${WIKI}December_2010_lunar_eclipse"))
                }
                2011 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2011, 18, 32, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2011, 14, 54, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2011, 23, 21))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2011, 9, 4))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2011, 17, 16))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2011, 5, 30))
                    add(event(PARTIAL_SOLAR, timeZone, JANUARY, 4, 2011, 8, 52, "Magnitude: 0.858", "${WIKI}Solar_eclipse_of_January_4,_2011"))
                    add(event(PARTIAL_SOLAR, timeZone, JUNE, 1, 2011, 21, 17, "Magnitude: 0.601", "${WIKI}Solar_eclipse_of_June_1,_2011"))
                    add(event(TOTAL_LUNAR, timeZone, JUNE, 15, 2011, 20, 14, null, "${WIKI}June_2011_lunar_eclipse"))
                    add(event(PARTIAL_SOLAR, timeZone, JULY, 1, 2011, 8, 39, "Magnitude: 0.097", "${WIKI}Solar_eclipse_of_July_1,_2011"))
                    add(event(PARTIAL_SOLAR, timeZone, NOVEMBER, 25, 2011, 6, 21, "Magnitude: 0.905", "${WIKI}Solar_eclipse_of_November_25,_2011"))
                    add(event(TOTAL_LUNAR, timeZone, DECEMBER, 10, 2011, 14, 33, null, "${WIKI}December_2011_lunar_eclipse"))
                }
                2012 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 5, 2012, 0, 32, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2012, 3, 32, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2012, 5, 14))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2012, 14, 49))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 20, 2012, 23, 9))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2012, 11, 11))
                    add(event(ANNULAR_SOLAR, timeZone, MAY, 20, 2012, 23, 54, "Magnitude: 0.944", "${WIKI}Solar_eclipse_of_May_20,_2012"))
                    add(event(TOTAL_SOLAR, timeZone, NOVEMBER, 13, 2012, 22, 13, "Magnitude: 1.050", "${WIKI}Solar_eclipse_of_November_13,_2012"))
                    add(event(PARTIAL_LUNAR, timeZone, JUNE, 4, 2012, 11, 4, null, "${WIKI}June_2012_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, NOVEMBER, 28, 2012, 14, 34, null, "${WIKI}November_2012_lunar_eclipse"))
                }
                2013 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 2, 2013, 4, 38, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2013, 14, 44, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2013, 11, 2))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2013, 20, 44))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2013, 5, 4))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2013, 17, 11))
                    add(event(ANNULAR_SOLAR, timeZone, MAY, 10, 2013, 0, 26, "Magnitude: 0.954", "${WIKI}Solar_eclipse_of_May_10,_2013"))
                    add(event(HYBRID_SOLAR, timeZone, NOVEMBER, 3, 2013, 12, 48, "Magnitude: 1.016", "${WIKI}Solar_eclipse_of_November_3,_2013"))
                    add(event(PARTIAL_LUNAR, timeZone, APRIL, 25, 2013, 20, 9, null, "${WIKI}April_2013_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, MAY, 25, 2013, 4, 11, null, "${WIKI}May_2013_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, OCTOBER, 18, 2013, 23, 51, null, "${WIKI}October_2013_lunar_eclipse"))
                }
                2014 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2014, 11, 59, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2014, 0, 13, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2014, 16, 57))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2014, 2, 29))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2014, 10, 51))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2014, 23, 3))
                    add(event(ANNULAR_SOLAR, timeZone, APRIL, 29, 2014, 6, 5, "Magnitude: 0.987", "${WIKI}Solar_eclipse_of_April_29,_2014"))
                    add(event(PARTIAL_SOLAR, timeZone, OCTOBER, 23, 2014, 21, 46, "Magnitude: 0.811", "${WIKI}Solar_eclipse_of_October_23,_2014"))
                    add(event(TOTAL_LUNAR, timeZone, APRIL, 15, 2014, 7, 47, null, "${WIKI}April_2014_lunar_eclipse"))
                    add(event(TOTAL_LUNAR, timeZone, OCTOBER, 8, 2014, 10, 56, null, "${WIKI}October_2014_lunar_eclipse"))
                }
                2015 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2015, 6, 36, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2015, 19, 40, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2015, 22, 45))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2015, 8, 21))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2015, 16, 38))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2015, 4, 48))
                    add(event(TOTAL_SOLAR, timeZone, MARCH, 20, 2015, 9, 47, "Magnitude: 1.045", "${WIKI}Solar_eclipse_of_March_20,_2015"))
                    add(event(PARTIAL_SOLAR, timeZone, SEPTEMBER, 13, 2015, 6, 55, "Magnitude: 0.788", "${WIKI}Solar_eclipse_of_September_13,_2015"))
                    add(event(TOTAL_LUNAR, timeZone, APRIL, 4, 2015, 12, 1, null, "${WIKI}April_2015_lunar_eclipse"))
                    add(event(TOTAL_LUNAR, timeZone, SEPTEMBER, 28, 2015, 2, 48, null, "${WIKI}September_2015_lunar_eclipse"))
                }
                2016 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 2, 2016, 22, 49, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2016, 16, 24, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2016, 4, 30))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2016, 14, 21))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 20, 2016, 22, 34))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2016, 10, 44))
                    add(event(TOTAL_SOLAR, timeZone, MARCH, 9, 2016, 1, 58, "Magnitude: 1.045", "${WIKI}Solar_eclipse_of_March_9,_2016"))
                    add(event(ANNULAR_SOLAR, timeZone, SEPTEMBER, 1, 2016, 9, 8, "Magnitude: 0.974", "${WIKI}Solar_eclipse_of_September_1,_2016"))
                    add(event(PENUMBRAL_LUNAR, timeZone, MARCH, 23, 2016, 11, 48, null, "${WIKI}March_2016_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, SEPTEMBER, 16, 2016, 18, 55, null, "${WIKI}September_2016_lunar_eclipse"))
                }
                2017 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2017, 14, 18, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 3, 2017, 20, 11, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2017, 10, 28))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2017, 20, 2))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2017, 4, 24))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2017, 16, 28))
                    add(event(ANNULAR_SOLAR, timeZone, FEBRUARY, 26, 2017, 14, 55, "Magnitude: 0.992", "${WIKI}Solar_eclipse_of_February_26,_2017"))
                    add(event(TOTAL_SOLAR, timeZone, AUGUST, 21, 2017, 18, 27, "Magnitude: 1.031", "${WIKI}Solar_eclipse_of_August_21,_2017"))
                    add(event(PENUMBRAL_LUNAR, timeZone, FEBRUARY, 11, 2017, 0, 45, null, "${WIKI}February_2017_lunar_eclipse"))
                    add(event(PARTIAL_LUNAR, timeZone, AUGUST, 7, 2017, 18, 22, null, "${WIKI}August_2017_lunar_eclipse"))
                }
                2018 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2018, 5, 35, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2018, 16, 47, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2018, 16, 15))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2018, 1, 54))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2018, 10, 7))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2018, 22, 23))
                    add(event(PARTIAL_SOLAR, timeZone, FEBRUARY, 15, 2018, 20, 53, "Magnitude: 0.599", "${WIKI}Solar_eclipse_of_February_15,_2018"))
                    add(event(PARTIAL_SOLAR, timeZone, JULY, 13, 2018, 3, 2, "Magnitude: 0.336", "${WIKI}Solar_eclipse_of_July_13,_2018"))
                    add(event(PARTIAL_SOLAR, timeZone, AUGUST, 11, 2018, 9, 47, "Magnitude: 0.737", "${WIKI}Solar_eclipse_of_August_11,_2018"))
                    add(event(TOTAL_LUNAR, timeZone, JANUARY, 31, 2018, 13, 31, null, "${WIKI}January_2018_lunar_eclipse"))
                    add(event(TOTAL_LUNAR, timeZone, JULY, 27, 2018, 20, 23, null, "${WIKI}July_2018_lunar_eclipse"))
                }
                2019 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2019, 5, 20, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2019, 22, 11, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2019, 21, 58))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2019, 7, 50))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2019, 15, 54))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2019, 4, 19))
                    add(event(PARTIAL_SOLAR, timeZone, JANUARY, 6, 2019, 1, 43, "Magnitude: 0.715", "${WIKI}Solar_eclipse_of_January_6,_2019"))
                    add(event(TOTAL_SOLAR, timeZone, JULY, 2, 2019, 19, 24, "Magnitude: 1.046", "${WIKI}Solar_eclipse_of_July_2,_2019"))
                    add(event(ANNULAR_SOLAR, timeZone, DECEMBER, 26, 2019, 5, 19, "Magnitude: 0.970", "${WIKI}Solar_eclipse_of_December_26,_2019"))
                    add(event(TOTAL_LUNAR, timeZone, JANUARY, 21, 2019, 5, 13, null, "${WIKI}January_2019_lunar_eclipse"))
                    add(event(PARTIAL_LUNAR, timeZone, JULY, 16, 2019, 21, 32, null, "${WIKI}July_2019_lunar_eclipse"))
                }
                2020 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 5, 2020, 7, 48, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2020, 11, 35, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2020, 3, 50))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2020, 13, 31))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 20, 2020, 21, 44))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2020, 10, 2))
                    add(event(ANNULAR_SOLAR, timeZone, JUNE, 21, 2020, 6, 41, "Magnitude: 0.994", "${WIKI}Solar_eclipse_of_June_21,_2020"))
                    add(event(TOTAL_SOLAR, timeZone, DECEMBER, 14, 2020, 16, 15, "Magnitude: 1.025", "${WIKI}Solar_eclipse_of_December_14,_2020"))
                    add(event(PENUMBRAL_LUNAR, timeZone, JANUARY, 10, 2020, 19, 11, null, "${WIKI}January_2020_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, JUNE, 5, 2020, 19, 26, null, "${WIKI}June_2020_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, JULY, 5, 2020, 4, 31, null, "${WIKI}July_2020_lunar_eclipse"))
                    add(event(PENUMBRAL_LUNAR, timeZone, NOVEMBER, 30, 2020, 9, 44, null, "${WIKI}November_2020_lunar_eclipse"))
                }
                2021 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 2, 2021, 13, 51, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2021, 22, 27, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2021, 9, 37))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2021, 3, 32))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2021, 19, 21))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2021, 15, 59))
                    add(event(ANNULAR_SOLAR, timeZone, JUNE, 10, 2021, 10, 43, "Magnitude: 0.9435", "${WIKI}Solar_eclipse_of_June_10,_2021"))
                    add(event(TOTAL_SOLAR, timeZone, DECEMBER, 4, 2021, 7, 35, "Magnitude: 1.0367", "${WIKI}Solar_eclipse_of_December_4,_2021"))
                    add(event(TOTAL_LUNAR, timeZone, MAY, 26, 2021, 11, 19, null, "${WIKI}May_2021_lunar_eclipse"))
                    add(event(PARTIAL_LUNAR, timeZone, NOVEMBER, 19, 2021, 9, 3, null, "${WIKI}November_2021_lunar_eclipse"))
                }
                2022 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2022, 6, 55, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 4, 2022, 7, 11, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2022, 15, 33))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2022, 9, 14))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2022, 1, 4))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2022, 21, 48))
                    add(event(PARTIAL_SOLAR, timeZone, APRIL, 30, 2022, 20, 43, "Magnitude: 0.6396", "${WIKI}Solar_eclipse_of_April_30,_2022"))
                    add(event(PARTIAL_SOLAR, timeZone, OCTOBER, 25, 2022, 11, 1, "Magnitude: 0.8623", "${WIKI}Solar_eclipse_of_October_25,_2022"))
                    add(event(TOTAL_LUNAR, timeZone, MAY, 16, 2022, 4, 11, null, "${WIKI}May_2022_lunar_eclipse"))
                    add(event(TOTAL_LUNAR, timeZone, NOVEMBER, 8, 2022, 10, 59, null, "${WIKI}November_2022_lunar_eclipse"))
                }
                2023 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2023, 16, 17, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2023, 20, 7, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2023, 21, 24))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2023, 14, 58))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 23, 2023, 6, 50))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 22, 2023, 3, 27))
                    add(event(TOTAL_SOLAR, timeZone, APRIL, 20, 2023, 4, 18, "Magnitude: 1.0132", "${WIKI}Solar_eclipse_of_April_20,_2023"))
                    add(event(ANNULAR_SOLAR, timeZone, OCTOBER, 14, 2023, 18, 1, "Magnitude: 0.952", "${WIKI}Solar_eclipse_of_October_14,_2023"))
                    add(event(PENUMBRAL_LUNAR, timeZone, MAY, 5, 2023, 17, 23, null, "${WIKI}May_2023_lunar_eclipse"))
                    add(event(PARTIAL_LUNAR, timeZone, OCTOBER, 28, 2023, 20, 14, null, "${WIKI}October_2023_lunar_eclipse"))
                }
                2024 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2024, 0, 39, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2024, 5, 6, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2024, 3, 7))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 20, 2024, 20, 51))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2024, 12, 44))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2024, 9, 20))
                    add(event(TOTAL_SOLAR, timeZone, APRIL, 8, 2024, 18, 18, "Magnitude: 1.0566", "${WIKI}Solar_eclipse_of_April_8,_2024"))
                    add(event(ANNULAR_SOLAR, timeZone, OCTOBER, 2, 2024, 18, 46, "Magnitude: 0.9326", "${WIKI}Solar_eclipse_of_October_2,_2024"))

                    add(event(PENUMBRAL_LUNAR, timeZone, MARCH, 25, 2024, 7, 13, null, "${WIKI}March_2024_lunar_eclipse"))
                    add(event(PARTIAL_LUNAR, timeZone, SEPTEMBER, 18, 2024, 2, 44, null, "${WIKI}September_2024_lunar_eclipse"))

                }
                2025 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 4, 2025, 13, 28, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 3, 2025, 19, 55, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(MARCH_EQUINOX, timeZone, MARCH, 20, 2025, 9, 2))
                    add(event(NORTHERN_SOLSTICE, timeZone, JUNE, 21, 2025, 2, 42))
                    add(event(SEPTEMBER_EQUINOX, timeZone, SEPTEMBER, 22, 2025, 18, 20))
                    add(event(SOUTHERN_SOLSTICE, timeZone, DECEMBER, 21, 2025, 15, 3))
                    add(event(PARTIAL_SOLAR, timeZone, MARCH, 29, 2025, 10, 49, "Magnitude: 0.9376", "${WIKI}Solar_eclipse_of_March_29,_2025"))
                    add(event(ANNULAR_SOLAR, timeZone, SEPTEMBER, 21, 2025, 19, 43, "Magnitude: 0.855", "${WIKI}Solar_eclipse_of_September_21,_2025"))
                    add(event(TOTAL_LUNAR, timeZone, MARCH, 14, 2025, 6, 59, null, "${WIKI}March_2025_lunar_eclipse"))
                    add(event(TOTAL_LUNAR, timeZone, SEPTEMBER, 7, 2025, 18, 12, null, "${WIKI}September_2025_lunar_eclipse"))
                }
                2026 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2026, 17, 16, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2026, 17, 31, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                }
                2027 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 3, 2027, 2, 33, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 5, 2027, 5, 6, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                }
                2028 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 5, 2028, 12, 28, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 3, 2028, 22, 18, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))

                }
                2029 -> {
                    add(event(EARTH_PERIHELION, timeZone, JANUARY, 2, 2029, 18, 13, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                    add(event(EARTH_APHELION, timeZone, JULY, 6, 2029, 5, 12, null, "${WIKI}Apsis#The_perihelion_and_aphelion_of_the_Earth"))
                }
            }
        }
        return eventsSet
    }

    private fun event(eventType: EventType, timeZone: TimeZone, month: Int, day: Int, year: Int, hour: Int, minute: Int, extra: String? = null, link: String? = null): Event {
        val time = getInstance(TimeZone.getTimeZone("UTC"))
        time.set(year, month, day, hour, minute, 0)
        val localTime = getInstance(timeZone)
        localTime.timeInMillis = time.timeInMillis
        return Event(eventType, extra, localTime, link)
    }

    class Event(var type: EventType, var extra: Any?, var time: Calendar, var link: String?) : Comparable<Event> {

        override fun compareTo(other: Event): Int {
            val result = time.compareTo(other.time)
            return if (result == 0) {
                1
            } else result
        }

    }

    enum class EventType constructor(val displayName: String, val body: Body) {

        MARCH_EQUINOX("March Equinox", Body.SUN),
        SEPTEMBER_EQUINOX("September Equinox", Body.SUN),
        NORTHERN_SOLSTICE("Northern Solstice", Body.SUN),
        SOUTHERN_SOLSTICE("Southern Solstice", Body.SUN),
        PHASE("Moon Phase", Body.MOON),
        PARTIAL_SOLAR("Partial Solar Eclipse", Body.SUN),
        TOTAL_SOLAR("Total Solar Eclipse", Body.SUN),
        ANNULAR_SOLAR("Annular Solar Eclipse", Body.SUN),
        HYBRID_SOLAR("Hybrid Solar Eclipse", Body.SUN),
        PARTIAL_LUNAR("Partial Lunar Eclipse", Body.MOON),
        TOTAL_LUNAR("Total Lunar Eclipse", Body.MOON),
        PENUMBRAL_LUNAR("Penumbral Lunar Eclipse", Body.MOON),
        EARTH_APHELION("Earth Aphelion", Body.SUN),
        EARTH_PERIHELION("Earth Perihelion", Body.SUN)

    }

}
