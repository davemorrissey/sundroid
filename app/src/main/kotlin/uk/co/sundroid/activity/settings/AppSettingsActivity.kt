package uk.co.sundroid.activity.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceActivity
import android.preference.PreferenceManager
import android.view.KeyEvent
import android.view.MenuItem

import uk.co.sundroid.R
import uk.co.sundroid.activity.data.DataActivity
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.theme.*

class AppSettingsActivity : PreferenceActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityCreateSetTheme(this)
        window.setBackgroundDrawableResource(getAppBg())
        val actionBar = actionBar
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(resources.getDrawable(getActionBarBg()))
            actionBar.title = "Settings"
            actionBar.subtitle = null
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        addPreferencesFromResource(R.xml.app_settings)
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this)
        val preference = preferenceManager.findPreference("defaultTimeZoneOverride")
        if (preference != null) {
            preference.isEnabled = PreferenceManager.getDefaultSharedPreferences(this).getString("defaultTimeZone", "~ASK") != "~ASK"
        }
    }

    private fun onNavBackSelected() {
        val intent = Intent(this, DataActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        val intent = Intent(this, DataActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onSharedPreferenceChanged(sharedPrefs: SharedPreferences, key: String) {
        d("ASA", "Setting changed: " + key)
        if (key == "defaultTimeZone") {
            val preference = preferenceManager.findPreference("defaultTimeZoneOverride")
            if (preference != null) {
                preference.isEnabled = sharedPrefs.getString("defaultTimeZone", "~ASK") != "~ASK"
            }
        } else if (key == "theme") {
            changeToTheme(this, sharedPrefs.getString("theme", "DARK")!!)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onNavBackSelected()
        return true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            startActivity(Intent(applicationContext, DataActivity::class.java))
        }
        return super.onKeyDown(keyCode, event)
    }

}
