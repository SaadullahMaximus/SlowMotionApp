package com.example.slowmotionapp.ui.activities

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ahmedbadereldin.videotrimmer.customVideoViews.BarThumb
import com.ahmedbadereldin.videotrimmer.customVideoViews.CustomRangeSeekBar
import com.ahmedbadereldin.videotrimmer.customVideoViews.OnRangeSeekBarChangeListener
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.customviews.CustomWaitingDialog
import com.example.slowmotionapp.databinding.ActivityTrimVideoBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.isFromTrim
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.trimFilePath
import com.example.slowmotionapp.utils.Utils.commandsGenerator
import com.example.slowmotionapp.utils.Utils.convertContentUriToFilePath
import com.example.slowmotionapp.utils.Utils.createCacheCopy
import com.example.slowmotionapp.utils.Utils.createTrimmedFile
import com.example.slowmotionapp.utils.Utils.deleteFromGallery
import com.example.slowmotionapp.utils.Utils.formatCSeconds
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
import com.example.slowmotionapp.utils.Utils.singleClick
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

    private lateinit var file: File
    private lateinit var outputFile: String

    private val mHandler = Handler(Looper.getMainLooper())

    private var mediaPlayer: MediaPlayer? = null

    private val mUpdateTimeTask: Runnable = object : Runnable {
        override fun run() {
            if (mediaPlayer?.currentPosition!! >= (mEndPosition * 1000)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer?.seekTo((mStartPosition * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
                } else {
                    mediaPlayer?.seekTo(mStartPosition * 1000)
                }
                mediaPlayer?.start()
            }
            binding.seekBar.progress =
                binding.trimVideoView.currentPosition - (mStartPosition * 1000)
            mHandler.postDelayed(this, 100)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrimVideoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        videoUri = intent.getStringExtra("VideoUri")
        type = intent.getIntExtra(Constants.TYPE, 0)

        if (isFromTrim) {
            binding.skipBtn.visibility = View.GONE
        } else {
            binding.skipBtn.visibility = View.VISIBLE
        }

        binding.skipBtn.setOnClickListener {

            mHandler.removeCallbacks(mUpdateTimeTask)
            showSkipTrimDialog()
        }

        binding.backBtn.setOnClickListener {
            exitDialog()
        }

        binding.btnDone.setOnClickListener {

            singleClick {
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
                showTrimDialog()
            }

        }

        binding.timeLineView.post {
            setBitmap(videoUri!!)
        }

        binding.trimVideoView.setOnPreparedListener { mp: MediaPlayer? ->

            this.mediaPlayer = mp

            mp?.let {
                onVideoPrepared()
            }

            if (mediaPlayer?.currentPosition!! >= (mEndPosition * 1000)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer?.seekTo((mStartPosition * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
                } else {
                    mediaPlayer?.seekTo(mStartPosition * 1000)
                }
            }
        }

        binding.trimVideoView.setOnCompletionListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mediaPlayer?.seekTo((mStartPosition * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
            } else {
                mediaPlayer?.seekTo(mStartPosition * 1000)
            }
            binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
        }

        binding.trimVideoView.setVideoURI(Uri.parse(videoUri))

        binding.timeLineBar.addOnRangeSeekBarListener(object : OnRangeSeekBarChangeListener {
            override fun onCreate(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                Log.d("timeLineBar", "onCreate: ")
            }

            override fun onSeek(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                Log.d("timeLineBar", "onSeek: ")

                onSeekThumbs(index, value)
            }

            override fun onSeekStart(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                Log.d("timeLineBar", "onSeekStart: ")

                mHandler.removeCallbacks(mUpdateTimeTask)
            }

            override fun onSeekStop(
                customRangeSeekBarNew: CustomRangeSeekBar,
                index: Int,
                value: Float
            ) {
                Log.d("timeLineBar", "onSeekStop: ")

            }
        })

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mediaPlayer?.seekTo(
                        (seekBar.progress + (mStartPosition * 1000)).toLong(),
                        MediaPlayer.SEEK_CLOSEST
                    )
                } else {
                    mediaPlayer?.seekTo(
                        seekBar.progress + (mStartPosition * 1000)
                    )
                }
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            }
        })

        binding.playPauseButton.setOnClickListener {
            if (binding.trimVideoView.isPlaying) {
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.trimVideoView.start()
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
                if (binding.seekBar.progress == 0) {
                    updateProgressBar()
                } else {
                    updateProgressBar()
                }
            }
        }
    }

    private fun startTrimming() {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, Uri.parse(videoUri))
        file = if (type == Constants.RECORD_VIDEO) {
            File(convertContentUriToFilePath(videoUri!!))
        } else {
            File(videoUri!!)
        }

        try {
            outputFile = createTrimmedFile().absolutePath

            val durationSeconds = (mEndPosition - mStartPosition)

            trimVideo(
                this,
                arrayOf(
                    "-ss",
                    formatCSeconds(mStartPosition.toLong())!!,
                    "-y",
                    "-i",
                    file.toString(),
                    "-t",
                    formatCSeconds(durationSeconds.toLong())!!,
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
                    "-strict",
                    "-2",
                    outputFile
                ),
                outputFile
            )


        } catch (e: Throwable) {
            Objects.requireNonNull(Thread.getDefaultUncaughtExceptionHandler())
                .uncaughtException(
                    Thread.currentThread(), e
                )
        }
    }

    private fun showTrimDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.trimmed_done)

        val noBtn = dialog.findViewById<TextView>(R.id.noBtn)
        val yesBtn = dialog.findViewById<TextView>(R.id.yesBtn)

        yesBtn.setOnClickListener {
            startTrimming()
            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showSkipTrimDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.skip_trim)

        val noBtn = dialog.findViewById<TextView>(R.id.noBtn)
        val yesBtn = dialog.findViewById<TextView>(R.id.yesBtn)

        yesBtn.setOnClickListener {

            trimFilePath = if (type == Constants.RECORD_VIDEO) {
                File(convertContentUriToFilePath(videoUri!!)).toString()
            } else {
                File(videoUri!!).toString()
            }

            mainCachedFile =
                createCacheCopy(this, trimFilePath)
                    .toString()
            playVideo = trimFilePath

            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("VideoUri", trimFilePath)
            intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
            startActivity(intent)
            dialog.dismiss()
            finish()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun exitDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.exit_dialog)

        val noBtn = dialog.findViewById<TextView>(R.id.noBtn)
        val yesBtn = dialog.findViewById<TextView>(R.id.yesBtn)

        yesBtn.setOnClickListener {
            finish()
            mHandler.removeCallbacks(mUpdateTimeTask)
            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 10)
    }

    private fun onSeekThumbs(index: Int, value: Float) {

        binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
        binding.trimVideoView.pause()

        when (index) {
            BarThumb.LEFT -> {
                mStartPosition = (mDuration * (value / 100)).toInt()
                binding.startTime.text = milliSecondsToTimer(((mStartPosition * 1000).toLong()))
            }
            BarThumb.RIGHT -> {
                mEndPosition = (mDuration * (value / 100)).toInt()
                binding.endTime.text = milliSecondsToTimer(((mEndPosition * 1000).toLong()))
            }
        }
        binding.totalDurationTextView.text =
            milliSecondsToTimer((mEndPosition - mStartPosition) * 1000L)

        binding.seekBar.max = (mEndPosition - mStartPosition) * 1000

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer?.seekTo((mStartPosition * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer?.seekTo(mStartPosition * 1000)
        }
        binding.seekBar.progress = 0

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        exitDialog()
    }

    private fun setBitmap(mVideoUri: String) {
        binding.timeLineView.setVideo(Uri.parse(mVideoUri))
    }

    private fun onVideoPrepared() {
        mDuration = binding.trimVideoView.duration / 1000
        binding.totalDurationTextView.text = milliSecondsToTimer(mDuration * 1000L)
        setSeekBarPosition()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mediaPlayer?.seekTo((mStartPosition * 1000).toLong(), MediaPlayer.SEEK_CLOSEST)
        } else {
            mediaPlayer?.seekTo(mStartPosition * 1000)
        }
        updateProgressBar()
    }

    private fun setSeekBarPosition() {
        mStartPosition = 0
        mEndPosition = mDuration
        binding.timeLineBar.setThumbValue(0, 0f)
        binding.timeLineBar.setThumbValue(1, (mEndPosition * 100 / mDuration).toFloat())

        timeLineSet(mDuration)

        binding.timeLineBar.initMaxWidth()
        binding.seekBar.max = mDuration * 1000
    }

    private fun timeLineSet(mDuration: Int) {
        binding.endTime.text = milliSecondsToTimer((mDuration * 1000).toLong())
        timeLineNumbersSet(mDuration)
    }

    private fun timeLineNumbersSet(mDuration: Int) {
        val parts = mDuration / 6.0
        val decimalFormat = DecimalFormat("#.0")
        binding.tv1.text = decimalFormat.format(parts)
        binding.tv2.text = decimalFormat.format(parts * 2)
        binding.tv3.text = decimalFormat.format(parts * 3)
        binding.tv4.text = decimalFormat.format(parts * 4)
        binding.tv5.text = decimalFormat.format(parts * 5)
    }

    private fun trimVideo(context: Context, strArr: Array<String>, str: String) {
        val progressDialog = CustomWaitingDialog(this)
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            FFmpeg.cancel()
        }
        progressDialog.show()

        val ffmpegCommand: String = commandsGenerator(strArr)
        FFmpeg.executeAsync(
            ffmpegCommand
        ) { _, returnCode ->
            Config.printLastCommandOutput(Log.INFO)
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    mHandler.removeCallbacks(mUpdateTimeTask)
                    progressDialog.dismiss()
                    trimFilePath = str
                    mainCachedFile =
                        createCacheCopy(this, trimFilePath)
                            .toString()
                    playVideo = str
                    switchActivity(str)
                }
                Config.RETURN_CODE_CANCEL -> {
                    try {
                        File(str).delete()
                        deleteFromGallery(str, context)
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
                        deleteFromGallery(str, context)
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
            isFromTrim = false
            startActivity(Intent(this, PlayerActivity::class.java))
            finish()
        } else {
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("VideoUri", videoPath)
            intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
            startActivity(intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
    }

}