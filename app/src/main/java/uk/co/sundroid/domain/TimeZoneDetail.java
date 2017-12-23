package uk.co.sundroid.domain;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.TimeZone;

public class TimeZoneDetail implements Comparable<TimeZoneDetail>, Serializable {

	private static final long serialVersionUID = 8968408131775567729L;

	private String id;
	private String cities;
	private TimeZone zone;
	private long currentOffset;
	
	public TimeZoneDetail(String id, String cities, TimeZone zone) {
		this.id = id;
		this.cities = cities;
		this.zone = zone;
		this.currentOffset = zone.getOffset(System.currentTimeMillis());
	}
	
	public String getOffset(long time) {
		
		if (zone.getID().equals("UTC")) {
			return "UTC";
		}
		
		long offset = Math.abs(zone.getOffset(time));
		long offsetHours = offset/(1000 * 60 * 60);
		offset = offset - (offsetHours * (1000 * 60 * 60));
		long offsetMinutes = offset/(1000 * 60);
		
		String offsetHoursStr = "00" + offsetHours;
		offsetHoursStr = offsetHoursStr.substring(offsetHoursStr.length() - 2);
		
		String offsetMinutesStr = "00" + offsetMinutes;
		offsetMinutesStr = offsetMinutesStr.substring(offsetMinutesStr.length() - 2);
		
		String offsetString = offsetHoursStr + ":" + offsetMinutesStr;
		if (zone.getOffset(time) < 0) {
			offsetString = "-" + offsetString;
		} else {
			offsetString = "+" + offsetString;
		}
		if (offsetString.equals("+00:00")) {
			offsetString = "";
		}
		return "GMT" + offsetString;
	}

	public String getId() {
		return id;
	}

	public String getCities() {
		return cities;
	}

	public TimeZone getZone() {
		return zone;
	}

	@Override
	public String toString() {
		return "id=" + id + ", cities=" + cities;
	}

	public int compareTo(@NonNull TimeZoneDetail other) {
		if (this.id.equals("UTC")) {
			return -1;
		} else if (other.id.equals("UTC")) {
			return 1;
		}
		int result = Long.valueOf(this.currentOffset).compareTo(other.currentOffset);
		if (result != 0) {
			return result;
		} else {
			return 1;
		}
	}

}
