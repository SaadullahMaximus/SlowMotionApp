package com.example.slowmotionapp.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.example.slowmotionapp.R
import com.example.slowmotionapp.adapters.ViewPagerAdapter
import com.example.slowmotionapp.databinding.FragmentEffectMusicBinding
import com.google.android.material.tabs.TabLayout

class EffectMusicFragment : Fragment() {

    private var _binding: FragmentEffectMusicBinding? = null
    private val binding get() = _binding!!

    private var childFragmentManager: FragmentManager? = null

    private lateinit var audioManager: AudioManager

    private lateinit var volumeChangeReceiver: BroadcastReceiver

    private val volumeChangedAction = "android.media.VOLUME_CHANGED_ACTION"

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioManager = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Register a broadcast receiver to listen for volume changes and audio becoming noisy
        volumeChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val action = intent.action
                if (action == volumeChangedAction || action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                    updateSeekBar()
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(volumeChangedAction)
            addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        }
        requireActivity().registerReceiver(volumeChangeReceiver, filter)

        // Update the SeekBar initially
        updateSeekBar()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEffectMusicBinding.inflate(inflater, container, false)

        childFragmentManager = getChildFragmentManager()

        val viewPagerAdapter = ViewPagerAdapter(childFragmentManager!!)

        viewPagerAdapter.add(EffectFragment(), "Effect")
        viewPagerAdapter.add(MusicFragment(), "Default Music")

        binding.viewpager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewpager)
        binding.tabLayout.setTabTextColors(R.color.baseColor, R.color.baseColor)

        val tab1: TabLayout.Tab? = binding.tabLayout.getTabAt(0)
        val tab2: TabLayout.Tab? = binding.tabLayout.getTabAt(1)

        tab1?.customView = createTabView("Filter", R.drawable.filter_ic)
        tab2?.customView = createTabView("Default Music", R.drawable.music_ic)

        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
            }

            override fun onPageScrollStateChanged(state: Int) {}

        })

        binding.seekBarSpeaker.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                val volume = (maxVolume * progress) / 100
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })



        return binding.root
    }

    // Create a custom view for the tab with both icon and text
    private fun createTabView(text: String, iconResId: Int): View {
        val tabView =
            LayoutInflater.from(requireContext()).inflate(R.layout.custom_tab_layout, null)
        val textView = tabView.findViewById<TextView>(R.id.tab_text)
        val iconView = tabView.findViewById<ImageView>(R.id.tab_icon)

        textView.text = text
        iconView.setImageResource(iconResId)

        return tabView
    }

    private fun updateSeekBar() {
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val progress = (currentVolume * 100) / maxVolume
        binding.seekBarSpeaker.progress = progress
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(volumeChangeReceiver)
    }

}