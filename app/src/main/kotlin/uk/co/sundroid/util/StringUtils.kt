package uk.co.sundroid.util

import android.text.Html
import android.text.Spanned
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

fun html(html: String): Spanned {
    return Html.fromHtml(html)
}

fun arrayString(array: IntArray): String? {
    var first = true
    var result: String? = ""
    for (i in array) {
        if (!first) {
            result += ", "
        }
        result += i
        first = false
    }
    return result
}
