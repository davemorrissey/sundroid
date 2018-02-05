package uk.co.sundroid.activity.settings;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MenuItem;

import uk.co.sundroid.R;
import uk.co.sundroid.activity.data.RealDataActivity;
import uk.co.sundroid.util.log.LogWrapper;
import uk.co.sundroid.util.theme.ThemePalette;

public class AppSettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ThemePalette.onActivityCreateSetTheme(this);
		getWindow().setBackgroundDrawableResource(ThemePalette.getAppBg());
		ActionBar actionBar = getActionBar();
		if (actionBar != null) {
			actionBar.setBackgroundDrawable(getResources().getDrawable(ThemePalette.getActionBarBg()));
			actionBar.setTitle("Settings");
			actionBar.setSubtitle(null);
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		addPreferencesFromResource(R.xml.app_settings);
		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
		Preference preference = getPreferenceManager().findPreference("defaultTimeZoneOverride");
		if (preference != null) {
			preference.setEnabled(!PreferenceManager.getDefaultSharedPreferences(this).getString("defaultTimeZone", "~ASK").equals("~ASK"));
		}
	}

    protected void onNavBackSelected() {
        Intent intent = new Intent(this, RealDataActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, RealDataActivity.class);
        startActivity(intent);
        finish();
    }

	@Override
	protected void onStop() {
		PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
		super.onStop();
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPrefs, String key) {
		LogWrapper.d("ASA", "Setting changed: " + key);
		if (key.equals("defaultTimeZone")) {
			Preference preference = getPreferenceManager().findPreference("defaultTimeZoneOverride");
			if (preference != null) {
				preference.setEnabled(!sharedPrefs.getString("defaultTimeZone", "~ASK").equals("~ASK"));
			}
		} else if (key.equals("theme")) {
            ThemePalette.changeToTheme(this, sharedPrefs.getString("theme", "DARK"));
        }
	}

	@Override
	public final boolean onOptionsItemSelected(MenuItem item) {
		onNavBackSelected();
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			startActivity(new Intent(getApplicationContext(), RealDataActivity.class));
		}
		return super.onKeyDown(keyCode, event);
	}

}
