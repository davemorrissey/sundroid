package uk.co.sundroid.activity.data;

import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import uk.co.sundroid.AbstractActivity;
import uk.co.sundroid.BuildConfig;
import uk.co.sundroid.NavItem;
import uk.co.sundroid.R;
import uk.co.sundroid.activity.data.fragments.*;
import uk.co.sundroid.activity.info.InfoActivity;
import uk.co.sundroid.activity.location.LocationSelectActivity;
import uk.co.sundroid.activity.location.TimeZonePickerActivity;
import uk.co.sundroid.activity.settings.AppSettingsActivity;
import uk.co.sundroid.dao.DatabaseHelper;
import uk.co.sundroid.domain.LocationDetails;
import uk.co.sundroid.util.location.LatitudeLongitude;
import uk.co.sundroid.util.*;
import uk.co.sundroid.util.log.LogWrapper;
import uk.co.sundroid.util.prefs.SharedPrefsHelper;
import uk.co.sundroid.util.time.TimeZoneResolver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static uk.co.sundroid.R.*;
import static uk.co.sundroid.NavItem.NavItemLocation.*;

public class RealDataActivity extends AbstractActivity implements OnClickListener, OnNavigationListener {

    public class SelectorItem {

        private String title;

        private String subtitle;

        private int action;

        public SelectorItem(String title, String subtitle, int action) {
            this.title = title;
            this.subtitle = subtitle;
            this.action = action;
        }

        public String getTitle() {
            return title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public int getAction() {
            return action;
        }

    }

    private List<SelectorItem> selectorItems;

    private static final int DIALOG_SAVE = 746;

    private static final int MENU_CHANGE_LOCATION = Menu.FIRST + 1;
    private static final int MENU_SAVE_LOCATION = Menu.FIRST + 2;
    private static final int MENU_HELP = Menu.FIRST + 5;
    private static final int MENU_SETTINGS = Menu.FIRST + 6;
    private static final int MENU_VIEW_SETTINGS = Menu.FIRST + 10;
    private static final int MENU_TIME_ZONE = Menu.FIRST + 12;

	private static final String STATE_DATA_GROUP = "dataView";
	private static final String STATE_DAY_DETAIL_TAB = "dayDetailTab";
	private static final String STATE_DATE_TIMESTAMP = "dateTimestamp";
    private static final String STATE_TIME_TIMESTAMP = "timeTimestamp";

	private static final String DATA_TAG = "dataFragment";

	private static final String TAG = RealDataActivity.class.getSimpleName();

    private boolean ignoreNextNavigation;

    private AbstractDataFragment fragment;

	private DataGroup dataGroup;
	
	private String dayDetailTab;
	
	private LocationDetails location;

	private Calendar dateCalendar;
    private Calendar timeCalendar;
	
	@Override
	public void onStop() {
		super.onStop();
		LogWrapper.d(TAG, "onStop()");
	}

	@Override
	public void onPause() {
		super.onPause();
		LogWrapper.d(TAG, "onPause()");
	}
	
    @Override
    public void onCreate(Bundle state) {
    	super.onCreate(state);
        LogWrapper.d(TAG, "onCreate()");
        setContentView(R.layout.data);
        setActionBarTitle("");
        boolean forceDateUpdate = false;
        LogWrapper.i(TAG, "Action: " + getIntent().getAction());
        if (getIntent().getAction() != null && getIntent().getAction().equals(Intent.ACTION_MAIN)) {
            LogWrapper.i(TAG, "Opened from launcher, forcing date update");
            forceDateUpdate = true;
            getIntent().setAction(null);
        }

    	SharedPrefsHelper.INSTANCE.initPreferences(this);
    	initCalendarAndLocation(forceDateUpdate);
    	restoreState(state);
		initDayDetailTabs();
    	updateDataFragment(state == null);
        ignoreNextNavigation = true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        LogWrapper.i(TAG, "New intent action: " + getIntent().getAction());
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_MAIN)) {
            LogWrapper.i(TAG, "Opened from launcher, forcing date update");
            intent.setAction(null);
            initCalendarAndLocation(true);
        }
    }

    @Override
	public void onResume() {
		super.onResume();
		LogWrapper.d(TAG, "onResume()");
		
    	SharedPreferences prefs = getSharedPreferences("sundroid-prefs", MODE_PRIVATE);
    	int lastVersion = prefs.getInt("last-version", 0);
    	LogWrapper.d(TAG, "Last version: " + lastVersion + ", current version: " + BuildConfig.VERSION_CODE);
    	prefs.edit().putInt("last-version", BuildConfig.VERSION_CODE).apply();

		refreshSelector();
        initCalendarAndLocation(false);
        initialiseDataFragmentView();

	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogWrapper.d(TAG, "onActivityResult(" + requestCode + ", " + resultCode + ")");
        super.onActivityResult(requestCode, resultCode, data);
        initCalendarAndLocation(false);
        initialiseDataFragmentView();
        updateDataFragmentView();
    }

    @Override
    public final boolean onNavigationItemSelected(int itemPosition, long itemId) {
        if (this.ignoreNextNavigation) {
            this.ignoreNextNavigation = false;
            return true;
        }
        if (selectorItems != null && selectorItems.size() >= itemPosition + 1) {
            DataGroup dataGroup = DataGroup.forIndex(selectorItems.get(itemPosition).getAction());
            if (dataGroup != null) {
                setDataGroup(dataGroup);
            }
        }
        return true;
    }

    private void initCalendarAndLocation(boolean forceDateUpdate) {
        location = SharedPrefsHelper.INSTANCE.getSelectedLocation(this);
        if (location == null) {
            location = new LocationDetails(new LatitudeLongitude(37.779093, -122.419109));
            location.setName("San Francisco");
            location.setTimeZone(TimeZoneResolver.INSTANCE.getTimeZone("US/Pacific"));
            SharedPrefsHelper.INSTANCE.saveSelectedLocation(this, location);
        }
        if (location.getTimeZone() == null) {
            location.setTimeZone(TimeZoneResolver.INSTANCE.getTimeZone("UTC"));
        }

    	if (dateCalendar == null || timeCalendar == null || forceDateUpdate) {
    		Calendar localCalendar = Calendar.getInstance();
            dateCalendar = Calendar.getInstance(location.getTimeZone().getZone());
            dateCalendar.set(localCalendar.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH), localCalendar.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
            dateCalendar.set(Calendar.MILLISECOND, 0);
            dateCalendar.getTimeInMillis();
            timeCalendar = Calendar.getInstance(location.getTimeZone().getZone());
            timeCalendar.set(localCalendar.get(Calendar.YEAR), localCalendar.get(Calendar.MONTH), localCalendar.get(Calendar.DAY_OF_MONTH), localCalendar.get(Calendar.HOUR_OF_DAY), localCalendar.get(Calendar.MINUTE), 0);
            timeCalendar.set(Calendar.MILLISECOND, 0);
            timeCalendar.getTimeInMillis();
    	} else {
    		int year = dateCalendar.get(Calendar.YEAR);
    		int month = dateCalendar.get(Calendar.MONTH);
    		int day = dateCalendar.get(Calendar.DAY_OF_MONTH);
            int hour = timeCalendar.get(Calendar.HOUR_OF_DAY);
            int minute = timeCalendar.get(Calendar.MINUTE);
    		dateCalendar = Calendar.getInstance(location.getTimeZone().getZone());
            dateCalendar.set(year, month, day, 0, 0, 0);
            dateCalendar.set(Calendar.MILLISECOND, 0);
            dateCalendar.getTimeInMillis();
            timeCalendar = Calendar.getInstance(location.getTimeZone().getZone());
            timeCalendar.set(year, month, day, hour, minute, 0);
            timeCalendar.set(Calendar.MILLISECOND, 0);
            timeCalendar.getTimeInMillis();
    	}

    }
    
    public Calendar getDateCalendar() {
    	return this.dateCalendar;
    }

    public Calendar getTimeCalendar() {
        return this.timeCalendar;
    }
    
    @Override
    public void onSaveInstanceState(Bundle state) {
    	super.onSaveInstanceState(state);
    	state.putSerializable(STATE_DATA_GROUP, this.dataGroup);
    	state.putString(STATE_DAY_DETAIL_TAB, this.dayDetailTab);
    	if (this.getDateCalendar() != null && this.getTimeCalendar() != null) {
    		state.putLong(STATE_DATE_TIMESTAMP, this.dateCalendar.getTimeInMillis());
            state.putLong(STATE_TIME_TIMESTAMP, this.timeCalendar.getTimeInMillis());
    	}
    }
    
    private void restoreState(Bundle state) {
    	this.dayDetailTab = "sun";
    	this.dataGroup = DataGroup.DAY_SUMMARY;
    	if (state != null) {
	    	if (state.containsKey(STATE_DAY_DETAIL_TAB)) {
	    		this.dayDetailTab = state.getString(STATE_DAY_DETAIL_TAB);
	    	}
	    	if (state.containsKey(STATE_DATA_GROUP)) {
	    		this.dataGroup = (DataGroup)state.get(STATE_DATA_GROUP);
	    	}
	    	if (state.containsKey(STATE_DATE_TIMESTAMP) && state.containsKey(STATE_TIME_TIMESTAMP)) {
	    		this.dateCalendar.setTimeInMillis(state.getLong(STATE_DATE_TIMESTAMP));
                this.timeCalendar.setTimeInMillis(state.getLong(STATE_TIME_TIMESTAMP));
	    	}
    	} else {
            this.dataGroup = SharedPrefsHelper.INSTANCE.getLastDataGroup(this);
            if (this.dataGroup == DataGroup.DAY_DETAIL) {
                this.dayDetailTab = SharedPrefsHelper.INSTANCE.getLastDetailTab(this);
            }
        }
    }
    
    private void setDataGroup(DataGroup dataGroup) {
    	if (dataGroup != this.dataGroup) {
    		LogWrapper.d(TAG, "Changing data group to " + dataGroup);
    		this.dataGroup = dataGroup;
            SharedPrefsHelper.INSTANCE.setLastDataGroup(this, dataGroup);
            updateDataFragment(true);
    	}
    }
    
    private void setDayDetailTab(String dayDetailTab) {
    	if (!dayDetailTab.equals(this.dayDetailTab)) {
	    	LogWrapper.d(TAG, "Changing day detail tab to " + dayDetailTab);
	    	this.dayDetailTab = dayDetailTab;
            SharedPrefsHelper.INSTANCE.setLastDayDetailTab(this, dayDetailTab);
	    	updateDayDetailTabs();
            updateDataFragment(true);
    	}
    }
    
    private void updateDataFragment(boolean recreateFragment) {
        LogWrapper.d(TAG, "updateDataFragment(" + recreateFragment + ")");
    	
        if (this.dataGroup == DataGroup.DAY_DETAIL) {
            updateDayDetailTabs();
        } else {
            hideDayDetailTabs();
        }

        if (recreateFragment) {

            remove(R.id.dataFragment);

            fragment = null;

            if (this.dataGroup == DataGroup.DAY_SUMMARY) {
                fragment = new DaySummaryFragment();
            } else if (this.dataGroup == DataGroup.DAY_DETAIL) {
                switch (this.dayDetailTab) {
                    case "sun":
                        fragment = new DayDetailSunFragment();
                        break;
                    case "moon":
                        fragment = new DayDetailMoonFragment();
                        break;
                    case "planets":
                        fragment = new DayDetailPlanetsFragment();
                        break;
                    default:
                        fragment = new DayDetailEventsFragment();
                        break;
                }
            } else if (this.dataGroup == DataGroup.TRACKER) {
                fragment = new TrackerFragment();
            } else if (this.dataGroup == DataGroup.MONTH_CALENDARS) {
                fragment = new MonthCalendarsFragment();
            } else if (this.dataGroup == DataGroup.MONTH_MOONPHASE) {
                fragment = new MonthMoonPhaseFragment();
            } else if (this.dataGroup == DataGroup.YEAR_EVENTS) {
                fragment = new YearEventsFragment();
            }

            if (fragment != null) {
                LogWrapper.d(TAG, "Changing fragment to " + fragment.getClass().getSimpleName());
                getFragmentManager()
                        .beginTransaction()
                        .replace(id.dataFragment, fragment, DATA_TAG)
                        .commit();
            }
            show(R.id.dataFragment);

        } else {

            fragment = (AbstractDataFragment)getFragmentManager().findFragmentByTag(DATA_TAG);

        }

        updateNavItems();
    	
    }
    
    private void initialiseDataFragmentView() {
		Fragment fragment = getFragmentManager().findFragmentByTag(DATA_TAG);
    	if (fragment instanceof AbstractDataFragment) {
    		try {
    			((AbstractDataFragment)fragment).initialise();
    		} catch (Exception e) {
    			// Unlikely. Inform user?
    			LogWrapper.e(TAG, "Failed to init fragment " + fragment.getClass().getSimpleName(), e);
    		}
    	}
    }

    private void updateDataFragmentView() {
        Fragment fragment = getFragmentManager().findFragmentByTag(DATA_TAG);
        if (fragment instanceof AbstractDataFragment) {
            LogWrapper.d(TAG, "Updating data in fragment " + fragment.getClass().getSimpleName());
            try {
                ((AbstractDataFragment)fragment).update();
            } catch (Exception e) {
                // Unlikely. Inform user?
                LogWrapper.e(TAG, "Failed to update data in fragment " + fragment.getClass().getSimpleName(), e);
            }
        }
    }

    // All data activities are root level so back exits.
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void hideDayDetailTabs() {
    	remove(R.id.dayDetailTabs);
    }
    
    private void initDayDetailTabs() {
    	show(R.id.dayDetailTabs);
    	findViewById(R.id.sunTabActive).setOnClickListener(this);
    	findViewById(R.id.sunTabInactive).setOnClickListener(this);
    	findViewById(R.id.moonTabActive).setOnClickListener(this);
    	findViewById(R.id.moonTabInactive).setOnClickListener(this);
    	findViewById(R.id.planetsTabActive).setOnClickListener(this);
    	findViewById(R.id.planetsTabInactive).setOnClickListener(this);
    	findViewById(R.id.eventsTabActive).setOnClickListener(this);
    	findViewById(R.id.eventsTabInactive).setOnClickListener(this);
    	updateDayDetailTabs();
    }
    
	private void updateDayDetailTabs() {
		show(R.id.dayDetailTabs);
		findViewById(R.id.sunTabActive).setVisibility(dayDetailTab.equals("sun") ? View.VISIBLE : View.GONE);
		findViewById(R.id.sunTabInactive).setVisibility(dayDetailTab.equals("sun") ? View.GONE : View.VISIBLE);
		findViewById(R.id.moonTabActive).setVisibility(dayDetailTab.equals("moon") ? View.VISIBLE : View.GONE);
		findViewById(R.id.moonTabInactive).setVisibility(dayDetailTab.equals("moon") ? View.GONE : View.VISIBLE);
		findViewById(R.id.planetsTabActive).setVisibility(dayDetailTab.equals("planets") ? View.VISIBLE : View.GONE);
		findViewById(R.id.planetsTabInactive).setVisibility(dayDetailTab.equals("planets") ? View.GONE : View.VISIBLE);
		findViewById(R.id.eventsTabActive).setVisibility(dayDetailTab.equals("events") ? View.VISIBLE : View.GONE);
		findViewById(R.id.eventsTabInactive).setVisibility(dayDetailTab.equals("events") ? View.GONE : View.VISIBLE);
	}

    private boolean isFragmentConfigurable() {
        return fragment instanceof ConfigurableFragment;
    }

	@Override
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.sunTabActive:
			case R.id.sunTabInactive:
				setDayDetailTab("sun");
				return;
			case R.id.moonTabActive:
			case R.id.moonTabInactive:
				setDayDetailTab("moon");
				return;
			case R.id.planetsTabActive:
			case R.id.planetsTabInactive:
				setDayDetailTab("planets");
				return;
			case R.id.eventsTabActive:
			case R.id.eventsTabInactive:
				setDayDetailTab("events");
				return;
		}
        super.onClick(button);
	}
	
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        	case DIALOG_SAVE: {
        		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        		dialog.setTitle("Save location");
        		
        		final View view = getLayoutInflater().inflate(R.layout.dialog_save, null);
        		final EditText saveField = view.findViewById(R.id.saveField);
            	if (StringUtils.isNotEmpty(location.getName()) && StringUtils.isEmpty(saveField.getText().toString())) {
                	saveField.setText(location.getName());
                } else {
                	saveField.setText("");
                }
            	dialog.setView(view);

            	dialog.setPositiveButton("OK", (d, b) -> {
                    String saveName = saveField.getText().toString();
                    DatabaseHelper db = null;
                    try {
                        if (StringUtils.isNotEmpty(saveName)) {
                            db = new DatabaseHelper(RealDataActivity.this);
                            location.setName(saveName);
                            SharedPrefsHelper.INSTANCE.saveSelectedLocation(RealDataActivity.this, location);
                            db.addSavedLocation(location);
                            Toast.makeText(RealDataActivity.this, "This location has been saved", Toast.LENGTH_SHORT).show();
                            refreshSelector();
                        } else {
                            Toast.makeText(RealDataActivity.this, "Please enter a name for this location", Toast.LENGTH_SHORT).show();
                        }
                    } finally {
                        if (db != null) {
                            db.close();
                        }
                    }
                    removeDialog(DIALOG_SAVE);
                });
            	dialog.setNegativeButton("Cancel", (d, b) -> removeDialog(DIALOG_SAVE));
        		return dialog.create();
        	}

        }
        return super.onCreateDialog(id);
    }

	/********************************** M E N U *************************************/
    
	private void updateNavItems() {
        List<NavItem> navItems = new ArrayList<>();
        if (isFragmentConfigurable()) {
            navItems.add(new NavItem("Page settings", drawable.icn_bar_viewsettings, HEADER, MENU_VIEW_SETTINGS));
        }
        navItems.add(new NavItem("Change location", drawable.icn_bar_location, HEADER, MENU_CHANGE_LOCATION));
        navItems.add(new NavItem("Save location", drawable.icn_menu_myplaces, HEADER_IF_ROOM, MENU_SAVE_LOCATION));
        navItems.add(new NavItem("Time zone", drawable.icn_menu_timezone, MENU, MENU_TIME_ZONE));
        navItems.add(new NavItem("Help", drawable.icn_menu_help, MENU, MENU_HELP));
        navItems.add(new NavItem("Settings", drawable.icn_menu_preferences, MENU, MENU_SETTINGS));
        setNavItems(navItems);
	}

    @Override
	protected void onNavItemSelected(int navItemAction) {
        switch (navItemAction) {
            case MENU_CHANGE_LOCATION:
                startLocationOptions();
                break;
            case MENU_SAVE_LOCATION:
                startSaveLocation();
                break;
            case MENU_TIME_ZONE:
                startTimeZone();
                break;
            case MENU_HELP:
                startActivity(new Intent(this, InfoActivity.class));
                break;
            case MENU_SETTINGS:
                startActivity(new Intent(this, AppSettingsActivity.class));
                break;
            case MENU_VIEW_SETTINGS:
                if (isFragmentConfigurable()) {
                    ((ConfigurableFragment)getFragmentManager().findFragmentByTag(DATA_TAG)).openSettingsDialog();
                }
                break;
        }
	}
    
    /********************************* NAVIGATION ***********************************/
 
	private void startLocationOptions() {
		Intent intent = new Intent(this, LocationSelectActivity.class);
		startActivityForResult(intent, LocationSelectActivity.REQUEST_LOCATION);
	}
    
	private void startSaveLocation() {
		showDialog(DIALOG_SAVE);
    }

    private void startTimeZone() {
        Intent intent = new Intent(getApplicationContext(), TimeZonePickerActivity.class);
        intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_CHANGE);
        startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE);
    }

    /******************************** LIST NAV *********************************/

    private void refreshSelector() {
        List<SelectorItem> selectorItems = new ArrayList<>();
        for (DataGroup dataGroup : DataGroup.values()) {
            selectorItems.add(new SelectorItem(location.getDisplayName(), dataGroup.getName(), dataGroup.getIndex()));
        }
        this.selectorItems = selectorItems;
        getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        refreshSelector(dataGroup.getIndex());
    }

    private void refreshSelector(int activeSelectorItem) {
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            Context context = actionBar.getThemedContext();
            ListNavigationAdaptor listNavigationAdaptor = new ListNavigationAdaptor(context, R.layout.nav_item_selected, selectorItems);
            listNavigationAdaptor.setDropDownViewResource(R.layout.nav_item);
            getActionBar().setListNavigationCallbacks(listNavigationAdaptor, this);
            if (selectorItems != null) {
                for (int i = 0; i < selectorItems.size(); i++) {
                    if (selectorItems.get(i).getAction() == activeSelectorItem) {
                        getActionBar().setSelectedNavigationItem(i);
                    }
                }
            }
        }
    }

    private final class ListNavigationAdaptor extends ArrayAdapter<SelectorItem> {

        private int viewResource;
        private int dropDownViewResource;

        private ListNavigationAdaptor(Context context, int viewResource, List<SelectorItem> list) {
            super(context, viewResource, list);
            this.viewResource = viewResource;
        }

        @Override
        public void setDropDownViewResource(int dropDownViewResource) {
            this.dropDownViewResource = dropDownViewResource;
            super.setDropDownViewResource(dropDownViewResource);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent, viewResource);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            return getView(position, convertView, parent, dropDownViewResource);
        }

        private View getView(int position, View convertView, ViewGroup parent, int resource) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(resource, parent, false);
            }
            SelectorItem item = getItem(position);
            if (item != null) {
                TextView loc = row.findViewById(R.id.location);
                if (loc != null) {
                    loc.setText(item.getTitle());
                }
                TextView data = row.findViewById(R.id.data);
                if (data != null) {
                    data.setText(item.getSubtitle());
                }
            }
            return(row);
        }

    }


}