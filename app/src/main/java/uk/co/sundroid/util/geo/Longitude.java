package uk.co.sundroid.util.geo;

import uk.co.sundroid.util.geometry.Angle;
import uk.co.sundroid.util.geometry.AngleFormat;

import java.text.ParseException;

/**
 * Represents a longitude value.
 */
public class Longitude extends Angle {

	private static final long serialVersionUID = -8737366556829205016L;

	/**
	 * Sign value for a East longitude.
	 */
	private static final int EAST = 1;

	/**
	 * Sign value for a West longitude.
	 */
	static final int WEST = -1;

	/**
	 * Indicates whether the angle is East or West, using one of the static fields above.
	 */
	private int sign = 1;
	
	/**
	 * Constructs an angle of longitude from a double value. The sign of the angle
	 * is based on the sign of the double value (East +, West -). Angles greater
	 * than 180 degrees are not accepted.
	 * @param doubleValue The double value of the angle. Must not be less than -180 or greater than +180.
	 * @throws IllegalArgumentException if an invalid double value is given.
	 */
	Longitude(double doubleValue) throws IllegalArgumentException {
		super(Math.abs(doubleValue));
		if (doubleValue < 0) {
			sign = WEST;
		} else {
			sign = EAST;
		}
	}
	
	/**
	 * Constructs an angle of longitude from arc degrees, minutes and seconds values plus E/W sign.
	 * @param degrees The degrees part of the angle. Must not be greater than 180. The sign is ignored.
	 * @param minutes The minutes part of the angle. The sign is ignored.
	 * @param seconds The seconds part of the angle. The sign is ignored.
	 * @param sign The sign - either EAST or WEST
	 * @throws IllegalArgumentException if an invalid value is given.
	 */
	Longitude(int degrees, int minutes, int seconds, int sign) throws IllegalArgumentException {
		super(Math.abs(degrees), Math.abs(minutes), Math.abs(seconds));
		if (sign != EAST && sign != WEST) {
			throw new IllegalArgumentException("Sign \"" + sign + "\" is not valid.");
		}
		this.sign = sign;
	}
	
	/**
	 * Overrides super method to set sign.
	 */
	@Override
	public double getDoubleValue() {
		return sign * super.getDoubleValue();
	}
	
	/**
	 * Overrides super method to set sign.
	 */
	@Override
	public int getE6() {
		return sign * super.getE6();
	}
	
	/**
	 * Overrides super method to check for angles over +/- 180 degrees, and set East/West sign.
	 */
	@Override
	public void setAngle(double doubleValue) {
		super.setAngle(Math.abs(doubleValue));
		if (doubleValue < -180 || doubleValue > 180) {
			throw new IllegalArgumentException("Latitude value cannot be less than -180 or greater than 180 degrees.");
		}
		if (doubleValue < 0) {
			sign = WEST;
		} else {
			sign = EAST;
		}
	}
	/**
	 * Overrides super method to check for angles over 180 degrees, and set North/South sign.
	 */
	private void setAngle(int degrees, int minutes, int seconds, int sign) throws IllegalArgumentException {
		super.setAngle(Math.abs(degrees), Math.abs(minutes), Math.abs(seconds));
		if (degrees < -180 || degrees > 180) {
			throw new IllegalArgumentException("Latitude value cannot be less than -180 or greater than 180 degrees.");
		}
		if (sign != EAST && sign != WEST) {
			throw new IllegalArgumentException("Sign \"" + sign + "\" is not valid.");
		}
		this.sign = sign;
	}

	/**
	 * Displays the longitude in padded, unpunctuated component format.
	 */
	String getAbbreviatedValue() {
		String value = AngleFormat.displayArcValue(this, AngleFormat.Accuracy.SECONDS, AngleFormat.Punctuation.NONE);
		if (sign == EAST) {
			value += "E";
		} else {
			value += "W";
		}
		return value;
	}

	/**
	 * Displays the longitude in padded, punctuated component format with specified accuracy.
	 */
	String getPunctuatedValue(AngleFormat.Accuracy accuracy) {
		String value = AngleFormat.displayArcValue(this, accuracy, AngleFormat.Punctuation.STANDARD);
		if (sign == EAST) {
			value += "E";
		} else {
			value += "W";
		}
		return value;
	}
	
	/**
	 * Sets the value from a string in arc components format.
	 */
	@Override
	public void parseArcValue(String string) throws ParseException {
		String signString = string.substring(string.length() - 1);
		int sign;
		switch (signString) {
			case "E":
				sign = EAST;
				break;
			case "W":
				sign = WEST;
				break;
			default:
				throw new ParseException("Couldn't parse \"" + string + "\" as an abbreviated longitude value.", 0);
		}
		try {
			Angle parsedAngle = AngleFormat.parseArcValue(string.substring(0, string.length() - 1));
			setAngle(sign*parsedAngle.getDegrees(), parsedAngle.getMinutes(), parsedAngle.getSeconds(), sign);
		} catch (Exception e) {
			throw new ParseException("Couldn't parse \"" + string + "\" as an abbreviated longitude value: " + e.toString(), 0);
		}
	}
	
	/**
	 * String display returns abbreviated value.
	 */
	@Override
	public String toString() {
		return getAbbreviatedValue();
	}
	
	/**
	 * Compare two longitudes for equality. They are considered the same if
	 * the displayed abbreviated values (which include degrees, minutes and
	 * seconds) are the same. Because of this, longitudes with very slightly
	 * different double values may be considered equal but the error equates
	 * to a maximum of about 15 metres.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Longitude)) {
			return false;
		}
		Longitude compLongitude = (Longitude)object;
		return (compLongitude.toString().equals(toString()));
	}
	
}
