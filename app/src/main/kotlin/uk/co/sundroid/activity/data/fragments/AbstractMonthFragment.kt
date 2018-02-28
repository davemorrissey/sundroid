package uk.co.sundroid.activity.data.fragments

import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.*
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.date.MonthPickerFragment
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.*
import uk.co.sundroid.util.view.ButtonDragGestureDetector
import uk.co.sundroid.util.view.ButtonDragGestureDetector.ButtonDragGestureDetectorListener
import uk.co.sundroid.util.log.*

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class AbstractMonthFragment<T> : AbstractDataFragment(), MonthPickerFragment.OnMonthSelectedListener {

    private val monthFormat = SimpleDateFormat("MMM yyyy", Locale.US)

    private var monthDetector: GestureDetector? = null

    protected abstract val layout: Int

    private val handler = Handler()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        if (container == null) {
            return null
        }
        val view = inflater.inflate(layout, container, false)
        safeUpdate(view)
        return view
    }

    override fun initialise() {
        if (view != null) {
            safeInit(view)
        }
    }

    override fun update() {
        if (view != null) {
            safeUpdate(view)
        }
    }

    override fun onMonthSet(year: Int, month: Int) {
        dateCalendar!!.set(Calendar.YEAR, year)
        dateCalendar!!.set(Calendar.MONTH, month)
        timeCalendar!!.set(Calendar.YEAR, year)
        timeCalendar!!.set(Calendar.MONTH, month)
        update()
    }

    private fun safeInit(view: View?) {
        val location = location
        val calendar = dateCalendar
        try {
            if (location != null && calendar != null && view != null && !isDetached) {
                d(TAG, hashCode().toString() + " running safeUpdate")
                initGestures(view)
                updateMonth(location, calendar, view)
            }
        } catch (e: Exception) {
            e(TAG, hashCode().toString() + " Failed to update data view", e)
        }

    }

    private fun safeUpdate(view: View?) {
        val location = location
        val calendar = dateCalendar
        try {
            if (location != null && calendar != null && view != null && !isDetached) {
                d(TAG, hashCode().toString() + " running safeUpdate")
                initGestures(view)
                updateMonth(location, calendar, view)
                update(location, calendar, view)
            }
        } catch (e: Exception) {
            e(TAG, hashCode().toString() + " Failed to update data view", e)
        }

    }

    private fun initGestures(view: View) {
        if (monthDetector == null) {
            val monthListener = object : ButtonDragGestureDetectorListener {
                override fun onButtonDragUp() {
                    prevYear()
                }

                override fun onButtonDragDown() {
                    nextYear()
                }

                override fun onButtonDragLeft() {
                    prevMonth()
                }

                override fun onButtonDragRight() {
                    nextMonth()
                }
            }
            monthDetector = GestureDetector(applicationContext, ButtonDragGestureDetector(monthListener, applicationContext!!))
        }

        view.findViewById<View>(R.id.monthPrev).setOnClickListener { _ -> prevMonth() }
        view.findViewById<View>(R.id.monthNext).setOnClickListener { _ -> nextMonth() }
        view.findViewById<View>(R.id.zoneButton).setOnClickListener { _ -> startTimeZone() }
        view.findViewById<View>(R.id.monthButton).setOnClickListener { _ -> showMonthPicker() }
        view.findViewById<View>(R.id.monthButton).setOnTouchListener { _, event -> monthDetector != null && monthDetector!!.onTouchEvent(event) }
    }

    private fun updateMonth(location: LocationDetails, calendar: Calendar, view: View) {
        if (SharedPrefsHelper.getShowTimeZone(applicationContext!!)) {
            showInView(view, R.id.zoneButton)
            val zone = location.timeZone!!.zone
            val zoneDST = zone.inDaylightTime(Date(calendar.timeInMillis + 12 * 60 * 60 * 1000))
            val zoneName = zone.getDisplayName(zoneDST, TimeZone.LONG)
            textInView(view, R.id.zoneName, zoneName)

            var zoneCities = location.timeZone!!.getOffset(calendar.timeInMillis + 12 * 60 * 60 * 1000) // Get day's main offset.
            if (isNotEmpty(location.timeZone!!.cities)) {
                zoneCities += " " + location.timeZone!!.cities!!
            }
            textInView(view, R.id.zoneCities, zoneCities)
        } else {
            removeInView(view, R.id.zoneButton)
        }

        monthFormat.timeZone = calendar.timeZone
        val month = monthFormat.format(Date(calendar.timeInMillis))
        showInView(view, R.id.month, month)
    }

    private fun showMonthPicker() {
        val monthPickerFragment = MonthPickerFragment.newInstance(dateCalendar!!)
        monthPickerFragment.setTargetFragment(this, 0)
        monthPickerFragment.show(fragmentManager, "monthPicker")
    }

    private fun nextMonth() {
        dateCalendar!!.add(Calendar.MONTH, 1)
        timeCalendar!!.add(Calendar.MONTH, 1)
        update()
    }

    private fun prevMonth() {
        dateCalendar!!.add(Calendar.MONTH, -1)
        timeCalendar!!.add(Calendar.MONTH, -1)
        update()
    }

    private fun nextYear() {
        dateCalendar!!.add(Calendar.YEAR, 1)
        timeCalendar!!.add(Calendar.YEAR, 1)
        update()
    }

    private fun prevYear() {
        dateCalendar!!.add(Calendar.YEAR, -1)
        timeCalendar!!.add(Calendar.YEAR, -1)
        update()
    }

    @Throws(Exception::class)
    protected abstract fun update(location: LocationDetails, calendar: Calendar, view: View)

    protected fun offThreadUpdate(location: LocationDetails, calendar: Calendar, view: View) {
        data class Params(val location: LocationDetails, val calendar: Calendar)
        class Task : AsyncTask<Params, Void, T>() {
            override fun doInBackground(vararg params: Params): T {
                return calculate(params[0].location, params[0].calendar)
            }
            override fun onPostExecute(data: T) {
                if (isSafe) {
                    post(view, data)
                }
            }
        }
        Task().execute(Params(location, calendar))
    }

    abstract fun calculate(location: LocationDetails, calendar: Calendar): T

    protected open fun post(view: View, data: T) {

    }

    companion object {
        private val TAG = AbstractMonthFragment::class.java.simpleName
    }

}
