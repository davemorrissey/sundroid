package uk.co.sundroid.activity.settings

import android.os.Bundle
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Preferences")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.app_settings, rootKey)

        val defaultTimeZone: ListPreference? = findPreference("defaultTimeZone")
        val overrideTimeZone: SwitchPreferenceCompat? = findPreference("defaultTimeZoneOverride")
        overrideTimeZone?.isVisible = defaultTimeZone?.value != "~ASK"

        defaultTimeZone?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        defaultTimeZone?.setOnPreferenceChangeListener { _, value ->
            overrideTimeZone?.isVisible = value != "~ASK"
            true
        }

        val locationTimeout: ListPreference? = findPreference("locationTimeout")
        locationTimeout?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        val clock: ListPreference? = findPreference("clock")
        clock?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        val firstWeekday: ListPreference? = findPreference("firstWeekday")
        firstWeekday?.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
    }

}
