package uk.co.sundroid.util.astro.image;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import uk.co.sundroid.util.LogWrapper;
import uk.co.sundroid.util.SharedPrefsHelper;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.BodyDay;
import uk.co.sundroid.util.astro.Position;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.util.geo.LatitudeLongitude;
import uk.co.sundroid.util.geometry.BearingHelper;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.CalendarUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;


public class TrackerImage {
	
	private static final String TAG = TrackerImage.class.getSimpleName();
	
	public static class TrackerStyle {
		
		private final int cardinals, circles, day, golden, night, nightLine, bodyRisen, bodySet, stroke, markerStroke;
        private final boolean isRadar;
		private float[] dash;

		public static final TrackerStyle NORMAL_MAP = new TrackerStyle(
				Color.argb(255, 0, 0, 0),
				Color.argb(100, 0, 0, 0),
				Color.argb(255, 255, 204, 0),
				Color.argb(255, 255, 168, 0),
				Color.argb(255, 72, 90, 144),
				Color.argb(255, 72, 90, 144),
				Color.argb(255, 255, 204, 0),
				Color.argb(255, 72, 90, 144),
				3,
				2,
				new float[] { 2, 2 },
                false
				);
		
		public static final TrackerStyle SATELLITE_MAP = new TrackerStyle(
				Color.argb(255, 255, 255, 255),
				Color.argb(150, 255, 255, 255),
				Color.argb(255, 255, 222, 107),
				Color.argb(255, 255, 198, 0),
				Color.argb(255, 129, 161, 241),
				Color.argb(255, 129, 161, 241),
				Color.argb(255, 255, 255, 255),
				Color.argb(255, 129, 161, 241),
				3,
				2,
				new float[] { 2, 2 },
                false
				);
		
		
		public TrackerStyle(int cardinals, int circles, int day, int golden, int night, int nightLine, int bodyRisen, int bodySet, int stroke, int markerStroke, float[] dash, boolean isRadar) {
			this.cardinals = cardinals;
			this.circles = circles;
			this.day = day;
			this.golden = golden;
			this.night = night;
			this.nightLine = nightLine;
			this.stroke = stroke;
			this.markerStroke = markerStroke;
			this.dash = dash;
			this.bodyRisen = bodyRisen;
			this.bodySet = bodySet;
            this.isRadar = isRadar;
		}
		
		public static TrackerStyle forMode(String mode, String mapMode) {
			if (mode.equals("radar")) {
				return ThemePalette.getTrackerRadarStyle();
			} else if (mapMode.equals("normal") || mapMode.equals("terrain")) {
				return NORMAL_MAP;
			} else if (mapMode.equals("satellite") || mapMode.equals("hybrid")) {
				return SATELLITE_MAP;
			}
			return ThemePalette.getTrackerRadarStyle();
		}
		
		
	}
	
	private Body body;
	private TrackerStyle style;
	private Context context;
	private LatitudeLongitude location;
	private boolean hourMarkers = false;
	private boolean linearElevation = false;
	private boolean magneticBearings = false;
	private double magneticDeclination = 0d;
	
	private Calendar dateCalendar;
	private Calendar timeCalendar;
	
	private Bitmap dateBitmap;
	private Bitmap timeBitmap;
	
	private long dateBitmapTimestamp = 0;
	private long timeBitmapTimestamp = 0;
	
	private int containerWidth = 0;
	private int containerHeight = 0;
	
	public TrackerImage(TrackerStyle style, Context context, LatitudeLongitude location) {
		this.style = style;
		this.context = context;
		this.location = location;
		this.body = SharedPrefsHelper.getSunTrackerBody(context);
		this.hourMarkers = SharedPrefsHelper.getSunTrackerHourMarkers(context);
		this.linearElevation = SharedPrefsHelper.getSunTrackerLinearElevation(context);
		this.magneticBearings = SharedPrefsHelper.getMagneticBearings(context);
	}
	
	public void setDate(Calendar dateCalendar, Calendar timeCalendar) {
		this.dateCalendar = CalendarUtils.clone(dateCalendar);
		this.timeCalendar = CalendarUtils.clone(timeCalendar);
		this.magneticDeclination = BearingHelper.getMagneticDeclination(this.location, this.dateCalendar);
	}
	
	public void setTime(Calendar timeCalendar) {
		this.timeCalendar = CalendarUtils.clone(timeCalendar);
	}
	
	public void drawOnCanvas(View container, Canvas canvas, float left, float top) {
		this.containerWidth = container.getWidth();
		this.containerHeight = container.getHeight();
		updateTimeImageIfStale(CalendarUtils.clone(timeCalendar));
		updateDateImageIfStale(CalendarUtils.clone(dateCalendar));
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setFilterBitmap(true);
		canvas.drawBitmap(dateBitmap, left - (dateBitmap.getWidth()/2f), top - (dateBitmap.getHeight()/2f), paint);
		canvas.drawBitmap(timeBitmap, left - (timeBitmap.getWidth()/2f), top - (timeBitmap.getHeight()/2f), paint);
	}
	
	public void generate() {
		if (containerWidth == 0 || containerHeight == 0) {
			LogWrapper.d(TAG, "Cannot generate, dimensions unknown");
			return;
		}
		try {
			updateTimeImageIfStale(CalendarUtils.clone(timeCalendar));
			updateDateImageIfStale(CalendarUtils.clone(dateCalendar));
		} catch (Throwable t) {
			LogWrapper.e(TAG, "Generate failed: ", t);
		}
	}
	
	private Bitmap createBitmap() {
		
		int size = Math.min(containerWidth, containerHeight);
		return Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
		
	}
	
	private void updateTimeImageIfStale(Calendar timeCalendar) {
		if (timeBitmapTimestamp == timeCalendar.getTimeInMillis()) {
			return;
		}
		Bitmap bitmap = createBitmap();
		
		int padding = size(17);
		int size = bitmap.getWidth();
		int outerRadius = (size - (2 * padding))/2;
		float centerX = (size/2f);
		float centerY = (size/2f);

		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();

		paint.setAntiAlias(true);
		
		if (body == null) {
			
			for (Body body : Body.values()) {
				
				paint.setStyle(Style.FILL);
				
				Position position = BodyPositionCalculator.calcPosition(body, location, timeCalendar);
				
				if (position.getAppElevation() >= 0) {
				
					float apparentRadius = linearElevation ? ((float)(outerRadius - ((Math.abs(position.getAppElevation())/90d) * outerRadius))) : (float)(Math.cos(degToRad(position.getAppElevation())) * outerRadius);
					float x = (float)(Math.sin(degToRad(position.getAzimuth())) * apparentRadius);
					float y = (float)(Math.cos(degToRad(position.getAzimuth())) * apparentRadius);
					
					if (!style.isRadar) {
						paint.setColor(Color.argb(100, 0, 0, 0));
						canvas.drawCircle(centerX + x, centerY - y, size(6), paint);
						
					    Paint strokePaint = new Paint();
					    strokePaint.setColor(Color.argb(100, 0, 0, 0));
					    strokePaint.setTextSize(size(14));
					    strokePaint.setStyle(Style.STROKE);
					    strokePaint.setStrokeWidth(size(2));
					    strokePaint.setAntiAlias(true);
					    canvas.drawText(body.name().substring(0, 1) + body.name().substring(1).toLowerCase(), centerX + x + size(6), centerY - y + size(5), strokePaint);
					}
						
					paint.setStrokeWidth(size(style.stroke));
					paint.setColor(ThemePalette.getBodyColor(body));
					canvas.drawCircle(centerX + x, centerY - y, size(4), paint);
					
					
				    Paint textPaint = new Paint();
				    textPaint.setTextSize(size(14));
				    textPaint.setColor(ThemePalette.getBodyColor(body));
				    textPaint.setAntiAlias(true);
					canvas.drawText(body.name().substring(0, 1) + body.name().substring(1).toLowerCase(), centerX + x + size(6), centerY - y + size(5), textPaint);
				}
			}

			
		} else {
			
			paint.setStyle(Style.STROKE);
			
			Position position = BodyPositionCalculator.calcPosition(body, location, timeCalendar);
			
			float apparentRadius = linearElevation ? ((float)(outerRadius - ((Math.abs(position.getAppElevation())/90d) * outerRadius))) : (float)(Math.cos(degToRad(position.getAppElevation())) * outerRadius);
			float x = (float)(Math.sin(degToRad(position.getAzimuth())) * apparentRadius);
			float y = (float)(Math.cos(degToRad(position.getAzimuth())) * apparentRadius);
			
			if (!style.isRadar) {
				paint.setStrokeWidth(size(style.stroke + 1));
				paint.setColor(Color.argb(100, 0, 0, 0));
				canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint);
				canvas.drawCircle(centerX + x, centerY - y, size(7), paint);
			}
				
			paint.setStrokeWidth(size(style.stroke));
			int color = getElevationColor(position.getAppElevation(), true);
			paint.setColor(color);
			canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint);
			canvas.drawCircle(centerX + x, centerY - y, size(7), paint);
			
		}
		
		synchronized (this) {
			timeBitmap = bitmap;
			timeBitmapTimestamp = timeCalendar.getTimeInMillis();
		}
		
	}
	
	private void updateDateImageIfStale(Calendar dateCalendar) {
		if (dateBitmapTimestamp == dateCalendar.getTimeInMillis()) {
			return;
		}
		Bitmap bitmap = createBitmap();
		
		int padding = size(17);
		int size = bitmap.getWidth();
		int outerRadius = (size - (2 * padding))/2;
		int fontSize = size(14);
		float centerX = (size/2f);
		float centerY = (size/2f);
		
		Canvas canvas = new Canvas(bitmap);
		Paint paint = new Paint();
		paint.setColor(style.circles);
		paint.setStyle(Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeWidth(0);
		
		for (int elev = 0; elev < 90; elev += 15) {
			float apparentRadius = linearElevation ? ((float)(outerRadius - ((Math.abs(elev)/90d) * outerRadius))) : (float)(Math.cos(degToRad(elev)) * outerRadius);
			canvas.drawCircle(centerX, centerY, apparentRadius, paint);
		}
		
		if (magneticBearings) {
			canvas.rotate((float)magneticDeclination, centerX, centerY);
		}
		
		Paint textPaint = new Paint();
		textPaint.setAntiAlias(true);
		textPaint.setTextSize(fontSize);
		textPaint.setTextAlign(Paint.Align.CENTER);
		textPaint.setFakeBoldText(false);
		textPaint.setTypeface(Typeface.DEFAULT);
		textPaint.setColor(style.cardinals);
		
		canvas.drawText(magneticBearings ? "N(M)" : "N(T)", size/2, size(12), textPaint);
		canvas.drawText("S", size/2, size - size(2), textPaint);
		canvas.drawText("E", size - 10, size/2 + size(5), textPaint);
		canvas.drawText("W", 10, size/2 + size(5), textPaint);
		
		paint.setColor(style.circles);
		for (int az = 0; az < 360; az += 45) {
			float x = (float)(Math.sin(degToRad(az)) * outerRadius);
			float y = (float)(Math.cos(degToRad(az)) * outerRadius);
			canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint);
		}
		
		if (magneticBearings) {
			canvas.rotate(-(float)magneticDeclination, centerX, centerY);
		}
		
		if (body != null) {

	    	paint.setStrokeWidth(size(style.stroke));
	    	
			Calendar loopCalendar = CalendarUtils.clone(dateCalendar);
			
			Path path = new Path();
			
			Position position = BodyPositionCalculator.calcPosition(body, location, loopCalendar);
			float apparentRadius = linearElevation ? ((float)(outerRadius - ((Math.abs(position.getAppElevation())/90d) * outerRadius))) : (float)(Math.cos(degToRad(position.getAppElevation())) * outerRadius);
			float x = (float)(Math.sin(degToRad(position.getAzimuth())) * apparentRadius);
			float y = (float)(Math.cos(degToRad(position.getAzimuth())) * apparentRadius);
			int currentColor = getElevationColor(position.getAppElevation(), false);
			paint.setColor(currentColor);
			path.moveTo(centerX + x, centerY - y);
			loopCalendar.add(Calendar.MINUTE, 10);
			
			// Get rise/set events that happen on this calendar day, midnight to midnight.
			Set<Event> eventsSet = new TreeSet<>();
			Calendar dayLoopCalendar = CalendarUtils.clone(dateCalendar);
			dayLoopCalendar.add(Calendar.DAY_OF_MONTH, -1);
			long riseTime = 0;
			long setTime = 0;
			for (int i = 0; i < 3; i++) {
				BodyDay bodyDay = BodyPositionCalculator.calcDay(body, location, dayLoopCalendar, false);
				if (bodyDay.getRise() != null && CalendarUtils.isSameDay(bodyDay.getRise(), dateCalendar) && eventsSet.size() < 2) {
					eventsSet.add(new Event(bodyDay.getRise(), bodyDay.getRiseAzimuth()));
					riseTime = bodyDay.getRise().getTimeInMillis();
				}
				if (bodyDay.getSet() != null && CalendarUtils.isSameDay(bodyDay.getSet(), dateCalendar) && eventsSet.size() < 2) {
					eventsSet.add(new Event(bodyDay.getSet(), bodyDay.getSetAzimuth()));
					setTime = bodyDay.getSet().getTimeInMillis();
				}
				dayLoopCalendar.add(Calendar.DAY_OF_MONTH, 1);
			}
			
			// Determine calculated times in advance so sunrise and sunset can be inserted in order.
			TreeSet<Long> calcTimes = new TreeSet<>();
			do {
				loopCalendar.add(Calendar.MINUTE, 10);
				calcTimes.add(loopCalendar.getTimeInMillis());
			} while (loopCalendar.get(Calendar.DAY_OF_YEAR) == dateCalendar.get(Calendar.DAY_OF_YEAR));
			for (Event event : eventsSet) {
				long time = event.time.getTimeInMillis();
				calcTimes.add(time);
			}
			
			float prevX = x;
			float prevY = y;
			
			HashMap<Path, Integer> paths = new LinkedHashMap<>();
			
			for (long calcTime : calcTimes) {
				loopCalendar.setTimeInMillis(calcTime);
				position = BodyPositionCalculator.calcPosition(body, location, loopCalendar);
				apparentRadius = linearElevation ? ((float)(outerRadius - ((Math.abs(position.getAppElevation())/90d) * outerRadius))) : (float)(Math.cos(degToRad(position.getAppElevation())) * outerRadius);
				x = (float)(Math.sin(degToRad(position.getAzimuth())) * apparentRadius);
				y = (float)(Math.cos(degToRad(position.getAzimuth())) * apparentRadius);
				
				int thisColor = getElevationColor(position.getAppElevation(), false);
				if (calcTime == riseTime) {
					thisColor = body == Body.SUN ? style.golden : style.bodyRisen;
				} else if (calcTime == setTime) {
					thisColor = body == Body.SUN ? style.night : style.bodySet;
				}
				
				if (loopCalendar.get(Calendar.MINUTE) == 0 && hourMarkers) {
					// Draw lines across the path at a tangent to the line from the previous
					// point. Could improve slightly by averaging next point as well.
					float dx = x - prevX;
					float dy = y - prevY;
					double angle = Math.atan(dy/dx);
					double inverse = (Math.PI/2) - angle;
					float markY = (float)(size(5) * Math.sin(inverse));
					float markX = (float)(size(5) * Math.cos(inverse));
					
					Cap cap = paint.getStrokeCap();
					
					paint.setStrokeCap(Cap.ROUND);
					if (!style.isRadar) {
						int color = paint.getColor();
						paint.setColor(Color.argb(100, 0, 0, 0));
						paint.setStrokeWidth(size(style.markerStroke + 2));
						canvas.drawLine((centerX + x) + markX, (centerY - y) + markY, (centerX + x) - markX, (centerY - y) - markY, paint);
						paint.setColor(color);
					}
	
					paint.setStrokeWidth(size(style.markerStroke));
					canvas.drawLine((centerX + x) + markX, (centerY - y) + markY, (centerX + x) - markX, (centerY - y) - markY, paint);
					paint.setStrokeWidth(size(style.stroke));
					
					paint.setStrokeCap(cap);
					
				}
				
				if (thisColor != currentColor) {
					path.lineTo(centerX + x, centerY - y);
					canvas.drawPath(path, paint);
					paths.put(path, currentColor);
					currentColor = thisColor;
					paint.setColor(currentColor);
					path = new Path();
					path.moveTo(centerX + x, centerY - y);
				} else {
					path.lineTo(centerX + x, centerY - y);
				}
				
				prevX = x;
				prevY = y;
			}
			
			paths.put(path, currentColor);
			
			if (!style.isRadar) {
				paint.setStrokeWidth(size(style.stroke + 1));
				paint.setColor(Color.argb(100, 0, 0, 0));
				for (Map.Entry<Path, Integer> entry : paths.entrySet()) {
					canvas.drawPath(entry.getKey(), paint);
				}
			}
			
			paint.setStrokeWidth(size(style.stroke));
			for (Map.Entry<Path, Integer> entry : paths.entrySet()) {
				paint.setColor(entry.getValue());
				canvas.drawPath(entry.getKey(), paint);
			}
	
			// Draw dotted lines for rise and set.
			
			paint.setPathEffect( new DashPathEffect(style.dash, 0) );
			
			for (Event event : eventsSet) {
				x = (float)(Math.sin(degToRad(event.azimuth)) * outerRadius);
				y = (float)(Math.cos(degToRad(event.azimuth)) * outerRadius);
				
				if (!style.isRadar) {
					paint.setPathEffect(null);
					paint.setStrokeWidth(size(style.stroke + 1));
					paint.setColor(Color.argb(50, 0, 0, 0));
					canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint);
				}
				
				paint.setPathEffect( new DashPathEffect(style.dash, 0) );
				paint.setStrokeWidth(size(style.stroke));
				paint.setColor(body == Body.SUN ? style.golden : style.bodyRisen);
				canvas.drawLine(centerX, centerY, centerX + x, centerY - y, paint);
			}
			
		}

		synchronized (this) {
			dateBitmap = bitmap;
			dateBitmapTimestamp = dateCalendar.getTimeInMillis();
		}
		
	}

	private int size(int size) {
		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
		return (int)((metrics.densityDpi/160d) * size);
	}
	
	private int getElevationColor(double elevation, boolean line) {
		if (body == Body.SUN) {
			if (elevation >= 6) {
				return style.day;
			} else if (elevation >= -0.833) {
				return style.golden;
			}
			return line ? style.nightLine : style.night;
		} else if (body == Body.MOON) {
			if (elevation >= -0.5) {
				return style.bodyRisen;
			}
			return style.bodySet;
		} else {
			if (elevation >= 0d) {
				return style.bodyRisen;
			}
			return style.bodySet;
		}
	}
	
	private static double degToRad(double angleDeg) {
		return (Math.PI * angleDeg / 180.0);
	}

    private static class Event implements Comparable<Event> {
    	private Calendar time;
    	private Double azimuth;
    	public Event(Calendar time, Double azimuth) {
    		this.time = time;
    		this.azimuth = azimuth;
    	}
		public int compareTo(@NonNull Event other) {
			int result = time.compareTo(other.time);
			if (result == 0) {
				return 1;
			}
			return result;
		}
    }
	
}