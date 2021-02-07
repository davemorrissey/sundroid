package uk.co.sundroid.util.prefs

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.text.format.DateFormat
import androidx.preference.PreferenceManager
import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.activity.data.fragments.CalendarView
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.theme.THEME_DARKBLUE
import uk.co.sundroid.util.time.TimeZoneResolver
import java.text.ParseException

object Prefs {

    private const val LOC_ID_KEY = "location-id"
    private const val LOC_LAT_KEY = "location-lat"
    private const val LOC_LON_KEY = "location-lon"
    private const val LOC_NAME_KEY = "location-name"
    private const val LOC_COUNTRY_KEY = "location-country"
    private const val LOC_STATE_KEY = "location-state"
    private const val LOC_ZONE_KEY = "location-zone"

    private const val LAST_DATA_GROUP_KEY = "lastDataGroup"
    private const val LAST_DETAIL_TAB_KEY = "lastDetailTab"
    private const val LAST_CALENDAR_VIEW_KEY = "lastCalendarView"

    private const val LOCATION_TIMEOUT_KEY = "locationTimeout"
    private const val SHOW_SECONDS_KEY = "showSeconds"
    private const val THEME_KEY = "theme"
    private const val CLOCK_KEY = "clock"
    private const val REVERSE_GEOCODE_KEY = "reverseGeocode"
    private const val DEFAULT_ZONE_KEY = "defaultTimeZone"
    private const val DEFAULT_ZONE_OVERRIDE_KEY = "defaultTimeZoneOverride"
    private const val FIRST_WEEKDAY_KEY = "firstWeekday"
    private const val SHOW_ZONE_KEY = "showTimeZone"
    private const val MAGNETIC_BEARINGS_KEY = "magneticBearings"
    private const val ALARM_IN_SILENT_KEY = "alarmInSilent"
    private const val ALARM_SOUND_TIMEOUT_KEY = "alarmSoundTimeout"
    private const val ALARM_VIBRATION_TIMEOUT_KEY = "alarmVibrationTimeout"
    
    private const val SUNTRACKER_BODY_KEY = "sunTrackerBody"
    private const val SUNTRACKER_MODE_KEY = "sunTrackerMode"
    private const val SUNTRACKER_MAPTYPE_KEY = "sunTrackerMapType"
    private const val SUNTRACKER_COMPASS_KEY = "sunTrackerCompass"
    private const val SUNTRACKER_LINEARELEVATION_KEY = "sunTrackerLinearElevation"
    private const val SUNTRACKER_HOURMARKERS_KEY = "sunTrackerHourMarkers"
    private const val SUNTRACKER_TEXT_KEY = "sunTrackerText"
    
    private const val LOCMAP_TYPE_KEY = "locMapType"

    private const val MAP_LOCATION_PERMISSION_DENIED_KEY = "mapLocationPermissionDenied"

    private const val LAST_VERSION_KEY = "last-version"

    private const val WIDGET_LOC_LAT_KEY = "widget-location-lat"
    private const val WIDGET_LOC_LON_KEY = "widget-location-lon"
    private const val WIDGET_LOC_NAME_KEY = "widget-location-name"
    private const val WIDGET_LOC_COUNTRY_KEY = "widget-location-country"
    private const val WIDGET_LOC_STATE_KEY = "widget-location-state"
    private const val WIDGET_LOC_ZONE_KEY = "widget-location-zone"
    private const val WIDGET_LOC_TIMESTAMP = "widget-location-timestamp"

    private const val WIDGET_LOCATION_KEY = "widget-location-"

    private const val WIDGET_LOCATION_RECEIVED_KEY = "widget-location-received-"

    private const val WIDGET_PHASE_SHADOW_OPACITY_KEY = "widget-shadow-opacity-"
    private const val WIDGET_PHASE_SHADOW_SIZE_KEY = "widget-shadow-size-"
    private const val WIDGET_PHASE_PHASENAME_KEY = "widget-phasename-"

    private const val WIDGET_BOX_OPACITY_KEY = "widget-box-opacity-"

    fun selectedLocation(context: Context): LocationDetails? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.contains(LOC_LAT_KEY) && prefs.contains(LOC_LON_KEY)) {
            try {
                val location = LatitudeLongitude(prefs.getFloat(LOC_LAT_KEY, 0f).toDouble(), prefs.getFloat(LOC_LON_KEY, 0f).toDouble())
                val locationDetails = LocationDetails(location)
                locationDetails.id = prefs.getLong(LOC_ID_KEY, 0)
                locationDetails.name = prefs.getString(LOC_NAME_KEY, null)
                locationDetails.country = prefs.getString(LOC_COUNTRY_KEY, null)
                locationDetails.state = prefs.getString(LOC_STATE_KEY, null)
                locationDetails.timeZone = TimeZoneResolver.getTimeZone(prefs.getString(LOC_ZONE_KEY, null))
                locationDetails.possibleTimeZones = TimeZoneResolver.getPossibleTimeZones(location, locationDetails.country, locationDetails.state)
                return locationDetails
            } catch (e: Exception) {
                // Should never save invalid coordinates.
            }
        }
        return null
    }
        
    fun saveSelectedLocation(context: Context, locationDetails: LocationDetails) {
        val location = locationDetails.location
        prefs(context).edit()
                .putLong(LOC_ID_KEY, locationDetails.id)
                .putFloat(LOC_LAT_KEY, location.latitude.doubleValue.toFloat())
                .putFloat(LOC_LON_KEY, location.longitude.doubleValue.toFloat())
                .putString(LOC_NAME_KEY, locationDetails.name)
                .putString(LOC_COUNTRY_KEY, locationDetails.country)
                .putString(LOC_STATE_KEY, locationDetails.state)
                .putString(LOC_ZONE_KEY, locationDetails.timeZone?.id)
                .apply()
    }

    fun saveSelectedLocationTimeZone(context: Context, timeZone: TimeZoneDetail) {
        prefs(context).edit()
                .putString(LOC_ZONE_KEY, timeZone.id)
                .apply()
    }
    
    fun initPreferences(context: Context) {
        val prefs = prefs(context)
        val editor = prefs.edit()
        if (!prefs.contains(CLOCK_KEY)) {
            editor.putString(CLOCK_KEY, ClockType.DEFAULT.name)
        }
        if (!prefs.contains(THEME_KEY)) {
            editor.putString(THEME_KEY, THEME_DARKBLUE)
        }
        if (!prefs.contains(SHOW_SECONDS_KEY)) {
            editor.putBoolean(SHOW_SECONDS_KEY, false)
        }
        if (!prefs.contains(REVERSE_GEOCODE_KEY)) {
            editor.putBoolean(REVERSE_GEOCODE_KEY, true)
        }
        if (!prefs.contains(DEFAULT_ZONE_KEY)) {
            editor.putString(DEFAULT_ZONE_KEY, "~ASK")
        }
        if (!prefs.contains(DEFAULT_ZONE_OVERRIDE_KEY)) {
            editor.putBoolean(DEFAULT_ZONE_OVERRIDE_KEY, false)
        }
        if (!prefs.contains(FIRST_WEEKDAY_KEY)) {
            editor.putString(FIRST_WEEKDAY_KEY, "1")
        }
        if (!prefs.contains(SHOW_ZONE_KEY)) {
            editor.putBoolean(SHOW_ZONE_KEY, false)
        }
        if (!prefs.contains(ALARM_IN_SILENT_KEY)) {
            editor.putBoolean(ALARM_IN_SILENT_KEY, true)
        }
        if (!prefs.contains(MAGNETIC_BEARINGS_KEY)) {
            editor.putBoolean(MAGNETIC_BEARINGS_KEY, false)
        }
        if (!prefs.contains(LOCATION_TIMEOUT_KEY)) {
            editor.putString(LOCATION_TIMEOUT_KEY, "60")
        }
        if (!prefs.contains(ALARM_SOUND_TIMEOUT_KEY)) {
            editor.putString(ALARM_SOUND_TIMEOUT_KEY, "300")
        }
        if (!prefs.contains(ALARM_VIBRATION_TIMEOUT_KEY)) {
            editor.putString(ALARM_VIBRATION_TIMEOUT_KEY, "300")
        }
        editor.apply()
        
    }

    fun setShowSeconds(context: Context, showSeconds: Boolean) {
        prefs(context).edit().putBoolean(SHOW_SECONDS_KEY, showSeconds).apply()
    }
    
    fun showSeconds(context: Context): Boolean {
        return prefs(context).getBoolean(SHOW_SECONDS_KEY, false)
    }
    
    fun reverseGeocode(context: Context): Boolean {
        return prefs(context).getBoolean(REVERSE_GEOCODE_KEY, true)
    }

    fun setClockType(context: Context, clockType: ClockType) {
        prefs(context).edit().putString(CLOCK_KEY, clockType.name).apply()
    }
    
    fun clockType24(context: Context): Boolean {
        val default24 = DateFormat.is24HourFormat(context)
        val clockType = ClockType.valueOf(prefs(context).getString(CLOCK_KEY, null) ?: ClockType.DEFAULT.name)
        return clockType == ClockType.TWENTYFOUR || (clockType == ClockType.DEFAULT && default24)
    }

    @SuppressLint("ApplySharedPref")
    fun setTheme(context: Context, theme: String) {
        prefs(context).edit().putString(THEME_KEY, theme).commit()
    }

    fun theme(context: Context): String {
        return prefs(context).getString(THEME_KEY, null) ?: THEME_DARKBLUE
    }

    fun locationTimeout(context: Context): Int {
        val timeout = prefs(context).getString(LOCATION_TIMEOUT_KEY, null) ?: "60"
		return try {
			Integer.parseInt(timeout)
        } catch (e: Exception) {
			60
        }
    }
    
    fun defaultZone(context: Context): TimeZoneDetail? {
        return when (val pref = prefs(context).getString(DEFAULT_ZONE_KEY, "~ASK")) {
            "~ASK" -> null
            "~DEVICE" ->  TimeZoneResolver.getTimeZone(null)
            else -> TimeZoneResolver.getTimeZone(pref)
        }
    }
    
    fun defaultZoneOverride(context: Context): Boolean {
        return prefs(context).getBoolean(DEFAULT_ZONE_OVERRIDE_KEY, false)
    }

    fun setShowTimeZone(context: Context, showTimeZone: Boolean): Boolean {
        return prefs(context).edit().putBoolean(SHOW_ZONE_KEY, showTimeZone).commit()
    }
    
    fun showTimeZone(context: Context): Boolean {
        return prefs(context).getBoolean(SHOW_ZONE_KEY, true)
    }
    
    fun firstWeekday(context: Context): Int {
        return Integer.parseInt(prefs(context).getString(FIRST_WEEKDAY_KEY, null) ?: "1")
    }
    
    fun magneticBearings(context: Context): Boolean {
        return prefs(context).getBoolean(MAGNETIC_BEARINGS_KEY, false)
    }
    
    fun setShowElement(context: Context, ref: String, show: Boolean) {
        prefs(context).edit().putBoolean("show-$ref", show).apply()
    }
    
    fun showElement(context: Context, ref: String, def: Boolean = true): Boolean {
        return prefs(context).getBoolean("show-$ref", def)
    }
    
    fun lastDataGroup(context: Context): DataGroup {
        val name = prefs(context).getString(LAST_DATA_GROUP_KEY, null) ?: DataGroup.DAY_SUMMARY.name
        try {
            return DataGroup.valueOf(name)
        } catch (e: Exception) {
            // Return default
        }
        return DataGroup.DAY_SUMMARY
    }
    
    fun setLastDataGroup(context: Context, dataGroup: DataGroup) {
        prefs(context).edit().putString(LAST_DATA_GROUP_KEY, dataGroup.name).apply()
    }

    fun lastDetailTab(context: Context): String {
        return prefs(context).getString(LAST_DETAIL_TAB_KEY, null) ?: "sun"
    }

    fun setLastDayDetailTab(context: Context, tab: String) {
        prefs(context).edit().putString(LAST_DETAIL_TAB_KEY, tab).apply()
    }

    fun lastCalendar(context: Context): CalendarView {
        return CalendarView.valueOf(prefs(context).getString(LAST_CALENDAR_VIEW_KEY, null) ?: CalendarView.SUN_RISE_SET_LIST.name)
    }

    fun setLastCalendar(context: Context, calendarView: CalendarView) {
        prefs(context).edit().putString(LAST_CALENDAR_VIEW_KEY, calendarView.name).apply()
    }

    fun locMapType(context: Context): MapType {
        return MapType.valueOf(prefs(context).getString(LOCMAP_TYPE_KEY, null) ?: MapType.NORMAL.name)
    }
    
    fun setLocMapType(context: Context, type: MapType) {
        prefs(context).edit().putString(LOCMAP_TYPE_KEY, type.name).apply()
    }
    
    fun sunTrackerBody(context: Context): Body? {
        val value = prefs(context).getString(SUNTRACKER_BODY_KEY, null) ?: "SUN"
		return if (value == "all") {
			null
        } else {
			Body.valueOf(value)
        }
    }
    
    fun sunTrackerMode(context: Context): String {
        return prefs(context).getString(SUNTRACKER_MODE_KEY, null) ?: "radar"
    }
    
    fun setSunTrackerBody(context: Context, body: Body?) {
		prefs(context).edit().putString(SUNTRACKER_BODY_KEY, body?.name ?: "all").apply()
    }
    
    fun setSunTrackerMode(context: Context, mode: String) {
        prefs(context).edit().putString(SUNTRACKER_MODE_KEY, mode).apply()
    }

    fun sunTrackerMapType(context: Context): MapType {
        return MapType.valueOf(prefs(context).getString(SUNTRACKER_MAPTYPE_KEY, null) ?: MapType.NORMAL.name)
    }
    
    fun setSunTrackerMapType(context: Context, mode: MapType) {
        prefs(context).edit().putString(SUNTRACKER_MAPTYPE_KEY, mode.name).apply()
    }
    
    fun sunTrackerLinearElevation(context: Context): Boolean {
        return prefs(context).getBoolean(SUNTRACKER_LINEARELEVATION_KEY, false)
    }
    
    fun setSunTrackerLinearElevation(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(SUNTRACKER_LINEARELEVATION_KEY, on).apply()
    }
    
    fun sunTrackerCompass(context: Context): Boolean {
        return prefs(context).getBoolean(SUNTRACKER_COMPASS_KEY, false)
    }
    
    fun setSunTrackerCompass(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(SUNTRACKER_COMPASS_KEY, on).apply()
    }
    
    fun sunTrackerHourMarkers(context: Context): Boolean {
        return prefs(context).getBoolean(SUNTRACKER_HOURMARKERS_KEY, true)
    }
    
    fun setSunTrackerHourMarkers(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(SUNTRACKER_HOURMARKERS_KEY, on).apply()
    }
    
    fun sunTrackerText(context: Context): Boolean {
        return prefs(context).getBoolean(SUNTRACKER_TEXT_KEY, true)
    }
    
    fun setSunTrackerText(context: Context, on: Boolean) {
        prefs(context).edit().putBoolean(SUNTRACKER_TEXT_KEY, on).apply()
    }

    fun mapLocationPermissionDenied(context: Context): Boolean {
        return prefs(context).getBoolean(MAP_LOCATION_PERMISSION_DENIED_KEY, false)
    }

    fun setMapLocationPermissionDenied(context: Context, denied: Boolean) {
        prefs(context).edit().putBoolean(MAP_LOCATION_PERMISSION_DENIED_KEY, denied).apply()
    }

    fun lastVersion(context: Context): Int {
        return prefs(context).getInt(LAST_VERSION_KEY, 0)
    }

    fun setVersion(context: Context, version: Int) {
        prefs(context).edit().putInt(LAST_VERSION_KEY, version).apply()
    }

    fun getWidgetLocation(context: Context): LocationDetails? {
        val prefs = prefs(context)
        val locLat = prefs.getFloat(WIDGET_LOC_LAT_KEY, Float.MIN_VALUE)
        val locLon = prefs.getFloat(WIDGET_LOC_LON_KEY, Float.MIN_VALUE)
        if (locLat != Float.MIN_VALUE && locLon != Float.MIN_VALUE) {
            try {
                val locationDetails = LocationDetails(LatitudeLongitude(locLat.toDouble(), locLon.toDouble()))
                locationDetails.name = prefs.getString(WIDGET_LOC_NAME_KEY, null)
                locationDetails.country = prefs.getString(WIDGET_LOC_COUNTRY_KEY, null)
                locationDetails.state = prefs.getString(WIDGET_LOC_STATE_KEY, null)
                locationDetails.timeZone = TimeZoneResolver.getTimeZone(prefs.getString(WIDGET_LOC_ZONE_KEY, null))
                return locationDetails
            } catch (e: ParseException) {
                // Should never save invalid coordinates.
            }
        }
        return null
    }

    fun getWidgetLocationTimestamp(context: Context): Long {
        return prefs(context).getLong(WIDGET_LOC_TIMESTAMP, 0)
    }

    fun saveWidgetLocation(context: Context, locationDetails: LocationDetails) {
        prefs(context).edit()
                .putFloat(WIDGET_LOC_LAT_KEY, locationDetails.location.latitude.doubleValue.toFloat())
                .putFloat(WIDGET_LOC_LON_KEY, locationDetails.location.longitude.doubleValue.toFloat())
                .putString(WIDGET_LOC_NAME_KEY, locationDetails.name)
                .putString(WIDGET_LOC_COUNTRY_KEY, locationDetails.country)
                .putString(WIDGET_LOC_STATE_KEY, locationDetails.state)
                .putString(WIDGET_LOC_ZONE_KEY, if (locationDetails.timeZone == null) null else locationDetails.timeZone!!.id)
                .putLong(WIDGET_LOC_TIMESTAMP, System.currentTimeMillis())
                .apply()
    }

    fun widgetLocationReceived(context: Context, widgetId: Int): Boolean {
        return prefs(context).getBoolean(WIDGET_LOCATION_RECEIVED_KEY + widgetId, false)
    }

    fun setWidgetLocationReceived(context: Context, widgetId: Int, received: Boolean) {
        prefs(context).edit().putBoolean(WIDGET_LOCATION_RECEIVED_KEY + widgetId, received).apply()
    }

    fun widgetBoxShadowOpacity(context: Context, widgetId: Int): Int {
        return prefs(context).getInt(WIDGET_BOX_OPACITY_KEY + widgetId, 200)
    }

    fun setWidgetBoxShadowOpacity(context: Context, widgetId: Int, shadow: Int) {
        prefs(context).edit().putInt(WIDGET_BOX_OPACITY_KEY + widgetId, shadow).apply()
    }

    fun widgetPhaseShadowOpacity(context: Context, widgetId: Int): Int {
        return prefs(context).getInt(WIDGET_PHASE_SHADOW_OPACITY_KEY + widgetId, 0)
    }

    fun setWidgetPhaseShadowOpacity(context: Context, widgetId: Int, shadow: Int) {
        prefs(context).edit().putInt(WIDGET_PHASE_SHADOW_OPACITY_KEY + widgetId, shadow).apply()
    }

    fun widgetPhaseShadowSize(context: Context, widgetId: Int): Int {
        return prefs(context).getInt(WIDGET_PHASE_SHADOW_SIZE_KEY + widgetId, 0)
    }

    fun setWidgetPhaseShadowSize(context: Context, widgetId: Int, size: Int) {
        prefs(context).edit().putInt(WIDGET_PHASE_SHADOW_SIZE_KEY + widgetId, size).apply()
    }

    fun removeWidgetPrefs(context: Context, widgetId: Int) {
        prefs(context).edit()
                .remove(WIDGET_LOCATION_KEY + widgetId)
                .remove(WIDGET_PHASE_SHADOW_OPACITY_KEY + widgetId)
                .remove(WIDGET_PHASE_SHADOW_SIZE_KEY + widgetId)
                .remove(WIDGET_PHASE_PHASENAME_KEY + widgetId)
                .remove(WIDGET_BOX_OPACITY_KEY + widgetId)
                .remove(WIDGET_LOCATION_RECEIVED_KEY + widgetId)
                .apply()
    }

	private fun prefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

}
