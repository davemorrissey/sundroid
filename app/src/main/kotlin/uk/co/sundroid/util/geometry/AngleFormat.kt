@file:JvmName("GeometryUtils")
package uk.co.sundroid.util.geometry

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
        when {
            string.contains("\u00b0") -> {
                // Parse components from standard punctuated string.
                val degrees = string.substring(0, string.indexOf("\u00b0")).toInt()
                var minutes = 0
                var seconds = 0

                string = string.substring(string.indexOf("\u00b0") + 1)
                if (string.contains("'")) {
                    minutes = Integer.parseInt(string.substring(0, string.indexOf("'")))

                    string = string.substring(string.indexOf("'") + 1)

                    if (string.contains("\"")) {
                        seconds = Integer.parseInt(string.substring(0, string.indexOf("\"")))
                    }
                }
                return Angle(degrees, minutes, seconds)

            }
            string.contains(":") -> {
                // Parse components from colon punctuated string.
                val degrees = string.substring(0, string.indexOf(":")).toInt()
                var minutes = 0
                var seconds = 0

                string = string.substring(string.indexOf(":") + 1)
                if (string.contains(":")) {
                    minutes = string.substring(0, string.indexOf(":")).toInt()

                    string = string.substring(string.indexOf(":") + 1)

                    if (string.length > 0) {
                        seconds = Integer.parseInt(string)
                    }
                } else if (string.length > 0) {
                    minutes = Integer.parseInt(string)
                }
                return Angle(degrees, minutes, seconds)

            }
            else -> {
                // Parse components from unpunctuated string.
                val degrees = string.substring(0, 3).toInt()
                var minutes = 0
                var seconds = 0

                if (string.length >= 5) {
                    minutes = string.substring(3, 5).toInt()
                }
                if (string.length >= 7) {
                    seconds = string.substring(5, 7).toInt()
                }
                return Angle(degrees, minutes, seconds)
            }
        }
    } catch (e: Exception) {
        throw IllegalArgumentException("The string \"$string\" could not be parsed as an angle: $e")
    }
}
