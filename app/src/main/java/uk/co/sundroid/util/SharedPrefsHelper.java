package uk.co.sundroid.util;

import uk.co.sundroid.activity.data.DataGroup;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.domain.TimeZoneDetail;
import uk.co.sundroid.util.geo.LatitudeLongitude;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.TimeZoneResolver;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

public class SharedPrefsHelper {
	
	private static final String PREFS_KEY = "sundroid-prefs";
	
	private static final String LOC_LAT_KEY = "location-lat";
	private static final String LOC_LON_KEY = "location-lon";
	private static final String LOC_NAME_KEY = "location-name";
	private static final String LOC_COUNTRY_KEY = "location-country";
	private static final String LOC_STATE_KEY = "location-state";
	private static final String LOC_ZONE_KEY = "location-zone";

    private static final String LAST_DATA_GROUP_KEY = "lastDataGroup";
    private static final String LAST_DETAIL_TAB_KEY = "lastDetailTab";
    private static final String LAST_CALENDAR_KEY = "lastCalendar";

    private static final String LOCATION_TIMEOUT_KEY = "locationTimeout";
	private static final String SHOW_SECONDS_KEY = "showSeconds";
    private static final String THEME_KEY = "theme";
	private static final String CLOCK_KEY = "clock";
	private static final String REVERSE_GEOCODE_KEY = "reverseGeocode";
	private static final String LAST_KNOWN_LOCATION_KEY = "lastKnownLocation";
	private static final String DEFAULT_ZONE_KEY = "defaultTimeZone";
	private static final String DEFAULT_ZONE_OVERRIDE_KEY = "defaultTimeZoneOverride";
	private static final String FIRST_WEEKDAY_KEY = "firstWeekday";
	private static final String SHOW_ZONE_KEY = "showTimeZone";
	private static final String MAGNETIC_BEARINGS_KEY = "magneticBearings";
	private static final String ALARM_IN_SILENT_KEY = "alarmInSilent";
    private static final String ALARM_SOUND_TIMEOUT_KEY = "alarmSoundTimeout";
    private static final String ALARM_VIBRATION_TIMEOUT_KEY = "alarmVibrationTimeout";
	
	private static final String SUNTRACKER_BODY_KEY = "sunTrackerBody";
	private static final String SUNTRACKER_MODE_KEY = "sunTrackerMode";
	private static final String SUNTRACKER_MAPMODE_KEY = "sunTrackerMapMode";
	private static final String SUNTRACKER_COMPASS_KEY = "sunTrackerCompass";
	private static final String SUNTRACKER_LINEARELEVATION_KEY = "sunTrackerLinearElevation";
	private static final String SUNTRACKER_HOURMARKERS_KEY = "sunTrackerHourMarkers";
	private static final String SUNTRACKER_TEXT_KEY = "sunTrackerText";
	
	private static final String LOCMAP_MODE_KEY = "locMapMode";
	
	
	public static LocationDetails getSelectedLocation(Context context) {
				
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		
		if (prefs.contains(LOC_LAT_KEY) && prefs.contains(LOC_LON_KEY)) {
			try {
				LocationDetails locationDetails = new LocationDetails();
				locationDetails.setLocation(new LatitudeLongitude(prefs.getFloat(LOC_LAT_KEY, 0f), prefs.getFloat(LOC_LON_KEY, 0f)));
				locationDetails.setName(prefs.getString(LOC_NAME_KEY, null));
				locationDetails.setCountry(prefs.getString(LOC_COUNTRY_KEY, null));
				locationDetails.setState(prefs.getString(LOC_STATE_KEY, null));
				locationDetails.setTimeZone(TimeZoneResolver.getTimeZone(prefs.getString(LOC_ZONE_KEY, null), true));
				locationDetails.setPossibleTimeZones(TimeZoneResolver.getPossibleTimeZones(locationDetails.getLocation(), locationDetails.getCountry(), locationDetails.getState()));
				return locationDetails;
			} catch (Exception e) {
				// Should never save invalid coordinates.
			}
		}
		
		return null;
		
	}
		
	public static void saveSelectedLocation(Context context, LocationDetails locationDetails) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit()
			.putFloat(LOC_LAT_KEY, (float)locationDetails.getLocation().getLatitude().getDoubleValue())
			.putFloat(LOC_LON_KEY, (float)locationDetails.getLocation().getLongitude().getDoubleValue())
			.putString(LOC_NAME_KEY, locationDetails.getName())
			.putString(LOC_COUNTRY_KEY, locationDetails.getCountry())
			.putString(LOC_STATE_KEY, locationDetails.getState())
			.putString(LOC_ZONE_KEY, locationDetails.getTimeZone() == null ? null : locationDetails.getTimeZone().getId())
			.apply();
	}
	
	public static void initPreferences(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		if (!prefs.contains(CLOCK_KEY)) {
			editor.putString(CLOCK_KEY, ClockType.DEFAULT.name());
		}
        if (!prefs.contains(THEME_KEY)) {
            editor.putString(THEME_KEY, ThemePalette.THEME_DARK);
        }
		if (!prefs.contains(SHOW_SECONDS_KEY)) {
			editor.putBoolean(SHOW_SECONDS_KEY, false);
		}
		if (!prefs.contains(REVERSE_GEOCODE_KEY)) {
			editor.putBoolean(REVERSE_GEOCODE_KEY, true);
		}
		if (!prefs.contains(DEFAULT_ZONE_KEY)) {
			editor.putString(DEFAULT_ZONE_KEY, "~ASK");
		}
		if (!prefs.contains(DEFAULT_ZONE_OVERRIDE_KEY)) {
			editor.putBoolean(DEFAULT_ZONE_OVERRIDE_KEY, false);
		}
		if (!prefs.contains(LAST_KNOWN_LOCATION_KEY)) {
			editor.putBoolean(LAST_KNOWN_LOCATION_KEY, false);
		}
		if (!prefs.contains(FIRST_WEEKDAY_KEY)) {
			editor.putString(FIRST_WEEKDAY_KEY, "1");
		}
		if (!prefs.contains(SHOW_ZONE_KEY)) {
			editor.putBoolean(SHOW_ZONE_KEY, true);
		}
		if (!prefs.contains(ALARM_IN_SILENT_KEY)) {
			editor.putBoolean(ALARM_IN_SILENT_KEY, true);
		}
		if (!prefs.contains(MAGNETIC_BEARINGS_KEY)) {
			editor.putBoolean(MAGNETIC_BEARINGS_KEY, false);
		}
        if (!prefs.contains(LOCATION_TIMEOUT_KEY)) {
            editor.putString(LOCATION_TIMEOUT_KEY, "60");
        }
        if (!prefs.contains(ALARM_SOUND_TIMEOUT_KEY)) {
            editor.putString(ALARM_SOUND_TIMEOUT_KEY, "300");
        }
        if (!prefs.contains(ALARM_VIBRATION_TIMEOUT_KEY)) {
            editor.putString(ALARM_VIBRATION_TIMEOUT_KEY, "300");
        }
		editor.apply();
		
	}
	
	public static boolean getShowSeconds(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(SHOW_SECONDS_KEY, false);
	}
	
	public static boolean getReverseGeocode(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(REVERSE_GEOCODE_KEY, true);
	}
	
	public static boolean getClockType24(Context context) {
		boolean default24 = DateFormat.is24HourFormat(context);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		ClockType clockType = ClockType.valueOf(prefs.getString(CLOCK_KEY, ClockType.DEFAULT.name()));
		return clockType == ClockType.TWENTYFOUR || (clockType == ClockType.DEFAULT && default24);
	}

    public static String getTheme(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(THEME_KEY, ThemePalette.THEME_DARK);
    }

    public static int getLocationTimeout(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String timeout = prefs.getString(LOCATION_TIMEOUT_KEY, "60");
        try {
            return Integer.parseInt(timeout);
        } catch (Exception e) {
            return 60;
        }
    }
	
	public static TimeZoneDetail getDefaultZone(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		String pref = prefs.getString(DEFAULT_ZONE_KEY, "~ASK");
		switch (pref) {
			case "~ASK":
				return null;
			case "~DEVICE":
				return TimeZoneResolver.getTimeZone(null, true);
			default:
				return TimeZoneResolver.getTimeZone(pref, true);
		}
	}
	
	public static boolean getDefaultZoneOverride(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(DEFAULT_ZONE_OVERRIDE_KEY, false);
	}
	
	public static boolean getLastKnownLocation(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(LAST_KNOWN_LOCATION_KEY, false);
	}
	
	public static boolean getShowTimeZone(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(SHOW_ZONE_KEY, true);
	}
	
	public static int getFirstWeekday(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return Integer.parseInt(prefs.getString(FIRST_WEEKDAY_KEY, "1"));
	}
	
	public static boolean getMagneticBearings(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean(MAGNETIC_BEARINGS_KEY, false);
	}
	
	public static void setShowElement(Context context, String ref, boolean show) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		prefs.edit().putBoolean("show-" + ref, show).apply();
	}
	
	public static boolean getShowElement(Context context, String ref, boolean def) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("show-" + ref, def);
	}
	
	public static DataGroup getLastDataGroup(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		String name = prefs.getString(LAST_DATA_GROUP_KEY, DataGroup.DAY_SUMMARY.name());
        try {
            return DataGroup.valueOf(name);
        } catch (Exception e) {
        	// Return default
		}
        return DataGroup.DAY_SUMMARY;
	}
	
	public static void setLastDataGroup(Context context, DataGroup dataGroup) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putString(LAST_DATA_GROUP_KEY, dataGroup.name()).apply();
	}

    public static String getLastDetailTab(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        return prefs.getString(LAST_DETAIL_TAB_KEY, "sun");
    }

    public static void setLastDayDetailTab(Context context, String tab) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        prefs.edit().putString(LAST_DETAIL_TAB_KEY, tab).apply();
    }

    public static int getLastCalendar(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        return prefs.getInt(LAST_CALENDAR_KEY, 0);
    }

    public static void setLastCalendar(Context context, int calendar) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
        prefs.edit().putInt(LAST_CALENDAR_KEY, calendar).apply();
    }


    public static String getLocMapMode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getString(LOCMAP_MODE_KEY, "normal");
	}
	
	public static void setLocMapMode(Context context, String mode) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putString(LOCMAP_MODE_KEY, mode).apply();
	}
	
	public static Body getSunTrackerBody(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		String value = prefs.getString(SUNTRACKER_BODY_KEY, "SUN");
		if (value.equals("all")) {
			return null;
		} else {
			return Body.valueOf(value);
		}
	}
	
	public static String getSunTrackerMode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getString(SUNTRACKER_MODE_KEY, "radar");
	}
	
	public static void setSunTrackerBody(Context context, Body body) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		if (body == null) {
			prefs.edit().putString(SUNTRACKER_BODY_KEY, "all").apply();
		} else {
			prefs.edit().putString(SUNTRACKER_BODY_KEY, body.name()).apply();
		}
	}
	
	public static void setSunTrackerMode(Context context, String mode) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putString(SUNTRACKER_MODE_KEY, mode).apply();
	}
	
	public static String getSunTrackerMapMode(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getString(SUNTRACKER_MAPMODE_KEY, "normal");
	}
	
	public static void setSunTrackerMapMode(Context context, String mode) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putString(SUNTRACKER_MAPMODE_KEY, mode).apply();
	}
	
	public static boolean getSunTrackerLinearElevation(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getBoolean(SUNTRACKER_LINEARELEVATION_KEY, false);
	}
	
	public static void setSunTrackerLinearElevation(Context context, boolean on) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(SUNTRACKER_LINEARELEVATION_KEY, on).apply();
	}
	
	public static boolean getSunTrackerCompass(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getBoolean(SUNTRACKER_COMPASS_KEY, false);
	}
	
	public static void setSunTrackerCompass(Context context, boolean on) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(SUNTRACKER_COMPASS_KEY, on).apply();
	}
	
	public static boolean getSunTrackerHourMarkers(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getBoolean(SUNTRACKER_HOURMARKERS_KEY, false);
	}
	
	public static void setSunTrackerHourMarkers(Context context, boolean on) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(SUNTRACKER_HOURMARKERS_KEY, on).apply();
	}
	
	public static boolean getSunTrackerText(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getBoolean(SUNTRACKER_TEXT_KEY, true);
	}
	
	public static void setSunTrackerText(Context context, boolean on) {
		SharedPreferences prefs = context.getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE);
		prefs.edit().putBoolean(SUNTRACKER_TEXT_KEY, on).apply();
	}
	
	private enum ClockType {
		TWELVE, TWENTYFOUR, DEFAULT
	}
	
}
