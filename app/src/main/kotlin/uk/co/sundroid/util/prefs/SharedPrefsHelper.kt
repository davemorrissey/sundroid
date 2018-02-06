package uk.co.sundroid.util.prefs

import uk.co.sundroid.activity.data.DataGroup
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.domain.TimeZoneDetail
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.time.TimeZoneResolver

import android.content.Context
import android.preference.PreferenceManager
import android.text.format.DateFormat
import uk.co.sundroid.util.theme.THEME_DARK

object SharedPrefsHelper {
    
    private val PREFS_KEY = "sundroid-prefs"
    
    private val LOC_LAT_KEY = "location-lat"
    private val LOC_LON_KEY = "location-lon"
    private val LOC_NAME_KEY = "location-name"
    private val LOC_COUNTRY_KEY = "location-country"
    private val LOC_STATE_KEY = "location-state"
    private val LOC_ZONE_KEY = "location-zone"

    private val LAST_DATA_GROUP_KEY = "lastDataGroup"
    private val LAST_DETAIL_TAB_KEY = "lastDetailTab"
    private val LAST_CALENDAR_KEY = "lastCalendar"

    private val LOCATION_TIMEOUT_KEY = "locationTimeout"
    private val SHOW_SECONDS_KEY = "showSeconds"
    private val THEME_KEY = "theme"
    private val CLOCK_KEY = "clock"
    private val REVERSE_GEOCODE_KEY = "reverseGeocode"
    private val LAST_KNOWN_LOCATION_KEY = "lastKnownLocation"
    private val DEFAULT_ZONE_KEY = "defaultTimeZone"
    private val DEFAULT_ZONE_OVERRIDE_KEY = "defaultTimeZoneOverride"
    private val FIRST_WEEKDAY_KEY = "firstWeekday"
    private val SHOW_ZONE_KEY = "showTimeZone"
    private val MAGNETIC_BEARINGS_KEY = "magneticBearings"
    private val ALARM_IN_SILENT_KEY = "alarmInSilent"
    private val ALARM_SOUND_TIMEOUT_KEY = "alarmSoundTimeout"
    private val ALARM_VIBRATION_TIMEOUT_KEY = "alarmVibrationTimeout"
    
    private val SUNTRACKER_BODY_KEY = "sunTrackerBody"
    private val SUNTRACKER_MODE_KEY = "sunTrackerMode"
    private val SUNTRACKER_MAPMODE_KEY = "sunTrackerMapMode"
    private val SUNTRACKER_COMPASS_KEY = "sunTrackerCompass"
    private val SUNTRACKER_LINEARELEVATION_KEY = "sunTrackerLinearElevation"
    private val SUNTRACKER_HOURMARKERS_KEY = "sunTrackerHourMarkers"
    private val SUNTRACKER_TEXT_KEY = "sunTrackerText"
    
    private val LOCMAP_MODE_KEY = "locMapMode"

    private val MAP_LOCATION_PERMISSION_DENIED_KEY = "mapLocationPermissionDenied"
    
    fun getSelectedLocation(context: Context): LocationDetails? {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        if (prefs.contains(LOC_LAT_KEY) && prefs.contains(LOC_LON_KEY)) {
            try {
                val location = LatitudeLongitude(prefs.getFloat(LOC_LAT_KEY, 0f).toDouble(), prefs.getFloat(LOC_LON_KEY, 0f).toDouble())
                val locationDetails = LocationDetails()
                locationDetails.location = location
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
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val location = locationDetails.location
        if (location != null) {
            prefs.edit()
                    .putFloat(LOC_LAT_KEY, location.latitude.doubleValue.toFloat())
                    .putFloat(LOC_LON_KEY, location.longitude.doubleValue.toFloat())
                    .putString(LOC_NAME_KEY, locationDetails.name)
                    .putString(LOC_COUNTRY_KEY, locationDetails.country)
                    .putString(LOC_STATE_KEY, locationDetails.state)
                    .putString(LOC_ZONE_KEY, locationDetails.timeZone?.id)
                    .apply()
        }
    }
    
    fun initPreferences(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
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
        if (!prefs.contains(LAST_KNOWN_LOCATION_KEY)) {
            editor.putBoolean(LAST_KNOWN_LOCATION_KEY, false)
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
    
    fun getShowSeconds(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(SHOW_SECONDS_KEY, false)
    }
    
    fun getReverseGeocode(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(REVERSE_GEOCODE_KEY, true)
    }
    
    fun getClockType24(context: Context): Boolean {
        val default24 = DateFormat.is24HourFormat(context)
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val clockType = ClockType.valueOf(prefs.getString(CLOCK_KEY, ClockType.DEFAULT.name))
        return clockType == ClockType.TWENTYFOUR || (clockType == ClockType.DEFAULT && default24)
    }

    fun getTheme(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(THEME_KEY, THEME_DARK)
    }

    fun getLocationTimeout(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val timeout = prefs.getString(LOCATION_TIMEOUT_KEY, "60")
        try {
            return Integer.parseInt(timeout)
        } catch (e: Exception) {
            return 60
        }
    }
    
    fun getDefaultZone(context: Context): TimeZoneDetail? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val pref = prefs.getString(DEFAULT_ZONE_KEY, "~ASK")
        return when (pref) {
            "~ASK" -> null
            "~DEVICE" ->  TimeZoneResolver.getTimeZone(null)
            else -> TimeZoneResolver.getTimeZone(pref)
        }
    }
    
    fun getDefaultZoneOverride(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(DEFAULT_ZONE_OVERRIDE_KEY, false)
    }
    
    fun getLastKnownLocation(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(LAST_KNOWN_LOCATION_KEY, false)
    }
    
    fun getShowTimeZone(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(SHOW_ZONE_KEY, true)
    }
    
    fun getFirstWeekday(context: Context): Int {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return Integer.parseInt(prefs.getString(FIRST_WEEKDAY_KEY, "1"))
    }
    
    fun getMagneticBearings(context: Context): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean(MAGNETIC_BEARINGS_KEY, false)
    }
    
    fun setShowElement(context: Context, ref: String, show: Boolean) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putBoolean("show-" + ref, show).apply()
    }
    
    fun getShowElement(context: Context, ref: String, def: Boolean): Boolean {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getBoolean("show-" + ref, def)
    }
    
    fun getLastDataGroup(context: Context): DataGroup {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val name = prefs.getString(LAST_DATA_GROUP_KEY, DataGroup.DAY_SUMMARY.name)
        try {
            return DataGroup.valueOf(name)
        } catch (e: Exception) {
            // Return default
        }
        return DataGroup.DAY_SUMMARY
    }
    
    fun setLastDataGroup(context: Context, dataGroup: DataGroup) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putString(LAST_DATA_GROUP_KEY, dataGroup.name).apply()
    }

    fun getLastDetailTab(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getString(LAST_DETAIL_TAB_KEY, "sun")
    }

    fun setLastDayDetailTab(context: Context, tab: String) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putString(LAST_DETAIL_TAB_KEY, tab).apply()
    }

    fun getLastCalendar(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getInt(LAST_CALENDAR_KEY, 0)
    }

    fun setLastCalendar(context: Context, calendar: Int) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putInt(LAST_CALENDAR_KEY, calendar).apply()
    }

    fun getLocMapMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getString(LOCMAP_MODE_KEY, "normal")
    }
    
    fun setLocMapMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putString(LOCMAP_MODE_KEY, mode).apply()
    }
    
    fun getSunTrackerBody(context: Context): Body? {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        val value = prefs.getString(SUNTRACKER_BODY_KEY, "SUN")
        if (value == "all") {
            return null
        } else {
            return Body.valueOf(value)
        }
    }
    
    fun getSunTrackerMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getString(SUNTRACKER_MODE_KEY, "radar")
    }
    
    fun setSunTrackerBody(context: Context, body: Body?) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        if (body == null) {
            prefs.edit().putString(SUNTRACKER_BODY_KEY, "all").apply()
        } else {
            prefs.edit().putString(SUNTRACKER_BODY_KEY, body.name).apply()
        }
    }
    
    fun setSunTrackerMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putString(SUNTRACKER_MODE_KEY, mode).apply()
    }
    
    fun getSunTrackerMapMode(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getString(SUNTRACKER_MAPMODE_KEY, "normal")
    }
    
    fun setSunTrackerMapMode(context: Context, mode: String) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putString(SUNTRACKER_MAPMODE_KEY, mode).apply()
    }
    
    fun getSunTrackerLinearElevation(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SUNTRACKER_LINEARELEVATION_KEY, false)
    }
    
    fun setSunTrackerLinearElevation(context: Context, on: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SUNTRACKER_LINEARELEVATION_KEY, on).apply()
    }
    
    fun getSunTrackerCompass(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SUNTRACKER_COMPASS_KEY, false)
    }
    
    fun setSunTrackerCompass(context: Context, on: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SUNTRACKER_COMPASS_KEY, on).apply()
    }
    
    fun getSunTrackerHourMarkers(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SUNTRACKER_HOURMARKERS_KEY, false)
    }
    
    fun setSunTrackerHourMarkers(context: Context, on: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SUNTRACKER_HOURMARKERS_KEY, on).apply()
    }
    
    fun getSunTrackerText(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(SUNTRACKER_TEXT_KEY, true)
    }
    
    fun setSunTrackerText(context: Context, on: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(SUNTRACKER_TEXT_KEY, on).apply()
    }

    fun getMapLocationPermissionDenied(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        return prefs.getBoolean(MAP_LOCATION_PERMISSION_DENIED_KEY, false)
    }

    fun setMapLocationPermissionDenied(context: Context, denied: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(MAP_LOCATION_PERMISSION_DENIED_KEY, denied).apply()
    }
    
}
