package uk.co.sundroid.widget.service

import android.app.job.JobParameters
import android.app.job.JobService
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
import android.location.Criteria.ACCURACY_COARSE
import android.content.ComponentName
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Looper
import android.view.View
import android.widget.RemoteViews
import uk.co.sundroid.R
import uk.co.sundroid.activity.Locater
import uk.co.sundroid.activity.LocaterListener
import uk.co.sundroid.activity.LocaterStatus
import uk.co.sundroid.activity.LocaterStatus.*
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.*
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.math.SunMoonCalculator
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.isEmpty
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.log.e
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.Time
import uk.co.sundroid.util.time.TimeZoneResolver
import uk.co.sundroid.util.time.formatTime
import uk.co.sundroid.widget.MoonPhaseWidget
import uk.co.sundroid.widget.MoonWidget
import uk.co.sundroid.widget.SunWidget
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

            var widgetLocations = getWidgetLocations(widgetId, operation)
            if (widgetLocations.containsValue(null)) {
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
                    } finally {
                        jobFinished(params, false)
                    }
                }.start()
                return true
            } else {
                // Known location for all widgets allows synchronous update.
                d(TAG, "Locations known for all widgets being updated")
                updateWidgets(widgetLocations, null)
                return false
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
        val manager = AppWidgetManager.getInstance(this)
        try {
            manager.getAppWidgetInfo(widgetId)?.let {
                it.provider.className
                when (it.provider.className) {
                    SunWidget::class.java.name -> {
                        val v = RemoteViews(this.packageName, R.layout.widget_sun)
                        v.setViewVisibility(R.id.special, View.GONE)
                        v.setViewVisibility(R.id.rise, View.GONE)
                        v.setViewVisibility(R.id.set, View.GONE)
                        v.setViewVisibility(R.id.location, View.GONE)
                        v.setViewVisibility(R.id.message, View.VISIBLE)
                        v.setTextViewText(R.id.message, "LOCATING ...")
                        SunWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                    MoonWidget::class.java.name -> {
                        val v = RemoteViews(this.packageName, R.layout.widget_moon)
                        v.setViewVisibility(R.id.special, View.GONE)
                        v.setViewVisibility(R.id.rise, View.GONE)
                        v.setViewVisibility(R.id.set, View.GONE)
                        v.setViewVisibility(R.id.location, View.GONE)
                        v.setViewVisibility(R.id.message, View.VISIBLE)
                        v.setTextViewText(R.id.message, "LOCATING ...")
                        MoonWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                    MoonPhaseWidget::class.java.name -> {
                        val v = RemoteViews(this.packageName, R.layout.widget_moon_phase)
                        v.setViewVisibility(R.id.message, View.VISIBLE)
                        v.setTextViewText(R.id.message, "LOCATING ...")
                        MoonPhaseWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                }
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update widget $widgetId", e)
        }
    }

    /**
     * Displays an error in an auto-located widget.
     */
    private fun showLocationError(widgetId: Int, error: LocaterStatus) {
        val manager = AppWidgetManager.getInstance(this)
        val message = when (error) {
            DENIED -> "LOCATION DENIED"
            DISABLED -> "LOCATION DISABLED"
            UNAVAILABLE -> "LOCATION UNAVAILABLE"
            TIMEOUT -> "LOCATION TIMEOUT"
            else -> "LOCATION ERROR"
        }
        try {
            manager.getAppWidgetInfo(widgetId)?.let {
                when (it.provider.className) {
                    SunWidget::class.java.name -> {
                        val v = RemoteViews(this.packageName, R.layout.widget_sun)
                        v.setViewVisibility(R.id.rise, View.GONE)
                        v.setViewVisibility(R.id.set, View.GONE)
                        v.setViewVisibility(R.id.location, View.GONE)
                        v.setViewVisibility(R.id.message, View.VISIBLE)
                        v.setTextViewText(R.id.message, message)
                        SunWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                    MoonWidget::class.java.name -> {
                        val v = RemoteViews(this.packageName, R.layout.widget_moon)
                        v.setViewVisibility(R.id.rise, View.GONE)
                        v.setViewVisibility(R.id.set, View.GONE)
                        v.setViewVisibility(R.id.location, View.GONE)
                        v.setViewVisibility(R.id.message, View.VISIBLE)
                        v.setTextViewText(R.id.message, message)
                        MoonWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                    MoonPhaseWidget::class.java.name -> {
                        val v = RemoteViews(this.packageName, R.layout.widget_moon_phase)
                        v.setViewVisibility(R.id.message, View.VISIBLE)
                        v.setTextViewText(R.id.message, message)
                        MoonPhaseWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                }
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update widget $widgetId", e)
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

        val manager = AppWidgetManager.getInstance(this)
        try {
            manager.getAppWidgetInfo(widgetId)?.let {
                when (it.provider.className) {
                    SunWidget::class.java.name -> {
                        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar, BodyDayEvent.Event.RISESET)
                        val v = RemoteViews(this.packageName, R.layout.widget_sun)
                        val boxOpacity = Prefs.widgetBoxShadowOpacity(applicationContext, widgetId)
                        v.setInt(R.id.root, "setBackgroundColor", Color.argb(boxOpacity, 0, 0, 0))
                        var lightDarkType: TwilightType? = null
                        if (sunDay.riseSetType === RiseSetType.RISEN) {
                            lightDarkType = TwilightType.LIGHT
                        } else if (sunDay.riseSetType === RiseSetType.SET) {
                            lightDarkType = TwilightType.DARK
                        }
                        val rise = sunDay.rise
                        val set = sunDay.set
                        v.setViewVisibility(R.id.message, View.GONE)
                        v.setViewVisibility(R.id.location, View.VISIBLE)
                        if (isEmpty(location.name)) {
                            v.setTextViewText(R.id.location, "MY LOCATION")
                        } else {
                            v.setTextViewText(R.id.location, location.displayName.toUpperCase(Locale.getDefault()))
                        }
                        if (lightDarkType != null && lightDarkType === TwilightType.LIGHT || lightDarkType === TwilightType.DARK) {
                            v.setViewVisibility(R.id.special, View.VISIBLE)
                            v.setViewVisibility(R.id.rise, View.GONE)
                            v.setViewVisibility(R.id.set, View.GONE)
                            v.setTextViewText(R.id.special, if (lightDarkType === TwilightType.LIGHT) "RISEN" else "SET")
                        } else {
                            v.setViewVisibility(R.id.special, View.GONE)
                            v.setViewVisibility(R.id.rise, View.VISIBLE)
                            v.setViewVisibility(R.id.set, View.VISIBLE)
                            if (rise == null) {
                                v.setTextViewText(R.id.riseTime, "NONE")
                                v.setViewVisibility(R.id.riseZone, View.GONE)
                            } else {
                                val time: Time = formatTime(applicationContext, rise, allowSeconds = false, allowRounding = true)
                                v.setViewVisibility(R.id.riseTime, View.VISIBLE)
                                v.setTextViewText(R.id.riseTime, time.time)
                                if (isNotEmpty(time.marker)) {
                                    v.setViewVisibility(R.id.riseZone, View.VISIBLE)
                                    v.setTextViewText(R.id.riseZone, time.marker.toUpperCase(Locale.getDefault()))
                                } else {
                                    v.setViewVisibility(R.id.riseZone, View.GONE)
                                }
                            }
                            if (set == null) {
                                v.setTextViewText(R.id.setTime, "NONE")
                                v.setViewVisibility(R.id.setZone, View.GONE)
                            } else {
                                val time: Time = formatTime(applicationContext, set, allowSeconds = false, allowRounding = true)
                                v.setViewVisibility(R.id.setTime, View.VISIBLE)
                                v.setTextViewText(R.id.setTime, time.time)
                                if (isNotEmpty(time.marker)) {
                                    v.setViewVisibility(R.id.setZone, View.VISIBLE)
                                    v.setTextViewText(R.id.setZone, time.marker.toUpperCase(Locale.getDefault()))
                                } else {
                                    v.setViewVisibility(R.id.setZone, View.GONE)
                                }
                            }
                        }
                        SunWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                    MoonWidget::class.java.name -> {
                        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay
                        val v = RemoteViews(this.packageName, R.layout.widget_moon)
                        val boxOpacity = Prefs.widgetBoxShadowOpacity(applicationContext, widgetId)
                        v.setInt(R.id.root, "setBackgroundColor", Color.argb(boxOpacity, 0, 0, 0))
                        v.setViewVisibility(R.id.message, View.GONE)
                        v.setViewVisibility(R.id.location, View.VISIBLE)
                        if (isEmpty(location.name)) {
                            v.setTextViewText(R.id.location, "MY LOCATION")
                        } else {
                            v.setTextViewText(R.id.location, location.displayName.toUpperCase(Locale.getDefault()))
                        }
                        if (moonDay.riseSetType === RiseSetType.RISEN || moonDay.riseSetType === RiseSetType.SET) {
                            v.setViewVisibility(R.id.special, View.VISIBLE)
                            v.setViewVisibility(R.id.rise, View.GONE)
                            v.setViewVisibility(R.id.set, View.GONE)
                            v.setTextViewText(R.id.special, if (moonDay.riseSetType === RiseSetType.RISEN) "RISEN" else "SET")
                        } else {
                            v.setViewVisibility(R.id.special, View.GONE)
                            v.setViewVisibility(R.id.rise, View.VISIBLE)
                            v.setViewVisibility(R.id.set, View.VISIBLE)
                            if (moonDay.rise == null) {
                                v.setTextViewText(R.id.riseTime, "None")
                                v.setViewVisibility(R.id.riseZone, View.GONE)
                            } else {
                                val time: Time = formatTime(applicationContext, moonDay.rise!!, allowSeconds = false, allowRounding = true)
                                v.setViewVisibility(R.id.riseTime, View.VISIBLE)
                                v.setTextViewText(R.id.riseTime, time.time)
                                if (isNotEmpty(time.marker)) {
                                    v.setViewVisibility(R.id.riseZone, View.VISIBLE)
                                    v.setTextViewText(R.id.riseZone, time.marker.toUpperCase(Locale.getDefault()))
                                } else {
                                    v.setViewVisibility(R.id.riseZone, View.GONE)
                                }
                            }
                            if (moonDay.set == null) {
                                v.setTextViewText(R.id.setTime, "None")
                                v.setViewVisibility(R.id.setZone, View.GONE)
                            } else {
                                val time: Time = formatTime(applicationContext, moonDay.set!!, allowSeconds = false, allowRounding = true)
                                v.setViewVisibility(R.id.setTime, View.VISIBLE)
                                v.setTextViewText(R.id.setTime, time.time)
                                if (isNotEmpty(time.marker)) {
                                    v.setViewVisibility(R.id.setZone, View.VISIBLE)
                                    v.setTextViewText(R.id.setZone, time.marker.toUpperCase(Locale.getDefault()))
                                } else {
                                    v.setViewVisibility(R.id.setZone, View.GONE)
                                }
                            }
                        }
                        val orientationAngles: OrientationAngles = SunMoonCalculator.moonDiskOrientationAngles(Calendar.getInstance(), location.location)
                        val moonBitmap: Bitmap = MoonPhaseImage.makeImage(resources, R.drawable.moon, orientationAngles)
                        v.setImageViewBitmap(R.id.icon, moonBitmap)
                        MoonWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                    MoonPhaseWidget::class.java.name -> {
                        val orientationAngles: OrientationAngles = SunMoonCalculator.moonDiskOrientationAngles(Calendar.getInstance(), location.location)
                        var shadowOpacity = Prefs.widgetPhaseShadowOpacity(applicationContext, widgetId)
                        var shadowSize = Prefs.widgetPhaseShadowSize(applicationContext, widgetId) / 4f
                        if (shadowOpacity * shadowSize == 0f) {
                            shadowOpacity = 0
                            shadowSize = 0f
                        }

                        val moonBitmap: Bitmap = MoonPhaseImage.makeImage(resources, R.drawable.moon, orientationAngles, shadowSizePercent = shadowSize, shadowOpacity = shadowOpacity)
                        val v = RemoteViews(this.packageName, R.layout.widget_moon_phase)
                        v.setImageViewBitmap(R.id.moon, moonBitmap)
                        v.setViewVisibility(R.id.message, View.GONE)
                        MoonPhaseWidget.staticResetTapAction(v, applicationContext, widgetId)
                        manager.updateAppWidget(widgetId, v)
                    }
                }
            }
        } catch (e: Exception) {
            e(TAG, "Failed to update widget $widgetId", e)
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
        return ids
    }

    /**
     * Returns map of widget ID to location - either fixed or cached. Returns all widget IDs if the
     * passed ID is 0. Will not return cached auto location for forced refresh.
     */
    private fun getWidgetLocations(widgetId: Int, operation: String): Map<Int, LocationDetails?> {
        DatabaseHelper(applicationContext).use { db ->
            val autoLocation = getCachedAutoLocation(operation)
            val ids = if (widgetId == INVALID_APPWIDGET_ID) getAllWidgetIds() else listOf(widgetId)
            return ids.associateWith { id -> db.getWidgetLocation(id) ?: autoLocation }
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