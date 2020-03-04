package uk.co.sundroid.activity

import androidx.fragment.app.Fragment
import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.activity.data.fragments.*
import uk.co.sundroid.activity.info.fragments.InfoFragment

enum class Page constructor(val displayName: String, val fragmentClass: Class<out Fragment>, val dataGroup: DataGroup?) {

    DAY_DETAIL("Day in detail", DayDetailFragment::class.java, DataGroup.DAY_DETAIL),
    TRACKER("Sun and moon tracker", TrackerFragment::class.java, DataGroup.TRACKER),
    MONTH_CALENDARS("Month calendars", MonthCalendarsFragment::class.java, DataGroup.MONTH_CALENDARS),
    MONTH_MOONPHASE("Moon phase calendar", MonthMoonPhaseFragment::class.java, DataGroup.MONTH_MOONPHASE),
    YEAR_EVENTS("Year events", YearEventsFragment::class.java, DataGroup.YEAR_EVENTS),
    HELP("Help", InfoFragment::class.java, null)

}
