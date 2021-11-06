package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.databinding.FragInfoGlossaryBinding
import uk.co.sundroid.util.theme.*

class InfoGlossaryFragment : AbstractFragment() {

    private lateinit var b: FragInfoGlossaryBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        b = FragInfoGlossaryBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mapOf(
                b.infGlossaryApsisDisclosure to b.infGlossaryApsisBody,
                b.infGlossaryAzimuthDisclosure to b.infGlossaryAzimuthBody,
                b.infGlossaryCivTwiDisclosure to b.infGlossaryCivTwiBody,
                b.infGlossaryNtcTwiDisclosure to b.infGlossaryNtcTwiBody,
                b.infGlossaryAstTwiDisclosure to b.infGlossaryAstTwiBody,
                b.infGlossarySolNoonDisclosure to b.infGlossarySolNoonBody,
                b.infGlossaryGoldenDisclosure to b.infGlossaryGoldenBody,
                b.infGlossarySolsticeDisclosure to b.infGlossarySolsticeBody,
                b.infGlossaryEquinoxDisclosure to b.infGlossaryEquinoxBody,
                b.infGlossarySolEclipseDisclosure to b.infGlossarySolEclipseBody,
                b.infGlossaryLunarEclipseDisclosure to b.infGlossaryLunarEclipseBody
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
