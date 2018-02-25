package uk.co.sundroid.activity.data.fragments.dialogs.settings

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import uk.co.sundroid.activity.data.fragments.dialogs.OnViewPrefsChangedListener
import uk.co.sundroid.util.prefs.SharedPrefsHelper

class DayEventsPickerFragment : DialogFragment() {

    private var currentEvents: BooleanArray? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.currentEvents = arguments.getBooleanArray("currentEvents")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true
        val builder = AlertDialog.Builder(activity)
        builder.setMultiChoiceItems(arrayOf<CharSequence>("Sun", "Moon", "Planets"), currentEvents) { _, id, value ->
            currentEvents?.set(id, value)
        }
        builder.setTitle("Select events")
        builder.setPositiveButton("OK") { _, _ ->
            val context = activity.applicationContext
            SharedPrefsHelper.setShowElement(context, "evtByTimeSun", currentEvents!![0])
            SharedPrefsHelper.setShowElement(context, "evtByTimeMoon", currentEvents!![1])
            SharedPrefsHelper.setShowElement(context, "evtByTimePlanets", currentEvents!![2])

            val parent = targetFragment
            if (parent != null && parent is OnViewPrefsChangedListener) {
                (parent as OnViewPrefsChangedListener).onViewPrefsUpdated()
            }
            dismiss()
        }
        builder.setNegativeButton("Cancel") { d, i -> dismiss() }
        return builder.create()
    }

    companion object {

        fun newInstance(currentEvents: BooleanArray): DayEventsPickerFragment {
            val fragment = DayEventsPickerFragment()
            val args = Bundle()
            args.putBooleanArray("currentEvents", currentEvents)
            fragment.arguments = args
            return fragment
        }
    }

}