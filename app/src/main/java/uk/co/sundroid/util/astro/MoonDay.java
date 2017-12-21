package uk.co.sundroid.util.astro;


/**
 * Wrapper for lunar location and phase information at a given time (or for the date)
 * and at a specified location.
 */
public class MoonDay extends BodyDay {
	
	/** Phase at noon. */
	private MoonPhase phase;
	
	/** Exact phase at noon as a double (0 = new, 0.5 = full). */
	private double phaseDouble;
	
	/** Illumination percentage. */
	private int illumination;
	
	/** Phase event (new, full, FQ, LQ) occurring on this day. */
	private MoonPhaseEvent phaseEvent;
	
	/**
	 * @return Returns the phase.
	 */
	public MoonPhase getPhase() {
		return phase;
	}

	/**
	 * @param phase The phase to set.
	 */
	public void setPhase(MoonPhase phase) {
		this.phase = phase;
	}

	public MoonPhaseEvent getPhaseEvent() {
		return phaseEvent;
	}

	public void setPhaseEvent(MoonPhaseEvent phaseEvent) {
		this.phaseEvent = phaseEvent;
	}

	public double getPhaseDouble() {
		return phaseDouble;
	}

	public void setPhaseDouble(double phaseDouble) {
		this.phaseDouble = phaseDouble;
	}

	public int getIllumination() {
		return illumination;
	}

	public void setIllumination(int illumination) {
		this.illumination = illumination;
	}
	
}
