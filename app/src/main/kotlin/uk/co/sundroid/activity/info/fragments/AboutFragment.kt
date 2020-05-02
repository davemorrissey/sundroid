package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.BuildConfig

import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.FragAboutBinding

class AboutFragment : AbstractFragment() {

    private lateinit var b: FragAboutBinding

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("About")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        b = FragAboutBinding.inflate(layoutInflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val version = String.format("Version %s", BuildConfig.VERSION_NAME)
        b.aboutVersion.text = version
        b.aboutSource.setOnClickListener { browseTo("https://github.com/davemorrissey/sundroid") }
    }
}
