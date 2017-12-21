package uk.co.sundroid.util.astro;

import android.graphics.Color;

public enum Body {
	
	SUN("Sun", Color.argb(255, 255, 204, 0), Color.argb(255, 255, 150, 0)),
	MOON("Moon", Color.argb(255, 255, 255, 255), Color.argb(255, 100, 100, 100)),
	MERCURY("Mercury", Color.argb(255, 220, 151, 110), Color.argb(255, 220, 151, 110)),
	VENUS("Venus", Color.argb(255, 255, 181, 85), Color.argb(255, 255, 181, 85)),
	MARS("Mars", Color.argb(255, 229, 144, 128), Color.argb(255, 229, 144, 128)),
	JUPITER("Jupiter", Color.argb(255, 244, 211, 172), Color.argb(255, 220, 180, 140)),
	SATURN("Saturn", Color.argb(255, 249, 210, 147), Color.argb(255, 249, 210, 147)),
	URANUS("Uranus", Color.argb(255, 98, 221, 222), Color.argb(255, 98, 221, 222)),
	NEPTUNE("Neptune", Color.argb(255, 98, 171, 222), Color.argb(255, 98, 171, 222));

	private int darkColor;
    private int lightColor;
	
	private String name;
	
	Body(String name, int darkColor, int lightColor) {
		this.name = name;
		this.darkColor = darkColor;
        this.lightColor = lightColor;
	}
	
	public String getName() {
		return name;
	}

    public int getDarkColor() {
        return darkColor;
    }

    public int getLightColor() {
        return lightColor;
    }
}
