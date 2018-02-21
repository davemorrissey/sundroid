package uk.co.sundroid

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.view.View.OnClickListener
import android.widget.TextView

import uk.co.sundroid.util.theme.*

import uk.co.sundroid.NavItem.NavItemLocation.*

/**
 * Provides some helper functions for activities.
 */
abstract class AbstractActivity : Activity(), OnClickListener {

    private var navItems: List<NavItem>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityCreateSetTheme(this)
        window.requestFeature(Window.FEATURE_ACTION_BAR)
        window.setBackgroundDrawableResource(getAppBg())
        actionBar?.setBackgroundDrawable(resources.getDrawable(getActionBarBg()))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.clear()
        if (navItems != null) {
            for (navItem in navItems!!) {
                val menuItem = menu.add(Menu.NONE, navItem.action, Menu.NONE, navItem.title)
                if (navItem.icon > 0) {
                    menuItem.setIcon(navItem.icon)
                }
                menuItem.setShowAsAction(when {
                    navItem.location === HEADER -> MenuItem.SHOW_AS_ACTION_ALWAYS
                    navItem.location === HEADER_IF_ROOM -> MenuItem.SHOW_AS_ACTION_IF_ROOM
                    else -> MenuItem.SHOW_AS_ACTION_NEVER
                })
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (navItems != null) {
            for (navItem in navItems!!) {
                if (navItem.action == item.itemId) {
                    onNavItemSelected(navItem.action)
                    return true
                }
            }
        }
        onNavBackSelected()
        return true
    }

    override fun onClick(view: View) {

    }

    protected open fun onNavItemSelected(itemPosition: Int) {

    }

    protected open fun onNavBackSelected() {

    }

    protected fun setNavItems(navItems: List<NavItem>) {
        this.navItems = navItems
        invalidateOptionsMenu()
    }

    @JvmOverloads
    protected fun setActionBarTitle(title: String, subtitle: String? = null) {
        val actionBar = actionBar
        if (actionBar != null) {
            actionBar.title = title
            actionBar.subtitle = subtitle
        }
    }

    protected fun setDisplayHomeAsUpEnabled() {
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun text(id: Int, text: String) {
        val view = findViewById<TextView>(id)
        view.text = text
    }

    protected fun show(id: Int, text: CharSequence) {
        val view = findViewById<TextView>(id)
        view.visibility = View.VISIBLE
        view.text = text
    }

    protected fun show(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id).visibility = View.VISIBLE
        }
    }

    protected fun remove(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id).visibility = View.GONE
        }
    }

    protected fun hide(vararg ids: Int) {
        for (id in ids) {
            findViewById<View>(id).visibility = View.INVISIBLE
        }
    }

    protected fun textInView(view: View, id: Int, text: String?) {
        val child = view.findViewById<TextView>(id)
        child.text = text.orEmpty()
    }

    protected fun showInView(view: View, id: Int, text: CharSequence) {
        val child = view.findViewById<TextView>(id)
        child.visibility = View.VISIBLE
        child.text = text
    }

    protected fun showInView(view: View, vararg ids: Int) {
        for (id in ids) {
            view.findViewById<View>(id).visibility = View.VISIBLE
        }
    }

    protected fun removeInView(view: View, vararg ids: Int) {
        for (id in ids) {
            view.findViewById<View>(id).visibility = View.GONE
        }
    }

}
