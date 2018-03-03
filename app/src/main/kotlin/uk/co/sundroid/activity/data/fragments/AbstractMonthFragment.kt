package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.inc_monthbar.*
import uk.co.sundroid.activity.data.fragments.dialogs.date.MonthPickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.async.Async
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.*

abstract class AbstractMonthFragment<T> : AbstractDataFragment() {

    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        update()
    }

    override fun initialise() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateMonth()
        }
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            initGestures()
            updateMonth()
            update(view)
        }
    }

    private fun initGestures() {
        val monthListener = object : ButtonDragGestureDetectorListener {
            override fun onButtonDragUp() = calendarDiff(YEAR, -1)
            override fun onButtonDragDown() = calendarDiff(YEAR, 1)
            override fun onButtonDragLeft() = calendarDiff(MONTH, -1)
            override fun onButtonDragRight() = calendarDiff(MONTH, 1)
        }
        val monthDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(monthListener, applicationContext!!))

        monthPrev.setOnClickListener { calendarDiff(MONTH, -1) }
        monthNext.setOnClickListener { calendarDiff(MONTH, 1) }
        zoneButton.setOnClickListener { startTimeZone() }
        monthButton.setOnClickListener { MonthPickerFragment.show(this) }
        monthButton.setOnTouchListener { _, event -> monthDetector.onTouchEvent(event) }
    }

    private fun updateMonth() {
        updateTimeZone()
        val calendar = getDateCalendar()
        monthFormat.timeZone = calendar.timeZone
        month.text = monthFormat.format(Date(calendar.timeInMillis))
    }

    protected fun asyncCalculate(location: LocationDetails, calendar: Calendar, view: View) {
        Async(
                inBackground = { calculate(location, calendar) },
                onDone = { data -> if (isSafe) post(view, data) }
        )
    }

    protected abstract fun update(view: View)

    protected abstract fun calculate(location: LocationDetails, calendar: Calendar): T

    protected open fun post(view: View, data: T) { }

}
