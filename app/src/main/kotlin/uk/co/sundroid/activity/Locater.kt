package uk.co.sundroid.activity

import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.location.Criteria
import android.location.Criteria.ACCURACY_FINE
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.activity.LocaterStatus.*

class Locater(private val listener: LocaterListener, private val context: Context) : LocationListener {

    private var finished = false
    private var timeoutThread: TimeoutThread? = null // TODO Handler with delayed post?

    fun start(accuracy: Int = ACCURACY_FINE): LocaterStatus {
        timeoutThread?.stop()
        timeoutThread = null

        val criteria = Criteria()
        criteria.accuracy = accuracy

        val locationManager = listener.getSystemService(LOCATION_SERVICE) as LocationManager? ?: return UNAVAILABLE
        val looper = listener.getMainLooper() ?: return UNAVAILABLE
        try {
            locationManager.requestSingleUpdate(criteria, this, looper)
        } catch (e: SecurityException) {
            return DENIED
        } catch (e: Exception) {
            return UNAVAILABLE
        }

        val timeoutThread = TimeoutThread()
        timeoutThread.start()

        this.timeoutThread = timeoutThread
        return STARTED
    }

    fun startLastKnown(): LocaterStatus {
        val locationManager = listener.getSystemService(LOCATION_SERVICE) as LocationManager? ?: return UNAVAILABLE
        for (provider in arrayOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)) {
            try {
                locationManager.getLastKnownLocation(provider)?.let {
                    Thread { onLocationChanged(it) }.start()
                    return STARTED
                }
            } catch (e: SecurityException) {
                return DENIED
            } catch (e: Exception) {
            }
        }
        return UNAVAILABLE
    }

    fun cancel() {
        if (!finished) {
            timeoutThread?.stop()
            timeoutThread = null
            finished = true
            (listener.getSystemService(LOCATION_SERVICE) as LocationManager?)?.removeUpdates(this)
            d(TAG, "Cancelled")
        }
    }

    override fun onLocationChanged(coords: Location) {
        cancel()
        d(TAG, "Location received: " + coords.provider + ", " + coords.latitude + " " + coords.longitude)
        val location = LatitudeLongitude(coords.latitude, coords.longitude)
        val locationDetails = Geocoder.getLocationDetails(location, context)
        listener.locationReceived(locationDetails)
    }

    fun onTimeout() {
        if (!finished) {
            cancel()
            listener.locationError(TIMEOUT)
        }
    }

    /**
     * A disabled message from the fused provider indicates location services are disabled. Errors
     * from other provider names aren't necessarily fatal and location will continue until timeout.
     */
    override fun onProviderDisabled(provider: String) {
        if (!finished) {
            d(TAG, "Provider disabled: $provider")
            cancel()
            listener.locationError(DISABLED)
        }
    }

    override fun onProviderEnabled(provider: String) {
        // Not required
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // Never called
    }

    inner class TimeoutThread : Runnable {

        @Volatile
        private var thread: Thread? = null

        private val tag = TimeoutThread::class.java.name

        fun start() {
            val thread = Thread(this, "LocationTimeout")
            thread.start()
            this.thread = thread
        }

        fun stop() {
            val copy = thread
            thread = null
            copy?.interrupt()
        }

        override fun run() {
            val thisThread = Thread.currentThread()
            try {
                Thread.sleep((1000 * Prefs.locationTimeout(context)).toLong())
                if (thread === thisThread) {
                    onTimeout()
                }
            } catch (e: InterruptedException) {
                d(tag, "Interrupted.")
                Thread.currentThread().interrupt()
            }
        }

    }

    companion object {
        private val TAG = Locater::class.java.name
    }

}
