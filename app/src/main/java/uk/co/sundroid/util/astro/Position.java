package uk.co.sundroid.util.astro;

public class Position {
	
	private long timestamp;
	
	private double azimuth;
	
	private double appElevation;
	
	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public double getAzimuth() {
		return azimuth;
	}

	public void setAzimuth(double azimuth) {
		this.azimuth = azimuth;
	}

	public double getAppElevation() {
		return appElevation;
	}

	public void setAppElevation(double appElevation) {
		this.appElevation = appElevation;
	}

}