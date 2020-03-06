package uk.co.sundroid.activity

import androidx.fragment.app.Fragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.activity.data.fragments.*
import uk.co.sundroid.activity.info.fragments.InfoFragment

enum class Page constructor(val displayName: String, val navItem: Int, val fragmentClass: Class<out Fragment>, val dataGroup: DataGroup?) {

    DAY_SUMMARY("Day summary", R.id.daySummary, DaySummaryFragment::class.java, DataGroup.DAY_SUMMARY),
    DAY_DETAIL("Day in detail", R.id.dayDetail, DayDetailFragment::class.java, DataGroup.DAY_DETAIL),
    TRACKER("Sun and moon tracker", 0, TrackerFragment::class.java, DataGroup.TRACKER),
    MONTH_CALENDARS("Month calendars", 0, MonthCalendarsFragment::class.java, DataGroup.MONTH_CALENDARS),
    MONTH_MOONPHASE("Moon phase calendar", 0, MonthMoonPhaseFragment::class.java, DataGroup.MONTH_MOONPHASE),
    YEAR_EVENTS("Year events", 0, YearEventsFragment::class.java, DataGroup.YEAR_EVENTS),
    HELP("Help", R.id.help, InfoFragment::class.java, null);

    companion object {
        fun fromFragment(fragment: Fragment): Page? {
            for (page in values()) {
                if (page.fragmentClass == fragment::class.java) {
                    return page
                }
            }
            return null
        }
    }

}
