package com.example.slowmotionapp.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.example.slowmotionapp.R
import com.example.slowmotionapp.adapters.ViewPagerAdapter
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.FragmentEffectMusicBinding
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.MusicApplied
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.filterPosition
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.myMusic
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.myMusicUri
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.wannaGoBack
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.wannaGoBackCheckViewModel
import com.example.slowmotionapp.utils.Utils.commandsGenerator
import com.example.slowmotionapp.utils.Utils.createCacheTempFile
import com.example.slowmotionapp.utils.Utils.createCroppedFile
import com.example.slowmotionapp.utils.Utils.deleteFromGallery
import com.example.slowmotionapp.utils.Utils.getAudioFilePathFromUri
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import com.example.slowmotionapp.utils.Utils.refreshAudioGallery
import com.example.slowmotionapp.utils.Utils.saveEditedVideo
import com.example.slowmotionapp.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import java.io.File

class EffectMusicFragment : Fragment() {

    private var _binding: FragmentEffectMusicBinding? = null
    private val binding get() = _binding!!

    private var childFragmentManager: FragmentManager? = null
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var filterTypes: List<FilterType>


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
        sharedViewModel.enhanced.observe(viewLifecycleOwner) { newValue ->
            if (newValue) {
                saveVideoWithFilter()
            }
        }
        sharedViewModel.downloadedMusic.observe(viewLifecycleOwner) { newValue ->
            setupFragment(newValue)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentEffectMusicBinding.inflate(inflater, container, false)

        binding.seekBarSpeaker.progress = 50
        binding.seekBarMusic.progress = 50

        filterTypes = FilterType.createFilterList()

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
                // Calculate the volume based on the SeekBar progress
                val volume = progress.toFloat() / seekBar.max

                sharedViewModel.videoVolumeLevelCheck(volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.seekBarMusic.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {

                if (fromUser) {
                    val minProgress = (seekBar.max * 0.2).toInt()
                    val restrictedProgress = if (progress < minProgress) minProgress else progress
                    seekBar.progress = restrictedProgress
                    // Calculate the volume based on the SeekBar progress
                    val volume = restrictedProgress.toFloat() / seekBar.max

                    sharedViewModel.audioVolumeLevelCheck(volume)
                }


            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
            }

        })

        binding.myMusicConstraint.setOnClickListener {
            openAudioFiles(Constants.PERMISSION_AUDIO)
        }

        binding.crossButton.setOnClickListener {
            binding.myMusicConstraint.visibility = View.VISIBLE
            binding.musicButton.visibility = View.GONE
            binding.seekBarMusic.visibility = View.GONE
            binding.crossButton.visibility = View.GONE
            MusicApplied = false
        }

        return binding.root
    }

    private fun openAudioFiles(permissionAudio: Array<String>) {
        for (permission in permissionAudio) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity() as Activity,
                    permission
                )
            ) {
                Toast.makeText(requireContext(), "Permission Denied", Toast.LENGTH_SHORT).show()
                break
            } else {
                if (ActivityCompat.checkSelfPermission(
                        requireActivity() as Activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //call the gallery intent
                    refreshAudioGallery(requireContext())
                    val i = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI)
                    i.type = "audio/*"
                    i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("audio/*"))
                    startActivityForResult(i, Constants.AUDIO_GALLERY)
                } else {
                    callPermissionSettings()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.AUDIO_GALLERY && resultCode == Activity.RESULT_OK) {
            // Handle the selected video here
            // Perform any required operations with the selected video

            setupFragment(getAudioFilePathFromUri(requireContext(), data?.data!!))
        }
    }

    private fun setupFragment(data: String?) {
        myMusicUri = data!!
        myMusic = true
        sharedViewModel.musicSetCheck(true)
        binding.myMusicConstraint.visibility = View.GONE
        binding.musicButton.visibility = View.VISIBLE
        binding.seekBarMusic.visibility = View.VISIBLE
        binding.crossButton.visibility = View.VISIBLE
        MusicApplied = true
    }


    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", requireContext().applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
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

    private fun saveVideoWithFilter() {

        val progressDialog =
            ProgressDialog(requireContext(), R.style.CustomDialog)
        progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")
        progressDialog.show()

        if (filterPosition != 0) {
            val outputFile = createCroppedFile().toString()
            val filter = FilterType.createGlFilter(
                FilterType.createFilterList()[filterPosition],
                requireContext()
            )
            Mp4Composer(mainCachedFile, outputFile)
                .rotation(Rotation.NORMAL)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(filter)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                    }

                    override fun onCompleted() {
                        mainCachedFile = outputFile
                        progressDialog.dismiss()

                        if (MusicApplied) {
                            audioVideoMixer()
                        } else {
                            if (wannaGoBack) {
                                wannaGoBackCheckViewModel.postValue(true)
                                wannaGoBack = false
                            } else {
                                saveEditedVideo(requireContext())
                            }
                        }
                    }

                    override fun onCanceled() {
                        progressDialog.dismiss()
                    }

                    override fun onFailed(exception: Exception) {
                        progressDialog.dismiss()
                        Log.e(Constants.APP_NAME, "onFailed() Filter", exception)
                    }
                })
                .start()
        } else {
            progressDialog.dismiss()
            if (MusicApplied) {
                audioVideoMixer()
            } else {
                saveEditedVideo(requireContext())
            }
        }
    }

    private fun audioVideoMixer() {

        val outputFile = createCacheTempFile(requireContext())

        val duration: Int = getVideoDuration(requireContext(), File(mainCachedFile)).toInt()

        executeFFMPEG(
            arrayOf(
                "-y",
                "-ss",
                "0",
                "-t",
                duration.toString(),
                "-i",
                mainCachedFile,
                "-ss",
                "0",
                "-i",
                myMusicUri,
                "-map",
                "0:0",
                "-map",
                "1:0",
                "-acodec",
                "copy",
                "-vcodec",
                "copy",
                "-preset",
                "ultrafast",
                "-shortest",
                "-c",
                "copy",
                outputFile
            ), outputFile
        )
    }

    private fun executeFFMPEG(strArr: Array<String>, str: String) {
        requireActivity().runOnUiThread {
            val progressDialog =
                ProgressDialog(requireContext(), R.style.CustomDialog)
            progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
            progressDialog.isIndeterminate = true
            progressDialog.setCancelable(false)
            progressDialog.setMessage("Please Wait")
            progressDialog.show()
            val ffmpegCommand: String = commandsGenerator(strArr)
            FFmpeg.executeAsync(
                ffmpegCommand
            ) { _, returnCode ->
                Config.printLastCommandOutput(Log.INFO)
                progressDialog.dismiss()
                when (returnCode) {
                    Config.RETURN_CODE_SUCCESS -> {
                        progressDialog.dismiss()
                        playVideo = str
                        mainCachedFile = str

                        if (wannaGoBack) {
                            wannaGoBack = false
                            sharedViewModel.wannaGoBackCheckFunction(true)
                        } else {
                            saveEditedVideo(requireContext())
                        }

                    }
                    Config.RETURN_CODE_CANCEL -> {
                        try {
                            File(str).delete()
                            deleteFromGallery(str, requireContext())
                            Toast.makeText(
                                requireContext(),
                                "Error Creating Video",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }
                    }
                    else -> {
                        try {
                            File(str).delete()
                            deleteFromGallery(str, requireContext())
                            Toast.makeText(
                                requireContext(),
                                "Error Creating Video",
                                Toast.LENGTH_LONG
                            ).show()
                        } catch (th: Throwable) {
                            th.printStackTrace()
                        }
                    }
                }
            }
        }
    }
}