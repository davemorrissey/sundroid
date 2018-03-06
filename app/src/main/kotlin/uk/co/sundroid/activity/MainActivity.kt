package uk.co.sundroid.activity

import android.os.Bundle

import kotlinx.android.synthetic.main.main.*
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private var fragmentId: Int = R.id.dayDetail

    var dateCalendar: Calendar = Calendar.getInstance()
    var timeCalendar: Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        setSupportActionBar(toolbar)
        initNavigationDrawer()
        if (savedInstanceState?.containsKey(BUNDLE_FRAGMENT) == true) {
            fragmentId = savedInstanceState.getInt(BUNDLE_FRAGMENT)
        }
        setFragment(fragmentId)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState?.putInt(BUNDLE_FRAGMENT, fragmentId)
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

    private fun initNavigationDrawer() {
        navigationView.setCheckedItem(R.id.dayDetail)
        navigationView.setNavigationItemSelectedListener { menuItem ->
            drawerLayout.closeDrawers()
            when (menuItem.itemId) {
                R.id.dayDetail,
                R.id.tracker,
                R.id.calendars,
                R.id.yearEvents -> setFragment(menuItem.itemId)
            }
            true
        }
        val actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    private fun setFragment(id: Int) {
        fragmentId = id
        val fragmentClass = when(id) {
//            R.id.daySummary -> DaySummaryFragment::class.java
            R.id.dayDetail -> DayDetailSunFragment::class.java
            R.id.tracker -> TrackerFragment::class.java
            R.id.calendars -> MonthCalendarsFragment::class.java
            R.id.yearEvents -> YearEventsFragment::class.java
            else -> DayDetailSunFragment::class.java
        }
        val existingFragment = fragmentManager.findFragmentByTag("ROOT")
        if (existingFragment?.javaClass != fragmentClass) {
            fragmentManager.beginTransaction()
                    .replace(R.id.content, fragmentClass.newInstance(), "ROOT")
                    .commit()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    companion object {
        private const val BUNDLE_FRAGMENT = "FRAGMENT"
    }
}
