package uk.co.sundroid.util.geometry;

import java.io.Serializable;
import java.text.ParseException;

/**
 * Represents an angle and provides methods for getting and setting the angle using either double
 * values or degrees, minutes, seconds values. Also allows the angle to be displayed in various
 * formats.
 */
public class Angle implements Serializable {

	private static final long serialVersionUID = 6445108267172572771L;

	/**
	 * The double value of this angle to the best possible accuracy.
	 */
	private double doubleValue = 0;
	
	/**
	 * The degrees part of the angle.
	 */
	private int degrees = 0;
	
	/**
	 * The minutes part of the angle.
	 */
	private int minutes = 0;
	
	/**
	 * The seconds part of the angle.
	 */
	private int seconds = 0;
	
	/**
	 * Construct an angle from a double value. The arc degrees, minutes and seconds values will be
	 * calculated to the best possible accuracy.
	 * @param doubleValue The value to set, in degrees.
	 * @throws IllegalArgumentException if the double value is greater than or equal to 360.
	 */
	public Angle(double doubleValue) throws IllegalArgumentException {
		setAngle(doubleValue);
	}
	
	/**
	 * Construct an angle using degrees, minutes and seconds values.
	 * @param degrees The degrees part of the angle.
	 * @param minutes The minutes part of the angle.
	 * @param seconds The seconds part of the angle.
	 * @throws IllegalArgumentException if degrees > 359 or minutes > 59 or seconds > 59.
	 */
	public Angle(int degrees, int minutes, int seconds) throws IllegalArgumentException {
		setAngle(degrees, minutes, seconds);
	}
	
	/**
	 * Set the angle to a double value. The arc degrees, minutes and seconds values will be calculated
	 * to the best possible accuracy.
	 * @param doubleValue The value to set, in degrees.
	 */
	public void setAngle(double doubleValue) {
		while (doubleValue > 360) {
			doubleValue -= 360;
		}
		while (doubleValue < 0) {
			doubleValue += 360;
		}
		this.doubleValue = doubleValue;
		degrees = (int)Math.floor(Math.abs(doubleValue));
		minutes = (int)Math.floor((Math.abs(doubleValue) - degrees) * 60);
		seconds = (int)((((Math.abs(doubleValue) - degrees) * 60) - minutes) * 60);
	}
	
	/**
	 * Set the angle using degrees, minutes and seconds values.
	 * @param degrees The degrees part of the angle.
	 * @param minutes The minutes part of the angle.
	 * @param seconds The seconds part of the angle.
	 * @throws IllegalArgumentException if 0 > degrees > 359 or 0 > minutes > 59 or 0 > seconds > 59.
	 */
	protected void setAngle(int degrees, int minutes, int seconds) throws IllegalArgumentException {
		if (degrees < 0 || degrees > 360 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 60) {
			throw new IllegalArgumentException("Degrees value must be < 360 and > 0, minutes < 60 and > 0, and seconds < 60 and > 0.");
		}
		this.degrees = degrees == 360 ? 0 : degrees;
		this.minutes = minutes;
		this.seconds = seconds;
		this.doubleValue = degrees + ((minutes + (seconds / 60d)) / 60d);
	}
	
	/**
	 * Sets the value from a string in arc components format.
	 * @param string An angle expressed using arc components.
	 * @throws ParseException if the string format is invalid.
	 */
	public void parseArcValue(String string) throws ParseException {
		try {
			Angle parsedAngle = AngleFormat.parseArcValue(string.substring(0, string.length() - 1));
			setAngle(parsedAngle.getDegrees(), parsedAngle.getMinutes(), parsedAngle.getSeconds());
		} catch (Exception e) {
			throw new ParseException("Couldn't parse \"" + string + "\" as an arc angle value: " + e.toString(), 0);
		}
	}
	
	/**
	 * Get the double value of this angle.
	 * @return the double value.
	 */
	public double getDoubleValue() {
		return doubleValue;
	}
	
	/**
	 * Get the degrees component.
	 * @return the degrees component.
	 */
	public int getDegrees() {
		return degrees;
	}
	
	/**
	 * Get the minutes component.
	 * @return the minutes component.
	 */
	public int getMinutes() {
		return minutes;
	}
	
	/**
	 * Get the seconds component.
	 * @return the seconds component.
	 */
	public int getSeconds() {
		return seconds;
	}
	
	/**
	 * Supports Google's E6 integer format.
	 * @return the angle expressed in Google's E6 format.
	 */
	public int getE6() {
		return (int)(doubleValue * 1e6);
	}
	
	/**
	 * String display returns padded punctuated arc representation.
	 */
	@Override
	public String toString() {
		return AngleFormat.displayArcValue(this, AngleFormat.Accuracy.SECONDS, AngleFormat.Punctuation.STANDARD);
	}

}
