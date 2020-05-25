package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.util.theme.*

import uk.co.sundroid.databinding.FragInfoGuideBinding

class InfoGuideFragment : AbstractFragment() {

    private lateinit var b: FragInfoGuideBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        b = FragInfoGuideBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapOf(
                b.infGuideDisclaimerDisclosure to b.infGuideDisclaimerBody,
                b.infGuideLocationDisclosure to b.infGuideLocationBody,
                b.infGuideOfflineDisclosure to b.infGuideOfflineBody,
                b.infGuideTimeDisclosure to b.infGuideTimeBody,
                b.infGuideZonesDisclosure to b.infGuideZonesBody,
                b.infGuideTrackerDisclosure to b.infGuideTrackerBody,
                b.infGuideWidgetsDisclosure to b.infGuideWidgetsBody
        ).forEach { (d, body) -> d.setOnClickListener { v -> toggle(v, body)} }
    }

    private fun toggle(disclosure: View, body: View) {
        val view = view
        if (view == null || isDetached) {
            return
        }
        if (body.visibility == View.GONE) {
            body.visibility = View.VISIBLE
            (disclosure as TextView).setCompoundDrawablesWithIntrinsicBounds(0, 0, getDisclosureOpen(), 0)
        } else {
            body.visibility = View.GONE
            (disclosure as TextView).setCompoundDrawablesWithIntrinsicBounds(0, 0, getDisclosureClosed(), 0)
        }
    }

}
