package uk.co.sundroid.activity.data.fragments

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import kotlinx.android.synthetic.main.inc_datebar.*
import uk.co.sundroid.activity.Page
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH
import java.util.Calendar.YEAR

abstract class AbstractDayFragment : AbstractDataFragment() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.US)
    private val weekdayFormat = SimpleDateFormat("EEEE", Locale.US)

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGestures()
        updateDate()
        updateData(view)
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateDate()
            updateData(view)
        }
    }

    private fun onDateSet(year: Int, month: Int, date: Int) {
        getDateCalendar().set(year, month, date)
        getTimeCalendar().set(year, month, date)
        update()
    }

    private fun initGestures() {
        val dateListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = calendarDiff(MONTH, -1)
            override fun onButtonDragDown() = calendarDiff(MONTH, 1)
            override fun onButtonDragLeft() = calendarDiff(DAY_OF_MONTH, -1)
            override fun onButtonDragRight() = calendarDiff(DAY_OF_MONTH, 1)
        }
        val dateDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(dateListener, applicationContext!!))

        datePrev.setOnClickListener { calendarDiff(DAY_OF_MONTH, -1) }
        dateNext.setOnClickListener { calendarDiff(DAY_OF_MONTH, 1) }
        zoneButton?.setOnClickListener { setPage(Page.TIME_ZONE) }
        dateButton.setOnClickListener { showDatePicker() }
        dateButton.setOnTouchListener { _, e -> dateDetector.onTouchEvent(e) }
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

    private fun showDatePicker() {
        val calendar = getDateCalendar()
        val today = Calendar.getInstance(calendar.timeZone)
        val listener = { _: DatePicker, y: Int, m: Int, d: Int -> onDateSet(y, m, d) }
        val dialog = DatePickerDialog(requireContext(), listener, calendar.get(YEAR), calendar.get(MONTH), calendar.get(DAY_OF_MONTH))
        dialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Today") { _, _ -> onDateSet(today.get(YEAR), today.get(MONTH), today.get(DAY_OF_MONTH))}
        dialog.show()
    }

    protected abstract fun updateData(view: View)

}
