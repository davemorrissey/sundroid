package uk.co.sundroid.activity.data.fragments;

import android.graphics.Bitmap;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import uk.co.sundroid.R;
import uk.co.sundroid.util.astro.Body;
import uk.co.sundroid.util.astro.MoonDay;
import uk.co.sundroid.util.astro.RiseSetType;
import uk.co.sundroid.util.astro.SunDay;
import uk.co.sundroid.util.astro.image.MoonPhaseImage;
import uk.co.sundroid.util.astro.math.BodyPositionCalculator;
import uk.co.sundroid.util.astro.math.SunCalculator;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.log.LogWrapper;
import uk.co.sundroid.util.theme.ThemePalette;
import uk.co.sundroid.util.time.TimeUtils;
import uk.co.sundroid.util.time.Time;

import java.util.Calendar;
import java.util.Set;
import java.util.TreeSet;

public class DaySummaryFragment extends AbstractDayFragment {

	private static final String TAG = DaySummaryFragment.class.getSimpleName();

	private Handler handler = new Handler();

	@Override
	protected int getLayout() {
		return R.layout.frag_data_daysummary;
	}

	@Override
	protected void update(final LocationDetails location, final Calendar calendar, final View view) throws Exception {

   		final SunDay sunDay = SunCalculator.calcDay(location.getLocation(), calendar);
   		final MoonDay moonDay = (MoonDay)BodyPositionCalculator.calcDay(Body.MOON, location.getLocation(), calendar, true);

	   	// Asynchronously generate moon graphic to speed up response.
    	if (moonDay != null) {
	    	Thread thread = new Thread() {
	    		@Override
	    		public void run() {
                    if (!isSafe()) {
                        return;
                    }

	    	    	try {
	    		    	Bitmap moonBitmap = MoonPhaseImage.makeImage(getResources(), R.drawable.moon, moonDay.getPhaseDouble(), location.getLocation().getLatitude().getDoubleValue() < 0, MoonPhaseImage.SIZE_MEDIUM);
	    	        	handler.post(() -> {
							if (!isSafe()) {
								return;
							}
							ImageView moon = view.findViewById(R.id.moonImage);
							moon.setImageBitmap(moonBitmap);
						});
	    	    	} catch (Exception e) {
	    	    		LogWrapper.e(TAG, "Error generating moon", e);
	    	    	}
	    		}
	    	};
	    	thread.start();
    	}

		if (sunDay != null) {

	    	if (sunDay.getRiseSetType() == RiseSetType.RISEN || sunDay.getRiseSetType() == RiseSetType.SET) {
	    		showInView(view, R.id.sunSpecial, sunDay.getRiseSetType() == RiseSetType.RISEN ? "Risen all day" : "Set all day");
	    		removeInView(view, R.id.sunEvt1Row, R.id.sunEvt2Row, R.id.sunUptimeRow);
	    	} else {
	    		removeInView(view, R.id.sunSpecial);
	    		removeInView(view, R.id.sunEvt1Row, R.id.sunEvt2Row);
	    		Set<SummaryEvent> events = new TreeSet<>();
	    		if (sunDay.getRise() != null) {
	    			events.add(new SummaryEvent("RISE", sunDay.getRise(), sunDay.getRiseAzimuth()));
	    		}
	    		if (sunDay.getSet() != null) {
	    			events.add(new SummaryEvent("SET", sunDay.getSet(), sunDay.getSetAzimuth()));
	    		}
	    		int index = 1;
	    		for (SummaryEvent event : events) {
	    			int rowId = view("sunEvt" + index + "Row");
	    			int labelId = view("sunEvt" + index + "Label");
	    			int timeId = view("sunEvt" + index + "Time");
	    			int imgId = view("sunEvt" + index + "Img");

	    			Time time = TimeUtils.formatTime(getApplicationContext(), event.getTime(), false);
	    			textInView(view, labelId, event.getName());
	    			textInView(view, timeId, time.getTime() + time.getMarker().toLowerCase());
	    			imageInView(view, imgId, event.getName().equals("RISE") ? ThemePalette.getRiseArrow() : ThemePalette.getSetArrow());
	    			showInView(view, rowId);

	    			index++;
	    		}

	    		if (sunDay.getUptimeHours() > 0 && sunDay.getUptimeHours() < 24) {
	    			showInView(view, R.id.sunUptimeRow);
	    			showInView(view, R.id.sunUptimeTime, TimeUtils.formatDurationHMS(getApplicationContext(), sunDay.getUptimeHours(), false));
	    		} else {
	    			removeInView(view, R.id.sunUptimeRow);
	    		}

	    	}
		}

    	if (moonDay != null) {

	    	if (moonDay.getRiseSetType() == RiseSetType.RISEN || moonDay.getRiseSetType() == RiseSetType.SET) {
	    		showInView(view, R.id.moonSpecial, moonDay.getRiseSetType() == RiseSetType.RISEN ? "Risen all day" : "Set all day");
	    		removeInView(view, R.id.moonEvt1Row, R.id.moonEvt2Row);
	    	} else {
	    		removeInView(view, R.id.moonSpecial);
	    		removeInView(view, R.id.moonEvt1Row, R.id.moonEvt2Row);
	    		Set<SummaryEvent> events = new TreeSet<>();
	    		if (moonDay.getRise() != null) {
	    			events.add(new SummaryEvent("RISE", moonDay.getRise(), moonDay.getRiseAzimuth()));
	    		}
	    		if (moonDay.getSet() != null) {
	    			events.add(new SummaryEvent("SET", moonDay.getSet(), moonDay.getSetAzimuth()));
	    		}
	    		int index = 1;
	    		for (SummaryEvent event : events) {
	    			int rowId = view("moonEvt" + index + "Row");
	    			int labelId = view("moonEvt" + index + "Label");
	    			int timeId = view("moonEvt" + index + "Time");
	    			int imgId = view("moonEvt" + index + "Img");

	    			Time time = TimeUtils.formatTime(getApplicationContext(), event.getTime(), false);
	    			textInView(view, labelId, event.getName());
	    			textInView(view, timeId, time.getTime() + time.getMarker().toLowerCase());
	    			imageInView(view, imgId, event.getName().equals("RISE") ? ThemePalette.getRiseArrow() : ThemePalette.getSetArrow());
	    			showInView(view, rowId);

	    			index++;
	    		}

	    	}
	    	if (moonDay.getPhaseEvent() == null) {
	    		showInView(view, R.id.moonPhase, moonDay.getPhase().getShortDisplayName());
	    	} else {
	    		Time time = TimeUtils.formatTime(getApplicationContext(), moonDay.getPhaseEvent().getTime(), false);
	    		showInView(view, R.id.moonPhase, moonDay.getPhase().getShortDisplayName() + " at " + time.getTime() + time.getMarker());
	    	}
	    	showInView(view, R.id.moonIllumination, Integer.toString(moonDay.getIllumination()) + "%");
    	}


	}



}
