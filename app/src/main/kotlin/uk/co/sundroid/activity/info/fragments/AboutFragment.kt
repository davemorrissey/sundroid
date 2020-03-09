package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.BuildConfig
import uk.co.sundroid.R.layout

import kotlinx.android.synthetic.main.frag_about.*
import uk.co.sundroid.activity.MainActivity

class AboutFragment : AbstractFragment() {

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("About")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        return inflater.inflate(layout.frag_about, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val version = String.format("Version %s", BuildConfig.VERSION_NAME)
        aboutVersion.text = version
        aboutSource.setOnClickListener { browseTo("https://github.com/davemorrissey/sundroid") }
    }
}
