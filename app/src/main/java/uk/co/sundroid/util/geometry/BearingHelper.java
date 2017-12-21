package uk.co.sundroid.util.geometry;

import java.math.BigDecimal;
import java.util.Calendar;

import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.geo.LatitudeLongitude;
import android.content.Context;
import android.hardware.GeomagneticField;

public class BearingHelper {
	
	public static String formatBearing(Context context, double bearing, LatitudeLongitude location, Calendar time) {
		
		if (SharedPrefsHelper.getMagneticBearings(context)) {
			bearing -= getMagneticDeclination(location, time);
		}
		while (bearing < 0) {
			bearing += 360;
		}
		while (bearing > 360) {
			bearing -= 360;
		}
		
		BigDecimal bd = new BigDecimal(bearing);
		bd = bd.setScale(1, BigDecimal.ROUND_HALF_DOWN);
		return bd.toString() + "\u00b0";
		
	}
	
	public static String formatElevation(double elevation) {
		
		BigDecimal bd = new BigDecimal(elevation);
		bd = bd.setScale(1, BigDecimal.ROUND_HALF_DOWN);
		return bd.toString() + "\u00b0";
		
	}
	
	public static double getMagneticDeclination(LatitudeLongitude location, Calendar time) {
		GeomagneticField field = new GeomagneticField((float)location.getLatitude().getDoubleValue(), (float)location.getLongitude().getDoubleValue(), 0f, time.getTimeInMillis());
		return field.getDeclination();
	}

}
