package uk.co.sundroid.util

import kotlin.math.max

fun isEmpty(string: String?): Boolean {
    return string.isNullOrEmpty()
}

fun isNotEmpty(string: String?): Boolean {
    return !isEmpty(string)
}

fun zeroPad(string: String, length: Int): String {
    return "0".repeat(max(0, length - string.length)) + string
}

fun zeroPad(number: Int, length: Int): String {
    return zeroPad(number.toString(), length)
}
