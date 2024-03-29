package uk.co.sundroid.activity.data.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.FragDataEphemerisBinding
import uk.co.sundroid.databinding.FragDataEphemerisBodyBinding
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.BodyDayEvent
import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.astro.SunDay
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.util.astro.math.SunCalculator
import uk.co.sundroid.util.astro.smc.Calculator
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.time.formatTimeStr
import uk.co.sundroid.util.time.shortDateAndMonth
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class EphemerisFragment : AbstractTimeFragment() {

    private lateinit var b: FragDataEphemerisBinding

    override val layout: Int
        get() = R.layout.frag_data_ephemeris

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
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
        val smcSunDay = Calculator.getDay(Body.SUN, dateCalendar, location.location) as SunDay
        val sunPosition = SunCalculator.calcPosition(location.location, timeCalendar)
        val smcSunPosition = Calculator.getPosition(Body.SUN, timeCalendar, location.location)
        val moonDay = BodyPositionCalculator.calcDay(Body.MOON, location.location, dateCalendar, true)
        val smcMoonDay = Calculator.getDay(Body.MOON, dateCalendar, location.location)
        val moonPosition = BodyPositionCalculator.calcPosition(Body.MOON, location.location, timeCalendar)
        val smcMoonPosition = Calculator.getPosition(Body.MOON, timeCalendar, location.location)
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
            b.sunRise1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), sunDay.riseAzimuth, location.location, timeCalendar)
        } ?: run {
            b.sunRise1.text = "-"
        }
        sunDay.set?.let {
            b.sunSet1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), sunDay.setAzimuth, location.location, timeCalendar)
        } ?: run {
            b.sunSet1.text = "-"
        }
        sunDay.transit?.let {
            b.sunTransit1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), sunDay.transitAppElevation, location.location, timeCalendar)
        } ?: run {
            b.sunTransit1.text = "-"
        }
        smcSunDay.rise?.let {
            b.sunRise2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcSunDay.riseAzimuth, location.location, timeCalendar)
        } ?: run {
            b.sunRise2.text = "-"
        }
        smcSunDay.set?.let {
            b.sunSet2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcSunDay.setAzimuth, location.location, timeCalendar)
        } ?: run {
            b.sunSet2.text = "-"
        }
        smcSunDay.transit?.let {
            b.sunTransit2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcSunDay.transitAppElevation, location.location, timeCalendar)
        } ?: run {
            b.sunTransit2.text = "-"
        }

        val twilights1 = ArrayList<String>()
        sunDay.astDawn?.let { twilights1.add("AR " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.ntcDawn?.let { twilights1.add("NR " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.civDawn?.let { twilights1.add("CR " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.rise?.let { twilights1.add("R " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.ghEnd?.let { twilights1.add("GHE " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.transit?.let { twilights1.add("TX " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.ghStart?.let { twilights1.add("GHS " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.set?.let { twilights1.add("S " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.civDusk?.let { twilights1.add("CS " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.ntcDusk?.let { twilights1.add("NS " + shortDateAndMonth(it) + " " + time(it)) }
        sunDay.astDusk?.let { twilights1.add("AS " + shortDateAndMonth(it) + " " + time(it)) }
        b.sunEvents1.text = twilights1.joinToString(separator = "\n")

        val twilights2 = ArrayList<String>()
        smcSunDay.astDawn?.let { twilights2.add("AR " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.ntcDawn?.let { twilights2.add("NR " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.civDawn?.let { twilights2.add("CR " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.rise?.let { twilights2.add("R " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.ghEnd?.let { twilights2.add("GHE " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.transit?.let { twilights2.add("TX " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.ghStart?.let { twilights2.add("GHS " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.set?.let { twilights2.add("S " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.civDusk?.let { twilights2.add("CS " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.ntcDusk?.let { twilights2.add("NS " + shortDateAndMonth(it) + " " + time(it)) }
        smcSunDay.astDusk?.let { twilights2.add("AS " + shortDateAndMonth(it) + " " + time(it)) }
        b.sunEvents2.text = twilights2.joinToString(separator = "\n")

        b.sunType1.text = sunDay.riseSetType?.name ?: "null"
        b.sunType2.text = smcSunDay.type(BodyDayEvent.Event.RISESET)?.name ?: "null"
        b.sunLength1.text = trimDouble(sunDay.uptimeHours)
        b.sunLength2.text = trimDouble(smcSunDay.length())
        b.sunJd1.text = "" + sunPosition.julianDay
        b.sunJd2.text = "" + smcSunPosition.julianDay
        b.sunAppEl1.text = trimDouble(sunPosition.appElevation)
        b.sunAppEl2.text = trimDouble(smcSunPosition.appElevation)
        b.sunAz1.text = trimDouble(sunPosition.azimuth)
        b.sunAz2.text = trimDouble(smcSunPosition.azimuth)

        moonDay.rise?.let {
            b.moonRise1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), moonDay.riseAzimuth, location.location, timeCalendar)
        } ?: run {
            b.moonRise1.text = "-"
        }
        moonDay.set?.let {
            b.moonSet1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), moonDay.setAzimuth, location.location, timeCalendar)
        } ?: run {
            b.moonSet1.text = "-"
        }
        moonDay.transit?.let {
            b.moonTransit1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), moonDay.transitAppElevation, location.location, timeCalendar)
        } ?: run {
            b.moonTransit1.text = "-"
        }
        smcMoonDay.rise?.let {
            b.moonRise2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcMoonDay.riseAzimuth, location.location, timeCalendar)
        } ?: run {
            b.moonRise2.text = "-"
        }
        smcMoonDay.set?.let {
            b.moonSet2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcMoonDay.setAzimuth, location.location, timeCalendar)
        } ?: run {
            b.moonSet2.text = "-"
        }
        smcMoonDay.transit?.let {
            b.moonTransit2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcMoonDay.transitAppElevation, location.location, timeCalendar)
        } ?: run {
            b.moonTransit2.text = "-"
        }

        val moonEvents1 = ArrayList<String>()
        moonDay.events.forEach {
            moonEvents1.add(it.direction.name.substring(0..1) + " " + shortDateAndMonth(it.time) + " " + time(it.time))
        }
        b.moonEvents1.text = moonEvents1.joinToString(separator = "\n")

        val moonEvents2 = ArrayList<String>()
        smcMoonDay.events.forEach {
            moonEvents2.add(it.direction.name.substring(0..1) + " " + shortDateAndMonth(it.time) + " " + time(it.time))
        }
        b.moonEvents2.text = moonEvents2.joinToString(separator = "\n")

        b.moonType1.text = moonDay.riseSetType?.name ?: "null"
        b.moonType2.text = smcMoonDay.riseSetType?.name ?: "null"
        b.moonLength1.text = trimDouble(moonDay.uptimeHours)
        b.moonLength2.text = trimDouble(smcMoonDay.uptimeHours)
        b.moonJd1.text = "" + moonPosition.julianDay
        b.moonJd2.text = "" + smcMoonPosition.julianDay
        b.moonAppEl1.text = trimDouble(moonPosition.appElevation)
        b.moonAppEl2.text = trimDouble(smcMoonPosition.appElevation)
        b.moonTrueEl1.text = trimDouble(moonPosition.trueElevation)
        b.moonTrueEl2.text = trimDouble(smcMoonPosition.trueElevation)
        b.moonAz1.text = trimDouble(moonPosition.azimuth)
        b.moonAz2.text = trimDouble(smcMoonPosition.azimuth)
        b.moonGeoRA1.text = trimDouble(moonPosition.geoRA) + "\n" +
                "Hours = " + trimDouble(moonPosition.geoRA / 15.0) + "\n" +
                "HMS = " + arc(moonPosition.geoRA/15.0)
        b.moonGeoRA2.text = trimDouble(smcMoonPosition.geoRA) + "\n" +
                "Hours = " + trimDouble(smcMoonPosition.geoRA / 15.0) + "\n" +
                "HMS = " + arc(smcMoonPosition.geoRA/15.0)
        b.moonGeoDec1.text = trimDouble(moonPosition.geoDec) + "\n" +
                "HMS = " + arc(moonPosition.geoDec)
        b.moonGeoDec2.text = trimDouble(smcMoonPosition.geoDec) + "\n" +
                "HMS = " + arc(smcMoonPosition.geoDec)
        b.moonGeoDist1.text = trimDouble(moonPosition.geoDistEarthRadii) + "ER\n" + trimDouble(moonPosition.geoDistKm) + "Km"
        b.moonGeoDist2.text = trimDouble(smcMoonPosition.geoDistEarthRadii) + "ER\n" + trimDouble(smcMoonPosition.geoDistKm) + "Km"
        b.moonTopoRA1.text = trimDouble(moonPosition.topoRA)+ "\n" +
                "HMS = " + arc(moonPosition.topoRA/15.0)
        b.moonTopoRA2.text = trimDouble(smcMoonPosition.topoRA)+ "\n" +
                "HMS = " + arc(smcMoonPosition.topoRA/15.0)
        b.moonTopoDec1.text = trimDouble(moonPosition.topoDec) + "\n" +
                "HMS = " + arc(moonPosition.topoDec)
        b.moonTopoDec2.text = trimDouble(smcMoonPosition.topoDec) + "\n" +
                "HMS = " + arc(smcMoonPosition.topoDec)
        b.moonTopoDist1.text = trimDouble(moonPosition.topoDistEarthRadii) + "ER\n" + trimDouble(moonPosition.topoDistKm) + "Km"
        b.moonTopoDist2.text = trimDouble(smcMoonPosition.topoDistEarthRadii) + "ER\n" + trimDouble(smcMoonPosition.topoDistKm) + "Km"
        b.moonAge2.text = trimDouble(smcMoonPosition.moonAge)
        b.moonPhase1.text = trimDouble(noonPhase) + " (Noon)"
        b.moonPhase2.text = trimDouble(smcMoonPosition.moonPhase)
        b.moonIllumination1.text = "${noonIllumination}% (Noon)"
        b.moonIllumination2.text = trimDouble(smcMoonPosition.moonIllumination)
        b.moonNextFull1.text = shortDateAndMonth(phaseEvents.first().time) + " " + time(phaseEvents.first().time)

        b.planets.removeAllViews()
        Body.values().filter { it != Body.SUN && it != Body.MOON }.forEach { body ->
            val bodyDay = BodyPositionCalculator.calcDay(body, location.location, dateCalendar, true)
            val smcBodyDay = Calculator.getDay(body, dateCalendar, location.location)
            val bodyPosition = BodyPositionCalculator.calcPosition(body, location.location, timeCalendar)
            val smcBodyPosition = Calculator.getPosition(body, timeCalendar, location.location)
            val bb = FragDataEphemerisBodyBinding.inflate(layoutInflater)
            bb.bodyName.text = body.name
            bodyDay.rise?.let {
                bb.bodyRise1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), bodyDay.riseAzimuth, location.location, timeCalendar)
            } ?: run {
                bb.bodyRise1.text = "-"
            }
            bodyDay.set?.let {
                bb.bodySet1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), bodyDay.setAzimuth, location.location, timeCalendar)
            } ?: run {
                bb.bodySet1.text = "-"
            }
            bodyDay.transit?.let {
                bb.bodyTransit1.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), bodyDay.transitAppElevation, location.location, timeCalendar)
            } ?: run {
                bb.bodyTransit1.text = "-"
            }
            smcBodyDay.rise?.let {
                bb.bodyRise2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcBodyDay.riseAzimuth, location.location, timeCalendar)
            } ?: run {
                bb.bodyRise2.text = "-"
            }
            smcBodyDay.set?.let {
                bb.bodySet2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcBodyDay.setAzimuth, location.location, timeCalendar)
            } ?: run {
                bb.bodySet2.text = "-"
            }
            smcBodyDay.transit?.let {
                bb.bodyTransit2.text = shortDateAndMonth(it) + " " + time(it) + " - " + formatBearing(requireContext(), smcBodyDay.transitAppElevation, location.location, timeCalendar)
            } ?: run {
                bb.bodyTransit2.text = "-"
            }

            val bodyEvents1 = ArrayList<String>()
            bodyDay.events.forEach {
                bodyEvents1.add(it.direction.name.substring(0..1) + " " + shortDateAndMonth(it.time) + " " + time(it.time))
            }
            bb.bodyEvents1.text = bodyEvents1.joinToString(separator = "\n")

            val bodyEvents2 = ArrayList<String>()
            smcBodyDay.events.forEach {
                bodyEvents2.add(it.direction.name.substring(0..1) + " " + shortDateAndMonth(it.time) + " " + time(it.time))
            }
            bb.bodyEvents2.text = bodyEvents2.joinToString(separator = "\n")

            bb.bodyType1.text = bodyDay.riseSetType?.name ?: "null"
            bb.bodyType2.text = smcBodyDay.riseSetType?.name ?: "null"
            bb.bodyLength1.text = trimDouble(bodyDay.uptimeHours)
            bb.bodyLength2.text = trimDouble(smcBodyDay.uptimeHours)
            bb.bodyAppEl1.text = trimDouble(bodyPosition.appElevation)
            bb.bodyAppEl2.text = trimDouble(smcBodyPosition.appElevation)
            bb.bodyAz1.text = trimDouble(bodyPosition.azimuth)
            bb.bodyAz2.text = trimDouble(smcBodyPosition.azimuth)
            b.planets.addView(bb.root)
        }

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
