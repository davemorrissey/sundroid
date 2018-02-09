package uk.co.sundroid.util.astro

enum class EventName private constructor(val name: String, val shortName: String, val id: Int, val isUp: Boolean, val elevation: Double) {

    SUNRISE("Sunrise", "Sunrise", 1, true, 90.833),
    SUNSET("Sunset", "Sunset", 2, false, 90.833),
    CIVIL_DAWN("Civil dawn", "Civ. dawn", 3, true, 96),
    CIVIL_DUSK("Civil dusk", "Civ. dusk", 4, false, 96),
    NAUTICAL_DAWN("Nautical dawn", "Naut. dawn", 5, true, 102),
    NAUTICAL_DUSK("Nautical dusk", "Naut. dusk", 6, false, 102),
    ASTRONOMICAL_DAWN("Astronomical dawn", "Ast. dawn", 7, true, 108),
    ASTRONOMICAL_DUSK("Astronomical dusk", "Ast. dusk", 8, false, 108),
    SOLAR_NOON("Solar noon", "Solar noon", 9, false, 0),
    MOONRISE("Moonrise", "Moonrise", 10, false, 0),
    MOONSET("Moonset", "Moonset", 11, false, 0),
    GOLDEN_HOUR_END("Golden hour end", "Golden hr end", 12, true, 84),
    GOLDEN_HOUR_START("Golden hour start", "Golden hr start", 13, false, 84)

}
