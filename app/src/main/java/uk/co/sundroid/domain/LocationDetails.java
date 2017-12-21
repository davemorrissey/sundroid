package uk.co.sundroid.domain;

import java.io.Serializable;
import java.util.ArrayList;

import uk.co.sundroid.util.geometry.AngleFormat;
import uk.co.sundroid.util.geo.LatitudeLongitude;
import uk.co.sundroid.util.StringUtils;

public class LocationDetails implements Serializable {
	
	private static final long serialVersionUID = -4706308086519494893L;

	private int id;
	
	private LatitudeLongitude location;
	
	private String name;
	
	private String country;
	
	private String countryName;
	
	private String state;
	
	private TimeZoneDetail timeZone;
	
	private ArrayList<TimeZoneDetail> possibleTimeZones;
	
	public LocationDetails() {
		
	}
	
	public LocationDetails(LatitudeLongitude location, String name, String country, String state, TimeZoneDetail timeZone) {
		this.location = location;
		this.name = name;
		this.country = country;
		this.state = state;
		this.timeZone = timeZone;
	}
	
	public LocationDetails(LocationDetails other) {
		this.id = other.id;
		this.location = other.location;
		this.name = other.name;
		this.country = other.country;
		this.countryName = other.countryName;
		this.state = other.state;
		this.timeZone = other.timeZone;
	}
	
	public String getDisplayName() {
		if (StringUtils.isEmpty(name)) {
			return location.getPunctuatedValue(AngleFormat.Accuracy.MINUTES);
		} else {
			return name;
		}
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public TimeZoneDetail getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(TimeZoneDetail timeZone) {
		this.timeZone = timeZone;
	}

	public ArrayList<TimeZoneDetail> getPossibleTimeZones() {
		return possibleTimeZones;
	}

	public void setPossibleTimeZones(ArrayList<TimeZoneDetail> possibleTimeZones) {
		this.possibleTimeZones = possibleTimeZones;
	}

	public LatitudeLongitude getLocation() {
		return location;
	}

	public void setLocation(LatitudeLongitude location) {
		this.location = location;
	}
	
	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	@Override
	public String toString() {
		return "location=" + location + ", name=" + name + ", country=" + country + ", state=" + state + ", timeZone=" + timeZone;
	}
	
}