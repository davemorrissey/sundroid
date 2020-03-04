package uk.co.sundroid.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import kotlinx.android.synthetic.main.main.*
import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.BuildConfig
import uk.co.sundroid.R
import uk.co.sundroid.activity.location.LocationSelectActivity
import uk.co.sundroid.activity.settings.AppSettingsActivity
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.location.LatitudeLongitude
import uk.co.sundroid.util.prefs.Prefs
import uk.co.sundroid.util.time.TimeZoneResolver
import java.util.*


class MainActivity : AbstractActivity() {

    private var page: Page = Page.DAY_DETAIL
    private var backPage: Page? = null

    var dateCalendar: Calendar = Calendar.getInstance()
    var timeCalendar: Calendar = Calendar.getInstance()
    var actionBarDrawerToggle: ActionBarDrawerToggle? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setSupportActionBar(toolbar)
        initNavigationDrawer()

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
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action != null && intent.action == Intent.ACTION_MAIN) {
            intent.action = null
            initCalendarAndLocation(true)
        }
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
        state.putSerializable(STATE_PAGE, this.page)
        state.putLong(STATE_DATE_TIMESTAMP, this.dateCalendar.timeInMillis)
        state.putLong(STATE_TIME_TIMESTAMP, this.timeCalendar.timeInMillis)
    }

    private fun restoreState(state: Bundle?) {
        this.page = Page.DAY_DETAIL
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

    private fun setPage(page: Page, force: Boolean = false) {
        if (page != this.page || force) {
            if (page.dataGroup != null) {
                Prefs.setLastDataGroup(this, page.dataGroup)
            } else {
                this.backPage = this.page
            }
            this.page = page
            val existingFragment = supportFragmentManager.findFragmentByTag("ROOT")
            if (existingFragment?.javaClass != page.fragmentClass || force) {
                val tx = supportFragmentManager.beginTransaction()
                        .replace(R.id.content, page.fragmentClass.newInstance(), "ROOT")
                if (page.dataGroup == null) {
                    tx.addToBackStack(null)
                    displayBackButton(true)
                } else {
                    displayBackButton(false)
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

    fun setToolbarTitle(title: Int) {
        toolbar?.setTitle(title)
    }

    fun setToolbarSubtitle(subtitle: String? = null) {
        toolbar?.subtitle = subtitle
    }

    fun setToolbarSubtitle(subtitle: Int) {
        toolbar?.setSubtitle(subtitle)
    }

    fun setDisplayHomeAsUp(homeAsUp: Boolean) {
//        supportActionBar?.setDisplayHomeAsUpEnabled(homeAsUp)
    }

    private fun initNavigationDrawer() {
        navigationView.setCheckedItem(R.id.dayDetail)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.dayDetail -> setPage(Page.DAY_DETAIL)
//                R.id.tracker -> setPage(Page.TRACKER)
//                R.id.calendars -> setPage(Page.MONTH_CALENDARS)
//                R.id.yearEvents -> setPage(Page.YEAR_EVENTS)
                R.id.location -> openActivity(LocationSelectActivity::class.java)
                R.id.help -> setPage(Page.HELP)
                R.id.settings -> openActivity(AppSettingsActivity::class.java)
            }
            true
        }
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle!!)
        actionBarDrawerToggle?.syncState()
    }

    private fun displayBackButton(enable: Boolean) {
        if (enable) {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            actionBarDrawerToggle?.isDrawerIndicatorEnabled = false
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            actionBarDrawerToggle?.toolbarNavigationClickListener = View.OnClickListener {
                onBackPressed()
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            actionBarDrawerToggle?.isDrawerIndicatorEnabled = true
            actionBarDrawerToggle?.toolbarNavigationClickListener = null
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        displayBackButton(false)
        if (backPage != null) {
            page = backPage!!
            backPage = null
        }
    }

    companion object {
        private const val STATE_PAGE = "page"
        private const val STATE_DATE_TIMESTAMP = "dateTimestamp"
        private const val STATE_TIME_TIMESTAMP = "timeTimestamp"
    }
}
