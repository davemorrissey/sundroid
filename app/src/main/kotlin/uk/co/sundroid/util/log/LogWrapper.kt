package uk.co.sundroid.util.log

import android.util.Log
import uk.co.sundroid.BuildConfig

fun d(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("sundroid.$tag", message)
    }
}
fun i(tag: String, message: String) {
    if (BuildConfig.DEBUG) {
        Log.i("sundroid.$tag", message)
    }
}

fun e(tag: String, message: String, tr: Throwable) {
    if (BuildConfig.DEBUG) {
        Log.e("sundroid.$tag", message, tr)
    }
}
