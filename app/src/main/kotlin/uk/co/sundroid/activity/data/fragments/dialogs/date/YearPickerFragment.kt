package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import uk.co.sundroid.R
import uk.co.sundroid.R.layout

import java.util.Calendar

import java.util.Calendar.YEAR
import java.util.Calendar.getInstance

class YearPickerFragment : DialogFragment(), DialogInterface.OnClickListener {

    private var year: Int = 0
    private var yearPicker: NumberPicker? = null

    @FunctionalInterface
    interface OnYearSelectedListener {
        fun onYearSet(year: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val today = getInstance()
        this.year = arguments.getInt("y", today.get(YEAR))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true // TODO May not be required
        val builder = AlertDialog.Builder(activity)

        val content = View.inflate(activity, layout.dialog_yearpicker, null)
        yearPicker = content.findViewById(R.id.yearPicker)
        yearPicker!!.minValue = 2000
        yearPicker!!.maxValue = 2020
        yearPicker!!.wrapSelectorWheel = false
        yearPicker!!.value = year
        builder.setView(content)
        builder.setTitle("Set year")
        builder.setPositiveButton("Set", this)
        builder.setNeutralButton("This year", this)
        builder.setNegativeButton("Cancel", this)
        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        val target = targetFragment
        if (target != null && target is OnYearSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                val today = getInstance()
                (target as OnYearSelectedListener).onYearSet(today.get(YEAR))
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                (target as OnYearSelectedListener).onYearSet(yearPicker!!.value)
            }
        }
        dismiss()
    }

    companion object {

        fun newInstance(calendar: Calendar): YearPickerFragment {
            val fragment = YearPickerFragment()
            val args = Bundle()
            args.putInt("y", calendar.get(YEAR))
            fragment.arguments = args
            return fragment
        }
    }

}