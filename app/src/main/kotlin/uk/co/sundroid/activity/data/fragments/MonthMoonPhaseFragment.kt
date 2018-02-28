package uk.co.sundroid.activity.data.fragments

import android.os.Handler
import android.view.View
import android.widget.ImageView
import android.widget.TableRow
import android.widget.TextView
import uk.co.sundroid.R
import uk.co.sundroid.R.id
import uk.co.sundroid.R.layout
import uk.co.sundroid.util.astro.MoonPhase
import uk.co.sundroid.util.astro.MoonPhaseEvent
import uk.co.sundroid.util.astro.math.MoonPhaseCalculator
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.theme.*

import java.math.BigDecimal
import java.util.Calendar

class MonthMoonPhaseFragment : AbstractMonthFragment<Any>() {

    private val handler = Handler()

    protected override val layout: Int
        get() = R.layout.frag_data_monthmoonphase

    @Throws(Exception::class)
    override fun update(location: LocationDetails, calendar: Calendar, view: View) {

        val thread = object : Thread() {
            override fun run() {
                if (!isSafe) {
                    return
                }

                handler.post {
                    if (isSafe) {
                        val packageName = applicationContext!!.packageName

                        // Set column headers according to weekday preference.
                        for (day in 1..7) {
                            val dayId = view("moonCalD" + day)
                            var altDay = day + SharedPrefsHelper.getFirstWeekday(applicationContext!!) - 1
                            if (altDay > 7) {
                                altDay -= 7
                            }
                            when (altDay) {
                                1 -> showInView(view, dayId, "Sun")
                                2 -> showInView(view, dayId, "Mon")
                                3 -> showInView(view, dayId, "Tue")
                                4 -> showInView(view, dayId, "Wed")
                                5 -> showInView(view, dayId, "Thu")
                                6 -> showInView(view, dayId, "Fri")
                                7 -> showInView(view, dayId, "Sat")
                            }
                        }

                        // Wipe all days.
                        for (row in 1..6) {
                            val datesRowId = view("moonCalDates" + row)
                            (view.findViewById<View>(datesRowId) as TableRow).removeAllViews()
                            val imagesRowId = view("moonCalImages" + row)
                            (view.findViewById<View>(imagesRowId) as TableRow).removeAllViews()
                        }

                        val loopCalendar = Calendar.getInstance()
                        loopCalendar.timeZone = calendar.timeZone
                        loopCalendar.timeInMillis = calendar.timeInMillis
                        loopCalendar.set(Calendar.DAY_OF_MONTH, 1)
                        val month = loopCalendar.get(Calendar.MONTH)
                        var row = 1

                        val todayCalendar = Calendar.getInstance(calendar.timeZone)

                        // Add empty cells to the first row.
                        var firstCol = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - SharedPrefsHelper.getFirstWeekday(applicationContext!!)) + 1
                        if (firstCol > 7) {
                            firstCol -= 7
                        }
                        for (i in 1 until firstCol) {
                            val datesRow = view.findViewById<TableRow>(R.id.moonCalDates1)
                            val dateCell = activity.layoutInflater.inflate(R.layout.frag_data_monthmoonphase_date, datesRow, false)
                            dateCell.setVisibility(View.INVISIBLE)
                            datesRow.addView(dateCell)
                            val imagesRow = view.findViewById<TableRow>(R.id.moonCalImages1)
                            val imageCell = activity.layoutInflater.inflate(R.layout.frag_data_monthmoonphase_image, imagesRow, false)
                            imageCell.setVisibility(View.INVISIBLE)
                            imagesRow.addView(imageCell)
                        }

                        val phaseEvents = MoonPhaseCalculator.getYearEvents(calendar.get(Calendar.YEAR), calendar.timeZone)

                        run {
                            var i = 0
                            while (i < 32 && loopCalendar.get(Calendar.MONTH) == month) {
                                var col = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - SharedPrefsHelper.getFirstWeekday(applicationContext!!)) + 1
                                if (col > 7) {
                                    col -= 7
                                }

                                val today = todayCalendar.get(Calendar.YEAR) == loopCalendar.get(Calendar.YEAR) &&
                                        todayCalendar.get(Calendar.MONTH) == loopCalendar.get(Calendar.MONTH) &&
                                        todayCalendar.get(Calendar.DAY_OF_MONTH) == loopCalendar.get(Calendar.DAY_OF_MONTH)

                                val datesRow = view.findViewById<TableRow>(view("moonCalDates" + row))
                                val imagesRow = view.findViewById<TableRow>(view("moonCalImages" + row))

                                val dateCell = activity.layoutInflater.inflate(R.layout.frag_data_monthmoonphase_date, datesRow, false)
                                (dateCell.findViewById(R.id.moonCalTitleText) as TextView).text = Integer.toString(loopCalendar.get(Calendar.DAY_OF_MONTH))

                                val phaseEvent = MoonPhaseCalculator.getDayEvent(loopCalendar, phaseEvents)
                                if (phaseEvent != null) {
                                    var phaseImg = getPhaseFull()
                                    if (phaseEvent.phase === MoonPhase.NEW) {
                                        phaseImg = getPhaseNew()
                                    } else if (phaseEvent.phase === MoonPhase.FIRST_QUARTER) {
                                        phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseRight() else getPhaseLeft()
                                    } else if (phaseEvent.phase === MoonPhase.LAST_QUARTER) {
                                        phaseImg = if (location.location.latitude.doubleValue >= 0) getPhaseLeft() else getPhaseRight()
                                    }
                                    (dateCell.findViewById(R.id.moonCalTitlePhase) as ImageView).setImageResource(phaseImg)
                                    dateCell.findViewById<View>(R.id.moonCalTitlePhase).setVisibility(View.VISIBLE)
                                } else {
                                    dateCell.findViewById<View>(R.id.moonCalTitlePhase).setVisibility(View.GONE)
                                }

                                datesRow.addView(dateCell)

                                val imageCell = activity.layoutInflater.inflate(R.layout.frag_data_monthmoonphase_image, imagesRow, false)

                                val phaseDbl = MoonPhaseCalculator.getNoonPhase(loopCalendar) / 2
                                var phaseBd = BigDecimal(phaseDbl)
                                phaseBd = phaseBd.setScale(2, BigDecimal.ROUND_HALF_DOWN)
                                phaseBd = phaseBd.multiply(BigDecimal(2))
                                if (location.location.latitude.doubleValue < 0) {
                                    phaseBd = BigDecimal(1).subtract(phaseBd)
                                }
                                val moonImg = phaseBd.toString().replace("\\.".toRegex(), "")

                                (imageCell.findViewById(R.id.moonCalImageOverlay) as ImageView).setImageResource(resources.getIdentifier(packageName + ":drawable/moonoverlay" + moonImg, null, null))

                                imagesRow.addView(imageCell)

                                if (today) {
                                    dateCell.setBackgroundColor(getCalendarHighlightColor())
                                    imageCell.setBackgroundColor(getCalendarHighlightColor())
                                } else {
                                    dateCell.setBackgroundColor(getCalendarHeaderColor())
                                    imageCell.setBackgroundColor(getCalendarDefaultColor())
                                }

                                loopCalendar.add(Calendar.DAY_OF_MONTH, 1)
                                if (loopCalendar.get(Calendar.MONTH) == month && col == 7) {
                                    row++
                                }
                                i++
                            }
                        }

                        // Fill out any remaining cells in the last row.
                        loopCalendar.add(Calendar.DAY_OF_MONTH, -1)
                        var lastCol = loopCalendar.get(Calendar.DAY_OF_WEEK) + (7 - SharedPrefsHelper.getFirstWeekday(applicationContext!!)) + 1
                        if (lastCol > 7) {
                            lastCol -= 7
                        }
                        for (i in lastCol + 1..7) {
                            val datesRow = view.findViewById<TableRow>(view("moonCalDates" + row))
                            val dateCell = activity.layoutInflater.inflate(R.layout.frag_data_monthmoonphase_date, datesRow, false)
                            dateCell.setVisibility(View.INVISIBLE)
                            datesRow.addView(dateCell)
                            val imagesRow = view.findViewById<TableRow>(view("moonCalImages" + row))
                            val imageCell = activity.layoutInflater.inflate(R.layout.frag_data_monthmoonphase_image, imagesRow, false)
                            imageCell.setVisibility(View.INVISIBLE)
                            imagesRow.addView(imageCell)
                        }
                    }

                }
            }
        }
        thread.start()
    }

    override fun calculate(location: LocationDetails, calendar: Calendar): Any {
        return "NOT IMPLEMENTED"
    }
}