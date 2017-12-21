package uk.co.sundroid.util;

import uk.co.sundroid.BuildConfig;
import android.util.Log;

public class LogWrapper {
	
	public static void d(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.d("sundroid." + tag, message);
		}
	}
	
	public static void i(String tag, String message) {
		if (BuildConfig.DEBUG) {
			Log.i("sundroid." + tag, message);
		}
	}
	
	public static void e(String tag, String message, Throwable tr) {
		if (BuildConfig.DEBUG) {
			Log.e("sundroid." + tag, message, tr);
		}
	}
	
}
