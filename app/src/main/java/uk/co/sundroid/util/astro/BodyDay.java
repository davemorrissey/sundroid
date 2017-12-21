package uk.co.sundroid.util.astro;
import java.util.Calendar;


public class BodyDay {
	
	private Calendar rise;
	
	private Calendar set;
	
	private Calendar transit;
	
	private double riseAzimuth;
	
	private double setAzimuth;
	
	private double transitAppElevation;
	
	private RiseSetType riseSetType;
	
	private double uptimeHours;

	public Calendar getRise() {
		return rise;
	}

	public void setRise(Calendar rise) {
		this.rise = rise;
	}

	public Calendar getSet() {
		return set;
	}

	public void setSet(Calendar set) {
		this.set = set;
	}

	public Calendar getTransit() {
		return transit;
	}

	public void setTransit(Calendar transit) {
		this.transit = transit;
	}

	public double getRiseAzimuth() {
		return riseAzimuth;
	}

	public void setRiseAzimuth(double riseAzimuth) {
		this.riseAzimuth = riseAzimuth;
	}

	public double getSetAzimuth() {
		return setAzimuth;
	}

	public void setSetAzimuth(double setAzimuth) {
		this.setAzimuth = setAzimuth;
	}

	public RiseSetType getRiseSetType() {
		return riseSetType;
	}

	public void setRiseSetType(RiseSetType riseSetType) {
		this.riseSetType = riseSetType;
	}

	public double getTransitAppElevation() {
		return transitAppElevation;
	}

	public void setTransitAppElevation(double transitAppElevation) {
		this.transitAppElevation = transitAppElevation;
	}

	public double getUptimeHours() {
		return uptimeHours;
	}

	public void setUptimeHours(double uptimeHours) {
		this.uptimeHours = uptimeHours;
	}
	
}
