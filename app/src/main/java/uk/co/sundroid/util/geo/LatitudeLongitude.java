package uk.co.sundroid.util.geo;

import uk.co.sundroid.util.geometry.AngleFormat;

import java.io.Serializable;
import java.text.ParseException;

/**
 * Represents a point on the surface of a planet by combining a latitude and longitude.
 */
public class LatitudeLongitude implements Serializable {

    private static final long serialVersionUID = -9133219631765541150L;

    /**
	 * The latitude angle.
	 */
	private Latitude latitude;
	
	/**
	 * The longitude angle.
	 */
	private Longitude longitude;
	
	/**
	 * Create a point from prepared lat and long values.
	 * @param latitude The latitude to set.
	 * @param longitude The longitude to set.
	 * @throws IllegalArgumentException if either the lat or long is null.
	 */
	public LatitudeLongitude(Latitude latitude, Longitude longitude) {
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	/**
	 * Create a point from two double values.
	 * @param latitude The latitude to set.
	 * @param longitude The longitude to set.
	 * @throws IllegalArgumentException if either the lat or long is out of range.
	 */
	public LatitudeLongitude(double latitude, double longitude) throws IllegalArgumentException {
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	/**
	 * Create a point from two string values in abbreviated format.
	 * @param latitude A valid abbreviated latitude string.
	 * @param longitude A valid abbreviated longitude string.
	 * @throws ParseException if either the lat or long could not be parsed.
	 */
	public LatitudeLongitude(String latitude, String longitude) throws ParseException {
		setLatitude(latitude);
		setLongitude(longitude);
	}
	
	/**
	 * Create a point from two string values in abbreviated format separated by
	 * a space e.g. ddmmssN dddmmssE.
	 * @param latitudeLongitude Abbreviated lat and long values separated by a single space.
	 * @throws ParseException if the string could not be parsed.
	 */
	public LatitudeLongitude(String latitudeLongitude) throws ParseException {
		if (latitudeLongitude != null && latitudeLongitude.contains(" ")) {
			String latitude = latitudeLongitude.substring(0, latitudeLongitude.indexOf(" "));
			String longitude = latitudeLongitude.substring(latitudeLongitude.indexOf(" ") + 1);
			setLatitude(latitude);
			setLongitude(longitude);
		} else {
			throw new ParseException("Could not parse lat/long coordinate from abbreviated string \"" + latitudeLongitude + "\"", 0);
		}
	}
	
	/**
	 * Set the latitude to a prepared value.
	 * @param latitude The latitude to set.
	 * @throws IllegalArgumentException if the supplied value is null.
	 */
	private void setLatitude(Latitude latitude) throws IllegalArgumentException {
		if (latitude == null) {
			throw new IllegalArgumentException("Latitude cannot be null.");
		}
		this.latitude = latitude;
	}
	
	/**
	 * Set the longitude to a prepared value.
	 * @param longitude The longitude to set.
	 * @throws IllegalArgumentException if the supplied value is null.
	 */
	private void setLongitude(Longitude longitude) throws IllegalArgumentException {
		if (longitude == null) {
			throw new IllegalArgumentException("Longitude cannot be null.");
		}
		this.longitude = longitude;
	}
	
	/**
	 * Set the latitude from a double value.
	 * @param latitude The latitude to set.
	 * @throws IllegalArgumentException if the value is out of range.
	 */
	private void setLatitude(double latitude) throws IllegalArgumentException {
		this.latitude = new Latitude(latitude);
	}

	/**
	 * Set the longitude from a double value.
	 * @param longitude The longitude to set.
	 * @throws IllegalArgumentException if the value is out of range.
	 */
	private void setLongitude(double longitude) throws IllegalArgumentException {
		this.longitude = new Longitude(longitude);
	}
	
	/**
	 * Set the latitude from a string value in abbreviated format.
	 * @param latitude A valid latitude string in abbreviated format.
	 * @throws ParseException if the string could not be parsed as a latitude value.
	 */
	private void setLatitude(String latitude) throws ParseException {
		if (latitude == null) {
			throw new ParseException("Latitude cannot be null.", 0);			
		}
		this.latitude = new Latitude(0, 0, 0, Latitude.NORTH);
		this.latitude.parseArcValue(latitude);
	}
	
	/**
	 * Set the longitude from a string value in abbreviated format.
	 * @param longitude A valid longitude string in abbreviated format.
	 * @throws ParseException if the string could not be parsed as a longitude value.
	 */
	private void setLongitude(String longitude) throws ParseException {
		if (longitude == null) {
			throw new ParseException("Longitude cannot be null.", 0);			
		}
		this.longitude = new Longitude(0, 0, 0, Longitude.WEST);
		this.longitude.parseArcValue(longitude);
	}
	
	/**
	 * Returns the latitude.
	 * @return the latitude.
	 */
	public Latitude getLatitude() {
		return new Latitude(latitude.getDoubleValue());
	}
	
	/**
	 * Returns the longitude.
	 * @return the longitude.
	 */
	public Longitude getLongitude() {
		return new Longitude(longitude.getDoubleValue());
	}
	
	/**
	 * Gets the co-ordinate in abbreviated format e.g. ddmmssN dddmmssE
	 */
	public String getAbbreviatedValue() {
		return latitude.getAbbreviatedValue() + " " + longitude.getAbbreviatedValue();
	}
	
	/**
	 * Gets the co-ordinate in standard punctuated format with specified accuracy.
	 */
	public String getPunctuatedValue(AngleFormat.Accuracy accuracy) {
		return latitude.getPunctuatedValue(accuracy) + " " + longitude.getPunctuatedValue(accuracy);
	}
	
	/**
	 * String display returns abbreviated value.
	 */
	@Override
	public String toString() {
		return getAbbreviatedValue();
	}
	
	/**
	 * Compare two locations for equality. They are considered the same if
	 * the displayed abbreviated values (which include degrees, minutes and
	 * seconds) are the same. Because of this, locations with very slightly
	 * different double values may be considered equal but the error equates
	 * to a maximum of about 15 metres.
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object object) {
		if (!(object instanceof LatitudeLongitude)) {
			return false;
		}
		LatitudeLongitude compLatLong = (LatitudeLongitude)object;
		return (compLatLong.toString().equals(toString()));
	}
	
	
}
