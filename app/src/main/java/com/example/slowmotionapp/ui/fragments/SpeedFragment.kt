package com.example.slowmotionapp.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.slowmotionapp.customviews.CustomWaitingDialog
import com.example.slowmotionapp.customviews.KnobView
import com.example.slowmotionapp.databinding.FragmentSpeedBinding
import com.example.slowmotionapp.extras.VideoPlayerState
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.knobFinalValue
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.knobPosition
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.utils.Utils.commandsGenerator
import com.example.slowmotionapp.utils.Utils.createCacheTempFile
import com.example.slowmotionapp.utils.Utils.deleteFromGallery
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import com.example.slowmotionapp.utils.Utils.player
import com.example.slowmotionapp.utils.Utils.singleClick
import com.example.slowmotionapp.viewmodel.SharedViewModel
import java.io.File


class SpeedFragment : Fragment() {

    private var _binding: FragmentSpeedBinding? = null
    private val binding get() = _binding!!

    private var videoPlayerState: VideoPlayerState = VideoPlayerState()

    private lateinit var sharedViewModel: SharedViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        sharedViewModel.animateKnob.observe(viewLifecycleOwner) { newValue ->
            animateKnob(newValue.toFloat())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSpeedBinding.inflate(inflater, container, false)

        videoPlayerState.setFilename(mainCachedFile)
        videoPlayerState.setStop(getVideoDuration(requireContext(), mainCachedFile))

        animateKnob(knobPosition)

        binding.knobView.setOnKnobPositionChangeListener(object :
            KnobView.OnKnobPositionChangeListener {

            override fun onKnobPositionChanged(knobValue: Int) {
                // Handle the knob value change here
                // You can display it in a TextView or perform any other actions
            }

            override fun onKnobStopped(knobValue: Int) {
                handleKnobStopMoving(knobValue)
            }
        })

        binding.btnOk.setOnClickListener {
            singleClick {
                if (knobFinalValue != 7) {
                    val tempPath = createCacheTempFile(requireContext())
                    videoMotionCommand(tempPath, knobFinalValue)

                    handleKnobStopMoving(7)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please move slider first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }



        return binding.root
    }

    private fun handleKnobStopMoving(knobValue: Int) {
        // Handle the event when the user stops moving the knob
        videoSpeedChange(knobValue)
    }

    private fun videoSpeedChange(knobValue: Int) {

        knobFinalValue = knobValue

        when (knobValue) {
            1 -> {
                player?.setPlaybackSpeed(0.1f)
            }
            2 -> {
                player?.setPlaybackSpeed(0.25f)
            }
            3 -> {
                player?.setPlaybackSpeed(0.40f)
            }
            4 -> {
                player?.setPlaybackSpeed(0.55f)
            }
            5 -> {
                player?.setPlaybackSpeed(0.70f)
            }
            6 -> {
                player?.setPlaybackSpeed(0.85f)
            }
            7 -> {
                player?.setPlaybackSpeed(1f)
            }
            8 -> {
                player?.setPlaybackSpeed(1.3f)
            }
            9 -> {
                player?.setPlaybackSpeed(1.6f)
            }
            10 -> {
                player?.setPlaybackSpeed(2f)
            }
            11 -> {
                player?.setPlaybackSpeed(2.3f)
            }
            12 -> {
                player?.setPlaybackSpeed(2.6f)
            }
            13 -> {
                player?.setPlaybackSpeed(3f)
            }
        }
    }

    private fun videoMotionCommand(path: String, value: Int) {
        val strArr: Array<String>
        var str = ""

        var f2 = 0.0f
        when (value) {
            1 -> {
                f2 = 4.0f
            }
            2 -> {
                f2 = 3.5f
            }
            3 -> {
                f2 = 3.0f
            }
            4 -> {
                f2 = 2.5f
            }
            5 -> {
                f2 = 2f
            }
            6 -> {
                f2 = 1.5f
            }
            7 -> {
                f2 = 1.0f
            }
            8 -> {
                f2 = 0.89f
            }
            9 -> {
                f2 = 0.75f
            }
            10 -> {
                f2 = 0.625f
            }
            11 -> {
                f2 = 0.5f
            }
            12 -> {
                f2 = 0.4167f
            }
            13 -> {
                f2 = 0.333f
            }
        }

        when (value) {
            1 -> {
                str = "atempo=0.5"
            }
            2 -> {
                str = "atempo=0.57"
            }
            3 -> {
                str = "atempo=0.64"
            }
            4 -> {
                str = "atempo=0.72"
            }
            5 -> {
                str = "atempo=0.8"
            }
            6 -> {
                str = "atempo=0.89"
            }
            7 -> {
                str = "atempo=1.0"
            }
            8 -> {
                str = "atempo=1.5"
            }
            9 -> {
                str = "atempo=1.5"
            }
            10 -> {
                str = "atempo=1.75"
            }
            11 -> {
                str = "atempo=2.0"
            }
            12 -> {
                str = "atempo=2.25"
            }
            13 -> {
                str = "atempo=2.5"
            }

        }

        try {

            Log.d("HelloWorld", "videoMotionCommand: $value, $f2, $str")

            val sb = StringBuilder()

            sb.append("setpts=")
            sb.append(f2)
            sb.append("*PTS")

            strArr = arrayOf(
                "-y",
                "-ss",
                "0",
                "-i",
                mainCachedFile,
                "-filter:v",
                sb.toString(),
                "-filter:a",
                str,
                "-r",
                "15",
                "-b:v",
                "2500k",
                "-strict",
                "experimental",
                "-t",
                (getVideoDuration(requireContext(), mainCachedFile) * f2).toString(),
                path
            )
            executeFFMPEG(strArr, path)
        } catch (unused: Exception) {
            val file = File(path)
            if (file.exists()) {
                file.delete()
                requireActivity().finish()
                return
            }
            Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
        }
    }

    private fun executeFFMPEG(strArr: Array<String>, str: String) {

        sharedViewModel.musicSelectPauseEveryThing(true)

        sharedViewModel.pauseVideo(true)
        val progressDialog = CustomWaitingDialog(requireContext())
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            FFmpeg.cancel()
        }
        progressDialog.show()
        progressDialog.setText("Please wait")

        val ffmpegCommand: String = commandsGenerator(strArr)
        FFmpeg.executeAsync(
            ffmpegCommand
        ) { _, returnCode ->

            Config.printLastCommandOutput(Log.INFO)
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    mainCachedFile = str
                    updateVideoUri(str)
                    animateKnob(700F)
                }
                Config.RETURN_CODE_CANCEL -> {
                    try {
                        animateKnob(700F)
                        File(str).delete()
                        deleteFromGallery(str, requireContext())
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
                else -> {
                    try {
                        File(str).delete()
                        deleteFromGallery(str, requireContext())
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
            }
        }
    }


    private fun animateKnob(interval: Float) {
        val knobAnimator = ValueAnimator.ofFloat(binding.knobView.knobPositionX, interval)
        knobAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Float
            binding.knobView.knobPositionX = animatedValue
        }
        // Set the interpolator for the bounce effect
        knobAnimator.interpolator = BounceInterpolator()

        // Set the duration of the animation in milliseconds
        knobAnimator.duration = 1000

        // Start the animation
        knobAnimator.start()
    }


    // Example function to update the video URI
    private fun updateVideoUri(path: String) {
        sharedViewModel.setVideoUri(path)
    }

}