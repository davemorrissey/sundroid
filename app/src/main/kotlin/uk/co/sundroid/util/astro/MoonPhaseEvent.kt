package uk.co.sundroid.util.astro

import java.util.Calendar

class MoonPhaseEvent(
        /** Phase. One of full, new, FQ, LQ.  */
        val phase: MoonPhase,
        /** Time the event occurs.  */
        val time: Calendar) {

    val phaseDouble: Double
        get() = if (phase == MoonPhase.NEW) {
            0.0
        } else if (phase == MoonPhase.FIRST_QUARTER) {
            0.25
        } else if (phase == MoonPhase.FULL) {
            0.5
        } else {
            0.75
        }

}