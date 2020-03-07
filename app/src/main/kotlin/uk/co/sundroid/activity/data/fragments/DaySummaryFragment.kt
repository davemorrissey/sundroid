package uk.co.sundroid.activity.data.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*
import java.util.*

import kotlinx.android.synthetic.main.frag_data_daysummary.*
import uk.co.sundroid.util.astro.RiseSetType.*
import android.view.View.*
import uk.co.sundroid.activity.MainActivity

class DaySummaryFragment : AbstractDayFragment() {
    private val handler = Handler()
    override val layout: Int
        get() = R.layout.frag_data_daysummary

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarSubtitle(R.string.data_summary_title)
    }

    @SuppressLint("SetTextI18n")
    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar)
        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, true) as MoonDay

        Thread(Runnable {
            context?.let {
                val phase: Double = moonDay.phaseDouble
                val moonBitmap: Bitmap = MoonPhaseImage.makeImage(resources, R.drawable.moon, phase, location.location.latitude.doubleValue < 0, MoonPhaseImage.SIZE_MEDIUM)
                handler.post {
                    moonImage?.setImageBitmap(moonBitmap)
                }
            }
        }).start()

        gone(sunSpecial, sunEvt0Row, sunEvt1Row, sunUptimeRow)
        if (sunDay.riseSetType in setOf(RISEN, SET)) {
            sunSpecial.text = sunDay.riseSetType?.description
            sunSpecial.visibility = VISIBLE
        } else {
            val events: MutableSet<SummaryEvent> = TreeSet()
            sunDay.rise?.let {
                events.add(SummaryEvent("RISE", it, sunDay.riseAzimuth))
            }
            sunDay.set?.let {
                events.add(SummaryEvent("SET", it, sunDay.setAzimuth))
            }
            events.forEachIndexed { index, event ->
                val time: Time = formatTime(requireContext(), event.time, false)
                text(view, view("sunEvt${index}Label"), event.name)
                text(view, view("sunEvt${index}Time"), time.time + time.marker.toLowerCase(Locale.getDefault()))
                image(view, view("sunEvt${index}Img"), if (event.name == "RISE") getRiseArrow() else getSetArrow())
                show(view, view("sunEvt${index}Row"))
            }
            if (sunDay.uptimeHours > 0 && sunDay.uptimeHours < 24) {
                sunUptimeRow.visibility = VISIBLE
                sunUptimeTime.text = formatDurationHMS(requireContext(), sunDay.uptimeHours, false)
            }
        }

        gone(moonSpecial, moonEvt0Row, moonEvt1Row)
        if (moonDay.riseSetType in setOf(RISEN, SET)) {
            moonSpecial.text = moonDay.riseSetType?.description
            moonSpecial.visibility = VISIBLE
        } else {
            val events: MutableSet<SummaryEvent> = TreeSet()
            moonDay.rise?.let {
                events.add(SummaryEvent("RISE", it, moonDay.riseAzimuth))
            }
            moonDay.set?.let {
                events.add(SummaryEvent("SET", it, moonDay.setAzimuth))
            }
            events.forEachIndexed { index, event ->
                val time: Time = formatTime(requireContext(), event.time, false)
                text(view, view("moonEvt${index}Label"), event.name)
                text(view, view("moonEvt${index}Time"), time.time + time.marker.toLowerCase(Locale.getDefault()))
                image(view, view("moonEvt${index}Img"), if (event.name == "RISE") getRiseArrow() else getSetArrow())
                show(view, view("moonEvt${index}Row"))
            }
        }
        moonDay.phaseEvent?.let {
            val time: Time = formatTime(requireContext(), it.time, false)
            moonPhase.text = "${moonDay.phase.shortDisplayName} at ${time.time}${time.marker}"
        } ?: run {
            moonPhase.text = moonDay.phase.shortDisplayName
        }
        moonIllumination.text = "${moonDay.illumination}%"
    }

}