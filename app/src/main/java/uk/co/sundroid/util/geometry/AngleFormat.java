package uk.co.sundroid.util.geometry;

public class AngleFormat {

	public enum Punctuation {
		/**
		 * No punctuation between components (applies only to deg/min/sec formatting).
		 */
		NONE,
		/**
		 * No punctuation between components (applies only to deg/min/sec formatting).
		 */
		STANDARD,
		/**
		 * No punctuation between components (applies only to deg/min/sec formatting).
		 */
		COLONS
	}

	public enum Accuracy {
		/**
		 * Display only the degrees part in the arc value.
		 */
		DEGREES,
		/**
		 * Display only the degrees and minutes parts in the arc value.
		 */
		MINUTES,
		/**
		 * Display the degress, minutes and seconds parts in the arc value.
		 */
		SECONDS
	}
	
	/**
	 * Display the arc value of angle, made up of degrees, minutes and
	 * seconds components.
	 * @param angle The angle to be displayed.
	 * @param accuracy The accuracy to be used - i.e. which components to display. See enums.
	 * @param punctuation The punctuation to be used between components. See enums.
	 * @return The angle expressed in arc components.
	 */
	public static String displayArcValue(Angle angle, Accuracy accuracy, Punctuation punctuation) {
		String result = "";
		int degrees;
		if (accuracy == Accuracy.DEGREES && angle.getMinutes() > 29) {
			degrees = angle.getDegrees() + 1;
		} else {
			degrees = angle.getDegrees();
		}
		String deg = "000" + degrees;
		result += deg.substring(deg.length()-3);
		if (accuracy != Accuracy.DEGREES) {
			if (punctuation == Punctuation.STANDARD) {
				result += "\u00b0";
			} else if (punctuation == Punctuation.COLONS) {
				result += ":";
			}
			int minutes;
			if (accuracy == Accuracy.MINUTES && angle.getSeconds() > 29) {
				minutes = angle.getMinutes() + 1;
			} else {
				minutes = angle.getMinutes();
			}
			String min = "00" + minutes;
			result += min.substring(min.length()-2);
			if (punctuation == Punctuation.STANDARD) {
				result += "'";
			}
			if (accuracy == Accuracy.SECONDS) {
				if (punctuation == Punctuation.COLONS) {
					result += ":";
				}
				String seconds = "" + angle.getSeconds();
				String sec = "00" + seconds;
				result += sec.substring(sec.length()-2);
				if (punctuation == Punctuation.STANDARD) {
					result += "\"";
				}
			}
		}
		return result;
	}
	
	/**
	 * Attempts to parse a string representation of an angle from strings in
	 * most of the formats that the display methods of this class can output.
	 * The exception to this is unpadded unpunctuated strings, where it isn't
	 * possible to separate the components.
	 * @param angle A string representation of an angle, made up of arc values.
	 * @return An angle set according to the supplied string.
	 * @throws IllegalArgumentException if the string could not be parsed.
	 */
	public static Angle parseArcValue(String angle) throws IllegalArgumentException {
		try {
			Angle result;
			if (angle.contains("�")) {
				// Parse components from standard punctuated string.
				int degrees, minutes = 0, seconds = 0;

				degrees = Integer.parseInt(angle.substring(0, angle.indexOf("�")));
				
				angle = angle.substring(angle.indexOf("�") + 1);
				if (angle.contains("'")) {
					minutes = Integer.parseInt(angle.substring(0, angle.indexOf("'")));
					
					angle = angle.substring(angle.indexOf("'") + 1);
					
					if (angle.contains("\"")) {
						seconds = Integer.parseInt(angle.substring(0, angle.indexOf("\"")));
					}
				}
				result = new Angle(degrees, minutes, seconds);
				
			} else if (angle.contains(":")) {
				// Parse components from colon punctuated string.
				int degrees, minutes = 0, seconds = 0;
				
				degrees = Integer.parseInt(angle.substring(0, angle.indexOf(":")));
				
				angle = angle.substring(angle.indexOf(":") + 1);
				if (angle.contains(":")) {
					minutes = Integer.parseInt(angle.substring(0, angle.indexOf(":")));
					
					angle = angle.substring(angle.indexOf(":") + 1);
					
					if (angle.length() > 0) {
						seconds = Integer.parseInt(angle);
					}
				} else if (angle.length() > 0) {
					minutes = Integer.parseInt(angle);
				}
				result = new Angle(degrees, minutes, seconds);
				
			} else {
				// Parse components from unpunctuated string.
				int degrees, minutes = 0, seconds = 0;

				degrees = Integer.parseInt(angle.substring(0, 3));
				
				if (angle.length() >= 5) {
					minutes = Integer.parseInt(angle.substring(3, 5));
				}
				if (angle.length() >= 7) {
					seconds = Integer.parseInt(angle.substring(5, 7));
				}
				result = new Angle(degrees, minutes, seconds);
			}
			return result;
		} catch (Exception e) {
			throw new IllegalArgumentException("The string \"" + angle + "\" could not be parsed as an angle: " + e.toString());
		}
	}
	
}
