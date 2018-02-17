package uk.co.sundroid.activity

import uk.co.sundroid.util.location.Geocoder
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.log.*
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.LocationProvider
import android.os.Bundle

class Locater(private val listener: LocaterListener, private val context: Context) : LocationListener {

    private var locationManager: LocationManager? = null
    private var provider: String? = null
    private var finished = false
    private var timeoutThread: TimeoutThread? = null

    enum class LocationType {
        UNKNOWN, // Something is providing location but don't know what
        GPS,
        NETWORK,
        UNAVAILABLE
    }

    @JvmOverloads
    fun start(allowLastKnown: Boolean = true): LocationType {

        if (timeoutThread != null) {
            timeoutThread!!.stop()
            timeoutThread = null
        }

        locationManager = listener.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (locationManager == null) {
            i(TAG, "No location manager service")
            return LocationType.UNAVAILABLE
        }

        if (SharedPrefsHelper.getLastKnownLocation(context) && allowLastKnown) {
            val lastKnownType = startLastKnown()
            if (lastKnownType != LocationType.UNAVAILABLE) {
                return lastKnownType
            }
        }

        var network = false
        try {
            network = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            // Presumably no network provider
        }

        var gps = false
        try {
            gps = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            // Presumably no GPS provider
        }

        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_COARSE
        criteria.accuracy = Criteria.ACCURACY_FINE

        provider = locationManager!!.getBestProvider(criteria, true)

        if (provider == null) {
            i(TAG, "No provider matching criteria")
            return LocationType.UNAVAILABLE
        }
        d(TAG, "Best provider: " + provider!!)

        locationManager!!.requestLocationUpdates(
                provider,
                (1000 * 60 * 10).toLong(), 1000f,
                this,
                listener.mainLooper)

        // Best provider will always attempt to lookup using GPS, which can take ages.
        // If both devices are available, specifically listen for network location as well.
        // Whichever returns first wins.
        if (provider != LocationManager.NETWORK_PROVIDER && network) {
            try {
                d(TAG, "Attempting network lookup in addition to GPS")
                locationManager!!.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        (1000 * 60 * 10).toLong(), 1000f,
                        this,
                        listener.mainLooper)
            } catch (e: Exception) {
                d(TAG, "Couldn't register for network lookup")
            }

        }

        d(TAG, "GPS: $gps, NETWORK: $network, provider: $provider")

        timeoutThread = TimeoutThread()
        timeoutThread!!.start()

        return if (gps && network) {
            LocationType.UNKNOWN
        } else if (gps) {
            LocationType.GPS
        } else if (network) {
            LocationType.NETWORK
        } else {
            // ??
            LocationType.UNKNOWN
        }

    }

    fun startLastKnown(): LocationType {
        try {
            val gpsLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (gpsLocation != null) {
                d(TAG, "GPS last known location received: " + gpsLocation.provider + ", " + gpsLocation.latitude + " " + gpsLocation.longitude)
                val thread = object : Thread() {
                    override fun run() {
                        onLocationChanged(gpsLocation)
                    }
                }
                thread.start()
                return LocationType.GPS
            }
        } catch (e: Exception) {
        }

        try {
            val netLocation = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (netLocation != null) {
                d(TAG, "Network last known location received: " + netLocation.provider + ", " + netLocation.latitude + " " + netLocation.longitude)
                val thread = object : Thread() {
                    override fun run() {
                        onLocationChanged(netLocation)
                    }
                }
                thread.start()
                return LocationType.NETWORK
            }
        } catch (e: Exception) {
        }

        return LocationType.UNAVAILABLE
    }


    fun cancel() {
        if (!finished) {
            if (timeoutThread != null) {
                timeoutThread!!.stop()
                timeoutThread = null
            }
            finished = true
            locationManager!!.removeUpdates(this)
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


    override fun onProviderDisabled(provider: String) {
        if (!finished) {
            d(TAG, "Provider disabled: " + provider)
            if (provider == this.provider) {
                listener.locationError()
            }
            cancel()
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
            d(TAG, "Provider enabled: " + provider)
        }
    }


    override fun onStatusChanged(provider: String, status: Int, extra: Bundle) {
        if (!finished) {
            d(TAG, "Provider state change: $provider, $status")
            if (provider == this.provider && status == LocationProvider.OUT_OF_SERVICE) {
                listener.locationError()
                cancel()
            }
        }
    }

    inner class TimeoutThread : Runnable {

        @Volatile
        private var thread: Thread? = null

        private val TAG = TimeoutThread::class.java.name

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
                Thread.sleep((1000 * SharedPrefsHelper.getLocationTimeout(context)).toLong())
                if (thread === thisThread) {
                    onTimeout()
                }
            } catch (e: InterruptedException) {
                d(TAG, "Interrupted.")
                Thread.currentThread().interrupt()
            }
        }

    }

    companion object {
        private val TAG = Locater::class.java.name
    }

}
