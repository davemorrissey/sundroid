package uk.co.sundroid.util.view

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

class SimpleAlertFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(arguments.getString("title"))
                .setMessage(arguments.getString("message"))
                .setPositiveButton("OK", null)
                .create()
    }

    companion object {
        fun newInstance(title: String, message: String): SimpleAlertFragment {
            val frag = SimpleAlertFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("message", message)
            frag.arguments = args
            return frag
        }
        fun show(activity: Activity, title: String, message: String) {
            newInstance(title, message).show(activity.fragmentManager, "ALERT")
        }
        fun show(activity: Activity, title: Int, message: Int) {
            newInstance(activity.resources.getString(title), activity.resources.getString(message)).show(activity.fragmentManager, "ALERT")
        }
    }
}