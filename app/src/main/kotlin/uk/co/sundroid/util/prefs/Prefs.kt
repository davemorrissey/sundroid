package uk.co.sundroid.util.prefs

import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.time.TimeZoneResolver

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.text.format.DateFormat
import uk.co.sundroid.domain.MapType
import uk.co.sundroid.util.theme.THEME_DARK

object Prefs {
    
    private const val LOC_LAT_KEY = "location-lat"
    private const val LOC_LON_KEY = "location-lon"
    private const val LOC_NAME_KEY = "location-name"
    private const val LOC_COUNTRY_KEY = "location-country"
    private const val LOC_STATE_KEY = "location-state"
    private const val LOC_ZONE_KEY = "location-zone"

    private const val LAST_DATA_GROUP_KEY = "lastDataGroup"
    private const val LAST_DETAIL_TAB_KEY = "lastDetailTab"
    private const val LAST_CALENDAR_KEY = "lastCalendar"

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
    
    fun selectedLocation(context: Context): LocationDetails? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (prefs.contains(LOC_LAT_KEY) && prefs.contains(LOC_LON_KEY)) {
            try {
                val location = LatitudeLongitude(prefs.getFloat(LOC_LAT_KEY, 0f).toDouble(), prefs.getFloat(LOC_LON_KEY, 0f).toDouble())
                val locationDetails = LocationDetails(location)
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
            editor.putString(THEME_KEY, THEME_DARK)
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
            editor.putBoolean(SHOW_ZONE_KEY, true)
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
    
    fun showSeconds(context: Context): Boolean {
        return prefs(context).getBoolean(SHOW_SECONDS_KEY, false)
    }
    
    fun reverseGeocode(context: Context): Boolean {
        return prefs(context).getBoolean(REVERSE_GEOCODE_KEY, true)
    }
    
    fun clockType24(context: Context): Boolean {
        val default24 = DateFormat.is24HourFormat(context)
        val clockType = ClockType.valueOf(prefs(context).getString(CLOCK_KEY, ClockType.DEFAULT.name))
        return clockType == ClockType.TWENTYFOUR || (clockType == ClockType.DEFAULT && default24)
    }

    fun theme(context: Context): String {
        return prefs(context).getString(THEME_KEY, THEME_DARK)
    }

    fun locationTimeout(context: Context): Int {
        val timeout = prefs(context).getString(LOCATION_TIMEOUT_KEY, "60")
		return try {
			Integer.parseInt(timeout)
        } catch (e: Exception) {
			60
        }
    }
    
    fun defaultZone(context: Context): TimeZoneDetail? {
        val pref = prefs(context).getString(DEFAULT_ZONE_KEY, "~ASK")
        return when (pref) {
            "~ASK" -> null
            "~DEVICE" ->  TimeZoneResolver.getTimeZone(null)
            else -> TimeZoneResolver.getTimeZone(pref)
        }
    }
    
    fun defaultZoneOverride(context: Context): Boolean {
        return prefs(context).getBoolean(DEFAULT_ZONE_OVERRIDE_KEY, false)
    }
    
    fun showTimeZone(context: Context): Boolean {
        return prefs(context).getBoolean(SHOW_ZONE_KEY, true)
    }
    
    fun firstWeekday(context: Context): Int {
        return Integer.parseInt(prefs(context).getString(FIRST_WEEKDAY_KEY, "1"))
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
        val name = prefs(context).getString(LAST_DATA_GROUP_KEY, DataGroup.DAY_SUMMARY.name)
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
        return prefs(context).getString(LAST_DETAIL_TAB_KEY, "sun")
    }

    fun setLastDayDetailTab(context: Context, tab: String) {
        prefs(context).edit().putString(LAST_DETAIL_TAB_KEY, tab).apply()
    }

    fun lastCalendar(context: Context): Int {
        return prefs(context).getInt(LAST_CALENDAR_KEY, 0)
    }

    fun setLastCalendar(context: Context, calendar: Int) {
        prefs(context).edit().putInt(LAST_CALENDAR_KEY, calendar).apply()
    }

    fun locMapType(context: Context): MapType {
        return MapType.valueOf(prefs(context).getString(LOCMAP_TYPE_KEY, MapType.NORMAL.name))
    }
    
    fun setLocMapType(context: Context, type: MapType) {
        prefs(context).edit().putString(LOCMAP_TYPE_KEY, type.name).apply()
    }
    
    fun sunTrackerBody(context: Context): Body? {
        val value = prefs(context).getString(SUNTRACKER_BODY_KEY, "SUN")
		return if (value == "all") {
			null
        } else {
			Body.valueOf(value)
        }
    }
    
    fun sunTrackerMode(context: Context): String {
        return prefs(context).getString(SUNTRACKER_MODE_KEY, "radar")
    }
    
    fun setSunTrackerBody(context: Context, body: Body?) {
		prefs(context).edit().putString(SUNTRACKER_BODY_KEY, body?.name ?: "all").apply()
    }
    
    fun setSunTrackerMode(context: Context, mode: String) {
        prefs(context).edit().putString(SUNTRACKER_MODE_KEY, mode).apply()
    }

    fun sunTrackerMapType(context: Context): MapType {
        return MapType.valueOf(prefs(context).getString(SUNTRACKER_MAPTYPE_KEY, MapType.NORMAL.name))
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
        return prefs(context).getBoolean(SUNTRACKER_HOURMARKERS_KEY, false)
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

	private fun prefs(context: Context): SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

}
