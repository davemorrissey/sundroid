package uk.co.sundroid.util.time

import uk.co.sundroid.util.zeroPad

class Clock(hours: Double, private val showSeconds: Boolean) {

    private val h: Int
    private val m: Int
    private val s: Int

    fun toHMS(): String {
        return if (showSeconds) {
            h.toString() + "h " + zeroPad(m, 2) + "m " + zeroPad(s, 2) + "s"
        } else {
            h.toString() + "h " + zeroPad(m, 2) + "m"
        }
    }

    fun toClock(): String {
        return if (showSeconds) {
            h.toString() + ":" + zeroPad(m, 2) + ":" + zeroPad(s, 2)
        } else {
            h.toString() + ":" + zeroPad(m, 2)
        }
    }

    init {
        var h = Math.floor(hours).toInt()
        var m = Math.floor((hours - h) * 60.0).toInt()
        var s = 0
        if (showSeconds) {
            s = Math.round((hours - h - (m / 60.0)) * 3600.0).toInt()
            if (s >= 60) {
                s -= 60
                m++
            }
        }
        if (m >= 60) {
            m -= 60
            h++
        }
        this.h = h
        this.m = m
        this.s = s
    }

}