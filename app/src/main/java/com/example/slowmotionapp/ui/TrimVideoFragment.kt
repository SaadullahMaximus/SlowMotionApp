package com.example.slowmotionapp.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.ahmedbadereldin.videotrimmer.Utility
import com.ahmedbadereldin.videotrimmer.customVideoViews.*
import com.example.slowmotionapp.EditorActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentTrimVideoBinding
import com.example.slowmotionapp.utils.Utils
import java.io.File
import java.util.*

class TrimVideoFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentTrimVideoBinding? = null
    private val binding get() = _binding!!

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mHandler = Handler()

    private lateinit var videoUri: Uri
    private lateinit var dstFile: String

    private var mProgressDialog: ProgressDialog? = null

    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

    var mOnVideoTrimListener: OnVideoTrimListener = object : OnVideoTrimListener {
        override fun onTrimStarted() {
            // Create an indeterminate progress dialog
            mProgressDialog = ProgressDialog(requireContext())
            mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            mProgressDialog!!.setTitle(getString(R.string.save))
            mProgressDialog!!.isIndeterminate = true
            mProgressDialog!!.setCancelable(false)
            mProgressDialog!!.show()
        }

        override fun getResult(uri: Uri) {
            Log.d("getResult", "getResult: $uri")
            mProgressDialog?.dismiss()
            val conData = Bundle()
            conData.putString("INTENT_VIDEO_FILE", uri.path)
            val intent = Intent()
            intent.putExtras(conData)
//            setResult(Activity.RESULT_OK, intent)
//            finish()
        }

        override fun cancelAction() {
            mProgressDialog!!.dismiss()
        }

        override fun onError(message: String) {
            mProgressDialog?.dismiss()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTrimVideoBinding.inflate(inflater, container, false)

        videoUri = (activity as EditorActivity?)!!.getVideoUri()!!

        dstFile = Utils.createTrimmedFile(requireContext()).toString()

        Log.d("MaximusFragment", "onCreateView:2 $videoUri")


//        val progressDialog = ProgressDialog(requireContext())
//        progressDialog.setMessage("Preparing Video...")
//        progressDialog.setCancelable(false)
//        progressDialog.show()

        binding.timeLineView.post {
            setBitmap(videoUri)
            binding.trimVideoView.setVideoURI(videoUri)
        }

        binding.btnDone.setOnClickListener(this)

        binding.trimVideoView.setOnPreparedListener { mp: MediaPlayer? ->
            mp?.let {
                this.onVideoPrepared()
            }
        }

        binding.trimVideoView.setOnCompletionListener { onVideoCompleted() }

//        binding.trimVideoView.setOnPreparedListener {
//            val totalDuration = it.duration
//            binding.seekBar.max = totalDuration
//            binding.totalDurationTextView.text = formatDuration(totalDuration)
//            progressDialog.dismiss()
//            it.start()
//
//            binding.trimVideoView.pause()
//
//            it.setOnCompletionListener {
//                binding.seekBar.progress = totalDuration
//                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
//            }
//
//            // Update progress of SeekBar and duration as video plays
//            Timer().scheduleAtFixedRate(object : TimerTask() {
//                override fun run() {
//                    val currentPosition = it.currentPosition
//                    activity?.runOnUiThread {
//                        binding.seekBar.progress = currentPosition
//                        binding.totalDurationTextView.text = formatDuration(currentPosition)
//                    }
//                }
//            }, 0, 100)
//
//            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
//                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
//                    if (fromUser) {
//                        it.seekTo(progress)
//                    }
//                }
//
//                override fun onStartTrackingTouch(seekBar: SeekBar) {
//                    // Not needed for your specific requirement, but you can implement any required functionality here
//                }
//
//                override fun onStopTrackingTouch(seekBar: SeekBar) {
//                    // Not needed for your specific requirement, but you can implement any required functionality here
//                }
//            })
//
//        }
//
//
//        binding.playPauseButton.setOnClickListener {
//            if (binding.trimVideoView.isPlaying) {
//                binding.trimVideoView.pause()
//                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
//            } else {
//                binding.trimVideoView.start()
//                binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
//            }
//        }

        binding.backBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Back Btn", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }


        // handle your range seekbar changes
        binding.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarChangeListener {
            override fun onCreate(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                // Do nothing
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
                if (binding.trimVideoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask)
                    binding.seekBar.progress = 0
                    binding.trimVideoView.seekTo(mStartPosition * 1000)
                    binding.trimVideoView.pause()
                    binding.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow)
                }
            }

            override fun onSeekStop(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                onStopSeekThumbs()
            }
        })

        binding.playPauseButton.setOnClickListener(this)


        // handle changes on seekbar for video play
        binding.seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                if (binding.trimVideoView != null) {
                    mHandler.removeCallbacks(mUpdateTimeTask)
                    binding.seekBar.max = mTimeVideo * 1000
                    binding.seekBar.progress = 0
                    binding.trimVideoView.seekTo(mStartPosition * 1000)
                    binding.trimVideoView.pause()
                    binding.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow)
                }
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

                // seek bar - 120 sec
                // start = 130
                // end = 255

//                if(mDuration)
                mHandler.removeCallbacks(mUpdateTimeTask)
                //                mVideoView.seekTo((mDuration * 1000) - seekBarVideo.getProgress());
                binding.trimVideoView.seekTo(mStartPosition * 1000 + seekBar.progress)
                //                mVideoView.start();
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
                mediaMetadataRetriever.setDataSource(requireContext(), videoUri)
                val file = File(videoUri.toString())
                Log.d(
                    "executeAAAA",
                    "execute: " + "A" + file.length() + " " + dstFile + " " + mStartPosition + " " + mEndPosition + " " + mOnVideoTrimListener
                )
                //notify that video trimming started
                mOnVideoTrimListener.onTrimStarted()
                BackgroundTask.execute(object : BackgroundTask.Task("", 0L, "") {
                    override fun execute() {
                        try {
                            Log.d(
                                "executeAAAA",
                                "execute: " + "A" + file.length() + " " + dstFile + " " + mStartPosition + " " + mEndPosition + " " + mOnVideoTrimListener
                            )
                            Utility.startTrim(
                                requireActivity(),
                                file,
                                dstFile,
                                (mStartPosition * 1000).toLong(),
                                (mEndPosition * 1000).toLong(),
                                mOnVideoTrimListener
                            )
                        } catch (e: Throwable) {
                            Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler())
                                .uncaughtException(
                                    Thread.currentThread(), e
                                )
                        }
                    }
                }
                )
            }
        } else if (view === binding.playPauseButton) {
            if (binding.trimVideoView.isPlaying) {
                if (binding.trimVideoView != null) {
                    binding.trimVideoView.pause()
                    binding.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow)
                }
            } else {
                if (binding.trimVideoView != null) {
                    binding.trimVideoView.start()
                    binding.playPauseButton.setBackgroundResource(R.drawable.baseline_pause)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //endregion
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
        // Add hours if there
        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        // Prepending 0 to seconds if it is one digit
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

        // return timer string
        return finalTimerString
    }


    private val mUpdateTimeTask: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (binding.seekBar.progress >= binding.seekBar.max) {
                binding.seekBar.progress = binding.trimVideoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                binding.trimVideoView.seekTo(mStartPosition * 1000)
                binding.trimVideoView.pause()
                binding.seekBar.progress = 0
                binding.totalDurationTextView.text = "00:00"
                binding.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow)
            } else {
                binding.seekBar.progress = binding.trimVideoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                mHandler.postDelayed(this, 100)
            }
        }
    }


    private fun onVideoPrepared() {
        // Adjust the size of the video
        // so it fits on the screen
        //TODO manage proportion for video
        /*int videoWidth = mp.getVideoWidth();
        int videoHeight = mp.getVideoHeight();
        float videoProportion = (float) videoWidth / (float) videoHeight;
        int screenWidth = rlVideoView.getWidth();
        int screenHeight = rlVideoView.getHeight();
        float screenProportion = (float) screenWidth / (float) screenHeight;
        ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();

        if (videoProportion > screenProportion) {
            lp.width = screenWidth;
            lp.height = (int) ((float) screenWidth / videoProportion);
        } else {
            lp.width = (int) (videoProportion * (float) screenHeight);
            lp.height = screenHeight;
        }
        mVideoView.setLayoutParams(lp);*/

        //mVideoView.getDuration() => get in mSec we need it in sec
        mDuration = binding.trimVideoView.duration / 1000
        setSeekBarPosition()
    }


//    private fun formatDuration(duration: Int): String {
//        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
//        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
//        return String.format("%02d:%02d", minutes, seconds)
//    }

    private fun setBitmap(mVideoUri: Uri) {
        binding.timeLineView.setVideo(mVideoUri)
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
        binding.seekBar.max = mTimeVideo * 1000
        binding.seekBar.progress = 0
        binding.trimVideoView.seekTo(mStartPosition * 1000)
        var mStart: String = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        val startMin = mStart.toInt() / 60
        val startSec = mStart.toInt() % 60
        var mEnd: String = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        val endMin = mEnd.toInt() / 60
        val endSec = mEnd.toInt() % 60
        binding.totalDurationTextView.text = String.format(
            Locale.US,
            "%02d:%02d - %02d:%02d",
            startMin,
            startSec,
            endMin,
            endSec
        )
    }

    //endregion
    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.seekBar.progress = 0
        binding.trimVideoView.seekTo(mStartPosition * 1000)
        binding.trimVideoView.pause()
        binding.playPauseButton.setBackgroundResource(R.drawable.baseline_play_arrow)
    }

    private fun onStopSeekThumbs() {
//        mMessageHandler.removeMessages(SHOW_PROGRESS);
//        mVideoView.pause();
//        mPlayView.setVisibility(View.VISIBLE);
    }

    private fun setSeekBarPosition() {
        if (mDuration >= mMaxDuration) {
            mStartPosition = 0
            mEndPosition = mMaxDuration
            binding.timeLineBar.setThumbValue(0, (mStartPosition * 100 / mDuration).toFloat())
            binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration).toFloat())
            //////
            mDurationWithoutEdit = mDuration
            //            mDuration = mMaxDuration;
        } else {
            mStartPosition = 0
            mEndPosition = mDuration
            mDurationWithoutEdit = mDuration
        }
        mTimeVideo = mDuration
        binding.timeLineBar.initMaxWidth()
        //        seekBarVideo.setMax(mMaxDuration * 1000);
        binding.seekBar.max = mDurationWithoutEdit * 1000
        binding.trimVideoView.seekTo(mStartPosition * 1000)
        var mStart = mStartPosition.toString() + ""
        if (mStartPosition < 10) mStart = "0$mStartPosition"
        val startMin = mStart.toInt() / 60
        val startSec = mStart.toInt() % 60
        var mEnd = mEndPosition.toString() + ""
        if (mEndPosition < 10) mEnd = "0$mEndPosition"
        val endMin = mEnd.toInt() / 60
        val endSec = mEnd.toInt() % 60
        binding.totalDurationTextView.text = String.format(
            Locale.US,
            "%02d:%02d - %02d:%02d",
            startMin,
            startSec,
            endMin,
            endSec
        )
    }


}