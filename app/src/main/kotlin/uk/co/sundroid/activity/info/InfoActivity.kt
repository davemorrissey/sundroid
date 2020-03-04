package uk.co.sundroid.activity.info

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import uk.co.sundroid.AbstractActivity
import uk.co.sundroid.R
import uk.co.sundroid.R.id
import uk.co.sundroid.activity.info.fragments.AboutFragment
import uk.co.sundroid.activity.info.fragments.GlossaryFragment
import uk.co.sundroid.activity.info.fragments.GuideFragment

class InfoActivity : AbstractActivity() {

    enum class InfoGroup(val fragmentClass: Class<out Fragment>, val activeTab: Int, val inactiveTab: Int) {
        GUIDE (GuideFragment::class.java, id.guideTabActive, id.guideTabInactive),
        GLOSSARY (GlossaryFragment::class.java, id.glossaryTabActive, id.glossaryTabInactive),
        ABOUT (AboutFragment::class.java, id.aboutTabActive, id.aboutTabInactive)
    }

    private var infoGroup: InfoGroup = InfoGroup.GUIDE

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info)
        setActionBarTitle("Help")
        setDisplayHomeAsUpEnabled()
        initTabs()
        changeFragment()
    }

    override fun onNavBackSelected() {
        finish()
    }

    private fun changeFragment() {
        supportFragmentManager
                .beginTransaction()
                .replace(id.infoFragment, infoGroup.fragmentClass.newInstance(), "INFO")
                .commit()
    }

    private fun setTab(infoGroup: InfoGroup) {
        if (infoGroup != this.infoGroup) {
            this.infoGroup = infoGroup
            updateTabs()
            changeFragment()
        }
    }

    private fun initTabs() {
        InfoGroup.values().forEach {
            findViewById<View>(it.activeTab).setOnClickListener { _ -> setTab(it) }
            findViewById<View>(it.inactiveTab).setOnClickListener { _ -> setTab(it) }
        }
        updateTabs()
    }

    private fun updateTabs() {
        InfoGroup.values().forEach {
            findViewById<View>(it.activeTab).visibility = if (infoGroup == it) View.VISIBLE else View.GONE
            findViewById<View>(it.inactiveTab).visibility = if (infoGroup == it) View.GONE else View.VISIBLE
        }
    }

}
