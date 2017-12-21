package uk.co.sundroid.util;

public class StringUtils {
	
	public static boolean isEmpty(String string) {
		return string == null || string.length() == 0;
	}

	public static boolean isNotEmpty(String string) {
		return !isEmpty(string);
	}
	
}
