package uk.co.sundroid.activity.info.fragments

import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.BuildConfig
import uk.co.sundroid.R
import uk.co.sundroid.R.layout

class AboutFragment : AbstractFragment() {

    val versionCodes: String
        get() = "Build: " +
                BuildConfig.VERSION_CODE +
                ", API: " +
                VERSION.SDK_INT +
                "\n" +
                Build.MANUFACTURER +
                " " +
                Build.DEVICE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle): View? {
        if (container == null) {
            return null
        }
        val view = inflater.inflate(layout.frag_info_about, container, false)
        val version = String.format("Sundroid %s$1", BuildConfig.VERSION_NAME)
        textInView(view, R.id.aboutVersion, version)
        textInView(view, R.id.aboutCodes, versionCodes)
        return view
    }

}
