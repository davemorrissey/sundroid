package uk.co.sundroid.util.prefs

import android.content.Context
import uk.co.sundroid.activity.data.fragments.CalendarView
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.domain.TimeZoneDetail

class PrefsWrapper(val context: Context) {

    fun showSeconds(): Boolean = Prefs.showSeconds(context)
    fun clockType24(): Boolean = Prefs.clockType24(context)
    fun theme(): String = Prefs.theme(context)
    fun defaultZone(): TimeZoneDetail? = Prefs.defaultZone(context)
    fun showTimeZone(): Boolean = Prefs.showTimeZone(context)
    fun firstWeekday(): Int = Prefs.firstWeekday(context)
    fun setShowElement(ref: String, show: Boolean) = Prefs.setShowElement(context, ref, show)
    fun showElement(ref: String, def: Boolean = true): Boolean = Prefs.showElement(context, ref, def)
    fun setLastDayDetailTab(tab: String) = Prefs.setLastDayDetailTab(context, tab)
    fun lastCalendar(): CalendarView = Prefs.lastCalendar(context)
    fun setLocMapType(type: MapType) = Prefs.setLocMapType(context, type)
    fun sunTrackerMode(): String = Prefs.sunTrackerMode(context)
    fun sunTrackerMapType(): MapType = Prefs.sunTrackerMapType(context)
    fun setSunTrackerCompass(on: Boolean) = Prefs.setSunTrackerCompass(context, on)
    fun sunTrackerHourMarkers(): Boolean = Prefs.sunTrackerHourMarkers(context)
    fun setSunTrackerHourMarkers(on: Boolean) = Prefs.setSunTrackerHourMarkers(context, on)
    fun sunTrackerText(): Boolean = Prefs.sunTrackerText(context)
    fun setSunTrackerText(on: Boolean) = Prefs.setSunTrackerText(context, on)
    fun setMapLocationPermissionDenied(denied: Boolean) = Prefs.setMapLocationPermissionDenied(context, denied)

}
