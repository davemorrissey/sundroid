package uk.co.sundroid.util.astro;
import java.util.Calendar;


public class SunDay extends BodyDay {
	
	private Calendar astDawn;
	
	private Calendar astDusk;
	
	private Calendar ntcDawn;
	
	private Calendar ntcDusk;
	
	private Calendar civDawn;
	
	private Calendar civDusk;
	
	private Calendar ghEnd;
	
	private Calendar ghStart;

	private TwilightType civType;
	
	private TwilightType ntcType;
	
	private TwilightType astType;
	
	private TwilightType ghType;
	
	private double civHours;
	
	private double ntcHours;
	
	private double astHours;
	
	private double ghHours;

	public Calendar getAstDawn() {
		return astDawn;
	}

	public void setAstDawn(Calendar astDawn) {
		this.astDawn = astDawn;
	}

	public Calendar getAstDusk() {
		return astDusk;
	}

	public void setAstDusk(Calendar astDusk) {
		this.astDusk = astDusk;
	}

	public Calendar getNtcDawn() {
		return ntcDawn;
	}

	public void setNtcDawn(Calendar ntcDawn) {
		this.ntcDawn = ntcDawn;
	}

	public Calendar getNtcDusk() {
		return ntcDusk;
	}

	public void setNtcDusk(Calendar ntcDusk) {
		this.ntcDusk = ntcDusk;
	}

	public Calendar getCivDawn() {
		return civDawn;
	}

	public void setCivDawn(Calendar civDawn) {
		this.civDawn = civDawn;
	}

	public Calendar getCivDusk() {
		return civDusk;
	}

	public void setCivDusk(Calendar civDusk) {
		this.civDusk = civDusk;
	}

	public TwilightType getCivType() {
		return civType;
	}

	public void setCivType(TwilightType civType) {
		this.civType = civType;
	}

	public TwilightType getNtcType() {
		return ntcType;
	}

	public void setNtcType(TwilightType ntcType) {
		this.ntcType = ntcType;
	}

	public TwilightType getAstType() {
		return astType;
	}

	public void setAstType(TwilightType astType) {
		this.astType = astType;
	}

	public double getCivHours() {
		return civHours;
	}

	public void setCivHours(double civHours) {
		this.civHours = civHours;
	}

	public double getNtcHours() {
		return ntcHours;
	}

	public void setNtcHours(double ntcHours) {
		this.ntcHours = ntcHours;
	}

	public double getAstHours() {
		return astHours;
	}

	public void setAstHours(double astHours) {
		this.astHours = astHours;
	}

	public Calendar getGhEnd() {
		return ghEnd;
	}

	public void setGhEnd(Calendar ghEnd) {
		this.ghEnd = ghEnd;
	}

	public Calendar getGhStart() {
		return ghStart;
	}

	public void setGhStart(Calendar ghStart) {
		this.ghStart = ghStart;
	}

	public TwilightType getGhType() {
		return ghType;
	}

	public void setGhType(TwilightType ghType) {
		this.ghType = ghType;
	}

	public double getGhHours() {
		return ghHours;
	}

	public void setGhHours(double ghHours) {
		this.ghHours = ghHours;
	}

}
