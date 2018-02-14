package uk.co.sundroid.util.astro.image

import java.util.Calendar
import java.util.LinkedHashMap
import java.util.TreeSet

import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.prefs.SharedPrefsHelper
import uk.co.sundroid.util.astro.Body
import uk.co.sundroid.util.astro.math.BodyPositionCalculator
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.geometry.*
import uk.co.sundroid.util.theme.*
import uk.co.sundroid.util.time.*

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Paint.Cap
import android.graphics.Paint.Style
import android.graphics.Path
import android.graphics.Typeface
import android.view.View


class TrackerImage(val style: TrackerStyle, val context: Context, val location: LatitudeLongitude) {

    companion object {
        val TAG: String = TrackerImage::class.java.name
    }

    class TrackerStyle(val cardinals: Int, val circles: Int, val day: Int, val golden: Int, val night: Int, val nightLine: Int,
                       val bodyRisen: Int, val bodySet: Int, val stroke: Int, val markerStroke: Int, val dash: Array<Float>, val isRadar: Boolean) {

        companion object {

            private val NORMAL_MAP: TrackerStyle = TrackerStyle(
                    Color.argb(255, 0, 0, 0),
                    Color.argb(100, 0, 0, 0),
                    Color.argb(255, 255, 204, 0),
                    Color.argb(255, 255, 168, 0),
                    Color.argb(255, 72, 90, 144),
                    Color.argb(255, 72, 90, 144),
                    Color.argb(255, 255, 204, 0),
                    Color.argb(255, 72, 90, 144),
                    3,
                    2,
                    arrayOf(2f, 2f),
                    false
            )
            private val SATELLITE_MAP: TrackerStyle = TrackerStyle(
                    Color.argb(255, 255, 255, 255),
                    Color.argb(150, 255, 255, 255),
                    Color.argb(255, 255, 222, 107),
                    Color.argb(255, 255, 198, 0),
                    Color.argb(255, 129, 161, 241),
                    Color.argb(255, 129, 161, 241),
                    Color.argb(255, 255, 255, 255),
                    Color.argb(255, 129, 161, 241),
                    3,
                    2,
                    arrayOf(2f, 2f),
                    false
            )

            fun forMode(mode: String, mapMode: String): TrackerStyle {
                if (mode == "radar") {
                    return getTrackerRadarStyle()
                } else if (mapMode == "normal" || mapMode == "terrain") {
                    return NORMAL_MAP
                } else if (mapMode == "satellite" || mapMode == "hybrid") {
                    return SATELLITE_MAP
                }
                return getTrackerRadarStyle()
            }
        }
    }
    
    private val body: Body? = SharedPrefsHelper.getSunTrackerBody(context)
    private val hourMarkers: Boolean = SharedPrefsHelper.getSunTrackerHourMarkers(context)
    private val linearElevation: Boolean = SharedPrefsHelper.getSunTrackerLinearElevation(context)
    private val magneticBearings: Boolean = SharedPrefsHelper.getMagneticBearings(context)
    private var magneticDeclination = 0.0
    
    private var dateCalendar = Calendar.getInstance()
    private var timeCalendar = Calendar.getInstance()
    
    private var dateBitmap: Bitmap? = null
    private var timeBitmap: Bitmap? = null
    
    private var dateBitmapTimestamp = 0L
    private var timeBitmapTimestamp = 0L
    
    private var containerWidth = 0
    private var containerHeight = 0

    fun setDate(dateCalendar: Calendar, timeCalendar: Calendar) {
        this.dateCalendar = clone(dateCalendar)
        this.timeCalendar = clone(timeCalendar)
        this.magneticDeclination = getMagneticDeclination(this.location, this.dateCalendar)
    }
    
    fun setTime(timeCalendar: Calendar) {
        this.timeCalendar = clone(timeCalendar)
    }
    
    fun drawOnCanvas(container: View, canvas: Canvas, left: Float, top: Float) {
        this.containerWidth = container.width
        this.containerHeight = container.height
        val timeBitmap = updateTimeImageIfStale(clone(timeCalendar))
        val dateBitmap = updateDateImageIfStale(clone(dateCalendar))
        val paint = Paint()
        paint.isAntiAlias = true
        paint.isFilterBitmap = true
        canvas.drawBitmap(dateBitmap, left - (dateBitmap.width/2f), top - (dateBitmap.height/2f), paint)
        canvas.drawBitmap(timeBitmap, left - (timeBitmap.width/2f), top - (timeBitmap.height/2f), paint)
    }
    
    fun generate() {
        if (containerWidth == 0 || containerHeight == 0) {
            d(TAG, "Cannot generate, dimensions unknown")
            return
        }
        try {
            updateTimeImageIfStale(clone(timeCalendar))
            updateDateImageIfStale(clone(dateCalendar))
        } catch (t: Throwable) {
            e(TAG, "Generate failed: ", t)
        }
    }
    
    private fun createBitmap(): Bitmap {
        val size = Math.min(containerWidth, containerHeight)
        return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    }
    
    private fun updateTimeImageIfStale(timeCalendar: Calendar): Bitmap {
        val currentBitmap = timeBitmap
        if (currentBitmap != null && timeBitmapTimestamp == timeCalendar.timeInMillis) {
            return currentBitmap
        }
        val bitmap = createBitmap()
        
        val padding = size(17)
        val size = bitmap.width
        val outerRadius = (size - (2 * padding))/2
        val centerX = (size/2f)
        val centerY = (size/2f)

        val canvas = Canvas(bitmap)
        val paint = Paint()

        paint.isAntiAlias = true
        
        if (body == null) {
            
            for (body in Body.values()) {
                
                paint.style = Style.FILL
                
                val position = BodyPositionCalculator.calcPosition(body, location, timeCalendar)
                
                if (position.appElevation >= 0) {
                
                    val apparentRadius = if (linearElevation) (outerRadius - ((Math.abs(position.appElevation)/90.0) * outerRadius)).toFloat() else (Math.cos(degToRad(position.appElevation)) * outerRadius).toFloat()
                    val x = (Math.sin(degToRad(position.azimuth)) * apparentRadius).toFloat()
                    val y = (Math.cos(degToRad(position.azimuth)) * apparentRadius).toFloat()
                    
                    if (!style.isRadar) {
                        paint.color = Color.argb(100, 0, 0, 0)
                        canvas.drawCircle(centerX + x, centerY - y, size(6), paint)
                        
                        val strokePaint = Paint()
                        strokePaint.color = Color.argb(100, 0, 0, 0)
                        strokePaint.textSize = size(14)
                        strokePaint.style = Style.STROKE
                        strokePaint.strokeWidth = size(2)
                        strokePaint.isAntiAlias = true
                        canvas.drawText(body.name.substring(0, 1) + body.name.substring(1).toLowerCase(), centerX + x + size(6), centerY - y + size(5), strokePaint)
                    }
                        
                    paint.strokeWidth = size(style.stroke)
                    paint.color = getBodyColor(body)
                    canvas.drawCircle(centerX + x, centerY - y, size(4), paint)
                    
                    
                    val textPaint = Paint()
                    textPaint.textSize = size(14)
                    textPaint.color = getBodyColor(body)
                    textPaint.isAntiAlias = true
                    canvas.drawText(body.name.substring(0, 1) + body.name.substring(1).toLowerCase(), centerX + x + size(6), centerY - y + size(5), textPaint)
                }
            }

            
        } else {
            
            paint.style = Style.STROKE
            
            val position = BodyPositionCalculator.calcPosition(body, location, timeCalendar)

            val apparentRadius = if (linearElevation) (outerRadius - ((Math.abs(position.appElevation)/90.0) * outerRadius)).toFloat() else (Math.cos(degToRad(position.appElevation)) * outerRadius).toFloat()
            val x = (Math.sin(degToRad(position.azimuth)) * apparentRadius).toFloat()
            val y = (Math.cos(degToRad(position.azimuth)) * apparentRadius).toFloat()
            
            if (!style.isRadar) {
                paint.strokeWidth = size(style.stroke + 1)
                paint.color = Color.argb(100, 0, 0, 0)
                canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint)
                canvas.drawCircle(centerX + x, centerY - y, size(7), paint)
            }
                
            paint.strokeWidth = size(style.stroke)
            val color = getElevationColor(position.appElevation, true)
            paint.color = color
            canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint)
            canvas.drawCircle(centerX + x, centerY - y, size(7), paint)
            
        }
        
        synchronized (this) {
            timeBitmap = bitmap
            timeBitmapTimestamp = timeCalendar.timeInMillis
        }
        return bitmap
    }
    
    private fun updateDateImageIfStale(dateCalendar: Calendar): Bitmap {
        val currentBitmap = dateBitmap
        if (currentBitmap != null && dateBitmapTimestamp == dateCalendar.timeInMillis) {
            return currentBitmap
        }
        val bitmap = createBitmap()
        
        val padding = size(17)
        val size = bitmap.width.toFloat()
        val outerRadius = (size - (2 * padding))/2
        val fontSize = size(14)
        val centerX = (size/2f)
        val centerY = (size/2f)
        
        val canvas = Canvas(bitmap)
        val paint = Paint()
        paint.color = style.circles
        paint.style = Style.STROKE
        paint.isAntiAlias = true
        paint.strokeWidth = 0F

        (0 until 90 step 15)
                .map { if (linearElevation) (outerRadius - ((Math.abs(it)/90.0) * outerRadius)).toFloat() else (Math.cos(degToRad(it.toDouble())) * outerRadius).toFloat() }
                .forEach { canvas.drawCircle(centerX, centerY, it, paint) }
        
        if (magneticBearings) {
            canvas.rotate(magneticDeclination.toFloat(), centerX, centerY)
        }
        
        val textPaint = Paint()
        textPaint.isAntiAlias = true
        textPaint.textSize = fontSize
        textPaint.textAlign = Paint.Align.CENTER
        textPaint.isFakeBoldText = false
        textPaint.typeface = Typeface.DEFAULT
        textPaint.color = style.cardinals
        
        canvas.drawText(if (magneticBearings) "N(M)" else "N(T)", (size/2), size(12), textPaint)
        canvas.drawText("S", size/2, size - size(2), textPaint)
        canvas.drawText("E", size - 10, size/2 + size(5), textPaint)
        canvas.drawText("W", 10F, size/2 + size(5), textPaint)
        
        paint.color = style.circles
        for (az in 0 until 360 step 45) {
            val x = (Math.sin(degToRad(az.toDouble())) * outerRadius).toFloat()
            val y = (Math.cos(degToRad(az.toDouble())) * outerRadius).toFloat()
            canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint)
        }
        
        if (magneticBearings) {
            canvas.rotate(-magneticDeclination.toFloat(), centerX, centerY)
        }
        
        if (body != null) {

            paint.strokeWidth = size(style.stroke)
            
            val loopCalendar = clone(dateCalendar)
            
            var path = Path()

            var position = BodyPositionCalculator.calcPosition(body, location, loopCalendar)
            var apparentRadius = if (linearElevation) (outerRadius - ((Math.abs(position.appElevation)/90.0) * outerRadius)).toFloat() else (Math.cos(degToRad(position.appElevation)) * outerRadius).toFloat()
            var x = (Math.sin(degToRad(position.azimuth)) * apparentRadius).toFloat()
            var y = (Math.cos(degToRad(position.azimuth)) * apparentRadius).toFloat()
            var currentColor = getElevationColor(position.appElevation, false)
            paint.color = currentColor
            path.moveTo(centerX + x, centerY - y)
            loopCalendar.add(Calendar.MINUTE, 10)
            
            // Get rise/set events that happen on this calendar day, midnight to midnight.
            val eventsSet = TreeSet<Event>()
            val dayLoopCalendar = clone(dateCalendar)
            dayLoopCalendar.add(Calendar.DAY_OF_MONTH, -1)
            var riseTime = 0L
            var setTime = 0L
            for (i in 0 until 3) {
                val bodyDay = BodyPositionCalculator.calcDay(body, location, dayLoopCalendar, false)
                val rise = bodyDay.rise
                if (rise != null && isSameDay(rise, dateCalendar) && eventsSet.size < 2) {
                    eventsSet.add(Event(rise, bodyDay.riseAzimuth))
                    riseTime = rise.timeInMillis
                }
                val set = bodyDay.set
                if (set != null && isSameDay(set, dateCalendar) && eventsSet.size < 2) {
                    eventsSet.add(Event(set, bodyDay.setAzimuth))
                    setTime = set.timeInMillis
                }
                dayLoopCalendar.add(Calendar.DAY_OF_MONTH, 1)
            }
            
            // Determine calculated times in advance so sunrise and sunset can be inserted in order.
            val calcTimes = TreeSet<Long>()
            do {
                loopCalendar.add(Calendar.MINUTE, 10)
                calcTimes.add(loopCalendar.timeInMillis)
            } while (loopCalendar.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR))
            eventsSet.mapTo(calcTimes) { it.time.timeInMillis }
            
            var prevX = x
            var prevY = y
            
            val paths = LinkedHashMap<Path, Int>()
            
            for (calcTime in calcTimes) {
                loopCalendar.timeInMillis = calcTime
                position = BodyPositionCalculator.calcPosition(body, location, loopCalendar)
                apparentRadius = if (linearElevation) (outerRadius - ((Math.abs(position.appElevation)/90.0) * outerRadius)).toFloat() else (Math.cos(degToRad(position.appElevation)) * outerRadius).toFloat()
                x = (Math.sin(degToRad(position.azimuth)) * apparentRadius).toFloat()
                y = (Math.cos(degToRad(position.azimuth)) * apparentRadius).toFloat()
                
                var thisColor = getElevationColor(position.appElevation, false)
                if (calcTime == riseTime) {
                    thisColor = if (body == Body.SUN) style.golden else style.bodyRisen
                } else if (calcTime == setTime) {
                    thisColor = if (body == Body.SUN) style.night else style.bodySet
                }
                
                if (loopCalendar.get(Calendar.MINUTE) == 0 && hourMarkers) {
                    // Draw lines across the path at a tangent to the line from the previous
                    // point. Could improve slightly by averaging next point as well.
                    val dx = x - prevX
                    val dy = y - prevY
                    val angle = Math.atan(dy.toDouble()/dx.toDouble())
                    val inverse = (Math.PI/2) - angle
                    val markY = (size(5) * Math.sin(inverse)).toFloat()
                    val markX = (size(5) * Math.cos(inverse)).toFloat()
                    
                    val cap = paint.strokeCap
                    
                    paint.strokeCap = Cap.ROUND
                    if (!style.isRadar) {
                        val color = paint.color
                        paint.color = Color.argb(100, 0, 0, 0)
                        paint.strokeWidth = size(style.markerStroke + 2)
                        canvas.drawLine((centerX + x) + markX, (centerY - y) + markY, (centerX + x) - markX, (centerY - y) - markY, paint)
                        paint.color = color
                    }
    
                    paint.strokeWidth = size(style.markerStroke)
                    canvas.drawLine((centerX + x) + markX, (centerY - y) + markY, (centerX + x) - markX, (centerY - y) - markY, paint)
                    paint.strokeWidth = size(style.stroke)
                    
                    paint.strokeCap = cap
                    
                }
                
                if (thisColor != currentColor) {
                    path.lineTo(centerX + x, centerY - y)
                    canvas.drawPath(path, paint)
                    paths[path] = currentColor
                    currentColor = thisColor
                    paint.color = currentColor
                    path = Path()
                    path.moveTo(centerX + x, centerY - y)
                } else {
                    path.lineTo(centerX + x, centerY - y)
                }
                
                prevX = x
                prevY = y
            }

            paths[path] = currentColor
            
            if (!style.isRadar) {
                paint.strokeWidth = size(style.stroke + 1)
                paint.color = Color.argb(100, 0, 0, 0)
                paths.keys.forEach { canvas.drawPath(it, paint) }
            }
            
            paint.strokeWidth = size(style.stroke)
            paths.forEach { (path, color) ->
                paint.color = color
                canvas.drawPath(path, paint)
            }
    
            // Draw dotted lines for rise and set.
            
            paint.pathEffect = DashPathEffect(style.dash.toFloatArray(), 0F)
            
            for (event in eventsSet) {
                x = (Math.sin(degToRad(event.azimuth)) * outerRadius).toFloat()
                y = (Math.cos(degToRad(event.azimuth)) * outerRadius).toFloat()
                
                if (!style.isRadar) {
                    paint.pathEffect = null
                    paint.strokeWidth = size(style.stroke + 1)
                    paint.color = Color.argb(50, 0, 0, 0)
                    canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint)
                }
                
                paint.pathEffect = DashPathEffect(style.dash.toFloatArray(), 0F)
                paint.strokeWidth = size(style.stroke)
                paint.color = if (body == Body.SUN) style.golden else style.bodyRisen
                canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint)
            }
            
        }

        synchronized (this) {
            dateBitmap = bitmap
            dateBitmapTimestamp = dateCalendar.timeInMillis
        }
        return bitmap
        
    }

    fun size(size: Int): Float {
        val metrics = context.resources.displayMetrics
        return ((metrics.densityDpi/160.0) * size).toInt().toFloat()
    }
    
    private fun getElevationColor(elevation: Double, line: Boolean): Int {
        when (body) {
            Body.SUN -> {
                if (elevation >= 6) {
                    return style.day
                } else if (elevation >= -0.833) {
                    return style.golden
                }
                return if (line) style.nightLine else style.night
            }
            Body.MOON -> {
                if (elevation >= -0.5) {
                    return style.bodyRisen
                }
                return style.bodySet
            }
            else -> {
                if (elevation >= 0.0) {
                    return style.bodyRisen
                }
                return style.bodySet
            }
        }
    }
    
    private fun degToRad(angleDeg: Double): Double {
        return (Math.PI * angleDeg / 180.0)
    }

    private class Event(val time: Calendar, val azimuth: Double) : Comparable<Event> {
        override fun compareTo(other: Event): Int {
            val result = time.compareTo(other.time)
            if (result == 0) {
                return 1
            }
            return result
        }
    }
    
}