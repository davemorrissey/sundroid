package uk.co.sundroid.activity.data.fragments

import android.os.Handler
import android.view.View
import android.widget.TableLayout
import android.widget.TextView
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.dialogs.settings.DayEventsPickerFragment
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDay
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.time.*

import java.util.*

class DayDetailEventsFragment : AbstractDayFragment(), ConfigurableFragment {

    private val handler = Handler()

    override val layout: Int
        get() = R.layout.frag_data_daydetail_events

    override fun openSettingsDialog() {
        val settingsDialog = DayEventsPickerFragment.newInstance(activity)
        settingsDialog.setTargetFragment(this, 0)
        settingsDialog.show(fragmentManager, "dayEventsSettings")
    }

    @Throws(Exception::class)
    override fun update(location: LocationDetails, calendar: Calendar, view: View) {

        val thread = object : Thread() {
            override fun run() {
                if (!isSafe) {
                    return
                }

                var sunDay: SunDay? = null
                var moonDay: MoonDay? = null
                var planetDays: MutableMap<Body, BodyDay>? = null

                if (SharedPrefsHelper.getShowElement(applicationContext!!, "evtByTimeSun", true)) {
                    sunDay = SunCalculator.calcDay(location.location, calendar)
                }
                if (SharedPrefsHelper.getShowElement(applicationContext!!, "evtByTimeMoon", true)) {
                    moonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, false) as MoonDay
                }
                if (SharedPrefsHelper.getShowElement(applicationContext!!, "evtByTimePlanets", false)) {
                    planetDays = LinkedHashMap()
                    for (body in Body.values()) {
                        if (body !== Body.SUN && body !== Body.MOON) {
                            planetDays[body] = BodyPositionCalculator.calcDay(body, location.location, calendar, true)
                        }
                    }
                }

                val eventsSet = TreeSet<SummaryEvent>()

                if (sunDay != null) {
                    if (sunDay.rise != null) {
                        eventsSet.add(SummaryEvent("Sunrise", sunDay.rise!!, sunDay.riseAzimuth))
                    }
                    if (sunDay.set != null) {
                        eventsSet.add(SummaryEvent("Sunset", sunDay.set!!, sunDay.setAzimuth))
                    }
                    if (sunDay.astDawn != null) {
                        eventsSet.add(SummaryEvent("Astro. dawn", sunDay.astDawn!!, null))
                    }
                    if (sunDay.astDusk != null) {
                        eventsSet.add(SummaryEvent("Astro. dusk", sunDay.astDusk!!, null))
                    }
                    if (sunDay.ntcDawn != null) {
                        eventsSet.add(SummaryEvent("Nautical dawn", sunDay.ntcDawn!!, null))
                    }
                    if (sunDay.ntcDusk != null) {
                        eventsSet.add(SummaryEvent("Nautical dusk", sunDay.ntcDusk!!, null))
                    }
                    if (sunDay.civDawn != null) {
                        eventsSet.add(SummaryEvent("Civil dawn", sunDay.civDawn!!, null))
                    }
                    if (sunDay.civDusk != null) {
                        eventsSet.add(SummaryEvent("Civil dusk", sunDay.civDusk!!, null))
                    }
                    if (sunDay.transit != null && sunDay.riseSetType !== RiseSetType.SET) {
                        eventsSet.add(SummaryEvent("Solar noon", sunDay.transit!!, null))
                    }
                    if (sunDay.ghEnd != null) {
                        eventsSet.add(SummaryEvent("Golden hr end", sunDay.ghEnd!!, null))
                    }
                    if (sunDay.ghStart != null) {
                        eventsSet.add(SummaryEvent("Golden hr start", sunDay.ghStart!!, null))
                    }
                }
                if (moonDay != null) {
                    if (moonDay.rise != null) {
                        eventsSet.add(SummaryEvent("Moonrise", moonDay.rise!!, moonDay.riseAzimuth))
                    }
                    if (moonDay.set != null) {
                        eventsSet.add(SummaryEvent("Moonset", moonDay.set!!, moonDay.setAzimuth))
                    }
                }
                if (planetDays != null) {
                    for ((planet, planetDay) in planetDays) {
                        if (planetDay.rise != null) {
                            eventsSet.add(SummaryEvent(planet.displayName + " rise", planetDay.rise!!, planetDay.riseAzimuth))
                        }
                        if (planetDay.set != null) {
                            eventsSet.add(SummaryEvent(planet.displayName + " set", planetDay.set!!, planetDay.setAzimuth))
                        }
                    }
                }

                val eventsList = ArrayList(eventsSet)

                handler.post {
                    if (isSafe) {
                        val eventsTable = view.findViewById<TableLayout>(R.id.eventsTable)
                        eventsTable.removeAllViews()

                        val eventsHeader = activity.layoutInflater.inflate(R.layout.frag_data_daydetail_events_header, null)
                        eventsTable.addView(eventsHeader)

                        if (eventsList.isEmpty()) {
                            showInView(view, R.id.eventsNone)
                            removeInView(view, R.id.eventsTable)
                        } else {
                            showInView(view, R.id.eventsTable)
                            removeInView(view, R.id.eventsNone)
                            for (event in eventsList) {
                                val eventRow = activity.layoutInflater.inflate(R.layout.frag_data_daydetail_events_row, null)
                                (eventRow.findViewById<View>(R.id.eventName) as TextView).text = event.name.toUpperCase()
                                val time = formatTime(applicationContext!!, event.time, true)
                                (eventRow.findViewById<View>(R.id.eventTime) as TextView).text = time.time + time.marker
                                if (event.azimuth != null) {
                                    val azimuth = formatBearing(applicationContext!!, event.azimuth, location.location, calendar)
                                    (eventRow.findViewById<View>(R.id.eventAz) as TextView).text = azimuth
                                } else {
                                    (eventRow.findViewById<View>(R.id.eventAz) as TextView).text = " "
                                }
                                eventsTable.addView(eventRow)
                            }
                        }
                        showInView(view, R.id.eventsDataBox)
                    }
                }
            }
        }
        thread.start()


    }


}
