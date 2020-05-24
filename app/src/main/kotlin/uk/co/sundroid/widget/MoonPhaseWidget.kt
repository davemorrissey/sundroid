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
import android.view.View
import android.widget.RemoteViews
import uk.co.sundroid.R
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.OrientationAngles
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.astro.math.SunMoonCalculator
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.widget.options.MoonPhaseWidgetOptionsActivity
import java.util.*

class MoonPhaseWidget : AbstractWidget() {

    override val widgetClass: Class<*>
        get() = MoonPhaseWidget::class.java

    /**
     * Show locating message.
     */
    override fun showLocating(context: Context, id: Int) {
        val v = RemoteViews(context.packageName, R.layout.widget_moon_phase)
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, "LOCATING ...")
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Show an error message.
     */
    override fun showError(context: Context, id: Int, message: String) {
        val v = RemoteViews(context.packageName, R.layout.widget_moon_phase)
        v.setViewVisibility(R.id.message, View.VISIBLE)
        v.setTextViewText(R.id.message, message)
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    /**
     * Updates a widget to show data for a location.
     */
    override fun showData(context: Context, id: Int, location: LocationDetails, calendar: Calendar) {
        val orientationAngles: OrientationAngles = SunMoonCalculator.moonDiskOrientationAngles(Calendar.getInstance(), location.location)
        var shadowOpacity = Prefs.widgetPhaseShadowOpacity(context, id)
        var shadowSize = Prefs.widgetPhaseShadowSize(context, id) / 4f
        if (shadowOpacity * shadowSize == 0f) {
            shadowOpacity = 0
            shadowSize = 0f
        }

        val moonBitmap: Bitmap = MoonPhaseImage.makeImage(context.resources, R.drawable.moon, orientationAngles, shadowSizePercent = shadowSize, shadowOpacity = shadowOpacity)
        val v = RemoteViews(context.packageName, R.layout.widget_moon_phase)
        v.setImageViewBitmap(R.id.moon, moonBitmap)
        v.setViewVisibility(R.id.message, View.GONE)
        staticResetTapAction(v, context, id)
        AppWidgetManager.getInstance(context).updateAppWidget(id, v)
    }

    companion object {

        /**
         * Sets the tap action on the widget. It has a tendency to disappear and needs regular updating.
         */
        fun staticResetTapAction(remoteViews: RemoteViews, context: Context?, widgetId: Int) {
            val opts = Intent(context, MoonPhaseWidgetOptionsActivity::class.java)
            opts.addFlags(FLAG_ACTIVITY_NEW_TASK)
            opts.addFlags(FLAG_ACTIVITY_NO_HISTORY)
            opts.putExtra(EXTRA_APPWIDGET_ID, widgetId)
            opts.data = getWidgetUri("sundroid_moonphase", widgetId)
            val pending = PendingIntent.getActivity(context, 0, opts, FLAG_UPDATE_CURRENT)
            remoteViews.setOnClickPendingIntent(R.id.root, pending)
        }

    }
}