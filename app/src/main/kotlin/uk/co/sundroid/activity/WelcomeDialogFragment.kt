package uk.co.sundroid.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import uk.co.sundroid.databinding.DialogWelcomeBinding

class WelcomeDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = DialogWelcomeBinding.inflate(layoutInflater).root

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Welcome to Sundroid!")
        builder.setPositiveButton("OK") { _, _ -> run { dismiss() } }
        builder.setView(view)
        return builder.create()
    }

}