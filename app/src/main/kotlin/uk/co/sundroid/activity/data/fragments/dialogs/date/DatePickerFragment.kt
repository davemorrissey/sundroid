package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import uk.co.sundroid.R
import uk.co.sundroid.R.layout

import java.util.Calendar
import java.util.TimeZone

import java.util.Calendar.*

class DatePickerFragment : DialogFragment(), DialogInterface.OnClickListener, OnDateChangedListener, OnSeekBarChangeListener {

    private var calendar: Calendar? = null
    private var datePicker: DatePicker? = null
    private var dateSeeker: SeekBar? = null

    @FunctionalInterface
    interface OnDateSelectedListener {
        fun onDateSet(year: Int, month: Int, date: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val ymd = arguments.getIntArray("ymd")
        val tz = arguments.getString("tz")
        calendar = Calendar.getInstance()
        if (ymd != null && tz != null) {
            calendar!!.timeZone = TimeZone.getTimeZone(tz)
            calendar!!.set(ymd[0], ymd[1], ymd[2])
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        retainInstance = true // TODO May not be required
        val builder = AlertDialog.Builder(activity)

        val view = View.inflate(activity, layout.dialog_datepicker, null)
        datePicker = view.findViewById(R.id.datePicker)
        datePicker!!.init(calendar!!.get(YEAR), calendar!!.get(MONTH), calendar!!.get(DAY_OF_MONTH), this)
        builder.setView(view)
        builder.setTitle("Set date")

        dateSeeker = view.findViewById(R.id.dateSeeker)
        dateSeeker!!.setOnSeekBarChangeListener(this)
        dateSeeker!!.max = 365
        dateSeeker!!.progress = calendar!!.get(DAY_OF_YEAR)

        builder.setPositiveButton("Set", this)
        builder.setNeutralButton("Today", this)
        builder.setNegativeButton("Cancel", this)
        return builder.create()
    }



    override fun onDateChanged(datePicker: DatePicker, year: Int, month: Int, day: Int) {
        if (dateSeeker != null && calendar != null) {
            val cal = Calendar.getInstance(calendar!!.timeZone)
            cal.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
            dateSeeker!!.progress = cal.get(DAY_OF_YEAR)
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, dayOfYear: Int, fromUser: Boolean) {
        var dayOfYear = dayOfYear
        if (fromUser && datePicker != null && calendar != null) {
            dayOfYear = Math.max(1, dayOfYear)
            val cal = Calendar.getInstance(calendar!!.timeZone)
            cal.set(Calendar.DAY_OF_YEAR, dayOfYear)
            datePicker!!.updateDate(datePicker!!.year, cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {}

    override fun onClick(dialogInterface: DialogInterface, button: Int) {
        val target = targetFragment
        if (target != null && target is OnDateSelectedListener) {
            if (button == DialogInterface.BUTTON_NEUTRAL) {
                val today = Calendar.getInstance()
                (target as OnDateSelectedListener).onDateSet(today.get(YEAR), today.get(MONTH), today.get(DAY_OF_MONTH))
            } else if (button == DialogInterface.BUTTON_POSITIVE) {
                (target as OnDateSelectedListener).onDateSet(datePicker!!.year, datePicker!!.month, datePicker!!.dayOfMonth)
            }
        }
        dismiss()
    }

    companion object {
        fun newInstance(calendar: Calendar): DatePickerFragment {
            val fragment = DatePickerFragment()
            val args = Bundle()
            args.putIntArray("ymd", intArrayOf(calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH)))
            args.putString("tz", calendar.timeZone.id)
            fragment.arguments = args
            return fragment
        }
    }

}