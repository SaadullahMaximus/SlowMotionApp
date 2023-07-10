package com.example.slowmotionapp.ui.fragments

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
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
import com.example.slowmotionapp.customviews.CustomWaitingDialog
import com.example.slowmotionapp.databinding.FragmentEffectMusicBinding
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.MusicApplied
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.backSave
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
import com.example.slowmotionapp.utils.Utils.getAudioFilePath
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import com.example.slowmotionapp.utils.Utils.logVideoBitrate
import com.example.slowmotionapp.utils.Utils.saveEditedVideo
import com.example.slowmotionapp.utils.Utils.singleClick
import com.example.slowmotionapp.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import java.io.File

class EffectMusicFragment : Fragment() {

    private var _binding: FragmentEffectMusicBinding? = null
    private val binding get() = _binding!!

    private var childFragmentManager: FragmentManager? = null
    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var filterTypes: List<FilterType>

    private var ifMuted = false

    private var progressBeforeMute = 50

    private var seekBarMax = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.enhanced.observe(viewLifecycleOwner) { newValue ->
            if (newValue) {
                Log.d("HELLOJIMMY", "showFullScreenDialog: Video Going to Enhanced")
                saveVideoWithFilter()
                sharedViewModel.enhanced(false)
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
                seekBarMax = seekBar.max
                val volume = progress.toFloat() / seekBarMax
                progressBeforeMute = progress

                sharedViewModel.videoVolumeLevelCheck(volume)

                if (progress == 0) {
                    ifMuted = true
                    binding.speakerButton.setImageResource(R.drawable.mute_icon)
                } else {
                    ifMuted = false
                    binding.speakerButton.setImageResource(R.drawable.speaker_ic)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}

            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.speakerButton.setOnClickListener {
            if (ifMuted) {
                ifMuted = false
                binding.seekBarSpeaker.progress = progressBeforeMute
                binding.speakerButton.setImageResource(R.drawable.speaker_ic)
                val volume = progressBeforeMute.toFloat() / seekBarMax
                sharedViewModel.videoVolumeLevelCheck(volume)
            } else {
                ifMuted = true
                binding.seekBarSpeaker.progress = 0
                binding.speakerButton.setImageResource(R.drawable.mute_icon)
                sharedViewModel.videoVolumeLevelCheck(0F)
            }
        }

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
            sharedViewModel.stopAllMusic(true)
            singleClick {
                openAudioFiles(Constants.PERMISSION_AUDIO)
            }
        }

        binding.crossButton.setOnClickListener {
            binding.myMusicConstraint.visibility = View.VISIBLE
            binding.musicButton.visibility = View.GONE
            binding.seekBarMusic.visibility = View.GONE
            binding.crossButton.visibility = View.GONE
            sharedViewModel.crossClick(true)
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
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.type = "audio/*"
                    startActivityForResult(intent, Constants.AUDIO_GALLERY)
                } else {
                    callPermissionSettings()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.AUDIO_GALLERY && resultCode == Activity.RESULT_OK) {

            Log.d("DATADATA", "onActivityResult: ${data?.data}")

            setupFragment(getAudioFilePath(requireContext(), data!!.data!!))

        } else if (requestCode == 456 && resultCode == Activity.RESULT_OK) {
            setupFragment(getAudioFilePath(requireContext(), data?.data!!))
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


        Log.d("HELLOJIMMY", "showFullScreenDialog: saveVideoWithFilter()")

        if (filterPosition != 0) {

            var targetWidth = 0 //720
            var targetHeight = 0 //1280

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(requireContext(), Uri.parse(mainCachedFile))

            val originalWidth =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                    ?: 0
            val originalHeight =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                    ?: 0

            if (originalWidth < originalHeight) {
                targetWidth = 720
                targetHeight = 1280
            } else if (originalWidth > originalHeight) {
                targetWidth = 1280
                targetHeight = 720
            } else {
                targetWidth = 1280
                targetHeight = 1280
            }

            logVideoBitrate(mainCachedFile)

            Log.d("VideoRes", "saveVideoWithFilter: $originalWidth $originalHeight")

            if (originalWidth > targetWidth || originalHeight > targetHeight) {
                Log.d("VideoRes", "saveVideoWithFilter: IF")
                val outputFilePath = createCacheTempFile(requireContext())

                val aspectRatio = "" + targetWidth + "x" + "" + targetHeight

                val ffmpegCommand =
                    arrayOf(
                        "-ss",
                        "0",
                        "-y",
                        "-i",
                        mainCachedFile,
                        "-t",
                        getVideoDuration(requireContext(), mainCachedFile).toString(),
                        "-s",
                        aspectRatio,
                        "-r",
                        "25",
                        "-vcodec",
                        "mpeg4",
                        "-b:v",
                        "150k",
                        "-b:a",
                        "48000",
                        "-ac",
                        "2",
                        "-ar",
                        "22050",
                        outputFilePath
                    )
                executeFFMPEGCommand(ffmpegCommand, outputFilePath)
            } else {
                logVideoBitrate(mainCachedFile)
                Log.d("VideoRes", "saveVideoWithFilter: ELSE")
                applyFilter()
            }
        } else {
            if (MusicApplied) {
                audioVideoMixer()
            } else {
                if (wannaGoBack) {
                    wannaGoBackCheckViewModel.postValue(true)
                    backSave = false

                    requireActivity().runOnUiThread {
                        sharedViewModel.enhanced(false)
                    }
                    wannaGoBack = false
                } else {
                    saveEditedVideo(requireContext())
                }
            }
        }
    }

    private fun executeFFMPEGCommand(command: Array<String>, outputFilePath: String) {
        val progressDialog = CustomWaitingDialog(requireContext())
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            FFmpeg.cancel()
        }
        progressDialog.show()

        val ffmpegCommand: String = commandsGenerator(command)

        FFmpeg.executeAsync(
            ffmpegCommand
        ) { _, returnCode ->
            Config.printLastCommandOutput(Log.INFO)
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    mainCachedFile = outputFilePath
                    applyFilter()
                }
                Config.RETURN_CODE_CANCEL -> {
                    progressDialog.dismiss()
                    try {
                        File(outputFilePath).delete()
                        deleteFromGallery(outputFilePath, requireContext())
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                    Log.i(
                        Config.TAG,
                        "Async command execution cancelled by user."
                    )
                }
                else -> {
                    progressDialog.dismiss()
                    try {
                        File(outputFilePath).delete()
                        deleteFromGallery(outputFilePath, requireContext())
                        Toast.makeText(requireContext(), "Error Creating Video", Toast.LENGTH_SHORT)
                            .show()
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                    Log.i(
                        Config.TAG,
                        String.format("Async command execution failed with rc=%d.", returnCode)
                    )
                }
            }
        }
    }


    private fun applyFilter() {

        logVideoBitrate(mainCachedFile)

        val progressDialog =
            ProgressDialog(requireContext(), R.style.CustomDialog)
        progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")
        progressDialog.show()

        Log.d("HELLOJIMMY", "showFullScreenDialog: filterPosition != 0")

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
                    Log.d("HELLOJIMMY", "showFullScreenDialog: Filter Progress $progress")
                }

                override fun onCompleted() {
                    mainCachedFile = outputFile
                    progressDialog.dismiss()

                    filterPosition = 0

                    Log.d("HELLOJIMMY", "showFullScreenDialog: Filter Applied")


                    if (MusicApplied) {
                        Log.d("HELLOJIMMY", "showFullScreenDialog: want to Add Music")

                        audioVideoMixer()
                    } else {
                        if (wannaGoBack) {

                            Log.d("HELLOJIMMY", "showFullScreenDialog: Wanna Go Back")


                            wannaGoBackCheckViewModel.postValue(true)

                            requireActivity().runOnUiThread {
                                sharedViewModel.enhanced(false)
                            }
                            wannaGoBack = false
                        } else {
                            saveEditedVideo(requireContext())
                        }
                    }
                }

                override fun onCanceled() {
                    Log.d("HELLOJIMMY", "showFullScreenDialog: Filter Cancel")
                    progressDialog.dismiss()
                }

                override fun onFailed(exception: Exception) {
                    progressDialog.dismiss()
                    Log.e(Constants.APP_NAME, "onFailed() Filter", exception)
                    Log.d("HELLOJIMMY", "showFullScreenDialog: Filter Failed")
                }
            })
            .start()
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

            Log.d("HELLOJIMMY", "showFullScreenDialog: Audio Video Mixer")


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

                        Log.d("HELLOJIMMY", "showFullScreenDialog: Audio Video Mixer Completed")

                        if (wannaGoBack) {
                            Log.d("HELLOJIMMY", "showFullScreenDialog: Wanna Go Back After Mixing")
                            wannaGoBack = false
                            wannaGoBackCheckViewModel.postValue(true)
                        } else {
                            backSave = false
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