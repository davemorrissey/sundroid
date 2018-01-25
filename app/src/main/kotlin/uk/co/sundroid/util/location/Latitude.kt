package uk.co.sundroid.util.location

import uk.co.sundroid.util.geometry.parseArcValue as utilsParseArcValue
import uk.co.sundroid.util.geometry.*

/**
 * Represents a latitude value.
 */
class Latitude : Angle {

    /**
     * Indicates whether the angle is North or South, using one of the static fields above.
     */
    private var sign = NORTH
    
    /**
     * Constructs an angle of latitude from a double value. The sign of the angle
     * is based on the sign of the double value (North +, South -). Angles greater
     * than 90 degrees are not accepted.
     * @param doubleValue The double value of the angle. Must not be less than -90 or greater than +90.
     */
    constructor(doubleValue: Double) : super(Math.abs(doubleValue)) {
        sign = if (doubleValue < 0) SOUTH else NORTH
    }

    /**
     * Create a latitude from a string value in abbreviated format.
     * @param latitude A valid abbreviated latitude string.
     */
    constructor(latitude: String) : super(0.0) {
        parseArcValue(latitude)
    }

    /**
     * Overrides super method to set sign.
     */
    override var doubleValue = 0.0
        get() {
            return sign * super.doubleValue
        }

    /**
     * Overrides super method to set sign.
     */
    override fun getE6(): Int = sign * super.getE6()

    /**
     * Overrides super method to check for angles over +/- 90 degrees, and set North/South sign.
     */
    override fun setAngle(doubleValue: Double) {
        super.setAngle(Math.abs(doubleValue))
        if (doubleValue < -90 || doubleValue > 90) {
            throw IllegalArgumentException("Latitude value cannot be less than -90 or greater than 90 degrees.")
        }
        sign = if (doubleValue < 0) SOUTH else NORTH
    }

    /**
     * Overloads super method to check for angles over 90 degrees, and set North/South sign.
     */
    private fun setAngle(degrees: Int, minutes: Int, seconds: Int, sign: Int) {
        super.setAngle(Math.abs(degrees), Math.abs(minutes), Math.abs(seconds))
        if (degrees < -90 || degrees > 90) {
            throw IllegalArgumentException("Latitude value cannot be less than -90 or greater than 90 degrees.")
        }
        if (sign != NORTH && sign != SOUTH) {
            throw IllegalArgumentException("Sign \"$sign\" is not valid.")
        }
        this.sign = sign
    }
    
    /**
     * Displays the latitude in padded, unpunctuated component format.
     */
    fun getAbbreviatedValue(): String {
        // Get the displayed angle value. Then trim off the unnecessary leading.
        var value = displayArcValue(this, Accuracy.SECONDS, Punctuation.NONE)
        value = value.substring(1)
        value += if (sign == NORTH) "N" else "S"
        return value
    }
    
    /**
     * Displays the latitude in padded, punctuated component format with specified accuracy.
     */
    fun getPunctuatedValue(accuracy: Accuracy): String {
        // Get the displayed angle value. Then trim off the unnecessary leading.
        var value = displayArcValue(this, accuracy, Punctuation.STANDARD)
        value = value.substring(1)
        value += if (sign == NORTH) "N" else "S"
        return value
    }
    
    /**
     * Sets the value from a string in arc components format.
     */
    override fun parseArcValue(string: String) {
        val signString = string.substring(string.length - 1)
        val sign = when (signString) {
            "N" -> NORTH
            "S" -> SOUTH
            else -> throw IllegalArgumentException("Couldn't parse \"$string\" as an abbreviated latitude value.")
        }
        try {
            val parsedAngle = utilsParseArcValue("0" + string.substring(0, string.length - 1))
            setAngle(sign * parsedAngle.degrees, parsedAngle.minutes, parsedAngle.seconds, sign)
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
