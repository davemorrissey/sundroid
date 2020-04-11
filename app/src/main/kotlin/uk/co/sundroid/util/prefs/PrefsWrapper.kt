package uk.co.sundroid.util.prefs

import android.content.Context
import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.activity.data.fragments.CalendarView
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.astro.Body

class PrefsWrapper(val context: Context) {

    fun showSeconds(): Boolean = Prefs.showSeconds(context)
    fun reverseGeocode(): Boolean = Prefs.reverseGeocode(context)
    fun clockType24(): Boolean = Prefs.clockType24(context)
    fun theme(): String = Prefs.theme(context)
    fun locationTimeout(): Int = Prefs.locationTimeout(context)
    fun defaultZone(): TimeZoneDetail? = Prefs.defaultZone(context)
    fun defaultZoneOverride(): Boolean = Prefs.defaultZoneOverride(context)
    fun showTimeZone(): Boolean = Prefs.showTimeZone(context)
    fun firstWeekday(): Int = Prefs.firstWeekday(context)
    fun magneticBearings(): Boolean = Prefs.magneticBearings(context)
    fun setShowElement(ref: String, show: Boolean) = Prefs.setShowElement(context, ref, show)
    fun showElement(ref: String, def: Boolean = true): Boolean = Prefs.showElement(context, ref, def)
    fun lastDataGroup(): DataGroup = Prefs.lastDataGroup(context)
    fun lastDataGroup(dataGroup: DataGroup) = Prefs.setLastDataGroup(context, dataGroup)
    fun lastDetailTab(): String = Prefs.lastDetailTab(context)
    fun setLastDayDetailTab(tab: String) = Prefs.setLastDayDetailTab(context, tab)
    fun lastCalendar(): CalendarView = Prefs.lastCalendar(context)
    fun setLastCalendar(calendar: CalendarView) = Prefs.setLastCalendar(context, calendar)
    fun locMapType(): MapType = Prefs.locMapType(context)
    fun setLocMapType(type: MapType) = Prefs.setLocMapType(context, type)
    fun sunTrackerBody(): Body? = Prefs.sunTrackerBody(context)
    fun sunTrackerMode(): String = Prefs.sunTrackerMode(context)
    fun setSunTrackerBody(body: Body?) = Prefs.sunTrackerBody(context)
    fun setSunTrackerMode(mode: String) = Prefs.sunTrackerMode(context)
    fun sunTrackerMapType(): MapType = Prefs.sunTrackerMapType(context)
    fun setSunTrackerMapType(mode: MapType) = Prefs.setSunTrackerMapType(context, mode)
    fun sunTrackerLinearElevation(): Boolean = Prefs.sunTrackerLinearElevation(context)
    fun setSunTrackerLinearElevation(on: Boolean) = Prefs.setSunTrackerLinearElevation(context, on)
    fun sunTrackerCompass(): Boolean = Prefs.sunTrackerCompass(context)
    fun setSunTrackerCompass(on: Boolean) = Prefs.setSunTrackerCompass(context, on)
    fun sunTrackerHourMarkers(): Boolean = Prefs.sunTrackerHourMarkers(context)
    fun setSunTrackerHourMarkers(on: Boolean) = Prefs.setSunTrackerHourMarkers(context, on)
    fun sunTrackerText(): Boolean = Prefs.sunTrackerText(context)
    fun setSunTrackerText(on: Boolean) = Prefs.setSunTrackerText(context, on)
    fun mapLocationPermissionDenied(): Boolean = Prefs.mapLocationPermissionDenied(context)
    fun setMapLocationPermissionDenied(denied: Boolean) = Prefs.setMapLocationPermissionDenied(context, denied)

}
