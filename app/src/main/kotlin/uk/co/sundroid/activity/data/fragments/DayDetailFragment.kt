package uk.co.sundroid.activity.data.fragments

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.View
import androidx.fragment.app.FragmentManager
import android.content.Intent
import androidx.fragment.app.FragmentPagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import uk.co.sundroid.R
import uk.co.sundroid.activity.MainActivity

class DayDetailFragment : AbstractDayFragment() {

    override val layout: Int
        get() = R.layout.frag_data_daydetail

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewPager = view.findViewById(R.id.viewPager) as ViewPager
        viewPager.adapter = DayDetailTabsAdapter(childFragmentManager)
        val tabLayout = view.findViewById(R.id.tabLayout) as TabLayout
        tabLayout.setupWithViewPager(viewPager)
    }

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).setToolbarSubtitle(R.string.data_detail_title)
    }

    override fun updateData(view: View) {
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(Intent("update"))
    }

    class DayDetailTabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getCount(): Int {
            return 4
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                1 -> "Moon"
                2 -> "Planets"
                3 -> "Events"
                else -> "Sun"
            }
        }

        override fun getItem(position: Int): Fragment {
            return when(position) {
                1 -> DayDetailMoonFragment()
                2 -> DayDetailPlanetsFragment()
                3 -> DayDetailEventsFragment()
                else -> DayDetailSunFragment()
            }
        }
    }
}