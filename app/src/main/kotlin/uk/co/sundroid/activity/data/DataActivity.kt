package uk.co.sundroid.activity.data

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.view.Menu
import android.view.View.OnClickListener
import android.widget.EditText
import android.widget.Toast

import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.NavItem
import uk.co.sundroid.R
import uk.co.sundroid.activity.data.fragments.*
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

abstract class DataActivity : AbstractActivity(), OnClickListener {

    private var fragment: AbstractDataFragment? = null

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

    private fun initialiseDataFragmentView() {
        val fragment = supportFragmentManager.findFragmentByTag(DATA_TAG)
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
        val fragment = supportFragmentManager.findFragmentByTag(DATA_TAG)
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

    override fun onCreateDialog(id: Int): Dialog {
        when (id) {
            DIALOG_SAVE -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Save location")

                val view = layoutInflater.inflate(layout.dialog_save, null)
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
                        } else {
                            Toast.makeText(this@DataActivity, "Please enter a name for this location", Toast.LENGTH_SHORT).show()
                        }
                    } finally {
                        db?.close()
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
//            MENU_HELP -> startActivity(Intent(this, InfoActivity::class.java))
            MENU_SETTINGS -> startActivity(Intent(this, AppSettingsActivity::class.java))
            MENU_VIEW_SETTINGS -> if (isFragmentConfigurable) {
                (supportFragmentManager.findFragmentByTag(DATA_TAG) as ConfigurableFragment).openSettingsDialog()
            }
        }
    }

    /********************************* NAVIGATION  */

    private fun startLocationOptions() {
    }

    private fun startSaveLocation() {
        showDialog(DIALOG_SAVE)
    }

    private fun startTimeZone() {
        val intent = Intent(applicationContext, TimeZonePickerActivity::class.java)
        intent.putExtra(TimeZonePickerActivity.INTENT_MODE, TimeZonePickerActivity.MODE_CHANGE)
        startActivityForResult(intent, TimeZonePickerActivity.REQUEST_TIMEZONE)
    }

    companion object {

        private const val DIALOG_SAVE = 746

        private const val MENU_CHANGE_LOCATION = Menu.FIRST + 1
        private const val MENU_SAVE_LOCATION = Menu.FIRST + 2
        private const val MENU_HELP = Menu.FIRST + 5
        private const val MENU_SETTINGS = Menu.FIRST + 6
        private const val MENU_VIEW_SETTINGS = Menu.FIRST + 10
        private const val MENU_TIME_ZONE = Menu.FIRST + 12

        private const val DATA_TAG = "dataFragment"

        private val TAG = DataActivity::class.java.simpleName
    }


}