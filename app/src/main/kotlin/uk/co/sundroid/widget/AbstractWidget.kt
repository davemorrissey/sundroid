package uk.co.sundroid.widget

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import uk.co.sundroid.util.arrayString
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.widget.service.WidgetUpdateService
import uk.co.sundroid.widget.service.EXTRA_OP
import uk.co.sundroid.widget.service.OP_REFRESH
import uk.co.sundroid.widget.service.OP_RENDER

abstract class AbstractWidget : AppWidgetProvider() {

    protected abstract val widgetClass: Class<*>

    /**
     * Scheduled refresh: start unforced refresh (uses cached location if fresh) and reset tap action.
     */
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        d(TAG, "onUpdate: " + arrayString(ids))
        ids.forEach { id -> startUpdateService(context, OP_REFRESH, id) }
    }

    /**
     * Size has changed. Perform an ordinary render.
     */
    override fun onAppWidgetOptionsChanged(context: Context?, manager: AppWidgetManager?, id: Int, options: Bundle?) {
        d(TAG, "onAppWidgetOptionsChanged: $id")
        context?.let {
            startUpdateService(it, OP_RENDER, id)
        }
    }

    /**
     * When a widget is deleted, delete the location and preferences associated with it. The single
     * alarm will be left scheduled if there are no widgets left, but cleared at the next reboot.
     */
    override fun onDeleted(context: Context, ids: IntArray) {
        d(TAG, "onDeleted: " + arrayString(ids))
        DatabaseHelper(context).use { db ->
            ids.forEach { id ->
                db.deleteWidgetLocation(id)
                Prefs.removeWidgetPrefs(context, id)
            }
        }
        super.onDeleted(context, ids)
    }

    private fun startUpdateService(context: Context, op: String, widgetId: Int) {
        val extras = PersistableBundle()
        extras.putString(EXTRA_OP, op)
        extras.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        extras.putString(EXTRA_WIDGET_CLASS_NAME, widgetClass.simpleName)
        val jobInfo = JobInfo.Builder(op.hashCode() + widgetId, ComponentName(context, WidgetUpdateService::class.java))
                .setExtras(extras)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .build()
        val scheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        scheduler.schedule(jobInfo)
    }

    companion object {
        private val TAG = AbstractWidget::class.java.simpleName
    }
}