package uk.co.sundroid.util.astro


/**
 * Phases of the moon.
 */
enum class MoonPhase constructor(val displayName: String) {

    NEW("New"),
    EVENING_CRESCENT("Evening crescent"),
    FIRST_QUARTER("First quarter"),
    WAXING_GIBBOUS("Waxing gibbous"),
    FULL("Full"),
    WANING_GIBBOUS("Waning gibbous"),
    LAST_QUARTER("Last quarter"),
    MORNING_CRESCENT("Morning crescent")

}
