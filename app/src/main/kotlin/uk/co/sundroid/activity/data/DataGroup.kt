package uk.co.sundroid.activity.data

import android.app.Fragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.*

enum class DataGroup constructor(val displayName: String, val navId: Int, val fragmentClass: Class<out Fragment>) {

    DAY_SUMMARY("Day summary", 0, DaySummaryFragment::class.java),
    DAY_DETAIL("Day in detail", R.id.dayDetail, DayDetailSunFragment::class.java),
    TRACKER("Sun and moon tracker", R.id.tracker, TrackerFragment::class.java),
    MONTH_CALENDARS("Month calendars", R.id.calendars, MonthCalendarsFragment::class.java),
    MONTH_MOONPHASE("Moon phase calendar", 0, MonthMoonPhaseFragment::class.java),
    YEAR_EVENTS("Year events", R.id.yearEvents, YearEventsFragment::class.java);

}
