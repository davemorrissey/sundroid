package uk.co.sundroid.activity.data.fragments

import uk.co.sundroid.util.astro.Body

enum class CalendarView constructor(val body: Body?, val type: String?, val grid: Boolean, val title: String = "") {

    SUN_RISE_SET_LIST(Body.SUN, "sun", false, "Sunrise and sunset"),
    SUN_RISE_SET_GRID(Body.SUN, "sun", true, "Sunrise and sunset"),
    CIVIL_DAWN_DUSK_LIST(null, "civ", false, "Civil dawn and dusk"),
    CIVIL_DAWN_DUSK_GRID(null, "civ", true, "Civil dawn and dusk"),
    NAUTICAL_DAWN_DUSK_LIST(null, "ntc", false, "Nautical dawn and dusk"),
    NAUTICAL_DAWN_DUSK_GRID(null, "ntc", true, "Nautical dawn and dusk"),
    ASTRONOMICAL_DAWN_DUSK_LIST(null, "ast", false, "Astronomical dawn and dusk"),
    ASTRONOMICAL_DAWN_DUSK_GRID(null, "ast", true, "Astronomical dawn and dusk"),
    LENGTH_OF_DAYLIGHT_LIST(null, "daylight", false, "Length of daylight"),
    LENGTH_OF_DAYLIGHT_GRID(null, "daylight", true, "Length of daylight"),
    MOON_RISE_SET_LIST(Body.MOON, "moon", false, "Moonrise and moonset"),
    MOON_RISE_SET_GRID(Body.MOON, "moon", true, "Moonrise and moonset"),
    MERCURY_RISE_SET_LIST(Body.MERCURY, null, false, "Mercury rise and set"),
    MERCURY_RISE_SET_GRID(Body.MERCURY, null, true, "Mercury rise and set"),
    VENUS_RISE_SET_LIST(Body.VENUS, null, false, "Venus rise and set"),
    VENUS_RISE_SET_GRID(Body.VENUS, null, true, "Venus rise and set"),
    MARS_RISE_SET_LIST(Body.MARS, null, false, "Mars rise and set"),
    MARS_RISE_SET_GRID(Body.MARS, null, true, "Mars rise and set"),
    JUPITER_RISE_SET_LIST(Body.JUPITER, null, false, "Jupiter rise and set"),
    JUPITER_RISE_SET_GRID(Body.JUPITER, null, true, "Jupiter rise and set"),
    SATURN_RISE_SET_LIST(Body.SATURN, null, false, "Saturn rise and set"),
    SATURN_RISE_SET_GRID(Body.SATURN, null, true, "Saturn rise and set"),
    URANUS_RISE_SET_LIST(Body.URANUS, null, false, "Uranus rise and set"),
    URANUS_RISE_SET_GRID(Body.URANUS, null, true, "Uranus rise and set"),
    NEPTUNE_RISE_SET_LIST(Body.NEPTUNE, null, false, "Neptune rise and set"),
    NEPTUNE_RISE_SET_GRID(Body.NEPTUNE, null, true, "Neptune rise and set");

}