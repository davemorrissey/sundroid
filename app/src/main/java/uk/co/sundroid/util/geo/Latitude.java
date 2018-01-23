package uk.co.sundroid.util.geo;

import uk.co.sundroid.util.geometry.Accuracy;
import uk.co.sundroid.util.geometry.Angle;
import uk.co.sundroid.util.geometry.GeometryUtils;
import uk.co.sundroid.util.geometry.Punctuation;

/**
 * Represents a latitude value.
 */
public class Latitude extends Angle {

	private static final long serialVersionUID = 4806228965383925168L;

	/**
	 * Sign value for a North latitude.
	 */
	static final int NORTH = 1;

	/**
	 * Sign value for a South latitude.
	 */
	private static final int SOUTH = -1;

	/**
	 * Indicates whether the angle is North or South, using one of the static fields above.
	 */
	private int sign = 1;
	
	/**
	 * Constructs an angle of latitude from a double value. The sign of the angle
	 * is based on the sign of the double value (North +, South -). Angles greater
	 * than 90 degrees are not accepted.
	 * @param doubleValue The double value of the angle. Must not be less than -90 or greater than +90.
	 * @throws IllegalArgumentException if an invalid double value is given.
	 */
	Latitude(double doubleValue) throws IllegalArgumentException {
		super(Math.abs(doubleValue));
		if (doubleValue < 0) {
			sign = SOUTH;
		} else {
			sign = NORTH;
		}
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
	 * Constructs an angle of latitude from arc degrees, minutes and seconds values pus N/S sign.
	 * @param degrees The degrees part of the angle. Must not be greater than +90. The sign is ignored.
	 * @param minutes The minutes part of the angle. The sign is ignored.
	 * @param seconds The seconds part of the angle. The sign is ignored.
	 * @param sign The sign - either NORTH or SOUTH.
	 * @throws IllegalArgumentException if an invalid value is given.
	 */
	Latitude(int degrees, int minutes, int seconds, int sign) throws IllegalArgumentException {
		super(Math.abs(degrees), Math.abs(minutes), Math.abs(seconds));
		if (sign != NORTH && sign != SOUTH) {
			throw new IllegalArgumentException("Sign \"" + sign + "\" is not valid.");
		}
		this.sign = sign;
	}
	
	/**
	 * Overrides super method to check for angles over +/- 90 degrees, and set North/South sign.
	 */
	@Override
	public void setAngle(double doubleValue) {
		super.setAngle(Math.abs(doubleValue));
		if (doubleValue < -90 || doubleValue > 90) {
			throw new IllegalArgumentException("Latitude value cannot be less than -90 or greater than 90 degrees.");
		}
		if (doubleValue < 0) {
			sign = SOUTH;
		} else {
			sign = NORTH;
		}
	}
	/**
	 * Overrides super method to check for angles over 90 degrees, and set North/South sign.
	 */
	private void setAngle(int degrees, int minutes, int seconds, int sign) throws IllegalArgumentException {
		super.setAngle(Math.abs(degrees), Math.abs(minutes), Math.abs(seconds));
		if (degrees < -90 || degrees > 90) {
			throw new IllegalArgumentException("Latitude value cannot be less than -90 or greater than 90 degrees.");
		}
		if (sign != NORTH && sign != SOUTH) {
			throw new IllegalArgumentException("Sign \"" + sign + "\" is not valid.");
		}
		this.sign = sign;
	}
	
	/**
	 * Displays the latitude in padded, unpunctuated component format.
	 */
	String getAbbreviatedValue() {
		// Get the displayed angle value. Then trim off the unnecessary leading.
		String value = GeometryUtils.displayArcValue(this, Accuracy.SECONDS, Punctuation.NONE);
		value = value.substring(1);
		if (sign == NORTH) {
			value += "N";
		} else {
			value += "S";
		}
		return value;
	}
	
	/**
	 * Displays the latitude in padded, punctuated component format with specified accuracy.
	 */
	String getPunctuatedValue(Accuracy accuracy) {
		// Get the displayed angle value. Then trim off the unnecessary leading.
		String value = GeometryUtils.displayArcValue(this, accuracy, Punctuation.STANDARD);
		value = value.substring(1);
		if (sign == NORTH) {
			value += "N";
		} else {
			value += "S";
		}
		return value;
	}
	
	/**
	 * Sets the value from a string in arc components format.
	 */
	@Override
	public void parseArcValue(String string) {
		String signString = string.substring(string.length() - 1);
		int sign;
		switch (signString) {
			case "N":
				sign = NORTH;
				break;
			case "S":
				sign = SOUTH;
				break;
			default:
				throw new IllegalArgumentException("Couldn't parse \"" + string + "\" as an abbreviated latitude value.");
		}
		try {
			Angle parsedAngle = GeometryUtils.parseArcValue("0" + string.substring(0, string.length() - 1));
			setAngle(sign*parsedAngle.getDegrees(), parsedAngle.getMinutes(), parsedAngle.getSeconds(), sign);
		} catch (Exception e) {
			throw new IllegalArgumentException("Couldn't parse \"" + string + "\" as an abbreviated latitude value: " + e.toString());
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
	 * Compare two latitudes for equality. They are considered the same if
	 * the displayed abbreviated values (which include degrees, minutes and
	 * seconds) are the same. Because of this, latitudes with very slightly
	 * different double values may be considered equal but the error equates
	 * to a maximum of about 15 metres.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof Latitude)) {
			return false;
		}
		Latitude compLatitude = (Latitude)object;
		return (compLatitude.toString().equals(toString()));
	}
	
}
