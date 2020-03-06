package uk.co.sundroid.util.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class SimpleAlertFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity)
                .setTitle(arguments?.getString("title"))
                .setMessage(arguments?.getString("message"))
                .setPositiveButton("OK", null)
                .create()
    }

    companion object {

        private const val TAG = "ALERT"

        fun newInstance(title: String, message: String): SimpleAlertFragment {
            val frag = SimpleAlertFragment()
            val args = Bundle()
            args.putString("title", title)
            args.putString("message", message)
            frag.arguments = args
            return frag
        }

        fun show(fragmentManager: FragmentManager, title: String, message: String) {
            newInstance(title, message).show(fragmentManager, TAG)
        }

        fun show(context: Context, fragmentManager: FragmentManager, title: Int, message: Int) {
            newInstance(context.resources.getString(title), context.resources.getString(message)).show(fragmentManager, TAG)
        }

    }
}