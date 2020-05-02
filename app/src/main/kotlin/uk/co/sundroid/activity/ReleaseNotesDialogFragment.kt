package uk.co.sundroid.activity

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import uk.co.sundroid.databinding.DialogReleasenotesBinding

class ReleaseNotesDialogFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = DialogReleasenotesBinding.inflate(layoutInflater).root

        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Release notes")
        builder.setPositiveButton("OK") { _, _ -> run { dismiss() } }
        builder.setView(view)
        return builder.create()
    }

}