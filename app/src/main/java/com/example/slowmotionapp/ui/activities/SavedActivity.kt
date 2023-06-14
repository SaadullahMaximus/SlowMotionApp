package com.example.slowmotionapp.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.ActivitySavedBinding
import com.example.slowmotionapp.ui.fragments.savedfragments.SavedCropFragment
import com.example.slowmotionapp.ui.fragments.savedfragments.SavedEditedFragment
import com.example.slowmotionapp.ui.fragments.savedfragments.SavedTrimFragment
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.Utils.fetchVideosFromDirectory
import com.google.android.material.tabs.TabLayout

class SavedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedBinding

    companion object {
        val trimmedFiles = fetchVideosFromDirectory(Utils.trimmedDir)
        val croppedFiles = fetchVideosFromDirectory(Utils.croppedDir)
        val editedFiles = fetchVideosFromDirectory(Utils.editedDir)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPagerAdapter = ViewPagerSetter(supportFragmentManager)

        viewPagerAdapter.addFragment(SavedEditedFragment(), "Edited")
        viewPagerAdapter.addFragment(SavedTrimFragment(), "Trimmed")
        viewPagerAdapter.addFragment(SavedCropFragment(), "Cropped")

        binding.viewpager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewpager)
        binding.tabLayout.setTabTextColors(R.color.baseColor, R.color.baseColor)

        val tab1: TabLayout.Tab? = binding.tabLayout.getTabAt(0)
        val tab2: TabLayout.Tab? = binding.tabLayout.getTabAt(1)
        val tab3: TabLayout.Tab? = binding.tabLayout.getTabAt(2)

        tab1?.customView = createTabView("Edited")
        tab2?.customView = createTabView("Trimmed")
        tab3?.customView = createTabView("Cropped")

        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    private class ViewPagerSetter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitleList = mutableListOf<String>()

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun createTabView(text: String): View {
        val tabView =
            LayoutInflater.from(this).inflate(R.layout.custome_saved_layout, null)
        val textView = tabView.findViewById<TextView>(R.id.tabText)

        textView.text = text
        return tabView
    }


}