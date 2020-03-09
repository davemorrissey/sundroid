package uk.co.sundroid.activity

import androidx.fragment.app.Fragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.activity.data.fragments.*
import uk.co.sundroid.activity.info.fragments.AboutFragment
import uk.co.sundroid.activity.info.fragments.InfoFragment
import uk.co.sundroid.activity.location.*

enum class Page constructor(val navItem: Int, val fragmentClass: Class<out Fragment>, val dataGroup: DataGroup? = null) {

    DAY_SUMMARY(R.id.daySummary, DaySummaryFragment::class.java, DataGroup.DAY_SUMMARY),
    DAY_DETAIL(R.id.dayDetail, DayDetailFragment::class.java, DataGroup.DAY_DETAIL),
    TRACKER(0, TrackerFragment::class.java, DataGroup.TRACKER),
    MONTH_CALENDARS(0, MonthCalendarsFragment::class.java, DataGroup.MONTH_CALENDARS),
    MONTH_MOONPHASE(0, MonthMoonPhaseFragment::class.java, DataGroup.MONTH_MOONPHASE),
    YEAR_EVENTS(0, YearEventsFragment::class.java, DataGroup.YEAR_EVENTS),
    HELP(R.id.help, InfoFragment::class.java),
    ABOUT(R.id.about, AboutFragment::class.java),
    LOCATION_OPTIONS(0, LocationSelectFragment::class.java),
    LOCATION_MAP(0, LocationMapFragment::class.java),
    LOCATION_LIST(0, LocationListFragment::class.java),
    LOCATION_SEARCH(0, LocationSearchFragment::class.java),
    TIME_ZONE(0, TimeZonePickerFragment::class.java);

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
