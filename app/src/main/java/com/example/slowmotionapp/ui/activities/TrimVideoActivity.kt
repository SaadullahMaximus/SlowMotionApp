package com.example.slowmotionapp.ui.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ahmedbadereldin.videotrimmer.customVideoViews.BarThumb
import com.ahmedbadereldin.videotrimmer.customVideoViews.CustomRangeSeekBar
import com.ahmedbadereldin.videotrimmer.customVideoViews.OnRangeSeekBarChangeListener
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityTrimVideoBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.isFromTrim
import com.example.slowmotionapp.utils.Utils
import java.io.File
import java.text.DecimalFormat
import java.util.*

class TrimVideoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTrimVideoBinding

    private var videoUri: String? = null
    private var type: Int = 0

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

    private lateinit var file: File

    private lateinit var outputFile: File

    private val mHandler = Handler(Looper.getMainLooper())

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

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrimVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        type = intent.getIntExtra(Constants.TYPE, 0)
        Log.d("MaximusTech", "onCreate: $videoUri")

        binding.backBtn.setOnClickListener {
            finish()
        }

        binding.timeLineView.post {
            setBitmap(videoUri!!)
            binding.trimVideoView.setVideoURI(Uri.parse(videoUri))
        }

        binding.trimVideoView.setOnPreparedListener { mp: MediaPlayer? ->
            mp?.let {
                onVideoPrepared()
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


        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
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

        binding.btnDone.setOnClickListener {
            val diff = mEndPosition - mStartPosition
            if (diff < 3) {
                Toast.makeText(
                    this, getString(R.string.video_length_validation),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                val mediaMetadataRetriever = MediaMetadataRetriever()
                mediaMetadataRetriever.setDataSource(this, Uri.parse(videoUri))
                file = if (type == Constants.RECORD_VIDEO) {
                    File(Utils.convertContentUriToFilePath(videoUri!!))
                } else {
                    File(videoUri!!)
                }

                try {
                    //output file is generated and send to video processing
                    outputFile = Utils.createTrimmedFile()

                    trimVideo(
                        this,
                        arrayOf(
                            "-ss",
                            mStartPosition.toString(),
                            "-y",
                            "-i",
                            file.toString(),
                            "-t",
                            mEndPosition.toString(),
                            "-vcodec",
                            "mpeg4",
                            "-b:v",
                            "2097152",
                            "-b:a",
                            "48000",
                            "-ac",
                            "2",
                            "-ar",
                            "22050",
                            outputFile.toString()
                        ), outputFile.toString()
                    )

                } catch (e: Throwable) {
                    Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler())
                        .uncaughtException(
                            Thread.currentThread(), e
                        )
                }
            }
        }

        binding.playPauseButton.setOnClickListener {
            if (binding.trimVideoView.isPlaying) {
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.trimVideoView.start()
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
                if (binding.seekBar.progress == 0) {
                    binding.totalDurationTextView.setText(R.string._00_00)
                    updateProgressBar()
                } else {
                    binding.totalDurationTextView.text = milliSecondsToTimer(
                        binding.seekBar.progress.toLong()
                    )
                    updateProgressBar()
                }
            }
        }
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    private fun onStopSeekThumbs() {}

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

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun setBitmap(mVideoUri: String) {
        binding.timeLineView.setVideo(Uri.parse(mVideoUri))
    }

    private fun onVideoPrepared() {
        mDuration = binding.trimVideoView.duration / 1000
        setSeekBarPosition()
    }

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
        timeLineSet(mDuration)
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

    private fun timeLineSet(mDuration: Int) {
        binding.endTime.text = milliSecondsToTimer((mDuration * 1000).toLong())
        timeLineNumbersSet(mDuration)

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

    private fun timeLineNumbersSet(mDuration: Int) {
        val parts = mDuration / 6.0
        val decimalFormat = DecimalFormat("#.00")
        binding.tv1.text = decimalFormat.format(parts)
        binding.tv2.text = decimalFormat.format(parts * 2)
        binding.tv3.text = decimalFormat.format(parts * 3)
        binding.tv4.text = decimalFormat.format(parts * 4)
        binding.tv5.text = decimalFormat.format(parts * 5)
    }

    private fun trimVideo(context: Context, strArr: Array<String>, str: String) {
        val progressDialog = ProgressDialog(context, R.style.CustomDialog)
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
                    Toast.makeText(context, "Success", Toast.LENGTH_SHORT).show()
                    MainActivity.trimFilePath = str
                    MainActivity.mainCachedFile =
                        Utils.createCacheCopy(this, MainActivity.trimFilePath)
                            .toString()
                    switchActivity(str)
                }
                Config.RETURN_CODE_CANCEL -> {
                    Log.d("FFMPEFailure", str)
                    try {
                        File(str).delete()
                        Utils.deleteFromGallery(str, context)
                        Toast.makeText(context, "Error Creating Video", Toast.LENGTH_SHORT)
                            .show()
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                    Log.i(
                        Config.TAG,
                        "Async command execution cancelled by user."
                    )
                }
                else -> {
                    try {
                        File(str).delete()
                        Utils.deleteFromGallery(str, context)
                        Toast.makeText(context, "Error Creating Video", Toast.LENGTH_SHORT)
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

    private fun switchActivity(videoPath: String) {
        if (isFromTrim) {
            Toast.makeText(this, "Video Trimmed Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("VideoUri", videoPath)
            intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
            startActivity(intent)
            finish()
        }
    }

}