package uk.co.sundroid.widget.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.content.ComponentName
import android.location.Criteria.ACCURACY_COARSE
import android.os.Build
import android.os.Looper
import uk.co.sundroid.activity.Locater
import uk.co.sundroid.activity.LocaterListener
import uk.co.sundroid.activity.LocaterStatus
import uk.co.sundroid.activity.LocaterStatus.*
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.log.e
import uk.co.sundroid.util.permission.backgroundLocationGranted
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import uk.co.sundroid.widget.*
import java.util.*

const val EXTRA_OP = "operation"

// Refresh, getting new location if last used one is over 6 hours old.
const val OP_REFRESH = "REFRESH"

// Refresh, getting a new location if possible.
const val OP_TAP_REFRESH = "TAP_REFRESH"

// Render only, always using old location if there is one available.
const val OP_RENDER = "RENDER"

class WidgetUpdateService : JobService() {

    override fun onStartJob(params: JobParameters?): Boolean {
        params?.let {
            // Widget ID is not sent for config changes, which prompts all widgets to be updated.
            val widgetId = params.extras.getInt(EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID)
            val operation = params.extras.getString(EXTRA_OP) ?: OP_RENDER
            d(TAG, "onStartJob $widgetId $operation")

            // Get a map of widget ID to the saved location for that widget if present
            var widgetLocations = getWidgetLocations(widgetId)
            val cachedLocation = getCachedAutoLocation(operation)
            val autoLocateWidgetIds = widgetLocations.entries.filter { e -> e.value == null }.map { e -> e.key }

            if (!widgetLocations.containsValue(null)) {
                // Fixed location for all widgets allows synchronous update
                d(TAG, "Fixed location for all widgets being updated")
                updateWidgets(widgetLocations, null)
            } else if (!backgroundLocationGranted(applicationContext) && Build.VERSION.SDK_INT > 23) {
                // No background location permission, show error message on autolocated widgets
                // This is done without using cached location so new widgets show error immediately
                d(TAG, "Background location permission denied")
                updateWidgets(widgetLocations, DENIED)
            } else if (cachedLocation != null) {
                // Use cached location if one is available
                d(TAG, "Fixed or cached location available for all widgets being updated")
                widgetLocations = widgetLocations.mapValues { entry -> entry.value ?: cachedLocation }
                updateWidgets(widgetLocations, null)
                recordAutoLocateSuccess(autoLocateWidgetIds)
            } else {
                // Block for location required.
                d(TAG, "Block for location required")
                widgetLocations.entries.forEach { entry ->
                    if (entry.value == null) {
                        showLocating(entry.key)
                    }
                }
                Thread {
                    try {
                        val locater = WidgetLocater()
                        val locationResult = locater.getLocation(operation)
                        widgetLocations = widgetLocations.mapValues { entry -> entry.value ?: locationResult.locationDetails }
                        updateWidgets(widgetLocations, locationResult.error)
                        recordAutoLocateSuccess(autoLocateWidgetIds)
                    } finally {
                        jobFinished(params, false)
                    }
                }.start()
                return true
            }
        }
        return false
    }

    /**
     * Thanks to BlockingLocater it's not currently possible to stop the job. This is unlikely to be
     * called because execution is not conditional on wifi, charging, idle or any other state.
     */
    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    /**
     * Update each widget in the map, showing data for the location if one has been resolved, and an
     * error otherwise.
     */
    private fun updateWidgets(locations: Map<Int, LocationDetails?>, locationError: LocaterStatus?) {
        locations.entries.forEach { entry ->
            val id = entry.key
            val location = entry.value
            location?.let {
                showData(id, location)
            } ?: run {
                showLocationError(id, locationError ?: BLOCKED)
            }
        }
    }

    /**
     * Displays a locating message in an auto-located widget.
     */
    private fun showLocating(widgetId: Int) {
        try {
            getWidgetInstance(this, widgetId)?.showLocating(this, widgetId)
        } catch (e: Exception) {
            e(TAG, "Failed to show locating message for widget $widgetId", e)
        }
    }

    /**
     * Displays an error in an auto-located widget.
     */
    private fun showLocationError(widgetId: Int, error: LocaterStatus) {
        val message = when (error) {
            DENIED -> if (Prefs.widgetLocationReceived(this, widgetId)) "LOCATION DENIED" else "TAP TO SET UP"
            DISABLED -> "LOCATION DISABLED"
            UNAVAILABLE -> "LOCATION UNAVAILABLE"
            TIMEOUT -> "LOCATION TIMEOUT"
            else -> "LOCATION ERROR"
        }
        try {
            getWidgetInstance(this, widgetId)?.showError(this, widgetId, message)
        } catch (e: Exception) {
            e(TAG, "Failed to show error message for widget $widgetId", e)
        }
    }

    /**
     * Calculates and displays data in a widget using the given location.
     */
    private fun showData(widgetId: Int, location: LocationDetails) {
        d(TAG, "showData: widgetId=$widgetId")

        location.timeZone = location.timeZone ?: TimeZoneResolver.getTimeZone(null)
        val localCalendar = Calendar.getInstance()
        val calendar = Calendar.getInstance(location.timeZone!!.zone)
        calendar[localCalendar[Calendar.YEAR], localCalendar[Calendar.MONTH], localCalendar[Calendar.DAY_OF_MONTH], 0, 0] = 0
        calendar[Calendar.MILLISECOND] = 0

        try {
            getWidgetInstance(this, widgetId)?.showData(this, widgetId, location, calendar)
        } catch (e: Exception) {
            e(TAG, "Failed to show data for widget $widgetId", e)
        }
    }

    /**
     * Returns the IDs of all widgets.
     */
    private fun getAllWidgetIds(): List<Int> {
        val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        val ids: MutableList<Int> = ArrayList()
        ids.addAll(appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, SunWidget::class.java)).asList())
        ids.addAll(appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, MoonWidget::class.java)).asList())
        ids.addAll(appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, MoonPhaseWidget::class.java)).asList())
        ids.addAll(appWidgetManager.getAppWidgetIds(ComponentName(applicationContext, SunMoonWidget::class.java)).asList())
        return ids
    }

    /**
     * Returns map of widget ID to location - either fixed or cached. Returns all widget IDs if the
     * passed ID is 0. Will not return cached auto location for forced refresh.
     */
    private fun getWidgetLocations(widgetId: Int): Map<Int, LocationDetails?> {
        DatabaseHelper(applicationContext).use { db ->
            val ids = if (widgetId == INVALID_APPWIDGET_ID) getAllWidgetIds() else listOf(widgetId)
            return ids.associateWith { id -> db.getWidgetLocation(id) }
        }
    }

    /**
     * Previously stored location is re-used if less than 6 hours old, unless a refresh is being
     * forced. This avoids fetching location on screen rotate, date/time change, and widget resizing.
     */
    private fun getCachedAutoLocation(operation: String): LocationDetails? {
        val location: LocationDetails? = Prefs.getWidgetLocation(applicationContext)
        val lastLocationTimestamp: Long = Prefs.getWidgetLocationTimestamp(applicationContext)
        val lastLocationAge = System.currentTimeMillis() - lastLocationTimestamp
        val lastLocationFreshnessLimit = 6 * 60 * 60 * 1000.toLong()

        val lastLocationFreshnessLimitStr = "6 hours"
        if ((location != null && operation == OP_RENDER) ||
                (location != null && operation == OP_REFRESH && lastLocationAge < lastLocationFreshnessLimit)) {
            d(TAG, "Returning last location under $lastLocationFreshnessLimitStr old: $location")
            return location
        }
        return null
    }

    /**
     * Record location received for auto located widgets. This allows the widgets to show "Tap to
     * set up" until permission is first granted, and "Location denied" if permission is revoked
     * later.
     */
    private fun recordAutoLocateSuccess(widgetIds: List<Int>) {
        widgetIds.forEach { id -> Prefs.setWidgetLocationReceived(this, id, true) }
    }

    private inner class WidgetLocaterResult(val locationDetails: LocationDetails?, val error: LocaterStatus?)

    private inner class WidgetLocater : LocaterListener {

        private val lock = Object()

        private var location: LocationDetails? = null
        private var error: LocaterStatus? = null

        fun getLocation(operation: String): WidgetLocaterResult {
            synchronized(this) {
                this.location = null
                this.error = null
            }
            var e: LocaterStatus? = null

            // Never allow a screen rotation or date change to start GPS or network
            // location, because if it's failing it will eat the battery.
            if (operation == OP_RENDER) {
                return WidgetLocaterResult(null, BLOCKED)
            }

            // No last location available or forcing refresh, so start looking for a
            // fresh location. Last known location will be used if this fails, so disable
            // it initially.
            d(TAG, "Starting locater, operation=$operation")
            val locater = Locater(this, applicationContext)
            synchronized(lock) {
                val locaterResult = locater.start(ACCURACY_COARSE)
                d(TAG, "Fresh location result: $locaterResult")
                if (locaterResult == STARTED) {
                    d(TAG, "Blocking for fresh location")
                    // Wait 2 seconds at a time and give up if nothing has happened
                    // after 70 seconds (10 seconds more than locater timeout).
                    val start = System.currentTimeMillis()
                    waitLoop@ while (location == null && error == null && System.currentTimeMillis() - start < 70000) {
                        try {
                            lock.wait(2000)
                            d(TAG, "Lock exited: elapsed=" + (System.currentTimeMillis() - start))
                        } catch (ie: InterruptedException) {
                            break@waitLoop
                        }
                    }
                } else if (locaterResult == DENIED) {
                    // Fail now to inform user the widget will not work properly.
                    return WidgetLocaterResult(null, locaterResult)
                } else {
                    e = locaterResult
                }
            }

            // Return fresh location if successful. Store it for later use.
            location?.let {
                d(TAG, "Returning fresh location: $it")
                Prefs.saveWidgetLocation(applicationContext, it)
                return WidgetLocaterResult(it, null)
            }

            // If this is a tap refresh, user will be expecting fresh location. Fail now.
            if (operation == OP_TAP_REFRESH) {
                return WidgetLocaterResult(null, error ?: e ?: UNAVAILABLE)
            }

            // Couldn't get fresh location so use last known.
            synchronized(lock) {
                val locaterResult = locater.startLastKnown()
                d(TAG, "Last known location result: $locaterResult")
                if (locaterResult == STARTED) {
                    d(TAG, "Blocking for last known location")
                    // Wait 2 seconds at a time and give up if nothing has happened
                    // after 70 seconds (10 seconds more than locater timeout).
                    val start = System.currentTimeMillis()
                    waitLoop@ while (location == null && error == null && System.currentTimeMillis() - start < 70000) {
                        try {
                            lock.wait(2000)
                            d(TAG, "Lock exited: elapsed=" + (System.currentTimeMillis() - start))
                        } catch (ie: InterruptedException) {
                            break@waitLoop
                        }
                    }
                } else if (locaterResult == DENIED) {
                    // Fail now to inform user the widget will not work properly.
                    return WidgetLocaterResult(null, locaterResult)
                } else {
                    e = locaterResult
                }
            }

            // Return last known location if successful. Store it for later use.
            location?.let {
                d(TAG, "Returning last known location: $it")
                Prefs.saveWidgetLocation(applicationContext, it)
                return WidgetLocaterResult(it, null)
            }

            // Finally fall back to last stored location, regardless of age.
            Prefs.getWidgetLocation(applicationContext)?.let {
                d(TAG, "Returning previous location ignoring age: $it")
                return WidgetLocaterResult(it, null)
            }

            // If no error result has been created, make a default one.
            d(TAG, "No location available for widget")
            return WidgetLocaterResult(null, e ?: UNAVAILABLE)
        }

        override fun locationError(status: LocaterStatus) {
            d(TAG, "Location error: $status")
            synchronized(this) { error = status }
            synchronized(lock) { lock.notifyAll() }
        }

        override fun locationReceived(locationDetails: LocationDetails) {
            d(TAG, "Location received: $locationDetails")
            synchronized(this) {
                error = null
                location = locationDetails
            }
            synchronized(lock) { lock.notifyAll() }
        }

        override fun getSystemService(id: String): Any? {
            return this@WidgetUpdateService.getSystemService(id)
        }

        override fun getMainLooper(): Looper {
            return mainLooper
        }

    }

    companion object {
        private val TAG = WidgetUpdateService::class.java.simpleName
    }
}