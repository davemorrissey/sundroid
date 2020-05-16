package uk.co.sundroid.util.astro

class Position(val timestamp: Long, val azimuth: Double, val appElevation: Double) {

    var julianDay: Double = 0.0
    var siderealTime: Double = 0.0
    var trueElevation: Double = 0.0
    var topoRA: Double = 0.0
    var topoDec: Double = 0.0
    var topoDistKm: Double = 0.0
    var topoDistEarthRadii: Double = 0.0
    var geoRA: Double = 0.0
    var geoDec: Double = 0.0
    var geoDistKm: Double = 0.0
    var geoDistEarthRadii: Double = 0.0

    var moonAge: Double = 0.0
    var moonPhase: Double = 0.0
    var moonIllumination: Double = 0.0

}
