package uk.co.sundroid.util.astro


/**
 * Phases of the moon.
 */
enum class MoonPhase constructor(val displayName: String, val shortDisplayName: String) {

    NEW("New", "New"),
    EVENING_CRESCENT("Evening crescent", "Eve. crescent"),
    FIRST_QUARTER("First quarter", "First q."),
    WAXING_GIBBOUS("Waxing gibbous", "Waxing gibbous"),
    FULL("Full", "Full"),
    WANING_GIBBOUS("Waning gibbous", "Waning gibbous"),
    LAST_QUARTER("Last quarter", "Last q."),
    MORNING_CRESCENT("Morning crescent", "Morn. crescent")

}
