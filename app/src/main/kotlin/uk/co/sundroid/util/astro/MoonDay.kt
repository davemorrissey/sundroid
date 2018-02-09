package uk.co.sundroid.util.astro


/**
 * Wrapper for lunar location and phase information at a given time (or for the date)
 * and at a specified location.
 */
class MoonDay : BodyDay() {

    /** Phase at noon.  */
    /**
     * @return Returns the phase.
     */
    /**
     * @param phase The phase to set.
     */
    var phase: MoonPhase? = null

    /** Exact phase at noon as a double (0 = new, 0.5 = full).  */
    var phaseDouble: Double = 0.toDouble()

    /** Illumination percentage.  */
    var illumination: Int = 0

    /** Phase event (new, full, FQ, LQ) occurring on this day.  */
    var phaseEvent: MoonPhaseEvent? = null

}
