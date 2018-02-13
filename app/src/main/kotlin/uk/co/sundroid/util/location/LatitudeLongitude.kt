package uk.co.sundroid.util.location

import uk.co.sundroid.util.geometry.Accuracy

import java.io.Serializable
import java.text.ParseException

/**
 * Represents a point on the surface of a planet by combining a latitude and longitude.
 */
class LatitudeLongitude : Serializable {

    /**
     * The latitude angle.
     */
    var latitude = Latitude(0.0)
        private set
    
    /**
     * The longitude angle.
     */
    var longitude = Longitude(0.0)
        private set
    
    /**
     * Create a point from prepared lat and long values.
     * @param latitude The latitude to set.
     * @param longitude The longitude to set.
     * @throws IllegalArgumentException if either the lat or long is null.
     */
    constructor(latitude: Latitude, longitude: Longitude) {
        this.latitude = latitude
        this.longitude = longitude
    }
    
    /**
     * Create a point from two double values.
     * @param latitude The latitude to set.
     * @param longitude The longitude to set.
     * @throws IllegalArgumentException if either the lat or long is out of range.
     */
    constructor(latitude: Double, longitude: Double) {
        this.latitude = Latitude(latitude)
        this.longitude = Longitude(longitude)
    }
    
    /**
     * Create a point from two string values in abbreviated format.
     * @param latitude A valid abbreviated latitude string.
     * @param longitude A valid abbreviated longitude string.
     * @throws ParseException if either the lat or long could not be parsed.
     */
    constructor(latitude: String, longitude: String) {
        this.latitude = Latitude(latitude)
        this.longitude = Longitude(longitude)
    }
    
    /**
     * Create a point from two string values in abbreviated format separated by
     * a space e.g. ddmmssN dddmmssE.
     * @param latitudeLongitude Abbreviated lat and long values separated by a single space.
     * @throws ParseException if the string could not be parsed.
     */
    constructor(latitudeLongitude: String) {
        if (latitudeLongitude.contains(" ")) {
            this.latitude = Latitude(latitudeLongitude.substringBefore(" "))
            this.longitude = Longitude(latitudeLongitude.substringAfter(" "))
        } else {
            throw ParseException("Could not parse lat/long coordinate from abbreviated string \"$latitudeLongitude\"", 0)
        }
    }
    
    /**
     * Gets the co-ordinate in abbreviated format e.g. ddmmssN dddmmssE
     */
    fun getAbbreviatedValue(): String {
        return "${latitude.getAbbreviatedValue()} ${longitude.getAbbreviatedValue()}"
    }
    
    /**
     * Gets the co-ordinate in standard punctuated format with specified accuracy.
     */
    fun getPunctuatedValue(accuracy: Accuracy): String {
        return "${latitude.getPunctuatedValue(accuracy)} ${longitude.getPunctuatedValue(accuracy)}"
    }
    
    /**
     * String display returns abbreviated value.
     */
    override fun toString(): String = getAbbreviatedValue()

    /**
     * Compare two locations for equality. They are considered the same if
     * the displayed abbreviated values (which include degrees, minutes and
     * seconds) are the same. Because of this, locations with very slightly
     * different double values may be considered equal but the error equates
     * to a maximum of about 15 metres.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(other: Any?): Boolean {
        return other is LatitudeLongitude && other.toString() == toString()
    }

    override fun hashCode(): Int = toString().hashCode()

    companion object {
        private const val serialVersionUID = -9133219631765541150L
    }

}
