package uk.co.sundroid.activity

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.android.synthetic.main.main.*
import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.BuildConfig
import uk.co.sundroid.NavItem
import uk.co.sundroid.NavItem.NavItemLocation.*
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.AbstractDataFragment
import uk.co.sundroid.activity.data.fragments.ConfigurableFragment
import uk.co.sundroid.activity.settings.AppSettingsActivity
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.dao.DatabaseHelper
import uk.co.sundroid.util.isEmpty
import uk.co.sundroid.util.isNotEmpty
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.log.d
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import java.util.*

class MainActivity : AbstractActivity(), FragmentManager.OnBackStackChangedListener {

    private var page: Page = Page.DAY_SUMMARY

    var dateCalendar: Calendar = Calendar.getInstance()
    var timeCalendar: Calendar = Calendar.getInstance()
    private var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setSupportActionBar(toolbar)
        initNavigationDrawer()
        supportFragmentManager.addOnBackStackChangedListener(this)

        var forceDateUpdate = false
        if (intent.action != null && intent.action == Intent.ACTION_MAIN) {
            forceDateUpdate = true
            intent.action = null
        }

        Prefs.initPreferences(this)
        initCalendarAndLocation(forceDateUpdate)
        restoreState(savedInstanceState)
        setPage(page, true)
    }

    public override fun onResume() {
        super.onResume()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putInt("last-version", BuildConfig.VERSION_CODE).apply()
        (getRootFragment() as? AbstractDataFragment)?.update()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action != null && intent.action == Intent.ACTION_MAIN) {
            intent.action = null
            initCalendarAndLocation(true)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // FIXME should be able to remove this once all location activities are fragments
        d(TAG, "onActivityResult($requestCode, $resultCode)")
        super.onActivityResult(requestCode, resultCode, data)
        initCalendarAndLocation(false)
        (getRootFragment() as? AbstractDataFragment)?.update()
    }

    private fun initCalendarAndLocation(forceDateUpdate: Boolean) {
        val location = Prefs.selectedLocation(this) ?: LocationDetails(LatitudeLongitude(37.779093, -122.419109)).apply {
            name = "San Francisco"
            timeZone = TimeZoneResolver.getTimeZone("US/Pacific")
            Prefs.saveSelectedLocation(this@MainActivity, this)
        }

        var dateDonor = dateCalendar
        var timeDonor = timeCalendar
        if (forceDateUpdate) {
            dateDonor = Calendar.getInstance()
            timeDonor = Calendar.getInstance()
        }
        dateCalendar.timeZone = location.timeZone!!.zone
        dateCalendar.set(dateDonor.get(Calendar.YEAR), dateDonor.get(Calendar.MONTH), dateDonor.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
        dateCalendar.set(Calendar.MILLISECOND, 0)
        dateCalendar.timeInMillis
        timeCalendar.timeZone = location.timeZone!!.zone
        timeCalendar.set(dateDonor.get(Calendar.YEAR), dateDonor.get(Calendar.MONTH), dateDonor.get(Calendar.DAY_OF_MONTH), timeDonor.get(Calendar.HOUR_OF_DAY), timeDonor.get(Calendar.MINUTE), 0)
        timeCalendar.set(Calendar.MILLISECOND, 0)
        timeCalendar.timeInMillis
    }

    override fun onSaveInstanceState(state: Bundle) {
        super.onSaveInstanceState(state)
        state.apply {
            putSerializable(STATE_PAGE, page)
            putLong(STATE_DATE_TIMESTAMP, dateCalendar.timeInMillis)
            putLong(STATE_TIME_TIMESTAMP, timeCalendar.timeInMillis)
        }
    }

    private fun restoreState(state: Bundle?) {
        this.page = Page.DAY_SUMMARY
        if (state != null) {
            if (state.containsKey(STATE_PAGE)) {
                this.page = state.get(STATE_PAGE) as Page
            }
            if (state.containsKey(STATE_DATE_TIMESTAMP) && state.containsKey(STATE_TIME_TIMESTAMP)) {
                this.dateCalendar.timeInMillis = state.getLong(STATE_DATE_TIMESTAMP)
                this.timeCalendar.timeInMillis = state.getLong(STATE_TIME_TIMESTAMP)
            }
//        } else {
//            this.dataGroup = Prefs.lastDataGroup(this)
        }
    }

    fun setPage(page: Page, force: Boolean = false) {
        if (page != this.page || force) {
            if (page.dataGroup != null) {
                Prefs.setLastDataGroup(this, page.dataGroup)
            }
            this.page = page
            val rootFragment = getRootFragment()
            if (rootFragment?.javaClass != page.fragmentClass || force) {
                val tx = supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.content, page.fragmentClass.newInstance(), ROOT)
                if (page.dataGroup == null) {
                    tx.addToBackStack(page.name)
                } else {
                    tx.runOnCommit { refreshChrome() }
                }
                tx.commit()
            }
        }
    }

    private fun openActivity(clazz: Class<out Activity>) {
        val intent = Intent(this, clazz)
        startActivity(intent)
    }

    fun setToolbarTitle(title: String) {
        toolbar?.title = title
    }

    fun setToolbarSubtitle(subtitle: String? = null) {
        toolbar?.subtitle = subtitle
    }

    fun setToolbarSubtitle(subtitle: Int) {
        toolbar?.setSubtitle(subtitle)
    }

    /**
     * Prepare the navigation drawer, adding listeners to all the options and enabling the toggle.
     */
    private fun initNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.daySummary -> setPage(Page.DAY_SUMMARY)
                R.id.dayDetail -> setPage(Page.DAY_DETAIL)
//                R.id.tracker -> setPage(Page.TRACKER)
//                R.id.calendars -> setPage(Page.MONTH_CALENDARS)
//                R.id.yearEvents -> setPage(Page.YEAR_EVENTS)
//                R.id.location -> openActivity(LocationSelectActivity::class.java)
                R.id.help -> setPage(Page.HELP)
                R.id.settings -> openActivity(AppSettingsActivity::class.java)
            }
            true
        }
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        actionBarDrawerToggle?.let {
            drawerLayout.addDrawerListener(it)
            it.syncState()
        }
    }

    override fun onNavItemSelected(itemPosition: Int) {
        when (itemPosition) {
            MENU_CHANGE_LOCATION -> setPage(Page.LOCATION_OPTIONS)
            MENU_SAVE_LOCATION -> showDialog(DIALOG_SAVE)
            MENU_TIME_ZONE -> setPage(Page.TIME_ZONE)
            MENU_VIEW_SETTINGS -> (getRootFragment() as? ConfigurableFragment)?.openSettingsDialog()
        }
    }

    /**
     * Identifies the current visible fragment, and updates the back/hamburger button and action bar
     * menu accordingly.
     */
    private fun refreshChrome() {
        d(TAG, "refreshChrome")
        getRootFragment()?.let { root ->
            Page.fromFragment(root)?.let { page ->
                this.page = page
                if (page.navItem > 0) {
                    navigationView.setCheckedItem(page.navItem)
                }
                page.dataGroup?.let {
                    displayBackButton(false)
                    val navItems = ArrayList<NavItem>()
                    navItems.apply {
                        if (root is ConfigurableFragment) {
                            add(NavItem("Page settings", R.drawable.icn_bar_viewsettings, HEADER, MENU_VIEW_SETTINGS))
                        }
                        add(NavItem("Change location", R.drawable.icn_bar_location, HEADER, MENU_CHANGE_LOCATION))
                        add(NavItem("Save location", R.drawable.icn_menu_myplaces, HEADER_IF_ROOM, MENU_SAVE_LOCATION))
                        add(NavItem("Time zone", R.drawable.icn_menu_timezone, MENU, MENU_TIME_ZONE))
                    }
                    setNavItems(navItems)
                } ?: run {
                    displayBackButton(true)
                    setNavItems(listOf())
                }
            }
        }
    }

    /**
     * Switch hamburger icon to back button.
     */
    private fun displayBackButton(enable: Boolean) {
        if (enable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            actionBarDrawerToggle?.toolbarNavigationClickListener = View.OnClickListener {
                supportFragmentManager.popBackStack()
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
            actionBarDrawerToggle?.toolbarNavigationClickListener = null
        }
    }

    override fun onCreateDialog(id: Int): Dialog {
        when (id) {
            DIALOG_SAVE -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Save location")

                val location = Prefs.selectedLocation(this)
                val view = layoutInflater.inflate(R.layout.dialog_save, null)
                val saveField = view.findViewById<EditText>(R.id.saveField)
                if (isNotEmpty(location!!.name) && isEmpty(saveField.text.toString())) {
                    saveField.setText(location.name)
                } else {
                    saveField.setText("")
                }
                dialog.setView(view)

                dialog.setPositiveButton("OK") { _, _ ->
                    val saveName = saveField.text.toString()
                    var db: DatabaseHelper? = null
                    try {
                        if (isNotEmpty(saveName)) {
                            db = DatabaseHelper(this@MainActivity)
                            location.name = saveName
                            Prefs.saveSelectedLocation(this@MainActivity, location)
                            db.addSavedLocation(location)
                            Toast.makeText(this@MainActivity, "This location has been saved", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "Please enter a name for this location", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        db?.close()
                    }
                    removeDialog(DIALOG_SAVE)
                }
                dialog.setNegativeButton("Cancel") { _, _ -> removeDialog(DIALOG_SAVE) }
                return dialog.create()
            }
        }
        return super.onCreateDialog(id)
    }

    private fun getRootFragment(): Fragment? {
        return supportFragmentManager.findFragmentByTag(ROOT)
    }

    /**
     * Returning from help and location screens pops them off the back stack. The action bar needs
     * to be updated, and when returning from location select the calendars must be reinitialised
     * and the data updated.
     */
    override fun onBackStackChanged() {
        refreshChrome()
        initCalendarAndLocation(false)
        (getRootFragment() as? AbstractDataFragment)?.update()
    }

    companion object {
        private const val DIALOG_SAVE = 746
        private const val STATE_PAGE = "page"
        private const val STATE_DATE_TIMESTAMP = "dateTimestamp"
        private const val STATE_TIME_TIMESTAMP = "timeTimestamp"

        private const val MENU_CHANGE_LOCATION = Menu.FIRST + 1
        private const val MENU_SAVE_LOCATION = Menu.FIRST + 2
        private const val MENU_VIEW_SETTINGS = Menu.FIRST + 10
        private const val MENU_TIME_ZONE = Menu.FIRST + 12

        private const val ROOT = "ROOT"

        private val TAG = MainActivity::class.java.name
    }

}
