package uk.co.sundroid.activity.data.fragments;

import android.support.annotation.NonNull;

import java.util.Calendar;

public class SummaryEvent implements Comparable<SummaryEvent> {
	
	private final String name;
	private final Calendar time;
	private final Double azimuth;
	
	public SummaryEvent(String name, Calendar time, Double azimuth) {
		this.name = name;
		this.time = time;
		this.azimuth = azimuth;
	}
	
	public int compareTo(@NonNull SummaryEvent other) {
		int result = time.compareTo(other.time);
		if (result == 0) {
			return 1;
		}
		return result;
	}
	
	public String getName() {
		return name;
	}
	
	public Calendar getTime() {
		return time;
	}
	
	public Double getAzimuth() {
		return azimuth;
	}
	
}
