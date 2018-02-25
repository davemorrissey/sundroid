package uk.co.sundroid.util.view

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle

class SimpleAlertFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle): Dialog {
        val title = arguments.getInt("title")

        return AlertDialog.Builder(activity)
                .setTitle(title)
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
    }
}