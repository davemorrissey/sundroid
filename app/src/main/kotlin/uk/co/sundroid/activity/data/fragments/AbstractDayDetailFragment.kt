package uk.co.sundroid.activity.data.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

abstract class AbstractDayDetailFragment : AbstractDataFragment() {

    protected abstract val layout: Int

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateData(view)
    }

    override fun update() {
        val view = view
        if (isSafe && view != null) {
            updateData(view)
        }
    }

    protected abstract fun updateData(view: View)

}
