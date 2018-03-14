package uk.co.sundroid

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.*
import android.view.View.OnClickListener
import android.widget.TextView
import android.widget.Toast

import uk.co.sundroid.util.theme.*

import uk.co.sundroid.NavItem.NavItemLocation.*
import uk.co.sundroid.util.prefs.PrefsWrapper
import uk.co.sundroid.util.view.SimpleAlertFragment

/**
 * Provides some helper functions for activities.
 */
abstract class AbstractActivity : AppCompatActivity(), OnClickListener {

    private var navItems: List<NavItem>? = null

    private var _prefs: PrefsWrapper? = null

    val prefs: PrefsWrapper
        get() {
            this._prefs?.let { return it }
            val prefs = PrefsWrapper(this)
            this._prefs = prefs
            return prefs
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onActivityCreateSetTheme(this)
//        window.requestFeature(Window.FEATURE_ACTION_BAR)
        window.setBackgroundDrawableResource(getAppBg())
//        actionBar?.setBackgroundDrawable(resources.getDrawable(getActionBarBg()))
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

    protected fun setActionBarTitle(title: String, subtitle: String? = null) {
        actionBar?.title = title
        actionBar?.subtitle = subtitle
    }

    protected fun setDisplayHomeAsUpEnabled() {
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    protected fun text(id: Int, text: String?) {
        val view = findViewById<TextView>(id)
        view.text = text.orEmpty()
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

    protected fun remove(vararg views: View) {
        for (view in views) {
            view.visibility = View.GONE
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

    protected fun alert(title: String, message: String) {
        SimpleAlertFragment.show(this, title, message)
    }

    protected fun alert(title: Int, message: Int) {
        SimpleAlertFragment.show(this, title, message)
    }

    protected fun longToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

}
