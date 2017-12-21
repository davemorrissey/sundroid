package uk.co.sundroid.util;

public class ArrayUtils {
	
	public static <T> boolean contains(T[] array, T object) {
		if (array == null) { return false; }
		for (T item : array) {
			if (item == object) {
				return true;
			}
		}
		return false;
	}

}
