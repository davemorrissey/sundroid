package uk.co.sundroid.activity.info.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import uk.co.sundroid.AbstractFragment
import uk.co.sundroid.activity.MainActivity
import uk.co.sundroid.databinding.FragInfoBinding

class InfoFragment : AbstractFragment() {

    private lateinit var b: FragInfoBinding

    override fun onResume() {
        super.onResume()
        (activity as MainActivity).apply {
            setToolbarTitle("Help")
            setToolbarSubtitle(null)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View? {
        b = FragInfoBinding.inflate(inflater)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        b.viewPager.adapter = InfoTabsAdapter(childFragmentManager)
        b.tabLayout.setupWithViewPager(b.viewPager)
    }

    class InfoTabsAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getCount(): Int {
            return 2
        }

        override fun getPageTitle(position: Int): CharSequence {
            return when (position) {
                1 -> "Glossary"
                else -> "Guide"
            }
        }

        override fun getItem(position: Int): Fragment {
            return when(position) {
                1 -> InfoGlossaryFragment()
                else -> InfoGuideFragment()
            }
        }
    }

}
