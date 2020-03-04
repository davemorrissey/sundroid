package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity

class InfoFragment : AbstractFragment() {

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarTitle("Help")
        (activity as MainActivity).setToolbarSubtitle(null)
        (activity as MainActivity).setDisplayHomeAsUp(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        if (container == null) {
            return null
        }
        return inflater.inflate(R.layout.frag_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById(R.id.viewPager) as ViewPager
        viewPager.adapter = InfoTabsAdapter(childFragmentManager)
        val tabLayout = view.findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
    }

    class InfoTabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return 3
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                1 -> "Glossary"
                2 -> "About"
                else -> "Guide"
            }
        }

        override fun getItem(position: Int): Fragment {
            return when(position) {
                1 -> InfoGlossaryFragment()
                2 -> InfoAboutFragment()
                else -> InfoGuideFragment()
            }
        }
    }

}
