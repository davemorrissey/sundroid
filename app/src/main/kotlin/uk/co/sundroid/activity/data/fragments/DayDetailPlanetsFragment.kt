package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.view.View.VISIBLE
import android.view.View.GONE
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.BodyDayEventType
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.geometry.formatElevation
import uk.co.sundroid.util.theme.getRiseArrow
import uk.co.sundroid.util.theme.getSetArrow
import uk.co.sundroid.util.time.formatDurationHMS
import uk.co.sundroid.util.time.formatTimeStr

class DayDetailPlanetsFragment : AbstractDayDetailFragment() {

    override val layout: Int
        get() = R.layout.frag_data_daydetail_planets

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        async(
                inBackground = { Body.PLANETS.associate { Pair(it, BodyPositionCalculator.calcDay(it, location.location, calendar, true)) } },
                onDone = { days: Map<Body, BodyDay> ->
                    if (isSafe) {
                        val wrapper = view.findViewById<ViewGroup>(planetsDataBox)
                        wrapper.removeAllViews()
                        for ((planet, day) in days) {
                            val row = inflate(R.layout.frag_data_daydetail_planets_planet)

                            modifyChild(row, planetName, text = planet.name)

                            var noTransit = false
                            var noUptime = false

                            if (day.riseSetType !== RiseSetType.SET && day.transitAppElevation > 0) {
                                val noon = formatTimeStr(requireContext(), day.transit!!, false, html = true)
                                modifyChild(row, planetTransitTime, html = "$noon &nbsp; ${formatElevation(day.transitAppElevation)}")
                            } else {
                                modifyChild(row, planetTransit, visibility = GONE)
                                noTransit = true
                            }

                            val eventsRow = row.findViewById<ViewGroup>(planetEventsRow)
                            eventsRow.removeAllViews()

                            if (day.riseSetType === RiseSetType.RISEN || day.riseSetType === RiseSetType.SET) {
                                val eventCell = inflate(R.layout.frag_data_daydetail_planets_planet_event, eventsRow, false)
                                modifyChild(eventCell, planetEvtTime, text = if (day.riseSetType === RiseSetType.RISEN) "RISEN ALL DAY" else "SET ALL DAY")
                                modifyChild(eventCell, planetEvtAz, visibility = GONE)
                                modifyChild(row, planetUptime, visibility = GONE)
                                eventsRow.addView(eventCell)
                                noUptime = true
                            } else {
                                day.events.forEach { event ->
                                    val eventCell = inflate(R.layout.frag_data_daydetail_planets_planet_event, eventsRow, false)
                                    val az = formatBearing(requireContext(), event.azimuth ?: 0.0, location.location, event.time)
                                    modifyChild(eventCell, planetEvtImg, image = if (event.event == BodyDayEventType.RISE) getRiseArrow() else getSetArrow())
                                    modifyChild(eventCell, planetEvtTime, html = formatTimeStr(requireContext(), event.time, false, html = true))
                                    modifyChild(eventCell, planetEvtAz, text = az)
                                    eventsRow.addView(eventCell)
                                }

                                if (day.uptimeHours > 0 && day.uptimeHours < 24) {
                                    modifyChild(row, planetUptimeTime, html = formatDurationHMS(requireContext(), day.uptimeHours, false, html = true))
                                } else {
                                    modifyChild(row, planetUptime, visibility = GONE)
                                }
                            }

                            if (noTransit && noUptime) {
                                modifyChild(row, planetTransitUptime, visibility = GONE)
                            }
                            wrapper.addView(row)

                        }
                        modify(wrapper, visibility = VISIBLE)
                    }
                }
        )
    }

}