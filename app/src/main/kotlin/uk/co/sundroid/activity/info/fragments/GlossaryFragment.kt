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

class GlossaryFragment : AbstractFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle): View? {
        if (container == null) {
            return null
        }
        val view = inflater.inflate(R.layout.frag_info_glossary, container, false)
        view.findViewById<View>(infGlossaryApsisDisclosure).setOnClickListener { v -> toggle(v, infGlossaryApsisBody) }
        view.findViewById<View>(infGlossaryAzimuthDisclosure).setOnClickListener { v -> toggle(v, infGlossaryAzimuthBody) }
        view.findViewById<View>(infGlossaryCivTwiDisclosure).setOnClickListener { v -> toggle(v, infGlossaryCivTwiBody) }
        view.findViewById<View>(infGlossaryNtcTwiDisclosure).setOnClickListener { v -> toggle(v, infGlossaryNtcTwiBody) }
        view.findViewById<View>(infGlossaryAstTwiDisclosure).setOnClickListener { v -> toggle(v, infGlossaryAstTwiBody) }
        view.findViewById<View>(infGlossarySolNoonDisclosure).setOnClickListener { v -> toggle(v, infGlossarySolNoonBody) }
        view.findViewById<View>(infGlossaryGoldenDisclosure).setOnClickListener { v -> toggle(v, infGlossaryGoldenBody) }
        view.findViewById<View>(infGlossarySolsticeDisclosure).setOnClickListener { v -> toggle(v, infGlossarySolsticeBody) }
        view.findViewById<View>(infGlossaryEquinoxDisclosure).setOnClickListener { v -> toggle(v, infGlossaryEquinoxBody) }
        view.findViewById<View>(infGlossarySolEclipseDisclosure).setOnClickListener { v -> toggle(v, infGlossarySolEclipseBody) }
        view.findViewById<View>(infGlossaryLunarEclipseDisclosure).setOnClickListener { v -> toggle(v, infGlossaryLunarEclipseBody) }
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
