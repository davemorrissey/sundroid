package uk.co.sundroid.util.astro

import java.util.Calendar

class MoonPhaseEvent(val phase: MoonPhase, val time: Calendar) {

    val phaseDouble: Double
        get() = when (phase) {
            MoonPhase.NEW -> 0.0
            MoonPhase.FIRST_QUARTER -> 0.25
            MoonPhase.FULL -> 0.5
            else -> 0.75
        }

}
