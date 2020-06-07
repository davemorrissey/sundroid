package uk.co.sundroid.util.astro.smc

/**
 * Holds the geocentric position of a body, with moon phase and age when relevant. Originally
 * represented by a double array in SunMoonCalculator.
 */
class SmcPosition(
        val eclipticLongitude: Double,
        val eclipticLatitude: Double,
        val distance: Double,
        val angularRadius: Double,
        val moonAge: Double = 0.0,
        val moonPhase: Double = 0.0
)
