package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.content.Intent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager

abstract class AbstractDayDetailFragment : AbstractDataFragment() {

    protected abstract val layout: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager
                .getInstance(requireContext())
                .registerReceiver(updateReceiver, IntentFilter("update"))
    }

    override fun onDestroy() {
        LocalBroadcastManager
                .getInstance(requireContext())
                .unregisterReceiver(updateReceiver)
        super.onDestroy()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    private val updateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            update()
        }
    }

    override fun onResume() {
        super.onResume()
        update()
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            updateData(view)
        }
    }

    protected abstract fun updateData(view: View)

}
