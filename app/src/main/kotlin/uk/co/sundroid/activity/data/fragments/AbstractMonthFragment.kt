package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.inc_monthbar.*
import uk.co.sundroid.activity.Page
import uk.co.sundroid.activity.data.fragments.dialogs.date.MonthPickerFragment
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

abstract class AbstractMonthFragment<T> : AbstractDataFragment() {

    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.US)

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initGestures()
        updateMonth()
        updateData(view)
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateMonth()
            updateData(view)
        }
    }

    private fun initGestures() {
        val monthListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = calendarDiff(YEAR, -1)
            override fun onButtonDragDown() = calendarDiff(YEAR, 1)
            override fun onButtonDragLeft() = calendarDiff(MONTH, -1)
            override fun onButtonDragRight() = calendarDiff(MONTH, 1)
        }
        val monthDetector = GestureDetector(requireContext(), ButtonDragGestureDetector(monthListener, requireContext()))

        monthPrev.setOnClickListener { calendarDiff(MONTH, -1) }
        monthNext.setOnClickListener { calendarDiff(MONTH, 1) }
        zoneButton?.setOnClickListener { setPage(Page.TIME_ZONE) }
        monthButton.setOnClickListener { showMonthPicker() }
        monthButton.setOnTouchListener { _, event -> monthDetector.onTouchEvent(event) }
    }

    private fun updateMonth() {
        updateTimeZone()
        val calendar = getDateCalendar()
        monthFormat.timeZone = calendar.timeZone
        month.text = monthFormat.format(Date(calendar.timeInMillis))
    }

    private fun showMonthPicker() {
        val monthPickerFragment = MonthPickerFragment.newInstance(getDateCalendar())
        monthPickerFragment.setTargetFragment(this, 0)
        monthPickerFragment.show(requireFragmentManager(), "monthPicker")
    }

    protected abstract fun updateData(view: View)

}
