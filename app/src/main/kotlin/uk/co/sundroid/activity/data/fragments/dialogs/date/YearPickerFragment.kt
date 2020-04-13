package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import androidx.fragment.app.DialogFragment
import uk.co.sundroid.R
import uk.co.sundroid.R.layout
import java.util.*
import java.util.Calendar.*

class YearPickerFragment : DialogFragment() {

    private var calendar: Calendar = getInstance()

    @FunctionalInterface
    interface OnYearSelectedListener {
        fun onYearSet(year: Int)
    }

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

        val view = View.inflate(activity, layout.dialog_yearpicker, null)

        view.findViewById<NumberPicker>(R.id.yearPicker)?.apply {
            minValue = 2000
            maxValue = 2025
            wrapSelectorWheel = false
            value = calendar.get(YEAR)
            setOnValueChangedListener { _, _, year -> calendar.set(YEAR, year) }
        }

        return AlertDialog.Builder(activity).apply {
            setView(view)
            setTitle("Set year")
            setPositiveButton("Set") { _, _ -> set(calendar) }
            setNeutralButton("This year") { _, _ -> set(getInstance()) }
            setNegativeButton("Cancel", null)
        }.create()
    }

    private fun set(calendar: Calendar) {
        val target = targetFragment
        if (target is OnYearSelectedListener) {
            (target as OnYearSelectedListener).onYearSet(calendar.get(YEAR))
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
        fun newInstance(calendar: Calendar): YearPickerFragment {
            return YearPickerFragment().apply {
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