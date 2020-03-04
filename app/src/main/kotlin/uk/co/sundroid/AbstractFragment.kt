package uk.co.sundroid

import androidx.fragment.app.Fragment
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import uk.co.sundroid.util.prefs.PrefsWrapper

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

    protected val applicationContext: Context?
        get() {
            return activity?.applicationContext
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
        for (id in ids) {
            view.findViewById<View>(id).visibility = View.VISIBLE
        }
    }

    protected fun remove(view: View, vararg ids: Int) {
        for (id in ids) {
            view.findViewById<View>(id).visibility = View.GONE
        }
    }

    protected fun hide(view: View, vararg ids: Int) {
        for (id in ids) {
            view.findViewById<View>(id).visibility = View.INVISIBLE
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
