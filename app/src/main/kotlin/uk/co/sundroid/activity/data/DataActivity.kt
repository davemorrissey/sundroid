package uk.co.sundroid.activity.data

import android.app.ActionBar.OnNavigationListener
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.view.Menu
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.NavItem
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.*
import uk.co.sundroid.activity.info.InfoActivity
import uk.co.sundroid.activity.location.LocationSelectActivity
import uk.co.sundroid.activity.location.TimeZonePickerActivity
import uk.co.sundroid.activity.settings.AppSettingsActivity
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.*
import uk.co.sundroid.util.log.*
import uk.co.sundroid.util.prefs.Prefs

import java.util.ArrayList
import java.util.Calendar

import uk.co.sundroid.R.*
import uk.co.sundroid.NavItem.NavItemLocation.*

class DataActivity : AbstractActivity(), OnClickListener, OnNavigationListener {

    private var selectorItems: List<SelectorItem>? = null

    private var ignoreNextNavigation: Boolean = false

    private var fragment: AbstractDataFragment? = null

    private var dataGroup: DataGroup? = null

    private var dayDetailTab: String? = null

    private var location: LocationDetails? = null

    var dateCalendar: Calendar = Calendar.getInstance()
    var timeCalendar: Calendar = Calendar.getInstance()

    private val isFragmentConfigurable: Boolean
        get() = fragment is ConfigurableFragment

    inner class SelectorItem(val title: String, val subtitle: String, val action: Int)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        d(TAG, "onActivityResult($requestCode, $resultCode)")
        super.onActivityResult(requestCode, resultCode, data)
        initialiseDataFragmentView()
        updateDataFragmentView()
    }

    override fun onNavigationItemSelected(itemPosition: Int, itemId: Long): Boolean {
        if (this.ignoreNextNavigation) {
            this.ignoreNextNavigation = false
            return true
        }
        if (selectorItems != null && selectorItems!!.size >= itemPosition + 1) {
//            val dataGroup = DataGroup.forIndex(selectorItems!![itemPosition].action)
//            if (dataGroup != null) {
//                setDataGroup(dataGroup)
//            }
        }
        return true
    }

    private fun setDayDetailTab(dayDetailTab: String) {
        if (dayDetailTab != this.dayDetailTab) {
            d(TAG, "Changing day detail tab to " + dayDetailTab)
            this.dayDetailTab = dayDetailTab
            Prefs.setLastDayDetailTab(this, dayDetailTab)
            updateDayDetailTabs()
            updateDataFragment(true)
        }
    }

    private fun updateDataFragment(recreateFragment: Boolean) {
        d(TAG, "updateDataFragment($recreateFragment)")

        if (this.dataGroup == DataGroup.DAY_DETAIL) {
            updateDayDetailTabs()
        } else {
            hideDayDetailTabs()
        }

        if (recreateFragment) {

            remove(R.id.dataFragment)

            var fragment: AbstractDataFragment = DaySummaryFragment()
            when {
                this.dataGroup == DataGroup.DAY_SUMMARY -> fragment = DaySummaryFragment()
                this.dataGroup == DataGroup.DAY_DETAIL -> fragment = when (this.dayDetailTab) {
                    "sun" -> DayDetailSunFragment()
                    "moon" -> DayDetailMoonFragment()
                    "planets" -> DayDetailPlanetsFragment()
                    else -> DayDetailEventsFragment()
                }
                this.dataGroup == DataGroup.TRACKER -> fragment = TrackerFragment()
                this.dataGroup == DataGroup.MONTH_CALENDARS -> fragment = MonthCalendarsFragment()
                this.dataGroup == DataGroup.MONTH_MOONPHASE -> fragment = MonthMoonPhaseFragment()
                this.dataGroup == DataGroup.YEAR_EVENTS -> fragment = YearEventsFragment()
            }
            this.fragment = fragment

            d(TAG, "Changing fragment to " + fragment.javaClass.simpleName)
            fragmentManager
                    .beginTransaction()
                    .replace(id.dataFragment, fragment, DATA_TAG)
                    .commit()
            show(R.id.dataFragment)

        } else {

            fragment = fragmentManager.findFragmentByTag(DATA_TAG) as AbstractDataFragment

        }

        updateNavItems()

    }

    private fun initialiseDataFragmentView() {
        val fragment = fragmentManager.findFragmentByTag(DATA_TAG)
        if (fragment is AbstractDataFragment) {
            try {
                fragment.initialise()
            } catch (e: Exception) {
                // Unlikely. Inform user?
                e(TAG, "Failed to init fragment " + fragment.javaClass.simpleName, e)
            }
        }
    }

    private fun updateDataFragmentView() {
        val fragment = fragmentManager.findFragmentByTag(DATA_TAG)
        if (fragment is AbstractDataFragment) {
            d(TAG, "Updating data in fragment " + fragment.javaClass.simpleName)
            try {
                fragment.update()
            } catch (e: Exception) {
                // Unlikely. Inform user?
                e(TAG, "Failed to update data in fragment " + fragment.javaClass.simpleName, e)
            }

        }
    }

    // All data activities are root level so back exits.
    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun hideDayDetailTabs() {
        remove(R.id.dayDetailTabs)
    }

    private fun initDayDetailTabs() {
        show(R.id.dayDetailTabs)
        findViewById<View>(R.id.sunTabInactive).setOnClickListener { setDayDetailTab("sun") }
        findViewById<View>(R.id.moonTabInactive).setOnClickListener { setDayDetailTab("moon") }
        findViewById<View>(R.id.planetsTabInactive).setOnClickListener { setDayDetailTab("planets") }
        findViewById<View>(R.id.eventsTabInactive).setOnClickListener { setDayDetailTab("events") }
        updateDayDetailTabs()
    }

    private fun updateDayDetailTabs() {
        show(R.id.dayDetailTabs)
        findViewById<View>(R.id.sunTabActive).visibility = if (dayDetailTab == "sun") View.VISIBLE else View.GONE
        findViewById<View>(R.id.sunTabInactive).visibility = if (dayDetailTab == "sun") View.GONE else View.VISIBLE
        findViewById<View>(R.id.moonTabActive).visibility = if (dayDetailTab == "moon") View.VISIBLE else View.GONE
        findViewById<View>(R.id.moonTabInactive).visibility = if (dayDetailTab == "moon") View.GONE else View.VISIBLE
        findViewById<View>(R.id.planetsTabActive).visibility = if (dayDetailTab == "planets") View.VISIBLE else View.GONE
        findViewById<View>(R.id.planetsTabInactive).visibility = if (dayDetailTab == "planets") View.GONE else View.VISIBLE
        findViewById<View>(R.id.eventsTabActive).visibility = if (dayDetailTab == "events") View.VISIBLE else View.GONE
        findViewById<View>(R.id.eventsTabInactive).visibility = if (dayDetailTab == "events") View.GONE else View.VISIBLE
    }

    override fun onCreateDialog(id: Int): Dialog {
        when (id) {
            DIALOG_SAVE -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Save location")

                val view = layoutInflater.inflate(R.layout.dialog_save, null)
                val saveField = view.findViewById<EditText>(R.id.saveField)
                if (isNotEmpty(location!!.name) && isEmpty(saveField.text.toString())) {
                    saveField.setText(location!!.name)
                } else {
                    saveField.setText("")
                }
                dialog.setView(view)

                dialog.setPositiveButton("OK") { d, b ->
                    val saveName = saveField.text.toString()
                    var db: DatabaseHelper? = null
                    try {
                        if (isNotEmpty(saveName)) {
                            db = DatabaseHelper(this@DataActivity)
                            location!!.name = saveName
                            Prefs.saveSelectedLocation(this@DataActivity, location!!)
                            db.addSavedLocation(location!!)
                            Toast.makeText(this@DataActivity, "This location has been saved", Toast.LENGTH_SHORT).show()
                            refreshSelector()
                        } else {
                            Toast.makeText(this@DataActivity, "Please enter a name for this location", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        if (db != null) {
                            db.close()
                        }
                    }
                    removeDialog(DIALOG_SAVE)
                }
                dialog.setNegativeButton("Cancel") { d, b -> removeDialog(DIALOG_SAVE) }
                return dialog.create()
            }
        }
        return super.onCreateDialog(id)
    }

    /********************************** M E N U  */

    private fun updateNavItems() {
        val navItems = ArrayList<NavItem>()
        if (isFragmentConfigurable) {
            navItems.add(NavItem("Page settings", drawable.icn_bar_viewsettings, HEADER, MENU_VIEW_SETTINGS))
        }
        navItems.add(NavItem("Change location", drawable.icn_bar_location, HEADER, MENU_CHANGE_LOCATION))
        navItems.add(NavItem("Save location", drawable.icn_menu_myplaces, HEADER_IF_ROOM, MENU_SAVE_LOCATION))
        navItems.add(NavItem("Time zone", drawable.icn_menu_timezone, MENU, MENU_TIME_ZONE))
        navItems.add(NavItem("Help", drawable.icn_menu_help, MENU, MENU_HELP))
        navItems.add(NavItem("Settings", drawable.icn_menu_preferences, MENU, MENU_SETTINGS))
        setNavItems(navItems)
    }

    override fun onNavItemSelected(itemPosition: Int) {
        when (itemPosition) {
            MENU_CHANGE_LOCATION -> startLocationOptions()
            MENU_SAVE_LOCATION -> startSaveLocation()
            MENU_TIME_ZONE -> startTimeZone()
            MENU_HELP -> startActivity(Intent(this, InfoActivity::class.java))
            MENU_SETTINGS -> startActivity(Intent(this, AppSettingsActivity::class.java))
            MENU_VIEW_SETTINGS -> if (isFragmentConfigurable) {
                (fragmentManager.findFragmentByTag(DATA_TAG) as ConfigurableFragment).openSettingsDialog()
            }
        }
    }

    /********************************* NAVIGATION  */

    private fun startLocationOptions() {
        val intent = Intent(this, LocationSelectActivity::class.java)
        startActivityForResult(intent, LocationSelectActivity.REQUEST_LOCATION)
    }

    private fun startSaveLocation() {
        showDialog(DIALOG_SAVE)
    }

    private fun startTimeZone() {
        val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
        intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_CHANGE)
        startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
    }

    /******************************** LIST NAV  */

    private fun refreshSelector() {
        val selectorItems = ArrayList<SelectorItem>()
//        for (dataGroup in DataGroup.values()) {
//            selectorItems.add(SelectorItem(location!!.displayName, dataGroup.displayName, dataGroup.index))
//        }
        this.selectorItems = selectorItems
//        actionBar!!.navigationMode = ActionBar.NAVIGATION_MODE_LIST
//        refreshSelector(dataGroup!!.index)
    }

    private fun refreshSelector(activeSelectorItem: Int) {
        val actionBar = actionBar
        if (actionBar != null) {
            val context = actionBar.themedContext
            val listNavigationAdaptor = ListNavigationAdaptor(context, R.layout.nav_item_selected, selectorItems!!)
            listNavigationAdaptor.setDropDownViewResource(R.layout.nav_item)
            getActionBar()!!.setListNavigationCallbacks(listNavigationAdaptor, this)
            if (selectorItems != null) {
                for (i in selectorItems!!.indices) {
                    if (selectorItems!![i].action == activeSelectorItem) {
                        getActionBar()!!.setSelectedNavigationItem(i)
                    }
                }
            }
        }
    }

    inner class ListNavigationAdaptor constructor(context: Context, private val viewResource: Int, list: List<SelectorItem>) : ArrayAdapter<SelectorItem>(context, viewResource, list) {
        private var dropDownViewResource: Int = 0

        override fun setDropDownViewResource(dropDownViewResource: Int) {
            this.dropDownViewResource = dropDownViewResource
            super.setDropDownViewResource(dropDownViewResource)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent, viewResource)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            return getView(position, convertView, parent, dropDownViewResource)
        }

        private fun getView(position: Int, convertView: View?, parent: ViewGroup, resource: Int): View {
            var row = convertView
            if (row == null) {
                val inflater = layoutInflater
                row = inflater.inflate(resource, parent, false)
            }
            val item = getItem(position)
            if (item != null) {
                val loc = row!!.findViewById<TextView>(R.id.location)
                if (loc != null) {
                    loc.text = item.title
                }
                val data = row.findViewById<TextView>(R.id.data)
                if (data != null) {
                    data.text = item.subtitle
                }
            }
            return row!!
        }

    }

    companion object {

        private val DIALOG_SAVE = 746

        private val MENU_CHANGE_LOCATION = Menu.FIRST + 1
        private val MENU_SAVE_LOCATION = Menu.FIRST + 2
        private val MENU_HELP = Menu.FIRST + 5
        private val MENU_SETTINGS = Menu.FIRST + 6
        private val MENU_VIEW_SETTINGS = Menu.FIRST + 10
        private val MENU_TIME_ZONE = Menu.FIRST + 12

        private val STATE_DATA_GROUP = "dataView"
        private val STATE_DAY_DETAIL_TAB = "dayDetailTab"
        private val STATE_DATE_TIMESTAMP = "dateTimestamp"
        private val STATE_TIME_TIMESTAMP = "timeTimestamp"

        private val DATA_TAG = "dataFragment"

        private val TAG = DataActivity::class.java.simpleName
    }


}