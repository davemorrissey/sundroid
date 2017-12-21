package uk.co.sundroid.util.astro;


/**
 * Phases of the moon.
 * */
public enum MoonPhase {
	
	NEW ("New", "New"),
	EVENING_CRESCENT ("Evening crescent", "Eve. crescent"),
	FIRST_QUARTER ("First quarter", "First q."),
	WAXING_GIBBOUS ("Waxing gibbous", "Waxing gibbous"),
	FULL ("Full", "Full"),
	WANING_GIBBOUS ("Waning gibbous", "Waning gibbous"),
	LAST_QUARTER ("Last quarter", "Last q."),
	MORNING_CRESCENT ("Morning crescent", "Morn. crescent");
	
	private String displayName;
	private String shortDisplayName;
	
	MoonPhase(String displayName, String shortDisplayName) {
		this.displayName = displayName;
		this.shortDisplayName = shortDisplayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public String getShortDisplayName() {
		return shortDisplayName;
	}

}
