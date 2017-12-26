package uk.co.sundroid.util

fun isEmpty(string: String?): Boolean {
    return string.isNullOrEmpty()
}

fun isNotEmpty(string: String?): Boolean {
    return !isEmpty(string)
}

fun zeroPad(string: String, length: Int): String {
    return "0".repeat(Math.max(0, length - string.length)) + string
}
