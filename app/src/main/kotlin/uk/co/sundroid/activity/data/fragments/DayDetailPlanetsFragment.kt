package uk.co.sundroid.activity.data.fragments

import android.view.View
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.R.id.*
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.async.async
import uk.co.sundroid.util.geometry.formatBearing
import uk.co.sundroid.util.geometry.formatElevation
import uk.co.sundroid.util.theme.getRiseArrow
import uk.co.sundroid.util.theme.getSetArrow
import uk.co.sundroid.util.time.formatDurationHMS
import uk.co.sundroid.util.html
import uk.co.sundroid.util.time.formatTimeStr
import java.util.*

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

                            text(row, planetName, planet.name)

                            var noTransit = false
                            var noUptime = false

                            if (day.riseSetType !== RiseSetType.SET && day.transitAppElevation > 0) {
                                val noon = formatTimeStr(requireContext(), day.transit!!, false, html = true)
                                show(row, planetTransit)
                                show(row, planetTransitTime, html("$noon &nbsp; ${formatElevation(day.transitAppElevation)}"))
                            } else {
                                remove(row, planetTransit)
                                noTransit = true
                            }

                            if (day.riseSetType === RiseSetType.RISEN || day.riseSetType === RiseSetType.SET) {
                                show(row, planetSpecial, if (day.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
                                remove(row, planetEvtsRow, planetEvt0, planetEvt1, planetUptime)
                                noUptime = true
                            } else {
                                remove(row, planetSpecial)
                                remove(row, planetEvt0, planetEvt1)
                                show(row, planetEvtsRow)
                                val events = TreeSet<RiseSetEvent>()
                                day.rise?.let { events.add(RiseSetEvent("Rise", it, day.riseAzimuth)) }
                                day.set?.let { events.add(RiseSetEvent("Set", it, day.setAzimuth)) }
                                events.forEachIndexed { index, event ->
                                    val rowId = view("planetEvt$index")
                                    val timeId = view("planetEvt${index}Time")
                                    val azId = view("planetEvt${index}Az")
                                    val imgId = view("planetEvt${index}Img")

                                    val time = formatTimeStr(requireContext(), event.time, false, html = true)
                                    val az = formatBearing(requireContext(), event.azimuth, location.location, event.time)

                                    text(row, timeId, html(time))
                                    text(row, azId, az)
                                    show(row, rowId)
                                    image(row, imgId, if (event.name == "Rise") getRiseArrow() else getSetArrow())
                                }

                                if (day.uptimeHours > 0 && day.uptimeHours < 24) {
                                    show(row, planetUptime)
                                    show(row, planetUptimeTime, html(formatDurationHMS(requireContext(), day.uptimeHours, false, html = true)))
                                } else {
                                    remove(row, planetUptime)
                                }
                            }

                            if (noTransit && noUptime) {
                                remove(row, planetTransitUptime)
                            } else {
                                show(row, planetTransitUptime)
                            }
                            wrapper.addView(row)

                        }
                        show(wrapper)
                    }
                }
        )
    }

}