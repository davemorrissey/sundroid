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
import uk.co.sundroid.util.time.formatTime
import java.util.*

class DayDetailPlanetsFragment : AbstractDayFragment() {

    override val layout: Int
        get() = R.layout.frag_data_daydetail_planets

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        async(
                inBackground = { Body.PLANETS.associate { Pair(it, BodyPositionCalculator.calcDay(it, location.location, calendar, true)) } },
                onDone = { planetDays: Map<Body, BodyDay> ->
                    if (isSafe) {
                        val planetsDataBox = view.findViewById<ViewGroup>(planetsDataBox)
                        for ((planet, planetDay) in planetDays) {
                            val planetRow = inflate(R.layout.frag_data_daydetail_planets_planet)

                            text(planetRow, planetName, planet.name)

                            var noTransit = false
                            var noUptime = false

                            if (planetDay.riseSetType !== RiseSetType.SET && planetDay.transitAppElevation > 0) {
                                val noon = formatTime(activity, planetDay.transit!!, false)
                                show(planetRow, planetTransit)
                                show(planetRow, planetTransitTime, "$noon  ${formatElevation(planetDay.transitAppElevation)}")
                            } else {
                                remove(planetRow, planetTransit)
                                noTransit = true
                            }

                            if (planetDay.riseSetType === RiseSetType.RISEN || planetDay.riseSetType === RiseSetType.SET) {
                                show(planetRow, planetSpecial, if (planetDay.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
                                remove(planetRow, planetEvtsRow, planetEvt1, planetEvt2, planetUptime)
                                noUptime = true
                            } else {
                                remove(planetRow, planetSpecial)
                                remove(planetRow, planetEvt1, planetEvt2)
                                show(planetRow, planetEvtsRow)
                                val events = TreeSet<RiseSetEvent>()
                                planetDay.rise?.let { events.add(RiseSetEvent("Rise", it, planetDay.riseAzimuth)) }
                                planetDay.set?.let { events.add(RiseSetEvent("Set", it, planetDay.setAzimuth)) }
                                events.forEachIndexed { index, event ->
                                    val rowId = view("planetEvt$index")
                                    val timeId = view("planetEvt${index}Time")
                                    val azId = view("planetEvt${index}Az")
                                    val imgId = view("planetEvt${index}Img")

                                    val time = formatTime(activity, event.time, false)
                                    val az = formatBearing(activity, event.azimuth, location.location, event.time)

                                    text(planetRow, timeId, "$time")
                                    text(planetRow, azId, az)
                                    show(planetRow, rowId)
                                    image(view, imgId, if (event.name == "Rise") getRiseArrow() else getSetArrow())
                                }

                                if (planetDay.uptimeHours > 0 && planetDay.uptimeHours < 24) {
                                    show(planetRow, planetUptime)
                                    show(planetRow, planetUptimeTime, formatDurationHMS(applicationContext!!, planetDay.uptimeHours, false))
                                } else {
                                    remove(planetRow, planetUptime)
                                }
                            }

                            if (noTransit && noUptime) {
                                remove(planetRow, planetTransitUptime)
                            } else {
                                show(planetRow, planetTransitUptime)
                            }
                            planetsDataBox.addView(planetRow)

                        }
                        show(planetsDataBox)
                    }
                }
        )
    }

}