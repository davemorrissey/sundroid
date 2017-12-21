package uk.co.sundroid.activity.data;

public enum DataGroup {

	DAY_SUMMARY ("Day summary", 0),
	DAY_DETAIL ("Day in detail", 1),
    TRACKER ("Sun and moon tracker", 2),
    MONTH_CALENDARS ("Month calendars", 3),
    MONTH_MOONPHASE ("Moon phase calendar", 4),
    YEAR_EVENTS ("Year events", 5);

	private final int index;
	
	private final String name;
	
	DataGroup(String name, int index) {
		this.name = name;
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}
	
	public static DataGroup forIndex(int index) {
		for (DataGroup dataView : values()) {
			if (dataView.getIndex() == index) {
				return dataView;
			}
		}
		return null;
	}
	
}
