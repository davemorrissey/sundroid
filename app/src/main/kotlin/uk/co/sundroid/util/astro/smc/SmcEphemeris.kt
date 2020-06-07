package uk.co.sundroid.util.astro.smc

/**
 * Class to hold the results of ephemerides. Angles in radians, event times as UT Julian days,
 * distances in AU.
 * @author T. Alonso Albi - OAN (Spain)
 */
class SmcEphemeris(
        var azimuth: Double,
        var elevation: Double,
        var rise: SmcEventEphemeris?,
        var set: SmcEventEphemeris?,
        var transit: SmcEventEphemeris?,
        var rightAscension: Double,
        var declination: Double,
        var distance: Double,
        var eclipticLongitude: Double,
        var eclipticLatitude: Double,
        var angularRadius: Double) {
    var moonIllumination = 100.0
    var moonAge = 0.0
    var moonPhase = 0.0
}
