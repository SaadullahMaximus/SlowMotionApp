package com.example.slowmotionapp.ui.fragments

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.media.MediaMetadataRetriever
import android.net.Uri
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
import com.example.slowmotionapp.R
import com.example.slowmotionapp.customviews.KnobView
import com.example.slowmotionapp.databinding.FragmentSpeedBinding
import com.example.slowmotionapp.extras.VideoPlayerState
import com.example.slowmotionapp.ui.activities.EditorActivity
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.dataBasePosition
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.knobPosition
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.viewmodel.SharedViewModel
import java.io.File


class SpeedFragment : Fragment() {

    private var _binding: FragmentSpeedBinding? = null
    private val binding get() = _binding!!

    private var videoPlayerState: VideoPlayerState = VideoPlayerState()

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var videoUri: String

    // Get a reference to the shared ViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSpeedBinding.inflate(inflater, container, false)

        videoUri = (activity as EditorActivity?)!!.getTrimmedPath()

        videoPlayerState.setFilename(videoUri)
        videoPlayerState.setStop(getVideoDuration())

        animateKnob(knobPosition)

        binding.knobView.setOnKnobPositionChangeListener(object :
            KnobView.OnKnobPositionChangeListener {
            override fun onKnobPositionChanged(knobValue: Int) {
                // Handle the knob value change here
                // You can display it in a TextView or perform any other actions
                Log.d("Knob", "onKnobPositionChanged: $knobValue")
            }

            override fun onKnobStopped(knobValue: Int) {
                handleKnobStopMoving(knobValue)
            }
        })



        return binding.root
    }

    private fun handleKnobStopMoving(knobValue: Int) {
        // Handle the event when the user stops moving the knob
        Log.d("Saad", "handleKnobStopMoving: Stop Moving")
        val tempFile = Utils.createCacheTempFile(requireContext())
        dataBasePosition += 1
        knobPosition = knobValue * 100F
        Log.d("Saad", "handleKnobStopMoving: FileName $tempFile")
        Log.d("Saad", "handleKnobStopMoving: KnobValue $knobValue")
        videoSpeedChange(tempFile.toString(), knobValue)
    }

    private fun videoSpeedChange(tempPath: String, knobValue: Int) {
        videoMotionCommand(tempPath, knobValue)
    }

    private fun videoMotionCommand(path: String, value: Int) {
        val strArr: Array<String>
        var str = ""

        val valueOf: String = java.lang.String.valueOf(this.videoPlayerState.getStart() / 1000)
        java.lang.String.valueOf(this.videoPlayerState.getStop() / 1000)


        val filename: String = this.videoPlayerState.getFilename()!!

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

        val valueOf2: String = java.lang.String.valueOf(this.videoPlayerState.getDuration() * f2)
        Log.d("valueOf2", "videoMotionCommand: $valueOf2 value $value")

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
            val sb = StringBuilder()
            sb.append("setpts=")
            sb.append(f2)
            sb.append("*PTS")
            strArr = arrayOf(
                "-y",
                "-ss",
                valueOf,
                "-i",
                filename,
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
                valueOf2,
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
            Toast.makeText(requireContext(), "Please select Quality", Toast.LENGTH_LONG).show()
        }
    }

    private fun executeFFMPEG(strArr: Array<String>, str: String) {
        val progressDialog =
            ProgressDialog(requireContext(), R.style.CustomDialog)
        progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")
        progressDialog.show()
        val ffmpegCommand: String = Utils.commandsGenerator(strArr)
        FFmpeg.executeAsync(
            ffmpegCommand
        ) { _, returnCode ->
            Log.d(
                "TAG",
                String.format("FFMPEG process exited with rc %d.", returnCode)
            )
            Log.d("TAG", "FFMPEG process output:")
            Config.printLastCommandOutput(Log.INFO)
            progressDialog.dismiss()
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Success", Toast.LENGTH_SHORT).show()
                    updateVideoUri(Uri.parse(str))
                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.d("FFMPEFailure", str)
                    try {
                        File(str).delete()
                        Utils.deleteFromGallery(str, requireContext())
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
                    Log.d("FFMPEFailure", str)
                    try {
                        File(str).delete()
                        Utils.deleteFromGallery(str, requireContext())
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

    private fun getVideoDuration(): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(requireContext(), Uri.parse(videoUri))

        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationStr!!.toLong()

        // Convert duration from milliseconds to seconds

        // Convert duration from milliseconds to seconds
        return (duration / 1000).toInt()
    }

    // Example function to update the video URI
    private fun updateVideoUri(uri: Uri) {
        sharedViewModel.setVideoUri(uri)
    }
}