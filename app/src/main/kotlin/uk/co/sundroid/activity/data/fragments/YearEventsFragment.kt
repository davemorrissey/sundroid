package uk.co.sundroid.activity.data.fragments

import android.os.Handler
import android.text.Html
import android.view.View
import android.view.ViewGroup
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.data.fragments.dialogs.settings.YearEventsPickerFragment
import uk.co.sundroid.util.*
import uk.co.sundroid.util.astro.MoonPhaseEvent
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*
import uk.co.sundroid.util.astro.YearData
import uk.co.sundroid.util.astro.YearData.Event
import uk.co.sundroid.util.astro.YearData.EventType
import java.util.*
import kotlin.math.abs

class YearEventsFragment : AbstractYearFragment(), ConfigurableFragment {

    private val handler = Handler()

    override val layout: Int
        get() = R.layout.frag_data_yearevents

    override fun openSettingsDialog() {
        val settingsDialog = YearEventsPickerFragment.newInstance(requireContext())
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(requireFragmentManager(), "yearEventsSettings")
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarSubtitle(R.string.data_year_title)
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
                            val eventTime = formatTime(requireContext(), event.time, false)
                            var title = ""
                            var time = eventTime.time + eventTime.marker.toLowerCase(Locale.getDefault())
                            var subtitle = ""
                            var link: String? = null
                            var image = 0
                            when (event.type) {
                                EventType.EARTH_APHELION, EventType.EARTH_PERIHELION -> {
                                    if (Prefs.showElement(requireContext(), "yearEarthApsis", true)) {
                                        title = event.type.displayName
                                        link = event.link
                                    }
                                }
                                EventType.PARTIAL_LUNAR, EventType.TOTAL_LUNAR, EventType.PENUMBRAL_LUNAR -> {
                                    if (Prefs.showElement(requireContext(), "yearLunarEclipse", true)) {
                                        title = event.type.displayName
                                        time = "Greatest eclipse: $time"
                                        link = event.link
                                    }
                                }
                                EventType.PARTIAL_SOLAR, EventType.TOTAL_SOLAR, EventType.ANNULAR_SOLAR, EventType.HYBRID_SOLAR -> {
                                    if (!Prefs.showElement(requireContext(), "yearSolarEclipse", true)) {
                                        title = event.type.displayName
                                        time = "Greatest eclipse: $time"
                                        subtitle = event.extra as String
                                        link = event.link
                                    }
                                }
                                EventType.MARCH_EQUINOX, EventType.SEPTEMBER_EQUINOX -> {
                                    if (Prefs.showElement(requireContext(), "yearEquinox", true)) {
                                        title = event.type.displayName
                                    }
                                }
                                EventType.NORTHERN_SOLSTICE -> {
                                    if (Prefs.showElement(requireContext(), "yearSolstice", true)) {
                                        title = event.type.displayName
                                        if (abs(location.location.latitude.doubleValue) > 23.44) {
                                            val sunDay = SunCalculator.calcDay(location.location, event.time, SunCalculator.Event.RISESET)
                                            val localExtreme = if (location.location.latitude.doubleValue >= 0) "Longest" else "Shortest"
                                            subtitle = localExtreme + " day: " + formatDurationHMS(requireContext(), sunDay.uptimeHours, true)
                                        }
                                    }
                                }
                                EventType.SOUTHERN_SOLSTICE -> {
                                    if (Prefs.showElement(requireContext(), "yearSolstice", true)) {
                                        title = event.type.displayName
                                        if (abs(location.location.latitude.doubleValue) > 23.44) {
                                            val sunDay = SunCalculator.calcDay(location.location, event.time, SunCalculator.Event.RISESET)
                                            val localExtreme = if (location.location.latitude.doubleValue >= 0) "Shortest" else "Longest"
                                            subtitle = localExtreme + " day: " + formatDurationHMS(requireContext(), sunDay.uptimeHours, true)
                                        }
                                    }
                                }
                                EventType.PHASE -> {
                                    val moonPhase = event.extra as MoonPhaseEvent?
                                    when (moonPhase?.phase) {
                                        MoonPhase.FULL -> {
                                            if (Prefs.showElement(requireContext(), "yearFullMoon", true)) {
                                                title = "Full Moon"
                                                image = getPhaseFull()
                                            }
                                        }
                                        MoonPhase.NEW -> {
                                            if (Prefs.showElement(requireContext(), "yearNewMoon", true)) {
                                                title = "New Moon"
                                                image = getPhaseNew()
                                            }
                                        }
                                        MoonPhase.FIRST_QUARTER -> {
                                            if (Prefs.showElement(requireContext(), "yearQuarterMoon", true)) {
                                                title = "First Quarter"
                                                image = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                                            }
                                        }
                                        MoonPhase.LAST_QUARTER -> {
                                            if (Prefs.showElement(requireContext(), "yearQuarterMoon", true)) {
                                                title = "Last Quarter"
                                                image = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                                            }
                                        }
                                        else -> { }
                                    }
                                }
                            }

                            if (title.isNotEmpty()) {
                                if (!first) {
                                    requireActivity().layoutInflater.inflate(R.layout.divider, eventsBox)
                                }
                                val eventRow = inflate(R.layout.frag_data_yearevents_event)

                                val today = todayCalendar.get(Calendar.YEAR) == event.time.get(Calendar.YEAR) &&
                                        todayCalendar.get(Calendar.MONTH) == event.time.get(Calendar.MONTH) &&
                                        todayCalendar.get(Calendar.DAY_OF_MONTH) == event.time.get(Calendar.DAY_OF_MONTH)
                                if (today) {
                                    eventRow.setBackgroundColor(getCalendarHighlightColor())
                                } else {
                                    eventRow.setBackgroundColor(getCalendarDefaultColor())
                                }

                                if (image > 0) {
                                    image(eventRow, R.id.yearEventImg, image)
                                    show(eventRow, R.id.yearEventImg)
                                }
                                text(eventRow, R.id.yearEventDate, event.time.get(Calendar.DAY_OF_MONTH).toString())
                                text(eventRow, R.id.yearEventMonth, getShortMonth(event.time).toUpperCase(Locale.getDefault()))
                                text(eventRow, R.id.yearEventTitle, Html.fromHtml(title))
                                text(eventRow, R.id.yearEventTime, Html.fromHtml(time))
                                if (isNotEmpty(subtitle)) {
                                    text(eventRow, R.id.yearEventSubtitle, Html.fromHtml(subtitle))
                                    show(eventRow, R.id.yearEventSubtitle)
                                }
                                if (isNotEmpty(link)) {
                                    show(eventRow, R.id.yearEventLink)
                                    eventRow.setOnClickListener {
                                        browseTo(link)
                                    }
                                } else {
                                    eventRow.isClickable = false
                                    eventRow.isFocusable = false
                                }

                                eventsBox.addView(eventRow)
                                first = false
                            }
                        }
                        if (first) {
                            remove(view, R.id.yearEventsBox)
                            show(view, R.id.yearEventsNone)
                        } else {
                            show(view, R.id.yearEventsBox)
                            remove(view, R.id.yearEventsNone)
                        }
                    }
                }
            }
        }
        thread.start()
    }

}