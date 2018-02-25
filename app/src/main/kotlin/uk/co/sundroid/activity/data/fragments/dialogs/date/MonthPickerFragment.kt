package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import uk.co.sundroid.R
import uk.co.sundroid.R.layout
import java.util.*
import java.util.Calendar.*

class MonthPickerFragment : DialogFragment(){

    private var calendar: Calendar = Calendar.getInstance()

    @FunctionalInterface
    interface OnMonthSelectedListener {
        fun onMonthSet(year: Int, month: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restore(arguments)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Companion.save(calendar, outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        restore(savedInstanceState)

        val view = View.inflate(activity, layout.dialog_monthpicker, null)

        view.findViewById<NumberPicker>(R.id.monthPicker).apply {
            minValue = 0
            maxValue = 11
            wrapSelectorWheel = true
            displayedValues = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            value = calendar.get(MONTH)
            setOnValueChangedListener { _, _, month -> calendar.set(MONTH, month) }
        }
        view.findViewById<NumberPicker>(R.id.yearPicker)?.apply {
            minValue = 1900
            maxValue = 2100
            wrapSelectorWheel = false
            value = calendar.get(YEAR)
            setOnValueChangedListener { _, _, year -> calendar.set(YEAR, year) }
        }

        return AlertDialog.Builder(activity).apply {
            setView(view)
            setTitle("Set month")
            setPositiveButton("Set", { _, _ -> set(calendar) })
            setNeutralButton("This month", { _, _ -> set(Calendar.getInstance()) })
            setNegativeButton("Cancel", null)
        }.create()
    }

    private fun set(calendar: Calendar) {
        val target = targetFragment
        if (target is OnMonthSelectedListener) {
            (target as OnMonthSelectedListener).onMonthSet(calendar.get(YEAR), calendar.get(MONTH))
        }
    }

    private fun restore(bundle: Bundle?) {
        if (bundle != null) {
            val ymd = bundle.getIntArray("ymd")
            val tz = bundle.getString("tz")
            if (ymd != null && tz != null) {
                calendar.timeZone = TimeZone.getTimeZone(tz)
                calendar.set(ymd[0], ymd[1], ymd[2])
            }
        }
    }

    companion object {
        fun newInstance(calendar: Calendar): MonthPickerFragment {
            return MonthPickerFragment().apply {
                arguments = save(calendar)
            }
        }

        private fun save(calendar: Calendar, bundle: Bundle? = Bundle()): Bundle? {
            return bundle?.apply {
                putIntArray("ymd", intArrayOf(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH)))
                putString("tz", calendar.timeZone.id)
            }
        }
    }

}