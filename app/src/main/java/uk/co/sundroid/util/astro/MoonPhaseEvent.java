package uk.co.sundroid.util.astro;

import java.util.Calendar;

public class MoonPhaseEvent {
	
	/** Phase. One of full, new, FQ, LQ. */
	private MoonPhase phase;
	
	/** Time the event occurs. */
	private Calendar time;
	
	public MoonPhaseEvent(MoonPhase phase, Calendar time) {
		this.phase = phase;
		this.time = time;
	}
	
	public double getPhaseDouble() {
		if (phase == MoonPhase.NEW) {
			return 0d;
		} else if (phase == MoonPhase.FIRST_QUARTER) {
			return 0.25d;
		} else if (phase == MoonPhase.FULL) {
			return 0.5d;
		} else {
			return 0.75d;
		}
	}

	public MoonPhase getPhase() {
		return phase;
	}

	public Calendar getTime() {
		return time;
	}
	
}