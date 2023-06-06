package com.example.slowmotionapp.ui.fragments

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentCropSpeedBinding
import com.example.slowmotionapp.ui.activities.EditorActivity


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
    private lateinit var videoUri: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropSpeedBinding.inflate(inflater, container, false)

        videoUri = (activity as EditorActivity?)!!.getTrimmedPath()

        Log.d("Hello", "onCreateView: $videoUri")
        binding.videoView.setVideoURI(Uri.parse(videoUri))

        binding.videoView.setOnPreparedListener { mediaPlayer: MediaPlayer? ->
            mediaPlayer?.let {
                this.onVideoPrepared()
            }
            this.mediaPlayer = mediaPlayer
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
                binding.videoView.start()
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


        return binding.root
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

    fun playVideoAgain(path: String){
        _binding?.let { binding ->
            Log.d("FinalOutput", "playVideoAgain: $path")
            binding.videoView.setVideoPath(path)
            binding.videoView.setOnPreparedListener { mediaPlayer: MediaPlayer? ->
                mediaPlayer?.let {
                    this.onVideoPrepared()
                }
                this.mediaPlayer = mediaPlayer
            }
            binding.videoView.start()
        }
    }
}