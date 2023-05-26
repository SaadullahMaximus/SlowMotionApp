package com.example.slowmotionapp.ui

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.ahmedbadereldin.videotrimmer.customVideoViews.*
import com.example.slowmotionapp.CropSpeedFragment
import com.example.slowmotionapp.EditorActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.FragmentTrimVideoBinding
import com.example.slowmotionapp.utils.FFMpegCallback
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.VideoEditor
import java.io.File
import java.util.*

class TrimVideoFragment : Fragment(), View.OnClickListener, FFMpegCallback {

    private var _binding: FragmentTrimVideoBinding? = null
    private val binding get() = _binding!!

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

    private lateinit var videoUri: String
    private var type: Int = 0

    private lateinit var file: File

    private lateinit var outputFile: File

    private val mHandler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrimVideoBinding.inflate(inflater, container, false)

        videoUri = (activity as EditorActivity?)!!.getVideoUri()!!
        type = (activity as EditorActivity?)!!.getType()

        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }

        binding.timeLineView.post {
            setBitmap(videoUri)
            binding.trimVideoView.setVideoURI(Uri.parse(videoUri))
        }

        binding.btnDone.setOnClickListener(this)
        binding.playPauseButton.setOnClickListener(this)

        binding.trimVideoView.setOnPreparedListener { mp: MediaPlayer? ->
            mp?.let {
                this.onVideoPrepared()
            }
        }

        binding.trimVideoView.setOnCompletionListener { onVideoCompleted() }

        binding.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarChangeListener {
            override fun onCreate(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
            }

            override fun onSeek(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                onSeekThumbs(index, value)
            }

            override fun onSeekStart(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.seekBar.progress = 0
                binding.trimVideoView.seekTo(mStartPosition * 1000)
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            }

            override fun onSeekStop(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                onStopSeekThumbs()
            }
        })


        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.seekBar.max = mTimeVideo * 1000
                binding.seekBar.progress = 0
                binding.trimVideoView.seekTo(mStartPosition * 1000)
                binding.trimVideoView.pause()
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
                    "onStopTrackingTouch: 123123123   mVideoView--->  " + binding.trimVideoView.duration
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

                binding.trimVideoView.seekTo(mStartPosition * 1000 + seekBar.progress)

            }
        })

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onClick(view: View) {
        if (view === binding.btnDone) {
            val diff = mEndPosition - mStartPosition
            if (diff < 3) {
                Toast.makeText(
                    requireContext(), getString(R.string.video_length_validation),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(requireContext(), "Trimming", Toast.LENGTH_SHORT).show()
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(requireContext(), Uri.parse(videoUri))
                file = if (type == Constants.RECORD_VIDEO) {
                    File(Utils.convertContentUriToFilePath(videoUri))
                } else {
                    File(videoUri)
                }

                try {
                    //output file is generated and send to video processing
                    outputFile = Utils.createTrimmedFile(requireContext())

                    Log.d("GGG", "onClick: input$file")
                    Log.d("GGG", "onClick: input$outputFile")

                    VideoEditor.with(requireContext())
                        .setType(Constants.VIDEO_TRIM)
                        .setFile(file)
                        .setOutputPath(outputFile.toString())
                        .setStartTime(mStartPosition.toString())
                        .setEndTime(mEndPosition.toString())
                        .setCallback(this@TrimVideoFragment)
                        .main()
                } catch (e: Throwable) {
                    Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler())
                        .uncaughtException(
                            Thread.currentThread(), e
                        )
                }
            }
        } else if (view === binding.playPauseButton) {
            if (binding.trimVideoView.isPlaying) {
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.trimVideoView.start()
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
                if (binding.seekBar.progress == 0) {
                    binding.totalDurationTextView.text = "00:00"
                    updateProgressBar()
                } else {
                    binding.totalDurationTextView.text = milliSecondsToTimer(
                        binding.seekBar.progress.toLong()
                    ) + ""
                    updateProgressBar()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
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


    private val mUpdateTimeTask: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (binding.seekBar.progress >= binding.seekBar.max) {
                binding.seekBar.progress =
                    binding.trimVideoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                binding.trimVideoView.seekTo(mStartPosition * 1000)
                binding.trimVideoView.pause()
                binding.seekBar.progress = 0
                binding.totalDurationTextView.text = "00:00"
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.seekBar.progress =
                    binding.trimVideoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                mHandler.postDelayed(this, 100)
            }
        }
    }


    private fun onVideoPrepared() {
        mDuration = binding.trimVideoView.duration / 1000
        setSeekBarPosition()
    }


    private fun setBitmap(mVideoUri: String) {
        binding.timeLineView.setVideo(Uri.parse(mVideoUri))
    }

    private fun onSeekThumbs(index: Int, value: Float) {
        when (index) {
            BarThumb.LEFT -> {
                mStartPosition = (mDuration * value / 100L).toInt()
                binding.trimVideoView.seekTo(mStartPosition * 1000)
            }
            BarThumb.RIGHT -> {
                mEndPosition = (mDuration * value / 100L).toInt()
            }
        }
        mTimeVideo = mEndPosition - mStartPosition
        binding.seekBar.progress = 0
        binding.trimVideoView.seekTo(mStartPosition * 1000)

        var mStart: String = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        mStart.toInt() / 60
        mStart.toInt() % 60
        var mEnd: String = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        mEnd.toInt() / 60
        mEnd.toInt() % 60
    }

    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.seekBar.progress = 0
        binding.trimVideoView.seekTo(mStartPosition * 1000)
        binding.trimVideoView.pause()
        binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
    }

    private fun onStopSeekThumbs() {}

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration).toFloat())
            binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration).toFloat())
            mDurationWithoutEdit = mDuration
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
            mDurationWithoutEdit = mDuration
        }
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
        binding.seekBar.max = mDurationWithoutEdit * 1000
        binding.trimVideoView.seekTo(mStartPosition * 1000)

        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        mStart.toInt() / 60
        mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        mEnd.toInt() / 60
        mEnd.toInt() % 60

    }

    override fun onProgress(progress: String) {
        Log.d("TrimFFMPEG", "onProgress() $progress")
    }

    override fun onSuccess(convertedFile: File, type: String) {
        Log.d("TrimFFMPEG", "onSuccess()")
        val fragment1 = TrimVideoFragment()
        val fragment2 = CropSpeedFragment()
        val transaction: FragmentTransaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment2)
        transaction.addToBackStack(null)
        transaction.remove(fragment1).commit()
        (activity as EditorActivity?)!!.setTrimVideoPath(outputFile)

    }

    override fun onFailure(error: Exception) {
        Log.d("TrimFFMPEG", "onFailure() " + error.localizedMessage)
    }

    override fun onNotAvailable(error: Exception) {
        Log.d("TrimFFMPEG", "onNotAvailable() " + error.message)
        Log.v("TrimFFMPEG", "Exception: ${error.localizedMessage}")
    }

    override fun onFinish() {
        Log.d("TrimFFMPEG", "onFinish()")
    }


}