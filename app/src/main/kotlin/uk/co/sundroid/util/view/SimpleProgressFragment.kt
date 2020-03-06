package uk.co.sundroid.util.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SimpleProgressFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity).apply {
            setMessage(arguments?.getString("message"))
            setNegativeButton("Cancel") { dialog, _ -> run {
                LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent(CANCELLED))
                dialog.cancel()
            }}
            setCancelable(false)
        }.create()
    }

    companion object {

        const val CANCELLED = "PROGRESS_CANCELLED"
        private const val TAG = "PROGRESS"

        fun newInstance(message: String): SimpleProgressFragment {
            val frag = SimpleProgressFragment()
            val args = Bundle()
            args.putString("message", message)
            frag.arguments = args
            return frag
        }

        fun show(fragmentManager: FragmentManager, message: String) {
            newInstance(message).show(fragmentManager, TAG)
        }

        fun close(fragmentManager: FragmentManager) {
            fragmentManager.findFragmentByTag(TAG)?.let {
                fragmentManager.beginTransaction().remove(it).commit()
            }
        }

    }
}