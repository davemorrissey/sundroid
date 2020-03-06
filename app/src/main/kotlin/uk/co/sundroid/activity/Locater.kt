package uk.co.sundroid.activity

import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.log.*
import android.content.Context
import android.content.Context.*
import android.location.Criteria
import android.location.Criteria.*
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle

class Locater(private val listener: LocaterListener, private val context: Context) : LocationListener {

    private var locationManager: LocationManager? = null
    private var finished = false
    private var timeoutThread: TimeoutThread? = null // TODO Handler with delayed post?

    fun start(): Boolean {
        timeoutThread?.stop()
        timeoutThread = null

        val criteria = Criteria()
        criteria.accuracy = ACCURACY_FINE

        val locationManager = listener.getSystemService(LOCATION_SERVICE) as LocationManager? ?: return false
        try {
            locationManager.requestSingleUpdate(criteria, this, listener.getMainLooper())
        } catch (e: SecurityException) {
            return false
        }

        val timeoutThread = TimeoutThread()
        timeoutThread.start()

        this.locationManager = locationManager
        this.timeoutThread = timeoutThread
        return true
    }

    fun cancel() {
        if (!finished) {
            timeoutThread?.stop()
            timeoutThread = null
            finished = true
            locationManager?.removeUpdates(this)
            d(TAG, "Cancelled")
        }
    }

    override fun onLocationChanged(coords: Location?) {
        if (coords != null) {
            cancel()
            d(TAG, "Location received: " + coords.provider + ", " + coords.latitude + " " + coords.longitude)
            val location = LatitudeLongitude(coords.latitude, coords.longitude)
            val locationDetails = Geocoder.getLocationDetails(location, context)
            listener.locationReceived(locationDetails)
        }
    }

    fun onTimeout() {
        if (!finished) {
            cancel()
            listener.locationTimeout()
        }
    }

    override fun onProviderEnabled(provider: String) {
        if (!finished) {
            d(TAG, "Provider enabled: $provider")
        }
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

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        // We don't know whether another provider will give a location. Continue until the timeout.
    }

    override fun onProviderDisabled(provider: String?) {
        // We don't know whether another provider will give a location. Continue until the timeout.
    }

    companion object {
        private val TAG = Locater::class.java.name
    }

}
