package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import uk.co.sundroid.activity.data.fragments.AbstractMonthFragment
import uk.co.sundroid.databinding.DialogMonthpickerBinding
import uk.co.sundroid.util.time.shortMonths
import java.util.*
import java.util.Calendar.*

class MonthPickerFragment : DialogFragment() {

    private var calendar: Calendar = getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restore(arguments)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        save(calendar, outState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        restore(savedInstanceState)

        val view = DialogMonthpickerBinding.inflate(layoutInflater)

        view.monthPicker.apply {
            minValue = 0
            maxValue = 11
            wrapSelectorWheel = true
            displayedValues = shortMonths
            value = calendar.get(MONTH)
            setOnValueChangedListener { _, _, month -> calendar.set(MONTH, month) }
        }
        view.yearPicker.apply {
            minValue = 1900
            maxValue = 2100
            wrapSelectorWheel = false
            value = calendar.get(YEAR)
            setOnValueChangedListener { _, _, year -> calendar.set(YEAR, year) }
        }

        return AlertDialog.Builder(activity).apply {
            setView(view.root)
            setTitle("Set month")
            setPositiveButton("Set") { _, _ -> set(calendar) }
            setNeutralButton("This month") { _, _ -> set(getInstance()) }
            setNegativeButton("Cancel", null)
        }.create()
    }

    private fun set(calendar: Calendar) {
        val target = targetFragment
        if (target is AbstractMonthFragment<*>) {
            target.calendarSet(calendar.get(YEAR), calendar.get(MONTH), target.getDateCalendar().get(DAY_OF_MONTH))
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