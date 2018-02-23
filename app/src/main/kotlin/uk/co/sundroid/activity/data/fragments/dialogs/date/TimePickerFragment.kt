package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import uk.co.sundroid.R
import uk.co.sundroid.R.layout
import uk.co.sundroid.util.prefs.SharedPrefsHelper

import java.util.Calendar

import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE
import java.util.Calendar.getInstance

class TimePickerFragment : DialogFragment(), DialogInterface.OnClickListener {

    private var hour: Int = 0
    private var minute: Int = 0
    private var timePicker: TimePicker? = null

    @FunctionalInterface
    interface OnTimeSelectedListener {
        fun onTimeSet(hour: Int, minute: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val today = getInstance()
        this.hour = arguments.getInt("h", today.get(HOUR_OF_DAY))
        this.minute = arguments.getInt("m", today.get(MINUTE))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true // TODO may not be required
        val builder = AlertDialog.Builder(activity)

        val view = View.inflate(activity, layout.dialog_timepicker, null)
        timePicker = view.findViewById(R.id.timePicker)
        timePicker!!.setIs24HourView(SharedPrefsHelper.getClockType24(activity.applicationContext))
        timePicker!!.currentHour = hour
        timePicker!!.currentMinute = minute
        builder.setView(view)
        builder.setTitle("Set time")

        builder.setPositiveButton("Set", this)
        builder.setNeutralButton("Now", this)
        builder.setNegativeButton("Cancel", this)
        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        val target = targetFragment
        if (target != null && target is OnTimeSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                val today = Calendar.getInstance()
                (target as OnTimeSelectedListener).onTimeSet(today.get(HOUR_OF_DAY), today.get(MINUTE))
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                (target as OnTimeSelectedListener).onTimeSet(timePicker!!.currentHour, timePicker!!.currentMinute)
            }
        }
        dismiss()
    }

    companion object {

        fun newInstance(calendar: Calendar): TimePickerFragment {
            val fragment = TimePickerFragment()
            val args = Bundle()
            args.putInt("h", calendar.get(HOUR_OF_DAY))
            args.putInt("m", calendar.get(MINUTE))
            fragment.arguments = args
            return fragment
        }
    }

}