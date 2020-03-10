package uk.co.sundroid

import androidx.fragment.app.Fragment
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.activity.Page
import uk.co.sundroid.util.prefs.PrefsWrapper
import androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE
import uk.co.sundroid.domain.LocationDetails
import uk.co.sundroid.util.prefs.Prefs

/**
 * Provides some helper functions for fragments.
 */
abstract class AbstractFragment : Fragment() {

    private var _prefs: PrefsWrapper? = null

    val prefs: PrefsWrapper
        get() {
            this._prefs?.let { return it }
            val prefs = PrefsWrapper(requireContext())
            this._prefs = prefs
            return prefs
        }

    protected fun onLocationSelected(location: LocationDetails) {
        Prefs.saveSelectedLocation(requireContext(), location)
        if (location.timeZone == null) {
            setPage(Page.TIME_ZONE)
        } else {
            returnToData()
        }
    }

    protected fun setPage(page: Page) {
        (activity as? MainActivity)?.setPage(page)
    }

    protected fun returnToData() {
        requireFragmentManager().popBackStack(null, POP_BACK_STACK_INCLUSIVE)
    }

    protected fun text(view: View, id: Int, text: CharSequence) {
        val child = view.findViewById<TextView>(id)
        child.text = text
    }

    protected fun show(view: View, id: Int, text: CharSequence) {
        val child = view.findViewById<TextView>(id)
        child.visibility = View.VISIBLE
        child.text = text
    }

    protected fun show(view: View, vararg ids: Int) {
        view.visibility = View.VISIBLE
        ids.forEach {
            view.findViewById<View>(it).visibility = View.VISIBLE
        }
    }

    protected fun text(view: TextView, text: CharSequence) {
        view.visibility = View.VISIBLE
        view.text = text
    }

    protected fun visible(vararg views: View) {
        views.forEach { it.visibility = View.VISIBLE }
    }

    protected fun gone(vararg views: View) {
        views.forEach { it.visibility = View.GONE }
    }

    protected fun remove(view: View, vararg ids: Int) {
        ids.forEach {
            view.findViewById<View>(it).visibility = View.GONE
        }
    }

    protected fun hide(view: View, vararg ids: Int) {
        ids.forEach {
            view.findViewById<View>(it).visibility = View.INVISIBLE
        }
    }

    protected fun toggle(view: View, on: Int, off: Int) {
        view.findViewById<View>(on).visibility = View.VISIBLE
        view.findViewById<View>(off).visibility = View.GONE
    }

    protected fun image(view: View, id: Int, drawable: Int) {
        (view.findViewById<View>(id) as ImageView).setImageResource(drawable)
    }

    protected fun image(view: View, id: Int, bitmap: Bitmap) {
        view.findViewById<ImageView>(id).setImageBitmap(bitmap)
    }

    protected fun view(name: String): Int {
        try {
            return R.id::class.java.getField(name).getInt(null)
        } catch (e: Exception) {
            throw IllegalArgumentException("No view with id $name exists")
        }
    }

    protected fun inflate(id: Int, parent: ViewGroup? = null): View {
        return requireActivity().layoutInflater.inflate(id, parent)
    }


    protected fun browseTo(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(it)
            startActivity(intent)
        }
    }

}
