package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.inc_datebar.*
import uk.co.sundroid.activity.data.fragments.dialogs.date.DatePickerFragment
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.DAY_OF_MONTH
import java.util.Calendar.MONTH

abstract class AbstractDayFragment : AbstractDataFragment(), DatePickerFragment.OnDateSelectedListener {

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

    override fun onDateSet(year: Int, month: Int, date: Int) {
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
        zoneButton.setOnClickListener { startTimeZone() }
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
        val datePickerFragment = DatePickerFragment.newInstance(getDateCalendar())
        datePickerFragment.setTargetFragment(this, 0)
        datePickerFragment.show(fragmentManager, "datePicker")
    }

    protected abstract fun updateData(view: View)

}
