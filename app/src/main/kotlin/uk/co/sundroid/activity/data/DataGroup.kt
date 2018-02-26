package uk.co.sundroid.activity.data

enum class DataGroup constructor(val displayName: String, val index: Int) {

    DAY_SUMMARY("Day summary", 0),
    DAY_DETAIL("Day in detail", 1),
    TRACKER("Sun and moon tracker", 2),
    MONTH_CALENDARS("Month calendars", 3),
    MONTH_MOONPHASE("Moon phase calendar", 4),
    YEAR_EVENTS("Year events", 5);

    companion object {
        fun forIndex(index: Int): DataGroup? {
            return values().firstOrNull { it.index == index }
        }
    }

}
