package uk.co.sundroid

import android.app.Fragment
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView

/**
 * Provides some helper functions for fragments.
 */
abstract class AbstractFragment : Fragment() {

    protected val applicationContext: Context?
        get() {
            return activity?.applicationContext
        }

    protected fun textInView(view: View, id: Int, text: CharSequence) {
        val child = view.findViewById<TextView>(id)
        child.text = text
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

    protected fun hideInView(view: View, vararg ids: Int) {
        for (id in ids) {
            view.findViewById<View>(id).visibility = View.INVISIBLE
        }
    }

    protected fun imageInView(view: View, id: Int, drawable: Int) {
        (view.findViewById<View>(id) as ImageView).setImageResource(drawable)
    }

    protected fun view(name: String): Int {
        try {
            return id::class.java.getField(name).getInt(null)
        } catch (e: Exception) {
            throw IllegalArgumentException("No view with id $name exists")
        }
    }

}
