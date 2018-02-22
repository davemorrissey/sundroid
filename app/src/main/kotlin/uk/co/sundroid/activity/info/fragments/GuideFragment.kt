package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.util.theme.*

import uk.co.sundroid.R.id.*

class GuideFragment : AbstractFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle): View? {
        if (container == null) {
            return null
        }
        val view = inflater.inflate(R.layout.frag_info_guide, container, false)
        view.findViewById<View>(infGuideDisclaimerDisclosure).setOnClickListener { v -> toggle(v, R.id.infGuideDisclaimerBody) }
        view.findViewById<View>(infGuideLocationDisclosure).setOnClickListener { v -> toggle(v, R.id.infGuideLocationBody) }
        view.findViewById<View>(infGuideOfflineDisclosure).setOnClickListener { v -> toggle(v, R.id.infGuideOfflineBody) }
        view.findViewById<View>(infGuideTimeDisclosure).setOnClickListener { v -> toggle(v, R.id.infGuideTimeBody) }
        view.findViewById<View>(infGuideZonesDisclosure).setOnClickListener { v -> toggle(v, R.id.infGuideZonesBody) }
        view.findViewById<View>(infGuideTrackerDisclosure).setOnClickListener { v -> toggle(v, R.id.infGuideTrackerBody) }

        val disclaimer = "THIS DATA IS PROVIDED WITH NO GUARANTEE OF ACCURACY AND NO WARRANTY OF FITNESS FOR ANY PURPOSE.\n\nTimes are usually accurate to within a few minutes, but they do not take into account local geography or your altitude, so the observed times in hilly regions may be different. Errors in the far North and South can be much greater."
        textInView(view, R.id.infGuideDisclaimerText, disclaimer)
        return view
    }

    private fun toggle(disclosure: View, bodyId: Int) {
        val view = view
        if (view == null || isDetached) {
            return
        }
        val bodyView = view.findViewById<View>(bodyId)
        if (bodyView.visibility == View.GONE) {
            bodyView.visibility = View.VISIBLE
            (disclosure as TextView).setCompoundDrawablesWithIntrinsicBounds(0, 0, getDisclosureOpen(), 0)
        } else {
            bodyView.visibility = View.GONE
            (disclosure as TextView).setCompoundDrawablesWithIntrinsicBounds(0, 0, getDisclosureClosed(), 0)
        }
    }

}
