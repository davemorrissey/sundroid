package uk.co.sundroid.util.location

import uk.co.sundroid.util.geometry.parseArcValue as utilsParseArcValue
import uk.co.sundroid.util.geometry.*

/**
 * Represents a longitude value.
 */
class Longitude : Angle {

    /**
     * Constructs an angle of longitude from a double value. The sign of the angle
     * is based on the sign of the double value (East +, West -). Angles greater
     * than 180 degrees are not accepted.
     * @param doubleValue The double value of the angle. Must not be less than -180 or greater than +180.
     */
    constructor(doubleValue: Double) : super(doubleValue)

    /**
     * Create a longitude from a string value in abbreviated format.
     * @param longitude A valid abbreviated latitude string.
     */
    constructor(longitude: String) : super(0.0) {
        parseArcValue(longitude)
    }

    /**
     * Overrides super method to check for angles over +/- 180 degrees, and set East/West sign.
     */
    override fun setAngle(doubleValue: Double) {
        super.setAngle(doubleValue)
        if (doubleValue < -180 || doubleValue > 180) {
            throw IllegalArgumentException("Latitude value cannot be less than -180 or greater than 180 degrees.")
        }
    }

    /**
     * Displays the longitude in padded, unpunctuated component format.
     */
    fun getAbbreviatedValue(): String {
        var value = displayArcValue(this, Accuracy.SECONDS, Punctuation.NONE)
        value += if (direction == Direction.CLOCKWISE) "E" else "W"
        return value
    }

    /**
     * Displays the longitude in padded, punctuated component format with specified accuracy.
     */
    fun getPunctuatedValue(accuracy: Accuracy): String {
        var value = displayArcValue(this, accuracy, Punctuation.STANDARD)
        value += if (direction == Direction.CLOCKWISE) "E" else "W"
        return value
    }
    
    /**
     * Sets the value from a string in arc components format.
     */
    private fun parseArcValue(string: String) {
        val sign = when(string.substring(string.length - 1)) {
            "E" -> EAST
            "W" -> WEST
            else -> throw IllegalArgumentException("Couldn't parse \"$string\" as an abbreviated longitude value.")
        }
        try {
            val parsedAngle = utilsParseArcValue(string.substring(0, string.length - 1))
            setAngle(sign * parsedAngle.degrees, parsedAngle.minutes, parsedAngle.seconds, if (sign == EAST) Direction.CLOCKWISE else Direction.ANTICLOCKWISE)
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't parse \"$string\" as an abbreviated longitude value: $e")
        }
    }
    
    /**
     * String display returns abbreviated value.
     */
    override fun toString(): String = getAbbreviatedValue()

    /**
     * Compare two longitudes for equality. They are considered the same if
     * the displayed abbreviated values (which include degrees, minutes and
     * seconds) are the same. Because of this, longitudes with very slightly
     * different double values may be considered equal but the error equates
     * to a maximum of about 15 metres.
     * @see java.lang.Object#equals(java.lang.Object)
     */
    override fun equals(other: Any?): Boolean {
        return other is Longitude && other.toString() == toString()
    }

    override fun hashCode(): Int = toString().hashCode()

    companion object {
        private const val serialVersionUID = -8737366556829205016L

        /**
         * Sign value for a East longitude.
         */
        private const val EAST = 1

        /**
         * Sign value for a West longitude.
         */
        private const val WEST = -1
    }
    
}
