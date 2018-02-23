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

import java.util.Calendar.*

class MonthPickerFragment : DialogFragment(), DialogInterface.OnClickListener {

    private var year: Int = 0
    private var month: Int = 0
    private var monthPicker: NumberPicker? = null
    private var yearPicker: NumberPicker? = null

    @FunctionalInterface
    interface OnMonthSelectedListener {
        fun onMonthSet(year: Int, month: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val today = getInstance()
        this.year = arguments.getInt("y", today.get(YEAR))
        this.month = arguments.getInt("m", today.get(MONTH))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true // TODO may not be required
        val builder = AlertDialog.Builder(activity)

        val content = View.inflate(activity, layout.dialog_monthpicker, null)
        monthPicker = content.findViewById(R.id.monthPicker)
        monthPicker!!.minValue = 0
        monthPicker!!.maxValue = 11
        monthPicker!!.wrapSelectorWheel = true
        monthPicker!!.displayedValues = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        monthPicker!!.value = month
        yearPicker = content.findViewById(R.id.yearPicker)
        yearPicker!!.minValue = 1900
        yearPicker!!.maxValue = 2100
        yearPicker!!.wrapSelectorWheel = false
        yearPicker!!.value = year

        builder.setView(content)
        builder.setTitle("Set month")
        builder.setPositiveButton("Set", this)
        builder.setNeutralButton("This month", this)
        builder.setNegativeButton("Cancel", this)
        return builder.create()
    }

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        val target = targetFragment
        if (target != null && target is OnMonthSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                val today = getInstance()
                (target as OnMonthSelectedListener).onMonthSet(today.get(YEAR), today.get(MONTH))
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                (target as OnMonthSelectedListener).onMonthSet(yearPicker!!.value, monthPicker!!.value)
            }
        }
        dismiss()
    }

    companion object {
        fun newInstance(calendar: Calendar): MonthPickerFragment {
            val fragment = MonthPickerFragment()
            val args = Bundle()
            args.putInt("y", calendar.get(YEAR))
            args.putInt("m", calendar.get(MONTH))
            fragment.arguments = args
            return fragment
        }
    }

}