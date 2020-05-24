package uk.co.sundroid.widget

import android.app.job.JobInfo
import android.app.job.JobInfo.NETWORK_TYPE_ANY
import android.app.job.JobScheduler
import android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID
import android.content.ComponentName
import android.content.Context
import android.content.Context.JOB_SCHEDULER_SERVICE
import android.net.Uri
import android.os.PersistableBundle
import uk.co.sundroid.widget.service.EXTRA_OP
import uk.co.sundroid.widget.service.WidgetUpdateService

const val EXTRA_WIDGET_CLASS_NAME = "widgetClassName"

fun getWidgetUri(scheme: String, widgetId: Int): Uri {
    return Uri.withAppendedPath(Uri.parse("$scheme://widget/id/"), widgetId.toString())
}

fun sendUpdate(context: Context, op: String, id: Int) {
    val extras = PersistableBundle()
    extras.putString(EXTRA_OP, op)
    extras.putInt(EXTRA_APPWIDGET_ID, id)
    val jobInfo = JobInfo.Builder(op.hashCode() + id, ComponentName(context, WidgetUpdateService::class.java))
            .setExtras(extras)
            .setRequiredNetworkType(NETWORK_TYPE_ANY)
            .build()
    val scheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
    scheduler.schedule(jobInfo)
}