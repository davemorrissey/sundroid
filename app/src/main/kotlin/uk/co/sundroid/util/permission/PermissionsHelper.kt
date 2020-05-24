package uk.co.sundroid.util.permission


import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Build.VERSION.SDK_INT
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

@RequiresApi(23)
fun backgroundLocationPermission(): Array<String> {
    return when {
        SDK_INT >= 29 -> arrayOf(ACCESS_FINE_LOCATION, ACCESS_BACKGROUND_LOCATION)
        else -> arrayOf(ACCESS_FINE_LOCATION)
    }
}

fun backgroundLocationGranted(context: Context): Boolean {
    return when {
        SDK_INT >= 29 -> backgroundLocationGranted29(context)
        SDK_INT >= 23 -> backgroundLocationGranted23(context)
        else -> true
    }
}

@RequiresApi(29)
fun backgroundLocationGranted29(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, ACCESS_BACKGROUND_LOCATION) == PERMISSION_GRANTED
}

@RequiresApi(23)
fun backgroundLocationGranted23(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) == PERMISSION_GRANTED
}