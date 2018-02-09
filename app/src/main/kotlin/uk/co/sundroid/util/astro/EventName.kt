package uk.co.sundroid.util.astro

enum class EventName constructor(val displayName: String, val shortName: String, val id: Int, val isUp: Boolean, val elevation: Double) {

    SUNRISE("Sunrise", "Sunrise", 1, true, 90.833),
    SUNSET("Sunset", "Sunset", 2, false, 90.833),
    CIVIL_DAWN("Civil dawn", "Civ. dawn", 3, true, 96.0),
    CIVIL_DUSK("Civil dusk", "Civ. dusk", 4, false, 96.0),
    NAUTICAL_DAWN("Nautical dawn", "Naut. dawn", 5, true, 102.0),
    NAUTICAL_DUSK("Nautical dusk", "Naut. dusk", 6, false, 102.0),
    ASTRONOMICAL_DAWN("Astronomical dawn", "Ast. dawn", 7, true, 108.0),
    ASTRONOMICAL_DUSK("Astronomical dusk", "Ast. dusk", 8, false, 108.0),
    SOLAR_NOON("Solar noon", "Solar noon", 9, false, 0.0),
    MOONRISE("Moonrise", "Moonrise", 10, false, 0.0),
    MOONSET("Moonset", "Moonset", 11, false, 0.0),
    GOLDEN_HOUR_END("Golden hour end", "Golden hr end", 12, true, 84.0),
    GOLDEN_HOUR_START("Golden hour start", "Golden hr start", 13, false, 84.0)

}
