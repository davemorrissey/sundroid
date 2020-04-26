package uk.co.sundroid.activity.data.fragments

import android.os.Handler
import android.view.View
import android.view.View.VISIBLE
import android.view.View.GONE
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.YearData
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.BodyDayEvent.Event.RISESET
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.astro.YearData.Event
import uk.co.sundroid.util.astro.YearData.EventType
import uk.co.sundroid.util.time.*
import uk.co.sundroid.util.html

import java.util.Calendar

import uk.co.sundroid.util.time.formatDurationHMS

import kotlinx.android.synthetic.main.frag_data_daydetail_sun.*
import uk.co.sundroid.util.astro.BodyDayEvent.Direction.*
import kotlin.math.abs

class DayDetailSunFragment : AbstractDayDetailFragment() {

    private val handler = Handler()

    override val layout: Int
        get() = R.layout.frag_data_daydetail_sun

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        val thread = object : Thread() {
            override fun run() {
                if (!isSafe) {
                    return
                }

                val yearEvents = YearData.getYearEvents(calendar.get(Calendar.YEAR), calendar.timeZone)
                val yearEvent: Event? = yearEvents.lastOrNull { it.type.body === Body.SUN && isSameDay(calendar, it.time) }
                val day = SunCalculator.calcDay(location.location, calendar)

                handler.post {
                    if (isSafe) {
                        if (yearEvent != null) {
                            view.findViewById<View>(R.id.sunEvent).setOnClickListener(null)
                            modifyChild(view, R.id.sunEvent, visibility = VISIBLE)
                            modifyChild(view, R.id.sunEventTitle, visibility = VISIBLE, text = yearEvent.type.displayName)
                            if (yearEvent.type === EventType.NORTHERN_SOLSTICE && abs(location.location.latitude.doubleValue) > 23.44) {
                                val localExtreme = if (location.location.latitude.doubleValue >= 0) "Longest" else "Shortest"
                                modifyChild(view, R.id.sunEventSubtitle, visibility = VISIBLE, text = "$localExtreme day")
                            } else if (yearEvent.type === EventType.SOUTHERN_SOLSTICE && abs(location.location.latitude.doubleValue) > 23.44) {
                                val localExtreme = if (location.location.latitude.doubleValue >= 0) "Shortest" else "Longest"
                                modifyChild(view, R.id.sunEventSubtitle, visibility = VISIBLE, text = "$localExtreme day")
                            } else if (yearEvent.type === EventType.ANNULAR_SOLAR || yearEvent.type === EventType.HYBRID_SOLAR || yearEvent.type === EventType.PARTIAL_SOLAR || yearEvent.type === EventType.TOTAL_SOLAR) {
                                modifyChild(view, R.id.sunEventSubtitle, visibility = VISIBLE, text = "Tap to check Wikipedia for visibility")
                                view.findViewById<View>(R.id.sunEvent).setOnClickListener {
                                    browseTo(yearEvent.link)
                                }
                            } else {
                                modifyChild(view, R.id.sunEventSubtitle, visibility = GONE)
                            }
                        } else {
                            modifyChild(view, R.id.sunEvent, visibility = GONE)
                        }

                        var noTransit = true
                        var noUptime = true

                        if (day.riseSetType !== RiseSetType.SET && day.transitAppElevation > 0) {
                            val noon = formatTimeStr(requireContext(), day.transit!!, false, html = true)
                            noTransit = false
                            modifyChild(view, R.id.sunTransit, visibility = VISIBLE)
                            modifyChild(view, R.id.sunTransitTime, html = "$noon &nbsp; ${formatElevation(day.transitAppElevation)}")
                        } else {
                            modifyChild(view, R.id.sunTransit, visibility = GONE)
                        }

                        val eventsRow = view.findViewById<ViewGroup>(R.id.sunEventsRow)
                        eventsRow.removeAllViews()

                        if (day.riseSetType === RiseSetType.RISEN || day.riseSetType === RiseSetType.SET) {
                            val eventCell = inflate(R.layout.frag_data_event, eventsRow, false)
                            modifyChild(eventCell, evtImg, image = if (day.riseSetType === RiseSetType.RISEN) getRisenAllDay() else getSetAllDay())
                            modifyChild(eventCell, evtTime, html = if (day.riseSetType === RiseSetType.RISEN) "<small>RISEN ALL DAY</small>" else "<small>SET ALL DAY</small>")
                            modifyChild(eventCell, evtAz, visibility = GONE)
                            modifyChild(view, R.id.sunUptime, visibility = GONE)
                            eventsRow.addView(eventCell)
                            noUptime = true
                        } else {
                            day.events.filter { e -> e.event == RISESET }.forEach { event ->
                                val eventCell = inflate(R.layout.frag_data_event, eventsRow, false)
                                val az = formatBearing(requireContext(), event.azimuth ?: 0.0, location.location, event.time)
                                modifyChild(eventCell, evtImg, image = if (event.direction == RISING) getRiseArrow() else getSetArrow())
                                modifyChild(eventCell, evtTime, html = formatTimeStr(requireContext(), event.time, false, html = true))
                                modifyChild(eventCell, evtAz, text = az)
                                eventsRow.addView(eventCell)
                            }

                            if (day.uptimeHours > 0 && day.uptimeHours < 24) {
                                modifyChild(view, R.id.sunUptimeTime, visibility = VISIBLE, html = formatDurationHMS(requireContext(), day.uptimeHours, false, html = true))
                                modifyChild(view, R.id.sunUptime, visibility = VISIBLE)
                            } else {
                                modifyChild(view, R.id.sunUptime, visibility = GONE)
                            }
                        }

                        modifyChild(view, R.id.sunTransitUptime, R.id.sunTransitUptimeDivider, visibility = if (noTransit && noUptime) GONE else VISIBLE)

                        day.apply {
                            hashMapOf(
                                    sunCivDawnTime to civDawn,
                                    sunCivDuskTime to civDusk,
                                    sunNtcDawnTime to ntcDawn,
                                    sunNtcDuskTime to ntcDusk,
                                    sunAstDawnTime to astDawn,
                                    sunAstDuskTime to astDusk
                            ).forEach {
                                (view, time) -> view.text = html(time?.let { formatTimeStr(requireContext(), it, html = true) } ?: "-")
                            }
                        }
                        modifyChild(view, R.id.sunDataBox, visibility = VISIBLE)
                    }
                }
            }
        }
        thread.start()
    }


}
