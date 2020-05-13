package uk.co.sundroid.activity.data.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.FragDataEphemerisBinding
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.time.formatTimeStr
import uk.co.sundroid.util.time.shortDateAndMonth
import java.math.BigDecimal
import java.util.*

class EphemerisFragment : AbstractTimeFragment() {

    private lateinit var b: FragDataEphemerisBinding

    override val layout: Int
        get() = R.layout.frag_data_ephemeris

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        b = FragDataEphemerisBinding.inflate(inflater)
        return b.root
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarSubtitle("Ephemeris")
        }
    }

    @SuppressLint("SetTextI18n")
    override fun updateData(view: View, timeOnly: Boolean) {
        val location = getLocation()
        val dateCalendar = getDateCalendar()
        val timeCalendar = getTimeCalendar()

        b.location.text = location.location.getAbbreviatedValue() + "\n" +
                location.location.getPunctuatedValue(Accuracy.SECONDS) + "\n" +
                trimDouble(location.location.latitude.doubleValue) + " " + trimDouble(location.location.longitude.doubleValue)
        b.time.text = "" + timeCalendar.timeInMillis + "\n" + shortDateAndMonth(timeCalendar) + " " + formatTimeStr(requireContext(), timeCalendar, allowSeconds = true)
        b.zone.text = dateCalendar.timeZone.id + "\n" + dateCalendar.timeZone.displayName


        val sunDay = SunCalculator.calcDay(location.location, dateCalendar)
        val sunPosition = SunCalculator.calcPosition(location.location, timeCalendar)
        val moonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, dateCalendar, true)
        val moonPosition = BodyPositionCalculator.calcPosition(Body.MOON, location.location, timeCalendar)
        val noonPhase = MoonPhaseCalculator.getNoonPhase(dateCalendar)
        val noonIllumination = MoonPhaseCalculator.getIlluminatedPercent(noonPhase)

        val phaseEvents = MoonPhaseCalculator.getYearEvents(dateCalendar.get(Calendar.YEAR), dateCalendar.timeZone)
                .filter { it.time.get(Calendar.DAY_OF_YEAR) >= dateCalendar.get(Calendar.DAY_OF_YEAR) }
                .filter { it.phase == MoonPhase.FULL }
                .toMutableList()
        if (phaseEvents.size < 1) {
            phaseEvents.addAll(MoonPhaseCalculator.getYearEvents(dateCalendar.get(Calendar.YEAR) + 1, dateCalendar.timeZone)
                    .filter { it.phase == MoonPhase.FULL }
            )
        }

        sunDay.rise?.let {
            b.sunRise1.text = time(it) + " - " + formatBearing(requireContext(), sunDay.riseAzimuth, location.location, timeCalendar)
        } ?: run {
            b.sunRise1.text = "-"
        }
        sunDay.set?.let {
            b.sunSet1.text = time(it) + " - " + formatBearing(requireContext(), sunDay.setAzimuth, location.location, timeCalendar)
        } ?: run {
            b.sunSet1.text = "-"
        }

        b.sunJd1.text = "" + sunPosition.julianDay
        b.sunAppEl1.text = trimDouble(sunPosition.appElevation)
        b.sunAz1.text = trimDouble(sunPosition.azimuth)

        moonDay.rise?.let {
            b.moonRise1.text = time(it) + " - " + formatBearing(requireContext(), moonDay.riseAzimuth, location.location, timeCalendar)
        } ?: run {
            b.moonRise1.text = "-"
        }
        moonDay.set?.let {
            b.moonSet1.text = time(it) + " - " + formatBearing(requireContext(), moonDay.setAzimuth, location.location, timeCalendar)
        } ?: run {
            b.moonSet1.text = "-"
        }

        b.moonJd1.text = "" + moonPosition.julianDay
        b.moonAppEl1.text = trimDouble(moonPosition.appElevation)
        b.moonTrueEl1.text = trimDouble(moonPosition.trueElevation)
        b.moonAz1.text = trimDouble(moonPosition.azimuth)
        b.moonGeoRA1.text = trimDouble(moonPosition.geoRA) + "\n" +
                "Hours = " + trimDouble(moonPosition.geoRA / 15.0) + "\n" +
                "HMS = " + arc(moonPosition.geoRA/15.0)
        b.moonGeoDec1.text = trimDouble(moonPosition.geoDec) + "\n" +
                "HMS = " + arc(moonPosition.geoDec)
        b.moonGeoDist1.text = trimDouble(moonPosition.geoDistEarthRadii)
        b.moonTopoRA1.text = trimDouble(moonPosition.topoRA)+ "\n" +
                "HMS = " + arc(moonPosition.topoRA/15.0)
        b.moonTopoDec1.text = trimDouble(moonPosition.topoDec) + "\n" +
                "HMS = " + arc(moonPosition.topoDec)
        b.moonTopoDist1.text = trimDouble(moonPosition.topoDistEarthRadii)
        b.moonPhase1.text = trimDouble(noonPhase) + " (Noon)"
        b.moonIllumination1.text = "${noonIllumination}% (Noon)"
        b.moonNextFull1.text = shortDateAndMonth(phaseEvents.first().time) + " " + time(phaseEvents.first().time)

    }

    private fun arc(value: Double): String {
        return displayArcValue(Angle(value), Accuracy.SECONDS, Punctuation.STANDARD)
    }

    private fun time(calendar: Calendar): String {
        return formatTimeStr(requireContext(), calendar, allowSeconds = true)
    }

    private fun trimDouble(d: Double): String {
        if (d.isInfinite()) {
            return "INFINITY"
        } else if (d.isNaN()) {
            return "NaN"
        }
        return BigDecimal(d).setScale(4, BigDecimal.ROUND_HALF_DOWN).toString()
    }

}
