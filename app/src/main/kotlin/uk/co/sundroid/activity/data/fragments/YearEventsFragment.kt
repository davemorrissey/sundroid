package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
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
import uk.co.sundroid.util.async.async
import java.util.*
import java.util.Calendar.*
import kotlin.math.abs

class YearEventsFragment : AbstractYearFragment() {

    override val layout: Int
        get() = R.layout.frag_data_yearevents

    fun openSettingsDialog() {
        val settingsDialog = YearEventsPickerFragment.newInstance(requireContext())
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(requireFragmentManager(), "yearEventsSettings")
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarSubtitle(R.string.data_year_title)
            setViewConfigurationCallback({ openSettingsDialog() })
        }
    }

    @Throws(Exception::class)
    override fun update(location: LocationDetails, calendar: Calendar, view: View) {

        async(
                inBackground = {
                    val eventsSet = YearData.getYearEvents(calendar.get(YEAR), location.timeZone!!.zone)
                    val moonPhases = MoonPhaseCalculator.getYearEvents(calendar.get(YEAR), location.timeZone!!.zone)
                    moonPhases.mapTo(eventsSet) { Event(EventType.PHASE, it, it.time, null) }
                    eventsSet
                },
                onDone = { eventsSet: Set<Event> ->
                    if (isSafe) {
                        val todayCalendar = getInstance(calendar.timeZone)
                        val eventsList = ArrayList(eventsSet)
                        val eventsBox = view.findViewById<ViewGroup>(yearEventsBox)
                        eventsBox.removeAllViews()

                        var first = true
                        for (event in eventsList) {
                            var title = ""
                            var time = formatTimeStr(requireContext(), event.time, false)
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
                                    requireActivity().layoutInflater.inflate(R.layout.divider_background, eventsBox)
                                }
                                val eventRow = inflate(R.layout.frag_data_yearevents_event)

                                val today = todayCalendar.get(YEAR) == event.time.get(YEAR) &&
                                        todayCalendar.get(MONTH) == event.time.get(MONTH) &&
                                        todayCalendar.get(DAY_OF_MONTH) == event.time.get(DAY_OF_MONTH)
                                if (today) {
                                    eventRow.setBackgroundColor(getCalendarGridHighlightColor())
                                    eventRow.findViewById<View>(yearEventDateMonth).setBackgroundColor(getCalendarGridHighlightColor())
                                } else {
                                    eventRow.setBackgroundColor(getCalendarDefaultColor())
                                    eventRow.findViewById<View>(yearEventDateMonth).setBackgroundColor(getCalendarHeaderColor())
                                }

                                if (image > 0) {
                                    modifyChild(eventRow, yearEventImg, visibility = VISIBLE, image = image)
                                }
                                modifyChild(eventRow, yearEventDate, visibility = VISIBLE, text = event.time.get(DAY_OF_MONTH).toString())
                                modifyChild(eventRow, yearEventMonth, visibility = VISIBLE, text = getShortMonth(event.time).toUpperCase(Locale.getDefault()))
                                modifyChild(eventRow, yearEventTitle, visibility = VISIBLE, html = title)
                                modifyChild(eventRow, yearEventTime, visibility = VISIBLE, html = time)
                                if (isNotEmpty(subtitle)) {
                                    modifyChild(eventRow, yearEventSubtitle, visibility = VISIBLE, html = subtitle)
                                }
                                if (isNotEmpty(link)) {
                                    modifyChild(eventRow, yearEventLink, visibility = VISIBLE)
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
                            modifyChild(view, yearEventsBox, visibility = GONE)
                            modifyChild(view, yearEventsNone, visibility = VISIBLE)
                        } else {
                            modifyChild(view, yearEventsBox, visibility = VISIBLE)
                            modifyChild(view, yearEventsNone, visibility = GONE)
                        }
                    }
                }
        )
    }

}