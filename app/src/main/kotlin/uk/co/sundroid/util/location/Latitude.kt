package uk.co.sundroid.util.location

import uk.co.sundroid.util.geometry.parseArcValue as utilsParseArcValue
import uk.co.sundroid.util.geometry.*

/**
 * Represents a latitude value.
 */
class Latitude : Angle {
    
    /**
     * Constructs an angle of latitude from a double value. The sign of the angle
     * is based on the sign of the double value (North +, South -). Angles greater
     * than 90 degrees are not accepted.
     * @param doubleValue The double value of the angle. Must not be less than -90 or greater than +90.
     */
    constructor(doubleValue: Double) : super(doubleValue)

    /**
     * Create a latitude from a string value in abbreviated format.
     * @param latitude A valid abbreviated latitude string.
     */
    constructor(latitude: String) : super(0.0) {
        parseArcValue(latitude)
    }

    /**
     * Overrides super method to check for angles over +/- 90 degrees, and set North/South sign.
     */
    override fun setAngle(doubleValue: Double) {
        super.setAngle(doubleValue)
        if (doubleValue < -90 || doubleValue > 90) {
            throw IllegalArgumentException("Latitude value cannot be less than -90 or greater than 90 degrees.")
        }
    }
    
    /**
     * Displays the latitude in padded, unpunctuated component format.
     */
    fun getAbbreviatedValue(): String {
        // Get the displayed angle value. Then trim off the unnecessary leading.
        var value = displayArcValue(this, Accuracy.SECONDS, Punctuation.NONE)
        value = value.substring(1)
        value += if (direction == Direction.CLOCKWISE) "N" else "S"
        return value
    }
    
    /**
     * Displays the latitude in padded, punctuated component format with specified accuracy.
     */
    fun getPunctuatedValue(accuracy: Accuracy): String {
        // Get the displayed angle value. Then trim off the unnecessary leading.
        var value = displayArcValue(this, accuracy, Punctuation.STANDARD)
        value = value.substring(1)
        value += if (direction == Direction.CLOCKWISE) "N" else "S"
        return value
    }
    
    /**
     * Sets the value from a string in arc components format.
     */
    private fun parseArcValue(string: String) {
        val sign = when (string.substring(string.length - 1)) {
            "N" -> NORTH
            "S" -> SOUTH
            else -> throw IllegalArgumentException("Couldn't parse \"$string\" as an abbreviated latitude value.")
        }
        try {
            val parsedAngle = utilsParseArcValue("0" + string.substring(0, string.length - 1))
            setAngle(parsedAngle.degrees, parsedAngle.minutes, parsedAngle.seconds, if (sign == NORTH) Direction.CLOCKWISE else Direction.ANTICLOCKWISE)
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't parse \"$string\" as an abbreviated latitude value: $e")
        }
    }
    
    /**
     * String display returns abbreviated value.
     */
    override fun toString(): String = getAbbreviatedValue()

    /**
     * Compare two latitudes for equality. They are considered the same if
     * the displayed abbreviated values (which include degrees, minutes and
     * seconds) are the same. Because of this, latitudes with very slightly
     * different double values may be considered equal but the error equates
     * to a maximum of about 15 metres.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(other: Any?): Boolean {
        return other is Latitude && other.toString() == toString()
    }

    override fun hashCode(): Int = toString().hashCode()

    companion object {
        private const val serialVersionUID = 4806228965383925168L

        /**
         * Sign value for a North latitude.
         */
        private const val NORTH = 1

        /**
         * Sign value for a South latitude.
         */
        private const val SOUTH = -1
    }
    
}
