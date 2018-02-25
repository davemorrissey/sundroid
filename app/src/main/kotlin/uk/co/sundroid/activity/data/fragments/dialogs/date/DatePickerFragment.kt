package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import uk.co.sundroid.R
import uk.co.sundroid.R.layout
import java.util.*
import java.util.Calendar.*

class DatePickerFragment : DialogFragment(), OnDateChangedListener, OnSeekBarChangeListener {

    private var calendar: Calendar = Calendar.getInstance()
    private var datePicker: DatePicker? = null
    private var dateSeeker: SeekBar? = null

    @FunctionalInterface
    interface OnDateSelectedListener {
        fun onDateSet(year: Int, month: Int, date: Int)
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

        val view = View.inflate(activity, layout.dialog_datepicker, null)

        datePicker = view.findViewById(R.id.datePicker)
        datePicker?.apply {
            init(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH), this@DatePickerFragment)
        }
        dateSeeker = view.findViewById(R.id.dateSeeker)
        dateSeeker?.apply {
            setOnSeekBarChangeListener(this@DatePickerFragment)
            max = 365
            progress = calendar.get(DAY_OF_YEAR)
        }

        return AlertDialog.Builder(activity).apply {
            setView(view)
            setTitle("Set date")
            setPositiveButton("Set", { _, _ -> set(calendar) })
            setNeutralButton("Today", { _, _ -> set(Calendar.getInstance()) })
            setNegativeButton("Cancel", null)
        }.create()
    }

    override fun onDateChanged(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
        dateSeeker?.progress = calendar.get(DAY_OF_YEAR)
    }

    override fun onProgressChanged(dateSeeker: SeekBar, dayOfYear: Int, fromUser: Boolean) {
        if (fromUser) {
            calendar.set(Calendar.DAY_OF_YEAR, Math.max(1, dayOfYear))
            datePicker?.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    private fun set(calendar: Calendar) {
        val target = targetFragment
        if (target is OnDateSelectedListener) {
            (target as OnDateSelectedListener).onDateSet(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
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
        fun newInstance(calendar: Calendar): DatePickerFragment {
            return DatePickerFragment().apply {
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