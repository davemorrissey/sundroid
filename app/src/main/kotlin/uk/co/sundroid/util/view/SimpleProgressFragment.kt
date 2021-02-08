package uk.co.sundroid.util.view

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
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

        fun show(callerFragment: Fragment, message: String) {
            safeGetFragmentManager(callerFragment)?.let { fm ->
                newInstance(message).show(fm, TAG)
            }
        }

        fun close(callerFragment: Fragment) {
            safeGetFragmentManager(callerFragment)?.let { fm ->
                fm.findFragmentByTag(TAG)?.let {
                    fm.beginTransaction().remove(it).commit()
                }
            }
        }

        private fun safeGetFragmentManager(callerFragment: Fragment): FragmentManager? {
            if (callerFragment.isAdded) {
                val fragmentManager = callerFragment.parentFragmentManager
                if (!fragmentManager.isStateSaved) {
                    return fragmentManager
                }
            }
            return null
        }

    }
}