package uk.co.sundroid.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import uk.co.sundroid.R

class WelcomeDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_welcome, null, false)

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Welcome to Sundroid!")
        builder.setPositiveButton("OK") { _, _ -> run { dismiss() } }
        builder.setView(view)
        return builder.create()
    }

}