package uk.co.sundroid.util.time

import uk.co.sundroid.util.zeroPad

class Clock(hours: Double, private val showSeconds: Boolean) {

    private val h: Int
    private val m: Int
    private val s: Int

    fun toHMS(html: Boolean = false): String {
        val hs = if (html) "<small>H</small>" else "h"
        val ms = if (html) "<small>M</small>" else "m"
        val ss = if (html) "<small>S</small>" else "s"
        return if (showSeconds) {
            "$h$hs ${zeroPad(m, 2)}$ms ${zeroPad(s, 2)}$ss"
        } else {
            "$h$hs ${zeroPad(m, 2)}$ms"
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