package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.util.theme.*

import kotlinx.android.synthetic.main.frag_info_glossary.*

class GlossaryFragment : AbstractFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle?): View? {
        return inflater.inflate(R.layout.frag_info_glossary, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        infGlossaryApsisDisclosure.setOnClickListener { v -> toggle(v, infGlossaryApsisBody) }
        infGlossaryAzimuthDisclosure.setOnClickListener { v -> toggle(v, infGlossaryAzimuthBody) }
        infGlossaryCivTwiDisclosure.setOnClickListener { v -> toggle(v, infGlossaryCivTwiBody) }
        infGlossaryNtcTwiDisclosure.setOnClickListener { v -> toggle(v, infGlossaryNtcTwiBody) }
        infGlossaryAstTwiDisclosure.setOnClickListener { v -> toggle(v, infGlossaryAstTwiBody) }
        infGlossarySolNoonDisclosure.setOnClickListener { v -> toggle(v, infGlossarySolNoonBody) }
        infGlossaryGoldenDisclosure.setOnClickListener { v -> toggle(v, infGlossaryGoldenBody) }
        infGlossarySolsticeDisclosure.setOnClickListener { v -> toggle(v, infGlossarySolsticeBody) }
        infGlossaryEquinoxDisclosure.setOnClickListener { v -> toggle(v, infGlossaryEquinoxBody) }
        infGlossarySolEclipseDisclosure.setOnClickListener { v -> toggle(v, infGlossarySolEclipseBody) }
        infGlossaryLunarEclipseDisclosure.setOnClickListener { v -> toggle(v, infGlossaryLunarEclipseBody) }
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
