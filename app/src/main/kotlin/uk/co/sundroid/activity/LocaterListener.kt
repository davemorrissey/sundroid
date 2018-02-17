package uk.co.sundroid.activity

import uk.co.sundroid.domain.LocationDetails
import android.os.Looper

interface LocaterListener {

    val mainLooper: Looper

    fun locationError()

    fun locationTimeout()

    fun locationReceived(locationDetails: LocationDetails)

    fun getSystemService(id: String): Any

}
