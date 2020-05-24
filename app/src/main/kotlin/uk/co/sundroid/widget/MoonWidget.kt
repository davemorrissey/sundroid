package uk.co.sundroid.widget

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_ACTIVITY_NO_HISTORY
import android.graphics.Bitmap
import android.graphics.Color
import android.view.View
import android.widget.RemoteViews
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.OrientationAngles
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunMoonCalculator
import uk.co.sundroid.util.isEmpty
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.Time
import uk.co.sundroid.util.time.formatTime
import uk.co.sundroid.widget.options.MoonWidgetOptionsActivity
import java.util.*

class MoonWidget : AbstractWidget() {

    override val widgetClass: Class<*>
        get() = MoonWidget::class.java

    /**
     * Show locating message.
     */
    override fun showLocating(context: Context, id: Int) {
        val v = RemoteViews(context.packageName, R.layout.widget_moon)
        v.setViewVisibility(R.id.special, View.GONE)
        v.setViewVisibility(R.id.rise, View.GONE)
        v.setViewVisibility(R.id.set, View.GONE)
        v.setViewVisibility(R.id.location, View.GONE)
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, "LOCATING ...")
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Show an error message.
     */
    override fun showError(context: Context, id: Int, message: String) {
        val v = RemoteViews(context.packageName, R.layout.widget_moon)
        v.setViewVisibility(R.id.special, View.GONE)
        v.setViewVisibility(R.id.rise, View.GONE)
        v.setViewVisibility(R.id.set, View.GONE)
        v.setViewVisibility(R.id.location, View.GONE)
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, message)
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Updates a widget to show data for a location.
     */
    override fun showData(context: Context, id: Int, location: LocationDetails, calendar: Calendar) {
        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay
        val v = RemoteViews(context.packageName, R.layout.widget_moon)
        val boxOpacity = Prefs.widgetBoxShadowOpacity(context, id)
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
                val time: Time = formatTime(context, moonDay.rise!!, allowSeconds = false, allowRounding = true)
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
                val time: Time = formatTime(context, moonDay.set!!, allowSeconds = false, allowRounding = true)
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
        val moonBitmap: Bitmap = MoonPhaseImage.makeImage(context.resources, R.drawable.moon, orientationAngles)
        v.setImageViewBitmap(R.id.icon, moonBitmap)
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    companion object {

        /**
         * Sets the tap action on the widget. It has a tendency to disappear and needs regular updating.
         */
        fun staticResetTapAction(remoteViews: RemoteViews, context: Context?, widgetId: Int) {
            val opts = Intent(context, MoonWidgetOptionsActivity::class.java)
            opts.addFlags(FLAG_ACTIVITY_NEW_TASK)
            opts.addFlags(FLAG_ACTIVITY_NO_HISTORY)
            opts.putExtra(EXTRA_APPWIDGET_ID, widgetId)
            opts.data = getWidgetUri("sundroid_moon", widgetId)
            val pending = PendingIntent.getActivity(context, 0, opts, FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.root, pending)
        }

    }
}