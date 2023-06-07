package com.example.slowmotionapp.ui.fragments

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentCropSpeedBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.trimFilePath
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.viewmodel.SharedViewModel
import java.io.File


class CropSpeedFragment : Fragment() {

    private var _binding: FragmentCropSpeedBinding? = null
    private val binding get() = _binding!!

    private var childFragmentManager: FragmentManager? = null
    private var currentChildFragment: Fragment? = null
    private var effectMusicFragment: Fragment? = null

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

    private val mHandler = Handler(Looper.getMainLooper())

    private var mediaPlayer: MediaPlayer? = null

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var videoView: VideoView
    private lateinit var playPause: ImageView

    // Get a reference to the shared ViewModel
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedViewModel::class.java]

        // Initialize and setup the VideoView
        videoView = view.findViewById(R.id.videoView)
        playPause = view.findViewById(R.id.playPauseButton)

        // Observe the video URI LiveData
        sharedViewModel.videoPath.observe(viewLifecycleOwner) { path ->
            path?.let {
                videoView.setVideoURI(Uri.parse(path))
            }
        }

        // Observe the booleanLiveData
        sharedViewModel.booleanLiveData.observe(viewLifecycleOwner) { newValue ->
            if (newValue == true && videoView.isPlaying) {
                videoView.pause()
                videoView.seekTo(0)
                playPause.setImageResource(R.drawable.baseline_play_arrow)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropSpeedBinding.inflate(inflater, container, false)

        binding.videoView.setVideoURI(Uri.parse(mainCachedFile))

        Log.d("Hello", "onCreateView: TrimFilePath $trimFilePath")
        Log.d("Hello", "onCreateView: MainCachedFile $mainCachedFile")

        binding.videoView.setOnPreparedListener { mediaPlayer: MediaPlayer? ->
            mediaPlayer?.let {
                this.onVideoPrepared()
            }
            this.mediaPlayer = mediaPlayer
            videoPlay()
        }

        binding.videoView.setOnCompletionListener { onVideoCompleted() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.seekBar.max = mTimeVideo * 1000
                binding.seekBar.progress = 0
                binding.videoView.seekTo(mStartPosition * 1000)
                binding.videoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   --->  " + mStartPosition * 1000 + " <-----> " + seekBar.progress + " <-----> " + binding.seekBar.progress
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   --->  " + mDuration * 1000 + " <-----> " + binding.seekBar.progress
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   --->  " + (mStartPosition * 1000 - binding.seekBar.progress)
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   --->  " + (mDuration * 1000 - binding.seekBar.progress)
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   mVideoView--->  " + binding.videoView.duration
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   seekBar--->  " + seekBar.progress
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   mStartPosition--->  " + mStartPosition * 1000
                )
                Log.d(
                    "onStopTrackingTouch",
                    "onStopTrackingTouch: 123123123   mEndPosition--->  " + mEndPosition * 1000
                )

                mHandler.removeCallbacks(mUpdateTimeTask)

                binding.videoView.seekTo(mStartPosition * 1000 + seekBar.progress)

            }
        })

        childFragmentManager = getChildFragmentManager()

        currentChildFragment = MainFragment()
        effectMusicFragment = EffectMusicFragment()

        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }

        childFragmentManager!!.beginTransaction()
            .add(R.id.fragment_container_main, currentChildFragment as MainFragment)
            .commit()

        binding.enhanceBtn.setOnClickListener {
            binding.enhanceBtn.visibility = View.GONE
            binding.backTextBtn.visibility = View.VISIBLE
            childFragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container_main, effectMusicFragment as EffectMusicFragment)
                .commit()
        }

        binding.backTextBtn.setOnClickListener {
            binding.enhanceBtn.visibility = View.VISIBLE
            binding.backTextBtn.visibility = View.GONE
            childFragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container_main, currentChildFragment as MainFragment)
                .commit()
        }

        binding.playPauseButton.setOnClickListener {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                videoPlay()
            }
        }

        binding.rotateRight.setOnClickListener {
            rotateVideoCommand(1)
        }

        binding.rotateLeft.setOnClickListener {
            rotateVideoCommand(2)
        }

        binding.saveBtn.setOnClickListener {
            Utils.saveEditedVideo(requireContext(), File(mainCachedFile))
        }


        return binding.root
    }

    private fun rotateVideoCommand(rotateValue: Int) {

        Log.d("HelloHello", "rotateVideoCommand: $rotateValue")

        val sb3 = StringBuilder()

        sb3.append("[0:v]transpose=$rotateValue[out]")

        val tempFile = Utils.createCacheTempFile(requireContext())

        executeFFMPEG(
            arrayOf(
                "-y",
                "-ss",
                "0",
                "-t",
                "${Utils.getVideoDuration(requireContext(), mainCachedFile)}",
                "-i",
                mainCachedFile,
                "-vf",
                sb3.toString(),
                "-c:a",
                "copy",
                tempFile
            ), tempFile
        )
    }

    private fun executeFFMPEG(strArr: Array<String>, str: String) {
        sharedViewModel.pauseVideo(true)
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
                    mainCachedFile = str
                    updateVideoUri(str)
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

    private fun updateVideoUri(path: String) {
        videoView.setVideoURI(Uri.parse(path))
    }

    private fun videoPlay() {
        binding.videoView.start()
        binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
        if (binding.seekBar.progress == 0) {
            "00:00".also { binding.totalDurationTextView.text = it }
            updateProgressBar()
        } else {
            binding.totalDurationTextView.text = milliSecondsToTimer(
                binding.seekBar.progress.toLong()
            ) + ""
            updateProgressBar()
        }
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.seekBar.progress = 0
        binding.videoView.seekTo(mStartPosition * 1000)
        binding.videoView.pause()
        binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
    }

    private val mUpdateTimeTask: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (binding.seekBar.progress >= binding.seekBar.max) {
                binding.seekBar.progress =
                    binding.videoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                binding.videoView.seekTo(mStartPosition * 1000)
                binding.videoView.pause()
                binding.seekBar.progress = 0
                binding.totalDurationTextView.text = "00:00"
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.seekBar.progress =
                    binding.videoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                mHandler.postDelayed(this, 100)
            }
        }
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        val secondsString: String
        val minutesString: String
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        minutesString = if (minutes < 10) {
            "0$minutes"
        } else {
            "" + minutes
        }
        finalTimerString = "$finalTimerString$minutesString:$secondsString"

        return finalTimerString
    }

    private fun onVideoPrepared() {
        mDuration = binding.videoView.duration / 1000
        setSeekBarPosition()
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            mDurationWithoutEdit = mDuration
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
            mDurationWithoutEdit = mDuration
        }
        mTimeVideo = mDuration
        binding.seekBar.max = mDurationWithoutEdit * 1000
        binding.videoView.seekTo(mStartPosition * 1000)

        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        mStart.toInt() / 60
        mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        mEnd.toInt() / 60
        mEnd.toInt() % 60

    }
}