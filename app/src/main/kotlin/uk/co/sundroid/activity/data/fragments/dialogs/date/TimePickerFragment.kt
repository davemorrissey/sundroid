package uk.co.sundroid.activity.data.fragments.dialogs.date

import android.app.AlertDialog
import android.app.Dialog
import android.app.DialogFragment
import android.os.Bundle
import android.view.View
import android.widget.TimePicker
import uk.co.sundroid.R
import uk.co.sundroid.R.layout
import uk.co.sundroid.util.prefs.Prefs
import java.util.*
import java.util.Calendar.*

class TimePickerFragment : DialogFragment() {

    private var calendar: Calendar = Calendar.getInstance()

    @FunctionalInterface
    interface OnTimeSelectedListener {
        fun onTimeSet(hour: Int, minute: Int)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        restore(arguments)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        Companion.save(calendar, outState)
    }

    @Suppress("DEPRECATION") // currentHour/Minute deprecated in 23
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        restore(savedInstanceState)

        val view = View.inflate(activity, layout.dialog_timepicker, null)

        view.findViewById<TimePicker>(R.id.timePicker)?.apply {
            currentHour = calendar.get(HOUR_OF_DAY)
            currentMinute = calendar.get(MINUTE)
            setIs24HourView(Prefs.clockType24(activity.applicationContext))
            setOnTimeChangedListener { _, hour, minute -> run {
                calendar.set(HOUR_OF_DAY, hour)
                calendar.set(MINUTE, minute)
            }}
        }
        
        return AlertDialog.Builder(activity).apply {
            setView(view)
            setTitle("Set time")
            setPositiveButton("Set", { _, _ -> set(calendar) })
            setNeutralButton("Now", { _, _ -> set(Calendar.getInstance()) })
            setNegativeButton("Cancel", null)
        }.create()
    }

    private fun set(calendar: Calendar) {
        val target = targetFragment
        if (target is OnTimeSelectedListener) {
            (target as OnTimeSelectedListener).onTimeSet(calendar.get(HOUR_OF_DAY), calendar.get(MINUTE))
        }
    }

    private fun restore(bundle: Bundle?) {
        if (bundle != null) {
            val hm = bundle.getIntArray("hm")
            val tz = bundle.getString("tz")
            if (hm != null && tz != null) {
                calendar.timeZone = TimeZone.getTimeZone(tz)
                calendar.set(HOUR_OF_DAY, hm[0])
                calendar.set(MINUTE, hm[1])
            }
        }
    }

    companion object {
        fun newInstance(calendar: Calendar): TimePickerFragment {
            return TimePickerFragment().apply {
                arguments = save(calendar)
            }
        }

        private fun save(calendar: Calendar, bundle: Bundle? = Bundle()): Bundle? {
            return bundle?.apply {
                putIntArray("hm", intArrayOf(calendar.get(HOUR_OF_DAY), calendar.get(MINUTE)))
                putString("tz", calendar.timeZone.id)
            }
        }
    }

}