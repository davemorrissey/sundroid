package uk.co.sundroid.activity.info

import android.app.Fragment
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener

import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.R
import uk.co.sundroid.R.id
import uk.co.sundroid.activity.info.fragments.AboutFragment
import uk.co.sundroid.activity.info.fragments.GlossaryFragment
import uk.co.sundroid.activity.info.fragments.GuideFragment

import uk.co.sundroid.R.id.*

class InfoActivity : AbstractActivity(), OnClickListener {

    private var infoGroup: InfoGroup? = null

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info)
        setActionBarTitle("Help")
        setDisplayHomeAsUpEnabled()

        this.infoGroup = InfoGroup.GUIDE

        initTabs()
        changeFragment()
    }

    override fun onNavBackSelected() {
        finish()
    }

    private fun changeFragment() {
        remove(id.infoFragment)
        val fragment: Fragment
        if (this.infoGroup == InfoGroup.GUIDE) {
            fragment = GuideFragment()
        } else if (this.infoGroup == InfoGroup.GLOSSARY) {
            fragment = GlossaryFragment()
        } else {
            fragment = AboutFragment()
        }
        fragmentManager
                .beginTransaction()
                .replace(id.infoFragment, fragment, INFO_TAG)
                .commit()
        show(infoFragment)
    }

    private fun setTab(infoGroup: InfoGroup) {
        if (infoGroup != this.infoGroup) {
            this.infoGroup = infoGroup
            updateTabs()
            changeFragment()
        }
    }

    private fun initTabs() {
        findViewById<View>(guideTabActive).setOnClickListener { v -> setTab(InfoGroup.GUIDE) }
        findViewById<View>(guideTabInactive).setOnClickListener { v -> setTab(InfoGroup.GUIDE) }
        findViewById<View>(glossaryTabActive).setOnClickListener { v -> setTab(InfoGroup.GLOSSARY) }
        findViewById<View>(glossaryTabInactive).setOnClickListener { v -> setTab(InfoGroup.GLOSSARY) }
        findViewById<View>(aboutTabActive).setOnClickListener { v -> setTab(InfoGroup.ABOUT) }
        findViewById<View>(aboutTabInactive).setOnClickListener { v -> setTab(InfoGroup.ABOUT) }
        updateTabs()
    }

    private fun updateTabs() {
        findViewById<View>(guideTabActive).visibility = if (infoGroup == InfoGroup.GUIDE) View.VISIBLE else View.GONE
        findViewById<View>(guideTabInactive).visibility = if (infoGroup == InfoGroup.GUIDE) View.GONE else View.VISIBLE
        findViewById<View>(glossaryTabActive).visibility = if (infoGroup == InfoGroup.GLOSSARY) View.VISIBLE else View.GONE
        findViewById<View>(glossaryTabInactive).visibility = if (infoGroup == InfoGroup.GLOSSARY) View.GONE else View.VISIBLE
        findViewById<View>(aboutTabActive).visibility = if (infoGroup == InfoGroup.ABOUT) View.VISIBLE else View.GONE
        findViewById<View>(aboutTabInactive).visibility = if (infoGroup == InfoGroup.ABOUT) View.GONE else View.VISIBLE
    }

    companion object {

        private val INFO_TAG = "infoFragment"
    }

}
