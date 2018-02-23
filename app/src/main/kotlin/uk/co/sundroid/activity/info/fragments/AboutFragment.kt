package uk.co.sundroid.activity.info.fragments

import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.BuildConfig
import uk.co.sundroid.R.layout

import kotlinx.android.synthetic.main.frag_info_about.*

class AboutFragment : AbstractFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup, state: Bundle?): View? {
        return inflater.inflate(layout.frag_info_about, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val version = String.format("Sundroid %s", BuildConfig.VERSION_NAME)
        val info = "Build: ${BuildConfig.VERSION_CODE}, API: ${VERSION.SDK_INT}\n${Build.MANUFACTURER} ${Build.DEVICE}"
        aboutVersion.text = version
        aboutCodes.text = info
    }
}
