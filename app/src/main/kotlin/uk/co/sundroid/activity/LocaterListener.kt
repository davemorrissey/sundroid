package uk.co.sundroid.activity

import uk.co.sundroid.domain.LocationDetails
import android.os.Looper

interface LocaterListener {

    fun getMainLooper(): Looper?

    fun locationError(status: LocaterStatus)

    fun locationReceived(locationDetails: LocationDetails)

    fun getSystemService(id: String): Any?

}
