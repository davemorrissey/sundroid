package uk.co.sundroid.util.astro

import java.util.Calendar


open class BodyDay {

    var rise: Calendar? = null

    var set: Calendar? = null

    var transit: Calendar? = null

    var riseAzimuth: Double = 0.0

    var setAzimuth: Double = 0.0

    var transitAppElevation: Double = 0.0

    var riseSetType: RiseSetType? = null

    var uptimeHours: Double = 0.0

}
