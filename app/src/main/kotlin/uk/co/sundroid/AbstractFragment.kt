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
import uk.co.sundroid.util.html

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
        parentFragmentManager.popBackStack(null, POP_BACK_STACK_INCLUSIVE)
    }

    protected fun toggle(view: View, on: Int, off: Int) {
        view.findViewById<View>(on).visibility = View.VISIBLE
        view.findViewById<View>(off).visibility = View.GONE
    }

    protected fun modifyChild(parent: View,
                              vararg ids: Int,
                              visibility: Int? = null,
                              text: CharSequence? = null,
                              html: String? = null,
                              image: Int? = null,
                              bitmap: Bitmap? = null) {
        ids.forEach { id -> run {
            val view = parent.findViewById<View>(id)
            visibility?.let { view.visibility = it }
            text?.let { (view as? TextView)?.text = it }
            html?.let { (view as? TextView)?.text = html(it) }
            image?.let { view as? ImageView}?.setImageResource(image)
            bitmap?.let { view as? ImageView}?.setImageBitmap(bitmap)
        }}
    }

    protected fun modify(vararg views: View,
                         visibility: Int? = null,
                         text: CharSequence? = null,
                         html: String? = null,
                         image: Int? = null) {
        views.forEach { view -> run {
            visibility?.let { view.visibility = it }
            text?.let { (view as? TextView)?.text = it }
            html?.let { (view as? TextView)?.text = html(it) }
            image?.let { view as? ImageView}?.setImageResource(image)
        }}
    }

    protected fun id(name: String): Int {
        try {
            return R.id::class.java.getField(name).getInt(null)
        } catch (e: Exception) {
            throw IllegalArgumentException("No view with id $name exists")
        }
    }

    protected fun inflate(id: Int, parent: ViewGroup? = null, attachToRoot: Boolean = false): View {
        return requireActivity().layoutInflater.inflate(id, parent, attachToRoot)
    }

    protected fun browseTo(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(it)
            startActivity(intent)
        }
    }

}
