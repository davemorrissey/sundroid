package uk.co.sundroid.activity.data.fragments

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TimePicker
import kotlinx.android.synthetic.main.inc_timebar.*
import kotlinx.android.synthetic.main.inc_timebar.dateButton
import kotlinx.android.synthetic.main.inc_timebar.dateDMY
import kotlinx.android.synthetic.main.inc_timebar.dateWeekday
import kotlinx.android.synthetic.main.inc_timebar.zoneButton
import uk.co.sundroid.activity.Page
import uk.co.sundroid.util.time.formatTimeStr
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.MINUTE
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.YEAR

abstract class AbstractTimeFragment : AbstractDataFragment(), OnSeekBarChangeListener {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.US)

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGestures()
        updateDate()
        updateTime()
        initialise()
        updateData(view, false)
    }

    override fun update(dateChanged: Boolean, timeChanged: Boolean) {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateDate()
            updateTime()
            updateData(view, !dateChanged)
        }
    }

    private fun onDateSet(year: Int, month: Int, date: Int) {
        getDateCalendar().set(year, month, date)
        getTimeCalendar().set(year, month, date)
        update(dateChanged = true, timeChanged = false)
    }

    private fun onTimeSet(hour: Int, minute: Int) {
        getTimeCalendar().set(HOUR_OF_DAY, hour)
        getTimeCalendar().set(MINUTE, minute)
        update(dateChanged = false, timeChanged = true)
    }

    private fun initGestures() {
        val dateListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = calendarDiff(MONTH, -1)
            override fun onButtonDragDown() = calendarDiff(MONTH, 1)
            override fun onButtonDragLeft() = calendarDiff(DAY_OF_MONTH, -1)
            override fun onButtonDragRight() = calendarDiff(DAY_OF_MONTH, 1)
        }
        val dateDetector = GestureDetector(requireContext(), ButtonDragGestureDetector(dateListener, requireContext()))
        zoneButton.setOnClickListener { setPage(Page.TIME_ZONE) }
        dateButton.setOnClickListener { showDatePicker() }
        dateButton.setOnTouchListener { _, e -> dateDetector.onTouchEvent(e) }

        val timeListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = calendarDiff(HOUR_OF_DAY, -1)
            override fun onButtonDragDown() = calendarDiff(HOUR_OF_DAY, 1)
            override fun onButtonDragLeft() = calendarDiff(MINUTE, -1)
            override fun onButtonDragRight() = calendarDiff(MINUTE, 1)
        }
        val timeDetector = GestureDetector(requireContext(), ButtonDragGestureDetector(timeListener, requireContext()))
        timeButton.setOnClickListener { showTimePicker() }
        timeButton.setOnTouchListener { _, e -> timeDetector.onTouchEvent(e) }
        timeSeeker.setOnSeekBarChangeListener(this)
    }

    private fun updateDate() {
        updateTimeZone()
        val calendar = getDateCalendar()
        dateFormat.timeZone = calendar.timeZone
        weekdayFormat.timeZone = calendar.timeZone
        val date = dateFormat.format(Date(calendar.timeInMillis))
        val weekday = weekdayFormat.format(Date(calendar.timeInMillis))
        dateDMY.text = date
        dateWeekday.text = weekday
    }

    private fun updateTime() {
        val calendar = getTimeCalendar()
        val time = formatTimeStr(requireContext(), calendar, allowSeconds = false, allowRounding = false)
        timeHM.text = time
        timeSeeker.progress = calendar.get(HOUR_OF_DAY) * 60 + calendar.get(MINUTE)
    }

    private fun showDatePicker() {
        val calendar = getDateCalendar()
        val today = Calendar.getInstance(calendar.timeZone)
        val listener = { _: DatePicker, y: Int, m: Int, d: Int -> onDateSet(y, m, d) }
        val dialog = DatePickerDialog(requireContext(), listener, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Today") { _, _ -> onDateSet(today.get(YEAR), today.get(MONTH), today.get(DAY_OF_MONTH))}
        dialog.show()
    }

    private fun showTimePicker() {
        val calendar = getTimeCalendar()
        val now = Calendar.getInstance(calendar.timeZone)
        val listener = { _: TimePicker, h: Int, m: Int -> onTimeSet(h, m) }
        val dialog = TimePickerDialog(activity, listener, calendar.get(HOUR_OF_DAY), calendar.get(MINUTE), prefs.clockType24())
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Now") { _, _ -> onTimeSet(now.get(HOUR_OF_DAY), now.get(MINUTE))}
        dialog.show()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            val hour = progress / 60
            val minute = progress - hour * 60
            onTimeSet(hour, minute)
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) { }
    override fun onStopTrackingTouch(seekBar: SeekBar) { }

    protected abstract fun updateData(view: View, timeOnly: Boolean)

}
