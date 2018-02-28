package uk.co.sundroid.activity.data.fragments

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.text.Html
import android.view.View
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.settings.YearEventsPickerFragment
import uk.co.sundroid.util.*
import uk.co.sundroid.util.astro.MoonPhaseEvent
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*
import uk.co.sundroid.util.astro.YearData
import uk.co.sundroid.util.astro.YearData.Event
import uk.co.sundroid.util.astro.YearData.EventType

import java.util.ArrayList
import java.util.Calendar

class YearEventsFragment : AbstractYearFragment(), ConfigurableFragment {

    private val handler = Handler()

    override val layout: Int
        get() = R.layout.frag_data_yearevents

    override fun openSettingsDialog() {
        val settingsDialog = YearEventsPickerFragment.newInstance(activity)
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(fragmentManager, "yearEventsSettings")
    }

    @Throws(Exception::class)
    override fun update(location: LocationDetails, calendar: Calendar, view: View) {

        val thread = object : Thread() {
            @Suppress("DEPRECATION")
            override fun run() {
                if (!isSafe) {
                    return
                }

                val todayCalendar = Calendar.getInstance(calendar.timeZone)
                val eventsSet = YearData.getYearEvents(calendar.get(Calendar.YEAR), location.timeZone!!.zone)
                val moonPhases = MoonPhaseCalculator.getYearEvents(calendar.get(Calendar.YEAR), location.timeZone!!.zone)
                moonPhases.mapTo(eventsSet) { Event(EventType.PHASE, it, it.time, null) }

                handler.post {
                    if (isSafe) {
                        val eventsList = ArrayList(eventsSet)
                        val eventsBox = view.findViewById<ViewGroup>(R.id.yearEventsBox)
                        eventsBox.removeAllViews()

                        var first = true
                        for (event in eventsList) {
                            val eventTime = formatTime(applicationContext!!, event.time, false)
                            var title = ""
                            var time = eventTime.time + eventTime.marker.toLowerCase()
                            var subtitle = ""
                            var link: String? = null
                            var image = 0
                            when (event.type) {
                                YearData.EventType.EARTH_APHELION, YearData.EventType.EARTH_PERIHELION -> {
                                    if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearEarthApsis", true)) {
                                        title = event.type.displayName
                                        link = event.link
                                    }
                                }
                                YearData.EventType.PARTIAL_LUNAR, YearData.EventType.TOTAL_LUNAR, YearData.EventType.PENUMBRAL_LUNAR -> {
                                    if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearLunarEclipse", true)) {
                                        title = event.type.displayName
                                        time = "Greatest eclipse: " + time
                                        link = event.link
                                    }
                                }
                                YearData.EventType.PARTIAL_SOLAR, YearData.EventType.TOTAL_SOLAR, YearData.EventType.ANNULAR_SOLAR, YearData.EventType.HYBRID_SOLAR -> {
                                    if (!SharedPrefsHelper.getShowElement(applicationContext!!, "yearSolarEclipse", true)) {
                                        title = event.type.displayName
                                        time = "Greatest eclipse: " + time
                                        subtitle = event.extra as String
                                        link = event.link
                                    }
                                }
                                YearData.EventType.MARCH_EQUINOX, YearData.EventType.SEPTEMBER_EQUINOX -> {
                                    if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearEquinox", true)) {
                                        title = event.type.displayName
                                    }
                                }
                                YearData.EventType.NORTHERN_SOLSTICE -> {
                                    if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearSolstice", true)) {
                                        title = event.type.displayName
                                        if (Math.abs(location.location.latitude.doubleValue) > 23.44) {
                                            val sunDay = SunCalculator.calcDay(location.location, event.time, SunCalculator.Event.RISESET)
                                            val localExtreme = if (location.location.latitude.doubleValue >= 0) "Longest" else "Shortest"
                                            subtitle = localExtreme + " day: " + formatDurationHMS(applicationContext!!, sunDay.uptimeHours, true)
                                        }
                                    }
                                }
                                YearData.EventType.SOUTHERN_SOLSTICE -> {
                                    if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearSolstice", true)) {
                                        title = event.type.displayName
                                        if (Math.abs(location.location.latitude.doubleValue) > 23.44) {
                                            val sunDay = SunCalculator.calcDay(location.location, event.time, SunCalculator.Event.RISESET)
                                            val localExtreme = if (location.location.latitude.doubleValue >= 0) "Shortest" else "Longest"
                                            subtitle = localExtreme + " day: " + formatDurationHMS(applicationContext!!, sunDay.uptimeHours, true)
                                        }
                                    }
                                }
                                YearData.EventType.PHASE -> {
                                    val moonPhase = event.extra as MoonPhaseEvent?
                                    when (moonPhase!!.phase) {
                                        MoonPhase.FULL -> {
                                            if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearFullMoon", true)) {
                                                title = "Full Moon"
                                                image = getPhaseFull()
                                            }
                                        }
                                        MoonPhase.NEW -> {
                                            if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearNewMoon", true)) {
                                                title = "New Moon"
                                                image = getPhaseNew()
                                            }
                                        }
                                        MoonPhase.FIRST_QUARTER -> {
                                            if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearQuarterMoon", true)) {
                                                title = "First Quarter"
                                                image = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                                            }
                                        }
                                        MoonPhase.LAST_QUARTER -> {
                                            if (SharedPrefsHelper.getShowElement(applicationContext!!, "yearQuarterMoon", true)) {
                                                title = "Last Quarter"
                                                image = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                                            }
                                        }
                                        else -> { }
                                    }
                                }
                            }

                            if (!first) {
                                activity.layoutInflater.inflate(R.layout.divider, eventsBox)
                            }
                            val eventRow = View.inflate(activity, R.layout.frag_data_yearevents_event, null)

                            val today = todayCalendar.get(Calendar.YEAR) == event.time.get(Calendar.YEAR) &&
                                    todayCalendar.get(Calendar.MONTH) == event.time.get(Calendar.MONTH) &&
                                    todayCalendar.get(Calendar.DAY_OF_MONTH) == event.time.get(Calendar.DAY_OF_MONTH)
                            if (today) {
                                eventRow.setBackgroundColor(getCalendarHighlightColor())
                            } else {
                                eventRow.setBackgroundColor(getCalendarDefaultColor())
                            }

                            if (image > 0) {
                                imageInView(eventRow, R.id.yearEventImg, image)
                                showInView(eventRow, R.id.yearEventImg)
                            }
                            textInView(eventRow, R.id.yearEventDate, Integer.toString(event.time.get(Calendar.DAY_OF_MONTH)))
                            textInView(eventRow, R.id.yearEventMonth, getShortMonth(event.time))
                            textInView(eventRow, R.id.yearEventTitle, Html.fromHtml(title))
                            textInView(eventRow, R.id.yearEventTime, Html.fromHtml(time))
                            if (isNotEmpty(subtitle)) {
                                textInView(eventRow, R.id.yearEventSubtitle, Html.fromHtml(subtitle))
                                showInView(eventRow, R.id.yearEventSubtitle)
                            }
                            if (isNotEmpty(link)) {
                                val finalLink = link
                                showInView(eventRow, R.id.yearEventLink)
                                eventRow.setOnClickListener { _ ->
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.data = Uri.parse(finalLink)
                                    startActivity(intent)
                                }
                            } else {
                                eventRow.isClickable = false
                                eventRow.isFocusable = false
                            }

                            eventsBox.addView(eventRow)
                            first = false
                        }
                        if (first) {
                            removeInView(view, R.id.yearEventsBox)
                            showInView(view, R.id.yearEventsNone)
                        } else {
                            showInView(view, R.id.yearEventsBox)
                            removeInView(view, R.id.yearEventsNone)
                        }
                    }
                }
            }
        }
        thread.start()
    }

    private fun getShortMonth(calendar: Calendar): String {
        when (calendar.get(Calendar.MONTH)) {
            0 -> return "JAN"
            1 -> return "FEB"
            2 -> return "MAR"
            3 -> return "APR"
            4 -> return "MAY"
            5 -> return "JUN"
            6 -> return "JUL"
            7 -> return "AUG"
            8 -> return "SEP"
            9 -> return "OCT"
            10 -> return "NOV"
            11 -> return "DEC"
        }
        return ""

    }

}