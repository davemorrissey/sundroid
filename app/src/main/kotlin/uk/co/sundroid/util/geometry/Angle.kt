package uk.co.sundroid.util.geometry

import uk.co.sundroid.util.geometry.AngleFormat.*
import uk.co.sundroid.util.geometry.AngleFormat.Accuracy.*
import uk.co.sundroid.util.geometry.AngleFormat.Punctuation.*
import java.io.Serializable
import java.text.ParseException

/**
 * Represents an angle and provides methods for getting and setting the angle using either double
 * values or degrees, minutes, seconds values. Also allows the angle to be displayed in various
 * formats.
 */
open class Angle : Serializable {

    /**
     * The double value of this angle to the best possible accuracy.
     */
    open var doubleValue: Double = 0.0

    /**
     * The degrees part of the angle.
     */
    var degrees: Int = 0
    
    /**
     * The minutes part of the angle.
     */
    var minutes: Int = 0
    
    /**
     * The seconds part of the angle.
     */
    var seconds: Int = 0
    
    /**
     * Construct an angle from a double value. The arc degrees, minutes and seconds values will be
     * calculated to the best possible accuracy.
     * @param doubleValue The value to set, in degrees.
     * @throws IllegalArgumentException if the double value is greater than or equal to 360.
     */
    constructor(doubleValue: Double) {
        this.setAngle(doubleValue)
    }
    
    /**
     * Construct an angle using degrees, minutes and seconds values.
     * @param degrees The degrees part of the angle.
     * @param minutes The minutes part of the angle.
     * @param seconds The seconds part of the angle.
     * @throws IllegalArgumentException if degrees > 359 or minutes > 59 or seconds > 59.
     */
    constructor(degrees: Int, minutes: Int, seconds: Int) {
        this.setAngle(degrees, minutes, seconds)
    }
    
    /**
     * Set the angle to a double value. The arc degrees, minutes and seconds values will be calculated
     * to the best possible accuracy.
     * @param doubleValue The value to set, in degrees.
     */
    open fun setAngle(doubleValue: Double) {
        var d = doubleValue
        while (d > 360) {
            d -= 360
        }
        while (d < 0) {
            d += 360
        }
        this.doubleValue = d
        degrees = Math.floor(Math.abs(doubleValue)).toInt()
        minutes = Math.floor((Math.abs(doubleValue) - degrees) * 60).toInt()
        seconds = ((((Math.abs(doubleValue) - degrees) * 60) - minutes) * 60).toInt()
    }
    
    /**
     * Set the angle using degrees, minutes and seconds values.
     * @param degrees The degrees part of the angle.
     * @param minutes The minutes part of the angle.
     * @param seconds The seconds part of the angle.
     * @throws IllegalArgumentException if 0 > degrees > 359 or 0 > minutes > 59 or 0 > seconds > 59.
     */
    open fun setAngle(degrees: Int, minutes: Int, seconds: Int) {
        if (degrees < 0 || degrees > 360 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 60) {
            throw IllegalArgumentException("Degrees value must be < 360 and > 0, minutes < 60 and > 0, and seconds < 60 and > 0.")
        }
        this.degrees = if (degrees == 360) 0 else degrees
        this.minutes = minutes
        this.seconds = seconds
        this.doubleValue = degrees + ((minutes + (seconds / 60.0)) / 60.0)
    }
    
    /**
     * Sets the value from a string in arc components format.
     * @param string An angle expressed using arc components.
     * @throws ParseException if the string format is invalid.
     */
    open fun parseArcValue(string: String) {
        try {
            val parsedAngle = AngleFormat.parseArcValue(string.substring(0, string.length - 1))
            setAngle(parsedAngle.degrees, parsedAngle.minutes, parsedAngle.seconds)
        } catch (e: Exception) {
            throw IllegalArgumentException("Couldn't parse $string as an arc angle value: $e")
        }
    }
    
    /**
     * Supports Google's E6 integer format.
     * @return the angle expressed in Google's E6 format.
     */
    open fun getE6(): Int {
        return (doubleValue * 1e6).toInt()
    }
    
    /**
     * String display returns padded punctuated arc representation.
     */
    override fun toString(): String {
        return displayArcValue(this, SECONDS, STANDARD)
    }

    companion object {
        private const val serialVersionUID = 6445108267172572771L
    }

}
