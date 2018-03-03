package uk.co.sundroid.util.geometry

import android.content.Context
import android.hardware.GeomagneticField
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.location.LatitudeLongitude
import java.math.BigDecimal
import java.util.*

enum class Punctuation {
    /**
     * No punctuation between components (applies only to deg/min/sec formatting).
     */
    NONE,
    /**
     * No punctuation between components (applies only to deg/min/sec formatting).
     */
    STANDARD,
    /**
     * No punctuation between components (applies only to deg/min/sec formatting).
     */
    COLONS
}

enum class Accuracy {
    /**
     * Display only the degrees part in the arc value.
     */
    DEGREES,
    /**
     * Display only the degrees and minutes parts in the arc value.
     */
    MINUTES,
    /**
     * Display the degress, minutes and seconds parts in the arc value.
     */
    SECONDS
}
    
/**
 * Display the arc value of angle, made up of degrees, minutes and
 * seconds components.
 * @param angle The angle to be displayed.
 * @param accuracy The accuracy to be used - i.e. which components to display. See enums.
 * @param punctuation The punctuation to be used between components. See enums.
 * @return The angle expressed in arc components.
 */
fun displayArcValue(angle: Angle, accuracy: Accuracy, punctuation: Punctuation): String {
    var result = ""
    val degrees = if (accuracy == Accuracy.DEGREES && angle.minutes > 29) {
        angle.degrees + 1
    } else {
        angle.degrees
    }
    val deg = "000$degrees"
    result += deg.substring(deg.length - 3)
    if (accuracy != Accuracy.DEGREES) {
        if (punctuation == Punctuation.STANDARD) {
            result += "\u00b0"
        } else if (punctuation == Punctuation.COLONS) {
            result += ":"
        }
        val minutes = if (accuracy == Accuracy.MINUTES && angle.seconds > 29) {
            angle.seconds + 1
        } else {
            angle.minutes
        }
        val min = "00$minutes"
        result += min.substring(min.length - 2)
        if (punctuation == Punctuation.STANDARD) {
            result += "'"
        }
        if (accuracy == Accuracy.SECONDS) {
            if (punctuation == Punctuation.COLONS) {
                result += ":"
            }
            val seconds = "${angle.seconds}"
            val sec = "00$seconds"
            result += sec.substring(sec.length - 2)
            if (punctuation == Punctuation.STANDARD) {
                result += '"'
            }
        }
    }
    return result
}
    
/**
 * Attempts to parse a string representation of an angle from strings in
 * most of the formats that the display methods of this class can output.
 * The exception to this is unpadded unpunctuated strings, where it isn't
 * possible to separate the components.
 * @param angle A string representation of an angle, made up of arc values.
 * @return An angle set according to the supplied string.
 * @throws IllegalArgumentException if the string could not be parsed.
 */
fun parseArcValue(angle: String): Angle {
    var string = angle
    try {
        val degrees: String
        var minutes = "0"
        var seconds = "0"

        when {
            string.contains("\u00b0") -> {
                // Parse components from standard punctuated string.
                degrees = string.substringBefore("\u00b0")
                string = string.substringAfter("\u00b0")

                if (string.contains("'")) {
                    minutes = string.substringBefore("'")
                    string = string.substringAfter("'")

                    if (string.contains('"')) {
                        seconds = string.substringBefore('"')
                    }
                }
            }
            string.contains(":") -> {
                // Parse components from colon punctuated string.
                degrees = string.substringBefore(":")
                string = string.substringAfter(":")

                if (string.contains(":")) {
                    minutes = string.substringBefore(":")
                    string = string.substringAfter(":")

                    if (string.isNotEmpty()) {
                        seconds = string
                    }
                } else if (string.isNotEmpty()) {
                    minutes = string
                }
            }
            else -> {
                // Parse components from unpunctuated string.
                degrees = string.substring(0, 3)

                if (string.length >= 5) {
                    minutes = string.substring(3, 5)
                }
                if (string.length >= 7) {
                    seconds = string.substring(5, 7)
                }
            }
        }
        return Angle(degrees.toInt(), minutes.toInt(), seconds.toInt(), Angle.Direction.CLOCKWISE)
    } catch (e: Exception) {
        throw IllegalArgumentException("The string \"$string\" could not be parsed as an angle: $e")
    }
}

fun formatBearing(context: Context, bearing: Double, location: LatitudeLongitude, time: Calendar): String {

    var b = bearing
    if (SharedPrefsHelper.getMagneticBearings(context)) {
        b -= getMagneticDeclination(location, time)
    }
    while (b < 0) {
        b += 360
    }
    while (b > 360) {
        b -= 360
    }

    val bd = BigDecimal(b).setScale(1, BigDecimal.ROUND_HALF_DOWN)
    return "$bd\u00b0"

}

fun formatElevation(elevation: Double): String {
    val bd = BigDecimal(elevation).setScale(1, BigDecimal.ROUND_HALF_DOWN)
    return "$bd\u00b0"
}

fun getMagneticDeclination(location: LatitudeLongitude, time: Calendar): Double {
    val field = GeomagneticField(location.latitude.doubleValue.toFloat(), location.longitude.doubleValue.toFloat(), 0f, time.timeInMillis)
    return field.declination.toDouble()
}
