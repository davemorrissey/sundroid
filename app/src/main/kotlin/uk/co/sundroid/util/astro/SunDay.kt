package uk.co.sundroid.util.astro

import java.util.*


class SunDay : BodyDay() {

    var astDawn: Calendar? = null

    var astDusk: Calendar? = null

    var ntcDawn: Calendar? = null

    var ntcDusk: Calendar? = null

    var civDawn: Calendar? = null

    var civDusk: Calendar? = null

    var ghEnd: Calendar? = null

    var ghStart: Calendar? = null

    var civType: TwilightType? = null

    var ntcType: TwilightType? = null

    var astType: TwilightType? = null

    var ghType: TwilightType? = null

    @Deprecated("Unused")
    var civHours: Double = 0.0

    @Deprecated("Unused")
    var ntcHours: Double = 0.0

    @Deprecated("Unused")
    var astHours: Double = 0.0

    @Deprecated("Unused")
    var ghHours: Double = 0.0

}
