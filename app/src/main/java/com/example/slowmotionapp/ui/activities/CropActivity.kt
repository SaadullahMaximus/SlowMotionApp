package com.example.slowmotionapp.ui.activities

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.edmodo.cropper.cropwindow.edge.Edge
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.customviews.CustomWaitingDialog
import com.example.slowmotionapp.databinding.ActivityCropBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.trimOrCrop
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.Utils.commandsGenerator
import com.example.slowmotionapp.utils.Utils.createCroppedFile
import com.example.slowmotionapp.utils.Utils.deleteFromGallery
import com.example.slowmotionapp.utils.Utils.getScreenWidth
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import java.io.File

class CropActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCropBinding

    private var videoUri: String? = null
    private var type: Int = 0

    private val mHandler = Handler(Looper.getMainLooper())

    private var cropSelected = false

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

    private var keyCodeR = 0
    private var keyCodeW = 0
    private var keyCodeQ = 0

    private var aa = 0f
    private var ab = 0f
    private var ac = 0f
    private var ad = 0f

    private var a = 0
    private var b = 0
    private var y = 0
    private var z = 0

    private var m = 0
    private var n = 0
    private var o = 0
    private var p = 0
    private var q = 0
    private var r = 0
    private var s = 0
    private var t = 0
    private var u = 0
    private var v = 0

    private var cropVideoDuration: Int? = null

    private var ag = 0
    private var ah = 0

    private var cropOutputFilePath: String? = null

    private var screenWidth = 0

    private lateinit var file: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCropBinding.inflate(layoutInflater)
        setContentView(binding.root)


        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        type = intent.getIntExtra(Constants.TYPE, 0)

        file = if (type == Constants.RECORD_VIDEO) {
            File(Utils.convertContentUriToFilePath(videoUri!!))
        } else {
            File(videoUri!!)
        }

        binding.videoView.setVideoURI(Uri.parse(videoUri))

        mainCachedFile = videoUri!!

        screenWidth = getScreenWidth()

        val layoutParams = binding.frameLayout.layoutParams
        layoutParams.width = screenWidth
        layoutParams.height = screenWidth
        binding.frameLayout.layoutParams = layoutParams

        cropViewDisplay()

        binding.videoView.setOnPreparedListener { mediaPlayer: MediaPlayer? ->
            mediaPlayer?.let {
                this.onVideoPrepared()
            }
            videoPlay()
        }

        binding.videoView.setOnCompletionListener { onVideoCompleted() }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                binding.seekBar.max = mTimeVideo * 1000
                binding.seekBar.progress = 0
                binding.videoView.seekTo(mStartPosition * 1000)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                binding.videoView.seekTo(mStartPosition * 1000 + seekBar.progress)
                binding.totalDurationTextView.text =
                    milliSecondsToTimer(binding.seekBar.progress.toLong())
            }
        })

        binding.backBtn.setOnClickListener {
            trimOrCrop = false
            exitDialog()
        }

        binding.playPauseButton.setOnClickListener {
            if (binding.videoView.isPlaying) {
                binding.videoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                videoPlay()
            }
        }

        binding.btnFree.setOnClickListener {
            cropSelect(1)
            binding.cropView.visibility = View.VISIBLE

            binding.imageViewFree.setImageResource(R.drawable.crop_select)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

            cropSelected = true
        }
        binding.btn11.setOnClickListener {
            cropSelect(2)
            binding.cropView.visibility = View.VISIBLE

            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_select)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

            cropSelected = true
        }
        binding.btnPortrait.setOnClickListener {
            cropSelect(3)
            binding.cropView.visibility = View.VISIBLE

            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_select)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

            cropSelected = true
        }
        binding.btnLandScape.setOnClickListener {
            cropSelect(4)
            binding.cropView.visibility = View.VISIBLE

            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_select)

            cropSelected = true
        }

        binding.btnCancel.setOnClickListener {
            binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
            binding.imageView11.setImageResource(R.drawable.crop_unselect)
            binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
            binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)
            binding.cropView.visibility = View.GONE

            cropSelected = false
        }
        binding.btnOk.setOnClickListener {
            if (cropSelected) {
                cropSelected = false
                getDimension()
                startCrop()
            } else {
                Toast.makeText(this, "Please select Crop aspect ratio.", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun cropViewDisplay() {
        val mediaMetadataRetriever = MediaMetadataRetriever()
        mediaMetadataRetriever.setDataSource(this, Uri.parse(mainCachedFile))

        keyCodeR = Integer.valueOf(mediaMetadataRetriever.extractMetadata(18)!!).toInt()
        keyCodeQ = Integer.valueOf(mediaMetadataRetriever.extractMetadata(19)!!).toInt()
        keyCodeW = Integer.valueOf(mediaMetadataRetriever.extractMetadata(24)!!).toInt()


        val layoutParams = binding.cropView.layoutParams as FrameLayout.LayoutParams

        if (keyCodeW == 90 || keyCodeW == 270) {
            if (keyCodeR >= keyCodeQ) {
                if (keyCodeR >= screenWidth) {
                    layoutParams.height = screenWidth
                    layoutParams.width =
                        (screenWidth.toFloat() / (keyCodeR.toFloat() / keyCodeQ.toFloat())).toInt()
                } else {
                    layoutParams.width = screenWidth
                    layoutParams.height =
                        (keyCodeQ.toFloat() * (screenWidth.toFloat() / keyCodeR.toFloat())).toInt()
                }
            } else if (keyCodeQ >= screenWidth) {
                layoutParams.width = screenWidth
                layoutParams.height =
                    (screenWidth.toFloat() / (keyCodeQ.toFloat() / keyCodeR.toFloat())).toInt()
            } else {
                layoutParams.width =
                    (keyCodeR.toFloat() * (screenWidth.toFloat() / keyCodeQ.toFloat())).toInt()
                layoutParams.height = screenWidth
            }
        } else if (keyCodeR >= keyCodeQ) {
            if (keyCodeR >= screenWidth) {
                layoutParams.width = screenWidth
                layoutParams.height =
                    (screenWidth.toFloat() / (keyCodeR.toFloat() / keyCodeQ.toFloat())).toInt()
            } else {
                layoutParams.width = screenWidth
                layoutParams.height =
                    (keyCodeQ.toFloat() * (screenWidth.toFloat() / keyCodeR.toFloat())).toInt()
            }
        } else if (keyCodeQ >= screenWidth) {
            layoutParams.width =
                (screenWidth.toFloat() / (keyCodeQ.toFloat() / keyCodeR.toFloat())).toInt()
            layoutParams.height = screenWidth
        } else {
            layoutParams.width =
                (keyCodeR.toFloat() * (screenWidth.toFloat() / keyCodeQ.toFloat())).toInt()
            layoutParams.height = screenWidth
        }
        binding.cropView.layoutParams = layoutParams

        binding.cropView.setImageBitmap(
            Bitmap.createBitmap(
                layoutParams.width, layoutParams.height, Bitmap.Config.ARGB_8888
            )
        )
    }


    private fun getDimension() {
        if (keyCodeW == 90 || keyCodeW == 270) {
            aa = keyCodeQ.toFloat()
            ab = keyCodeR.toFloat()
            ac = binding.cropView.width.toFloat()
            ad = binding.cropView.height.toFloat()
            z = (Edge.LEFT.coordinate * aa / ac).toInt()
            a = (Edge.RIGHT.coordinate * aa / ac).toInt()
            b = (Edge.TOP.coordinate * ab / ad).toInt()
            y = (Edge.BOTTOM.coordinate * ab / ad).toInt()
            return
        }
        aa = keyCodeR.toFloat()
        ab = keyCodeQ.toFloat()
        ac = binding.cropView.width.toFloat()
        ad = binding.cropView.height.toFloat()
        z = (Edge.LEFT.coordinate * aa / ac).toInt()
        a = (Edge.RIGHT.coordinate * aa / ac).toInt()
        b = (Edge.TOP.coordinate * ab / ad).toInt()
        y = (Edge.BOTTOM.coordinate * ab / ad).toInt()
    }

    private fun startCrop() {
        when (keyCodeW) {
            90 -> {
                try {
                    o = b
                    val i2: Int = z
                    u = b
                    v = a
                    m = y
                    n = z
                    s = y
                    t = a
                    ag = m - o
                    ah = v - i2
                    p = q - (ah + i2)
                } catch (e2: Exception) {
                    e2.printStackTrace()
                }
            }
            270 -> {
                try {
                    val i3: Int = b
                    val i4: Int = z
                    u = b
                    v = a
                    m = y
                    n = z
                    s = y
                    t = a
                    ag = m - i3
                    ah = v - i4
                    o = r - (ag + i3)
                    p = i4
                } catch (e3: Exception) {
                    e3.printStackTrace()
                }
            }
            else -> {
                try {
                    o = z
                    p = b
                    u = a
                    v = b
                    m = z
                    n = y
                    s = a
                    t = y
                    ag = u - o
                    ah = n - p
                } catch (e4: Exception) {
                    e4.printStackTrace()
                }
            }
        }
        cropVideoDuration = getVideoDuration(this, mainCachedFile)

        cropOutputFilePath = createCroppedFile().toString()

        try {
            val sb = java.lang.StringBuilder()
            sb.append("crop=w=")
            sb.append(ag)
            sb.append(":h=")
            sb.append(ah)
            sb.append(":x=")
            sb.append(o)
            sb.append(":y=")
            sb.append(p)

            executeFFMPEG(
                arrayOf(
                    "-y",
                    "-ss",
                    "0",
                    "-t",
                    cropVideoDuration.toString(),
                    "-i",
                    file.toString(),
                    "-strict",
                    "experimental",
                    "-vf",
                    sb.toString(),
                    "-r",
                    "15",
                    "-ab",
                    "128k",
                    "-vcodec",
                    "mpeg4",
                    "-acodec",
                    "copy",
                    "-b:v",
                    "2500k",
                    "-sample_fmt",
                    "s16",
                    "-ss",
                    "0",
                    "-t",
                    cropVideoDuration.toString(),
                    cropOutputFilePath!!
                ), cropOutputFilePath!!
            )
        } catch (unused: Exception) {
            val file2 = File(cropOutputFilePath!!)
            if (file2.exists()) {
                file2.delete()
                return
            }
            Toast.makeText(this, "Please select any option!", Toast.LENGTH_LONG).show()
        }
    }

    private fun executeFFMPEG(strArr: Array<String>, str: String) {
        val progressDialog = CustomWaitingDialog(this)
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            FFmpeg.cancel()
        }
        progressDialog.show()
        progressDialog.setText("Cropped 0%")

        binding.imageViewFree.setImageResource(R.drawable.crop_unselect)
        binding.imageView11.setImageResource(R.drawable.crop_unselect)
        binding.imageViewPortrait.setImageResource(R.drawable.crop_unselect)
        binding.imageViewLandScape.setImageResource(R.drawable.crop_unselect)

        if (binding.videoView.isPlaying) {
            binding.videoView.pause()
            binding.videoView.seekTo(0)
            binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
        }

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
                    startActivity(Intent(this, PlayerActivity::class.java))
                    Config.resetStatistics()
                    finish()
                }
                Config.RETURN_CODE_CANCEL -> {
                    try {
                        File(str).delete()
                        deleteFromGallery(str, this)
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
                else -> {
                    try {
                        Config.resetStatistics()
                        File(str).delete()
                        deleteFromGallery(str, this)
                        Toast.makeText(
                            this, "Please Try Again $returnCode", Toast.LENGTH_LONG
                        ).show()
                    } catch (th: Throwable) {
                        th.printStackTrace()
                    }
                }
            }
        }

        Config.printLastCommandOutput(Log.INFO)
        Config.enableStatisticsCallback {
            val percentage =
                ((it.time.toFloat() / (cropVideoDuration!! * 1000).toFloat()) * 100).toInt()
            if (percentage in 0..100) {
                progressDialog.setText("Cropped $percentage%")
            } else {
                progressDialog.setText("Please wait")
            }
        }

    }


    private fun cropSelect(newValue: Int) {
        when (newValue) {
            1 -> {
                binding.cropView.setFixedAspectRatio(false)
            }
            2 -> {
                binding.cropView.setFixedAspectRatio(true)
                binding.cropView.setAspectRatio(10, 10)
            }
            3 -> {
                binding.cropView.setFixedAspectRatio(true)
                binding.cropView.setAspectRatio(9, 16)
            }
            4 -> {
                binding.cropView.setFixedAspectRatio(true)
                binding.cropView.setAspectRatio(16, 9)
            }
        }
    }

    private fun onVideoPrepared() {
        mDuration = binding.videoView.duration / 1000
        setSeekBarPosition()
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
            )
            updateProgressBar()
        }
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    private fun milliSecondsToTimer(milliseconds: Long): String {
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
                binding.seekBar.progress = binding.videoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                binding.videoView.seekTo(mStartPosition * 1000)
                binding.videoView.pause()
                binding.seekBar.progress = 0
                binding.totalDurationTextView.text = "00:00"
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.seekBar.progress = binding.videoView.currentPosition - mStartPosition * 1000
                binding.totalDurationTextView.text = milliSecondsToTimer(
                    binding.seekBar.progress.toLong()
                ) + ""
                mHandler.postDelayed(this, 100)
            }
        }
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

    private fun onVideoCompleted() {
        mHandler.removeCallbacks(mUpdateTimeTask)
        binding.seekBar.progress = 0
        binding.videoView.seekTo(mStartPosition * 1000)
        binding.videoView.pause()
        binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
    }

    override fun onBackPressed() {
        exitDialog()
        trimOrCrop = false
    }

    private fun exitDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.exit_dialog)

        val noBtn = dialog.findViewById<TextView>(R.id.noBtn)
        val yesBtn = dialog.findViewById<TextView>(R.id.yesBtn)

        yesBtn.setOnClickListener {
            finish()
            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }


}