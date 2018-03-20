package uk.co.sundroid.activity.data

import android.app.Fragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.*

enum class DataGroup constructor(val displayName: String, val fragmentClass: Class<out Fragment>) {

    DAY_DETAIL("Day in detail", DayDetailFragment::class.java),
    TRACKER("Sun and moon tracker", TrackerFragment::class.java),
    MONTH_CALENDARS("Month calendars", MonthCalendarsFragment::class.java),
    MONTH_MOONPHASE("Moon phase calendar", MonthMoonPhaseFragment::class.java),
    YEAR_EVENTS("Year events", YearEventsFragment::class.java);

}
