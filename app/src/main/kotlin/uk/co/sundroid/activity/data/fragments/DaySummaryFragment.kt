package uk.co.sundroid.activity.data.fragments

import android.graphics.Bitmap
import android.os.Handler
import android.view.View
import uk.co.sundroid.R
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.MoonDay
import uk.co.sundroid.util.astro.RiseSetType
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.image.MoonPhaseImage
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*
import java.util.*

import kotlinx.android.synthetic.main.frag_data_daysummary.*

class DaySummaryFragment : AbstractDayFragment() {
    private val handler = Handler()
    override val layout: Int
        get() = R.layout.frag_data_daysummary

    override fun updateData(view: View) {
        val location = getLocation()
        val calendar = getDateCalendar()

        val sunDay: SunDay = SunCalculator.calcDay(location.location, calendar)
        val moonDay: MoonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, calendar, true) as MoonDay

        // Asynchronously generate moon graphic to speed up response.
        val thread: Thread = object : Thread() {
            override fun run() {
                context?.let {
                    val phase: Double = moonDay.phaseDouble
                    val moonBitmap: Bitmap = MoonPhaseImage.makeImage(resources, R.drawable.moon, phase, location.location.latitude.doubleValue < 0, MoonPhaseImage.SIZE_MEDIUM)
                    handler.post {
                        moonImage?.setImageBitmap(moonBitmap)
                    }
                }
            }
        }
        thread.start()
        if (sunDay.riseSetType === RiseSetType.RISEN || sunDay.riseSetType === RiseSetType.SET) {
            show(view, R.id.sunSpecial, if (sunDay.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
            remove(view, R.id.sunEvt1Row, R.id.sunEvt2Row, R.id.sunUptimeRow)
        } else {
            remove(view, R.id.sunSpecial)
            remove(view, R.id.sunEvt1Row, R.id.sunEvt2Row)
            val events: MutableSet<SummaryEvent> = TreeSet()
            sunDay.rise?.let {
                events.add(SummaryEvent("RISE", it, sunDay.riseAzimuth))
            }
            sunDay.set?.let {
                events.add(SummaryEvent("SET", it, sunDay.setAzimuth))
            }
            var index = 1
            for (event in events) {
                val rowId = view("sunEvt${index}Row")
                val labelId = view("sunEvt${index}Label")
                val timeId = view("sunEvt${index}Time")
                val imgId = view("sunEvt${index}Img")
                val time: Time = formatTime(requireContext(), event.time, false)
                text(view, labelId, event.name)
                text(view, timeId, time.time + time.marker.toLowerCase(Locale.getDefault()))
                image(view, imgId, if (event.name == "RISE") getRiseArrow() else getSetArrow())
                show(view, rowId)
                index++
            }
            if (sunDay.uptimeHours > 0 && sunDay.uptimeHours < 24) {
                show(view, R.id.sunUptimeRow)
                show(view, R.id.sunUptimeTime, formatDurationHMS(requireContext(), sunDay.uptimeHours, false))
            } else {
                remove(view, R.id.sunUptimeRow)
            }
        }
        if (moonDay.riseSetType === RiseSetType.RISEN || moonDay.riseSetType === RiseSetType.SET) {
            show(view, R.id.moonSpecial, if (moonDay.riseSetType === RiseSetType.RISEN) "Risen all day" else "Set all day")
            remove(view, R.id.moonEvt1Row, R.id.moonEvt2Row)
        } else {
            remove(view, R.id.moonSpecial)
            remove(view, R.id.moonEvt1Row, R.id.moonEvt2Row)
            val events: MutableSet<SummaryEvent> = TreeSet()
            moonDay.rise?.let {
                events.add(SummaryEvent("RISE", it, moonDay.riseAzimuth))
            }
            moonDay.set?.let {
                events.add(SummaryEvent("SET", it, moonDay.setAzimuth))
            }
            var index = 1
            for (event in events) {
                val rowId = view("moonEvt${index}Row")
                val labelId = view("moonEvt${index}Label")
                val timeId = view("moonEvt${index}Time")
                val imgId = view("moonEvt${index}Img")
                val time: Time = formatTime(requireContext(), event.time, false)
                text(view, labelId, event.name)
                text(view, timeId, time.time + time.marker.toLowerCase(Locale.getDefault()))
                image(view, imgId, if (event.name == "RISE") getRiseArrow() else getSetArrow())
                show(view, rowId)
                index++
            }
        }
        if (moonDay.phaseEvent == null) {
            show(view, R.id.moonPhase, moonDay.phase.shortDisplayName)
        } else {
            val time: Time = formatTime(requireContext(), moonDay.phaseEvent!!.time, false)
            show(view, R.id.moonPhase, moonDay.phase.shortDisplayName + " at " + time.time + time.marker)
        }
        show(view, R.id.moonIllumination, moonDay.illumination.toString() + "%")
    }

}