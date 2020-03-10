package uk.co.sundroid

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity

import uk.co.sundroid.util.theme.*

import uk.co.sundroid.NavItem.NavItemLocation.*

/**
 * Provides some helper functions for activities.
 */
abstract class AbstractActivity : AppCompatActivity() {

    private var navItems: List<NavItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityCreateSetTheme(this)
        window.setBackgroundDrawableResource(getAppBg())
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        navItems?.forEach {
            val menuItem = menu.add(Menu.NONE, it.action, Menu.NONE, it.title)
            if (it.icon > 0) {
                menuItem.setIcon(it.icon)
            }
            menuItem.setShowAsAction(when {
                it.location === HEADER -> MenuItem.SHOW_AS_ACTION_ALWAYS
                it.location === HEADER_IF_ROOM -> MenuItem.SHOW_AS_ACTION_IF_ROOM
                else -> MenuItem.SHOW_AS_ACTION_NEVER
            })
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        navItems?.forEach {
            if (it.action == item.itemId) {
                onNavItemSelected(it.action)
                return true
            }
        }
        return true
    }

    protected open fun onNavItemSelected(itemPosition: Int) {

    }

    protected fun setNavItems(navItems: List<NavItem>) {
        this.navItems = navItems
        invalidateOptionsMenu()
    }

}
