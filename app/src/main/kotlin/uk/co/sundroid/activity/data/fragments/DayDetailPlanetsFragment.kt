package uk.co.sundroid.activity.data.fragments

import android.os.Handler
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*

import java.util.*

class DayDetailPlanetsFragment : AbstractDayFragment() {

    private val handler = Handler()

    override val layout: Int
        get() = R.layout.frag_data_daydetail_planets

    override fun update(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        val thread = object : Thread() {
            override fun run() {
                if (!isSafe) {
                    return
                }

                val planetDays = LinkedHashMap<Body, BodyDay>()
                Body.values()
                        .filter { it !== Body.SUN && it !== Body.MOON }
                        .forEach { planetDays[it] = BodyPositionCalculator.calcDay(it, location.location, calendar, true) }

                handler.post {
                    if (isSafe) {
                        val planetsDataBox = view.findViewById<ViewGroup>(R.id.planetsDataBox)
                        for ((planet, planetDay) in planetDays) {
                            val planetRow = View.inflate(activity, R.layout.frag_data_daydetail_planets_planet, null)

                            textInView(planetRow, R.id.planetName, planet.name)

                            var noTransit = false
                            var noUptime = false

                            if (planetDay.riseSetType !== RiseSetType.SET && planetDay.transitAppElevation > 0) {
                                val noon = formatTime(applicationContext!!, planetDay.transit!!, false)
                                showInView(planetRow, R.id.planetTransit)
                                showInView(planetRow, R.id.planetTransitTime, noon.time + noon.marker + "  " + formatElevation(planetDay.transitAppElevation))
                            } else {
                                removeInView(planetRow, R.id.planetTransit)
                                noTransit = true
                            }

                            if (planetDay.riseSetType === RiseSetType.RISEN || planetDay.riseSetType === RiseSetType.SET) {
                                showInView(planetRow, R.id.planetSpecial, if (planetDay.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
                                removeInView(planetRow, R.id.planetEvtsRow, R.id.planetEvt1, R.id.planetEvt2, R.id.planetUptime)
                                noUptime = true
                            } else {
                                removeInView(planetRow, R.id.planetSpecial)
                                removeInView(planetRow, R.id.planetEvt1, R.id.planetEvt2)
                                showInView(planetRow, R.id.planetEvtsRow)
                                val events = TreeSet<SummaryEvent>()
                                if (planetDay.rise != null) {
                                    events.add(SummaryEvent("Rise", planetDay.rise!!, planetDay.riseAzimuth))
                                }
                                if (planetDay.set != null) {
                                    events.add(SummaryEvent("Set", planetDay.set!!, planetDay.setAzimuth))
                                }
                                var index = 1
                                for (event in events) {
                                    val rowId = view("planetEvt" + index)
                                    val timeId = view("planetEvt" + index + "Time")
                                    val azId = view("planetEvt" + index + "Az")
                                    val imgId = view("planetEvt" + index + "Img")

                                    val time = formatTime(applicationContext!!, event.time, false)
                                    val az = formatBearing(applicationContext!!, event.azimuth!!, location.location, event.time)

                                    textInView(planetRow, timeId, time.time + time.marker)
                                    textInView(planetRow, azId, az)
                                    showInView(planetRow, rowId)

                                    if (event.name == "Rise") {
                                        (planetRow.findViewById<View>(imgId) as ImageView).setImageResource(getRiseArrow())
                                    } else {
                                        (planetRow.findViewById<View>(imgId) as ImageView).setImageResource(getSetArrow())
                                    }

                                    index++
                                }

                                if (planetDay.uptimeHours > 0 && planetDay.uptimeHours < 24) {
                                    showInView(planetRow, R.id.planetUptime)
                                    showInView(planetRow, R.id.planetUptimeTime, formatDurationHMS(applicationContext!!, planetDay.uptimeHours, false))
                                } else {
                                    removeInView(planetRow, R.id.planetUptime)
                                }

                            }

                            if (noTransit && noUptime) {
                                removeInView(planetRow, R.id.planetTransitUptime)
                            } else {
                                showInView(planetRow, R.id.planetTransitUptime)
                            }
                            planetsDataBox.addView(planetRow)

                        }
                        showInView(view, R.id.planetsDataBox)
                    }
                }
            }
        }
        thread.start()
    }

}