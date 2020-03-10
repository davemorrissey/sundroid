package uk.co.sundroid.util.geometry

import uk.co.sundroid.util.geometry.Accuracy.*
import uk.co.sundroid.util.geometry.Punctuation.*
import java.io.Serializable
import kotlin.math.abs
import kotlin.math.floor

/**
 * Represents an angle and provides methods for getting and setting the angle using either double
 * values or degrees, minutes, seconds values. Also allows the angle to be displayed in various
 * formats.
 */
open class Angle : Serializable {

    enum class Direction {
        CLOCKWISE,
        ANTICLOCKWISE
    }

    /**
     * The double value of this angle to the best possible accuracy.
     */
    var doubleValue: Double = 0.0

    /**
     * The angle's direction or sign.
     */
    var direction: Direction = Direction.CLOCKWISE

    /**
     * The degrees part of the angle.
     */
    val degrees: Int
        get() {
            return floor(abs(doubleValue)).toInt()
        }

    /**
     * The minutes part of the angle.
     */
    val minutes: Int
        get() {
            val degrees = floor(abs(doubleValue)).toInt()
            return floor((abs(doubleValue) - degrees) * 60).toInt()
        }

    /**
     * The seconds part of the angle.
     */
    val seconds: Int
        get() {
            val degrees = floor(abs(doubleValue)).toInt()
            val minutes = floor((abs(doubleValue) - degrees) * 60).toInt()
            return ((((abs(doubleValue) - degrees) * 60) - minutes) * 60).toInt()
        }

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
     * @param degrees The degrees part of the angle. Can be negative.
     * @param minutes The minutes part of the angle. Must be positive.
     * @param seconds The seconds part of the angle. Must be positive.
     * @throws IllegalArgumentException if degrees > 359 or minutes > 59 or seconds > 59.
     */
    constructor(degrees: Int, minutes: Int, seconds: Int, direction: Direction) {
        this.setAngle(degrees, minutes, seconds, direction)
    }
    
    /**
     * Set the angle to a double value. The arc degrees, minutes and seconds values will be calculated
     * to the best possible accuracy.
     * @param doubleValue The value to set, in degrees.
     */
    open fun setAngle(doubleValue: Double) {
        var double = doubleValue
        while (double > 360) {
            double -= 360
        }
        while (double < -360) {
            double += 360
        }
        this.doubleValue = double
        this.direction = if (doubleValue < 0) Direction.ANTICLOCKWISE else Direction.CLOCKWISE
    }
    
    /**
     * Set the angle using degrees, minutes and seconds values.
     * @param degrees The degrees part of the angle.
     * @param minutes The minutes part of the angle.
     * @param seconds The seconds part of the angle.
     *
     * @throws IllegalArgumentException if 0 > degrees > 359 or 0 > minutes > 59 or 0 > seconds > 59.
     */
    protected open fun setAngle(degrees: Int, minutes: Int, seconds: Int, direction: Direction) {
        if (degrees < -360 || degrees > 360 || minutes < 0 || minutes > 59 || seconds < 0 || seconds > 60) {
            throw IllegalArgumentException("Degrees value must be < 360 and > -360, minutes < 60 and > 0, and seconds < 60 and > 0.")
        }
        this.doubleValue = (degrees + ((minutes + (seconds / 60.0)) / 60.0)) * if (degrees < 0) -1 else 1
        this.direction = direction
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
