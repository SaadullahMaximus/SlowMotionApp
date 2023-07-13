package com.example.slowmotionapp.ui.activities

import android.app.Dialog
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.arthenica.mobileffmpeg.Config
import com.arthenica.mobileffmpeg.FFmpeg
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.customviews.CustomWaitingDialog
import com.example.slowmotionapp.databinding.ActivityEffectBinding
import com.example.slowmotionapp.effects.EPlayerView
import com.example.slowmotionapp.effects.FilterAdapter
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.Utils.createCacheTempFile
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import com.example.slowmotionapp.utils.Utils.getVideoSize
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
import com.example.slowmotionapp.utils.Utils.player
import com.example.slowmotionapp.utils.Utils.saveEditedVideo
import com.example.slowmotionapp.utils.Utils.setUpSimpleExoPlayer
import com.example.slowmotionapp.utils.Utils.singleClick
import com.google.android.exoplayer2.Player
import java.io.File

class EffectActivity : AppCompatActivity(), FilterAdapter.OnItemClickListener {

    private lateinit var binding: ActivityEffectBinding

    private var videoUri: String? = null
    private var type: Int = 0

    private lateinit var adapter: FilterAdapter
    private lateinit var filterTypes: List<FilterType>

    private var handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var mStartPosition = 0

    private lateinit var file: File

    private var mp4Composer: Mp4Composer? = null

    companion object {
        var exoPLayerView: EPlayerView? = null
        var effectPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEffectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        type = intent.getIntExtra(Constants.TYPE, 0)

        file = if (type == Constants.RECORD_VIDEO) {
            File(Utils.convertContentUriToFilePath(videoUri!!))
        } else {
            File(videoUri!!)
        }

        mainCachedFile = file.toString()

        binding.backBtn.setOnClickListener {
            exitDialog()
        }

        setUpSimpleExoPlayer(this)
        setUoGlPlayerView()

        filterTypes = FilterType.createFilterList()

        // Initialize RecyclerView
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = FilterAdapter(filterTypes, this, lifecycleScope, this)
        binding.recyclerView.adapter = adapter

        player!!.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    // ExoPlayer is ready to play
                    // You can start playing the media here
                    binding.seekBar.max = player!!.duration.toInt()

                    // Initialize handler and runnable
                    handler = Handler(Looper.getMainLooper())
                    runnable = Runnable { updateSeekBar() }
                }
                if (playbackState == Player.STATE_ENDED) {
                    player!!.seekTo(0)
                }
            }
        })

        binding.playPauseButton.setOnClickListener {
            if (player!!.isPlaying) {
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
                player!!.pause()
                // Stop tracking the seek bar progress
                stopTrackingSeekBar()
            } else {
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
                player!!.play()
                startTrackingSeekBar()
            }
        }


        binding.saveBtn.setOnClickListener {
            singleClick {
                saveVideoWithFilter()
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((mStartPosition * 1000 + seekBar.progress).toLong())
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                player?.seekTo((mStartPosition * 1000 + seekBar.progress).toLong())
            }
        })

        binding.totalDurationTextView.text =
            milliSecondsToTimer(getVideoDuration(this, mainCachedFile).toLong() * 1000)

    }

    override fun onBackPressed() {
        exitDialog()
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

    private fun saveVideoWithFilter() {

        if (effectPosition != 0) {

            val targetWidth = 720
            val targetHeight = 1280

            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(mainCachedFile)

            val originalWidth =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toInt()
                    ?: 0
            val originalHeight =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toInt()
                    ?: 0

            Log.d("VideoRes", "saveVideoWithFilter: $originalWidth $originalHeight")

            if (originalWidth > targetWidth || originalHeight > targetHeight) {
                Log.d("VideoRes", "saveVideoWithFilter: IF")
                val outputFilePath = createCacheTempFile(this)

                val aspectRatio = "" + targetWidth + "x" + "" + targetHeight

                val ffmpegCommand =
                    arrayOf(
                        "-ss",
                        "0",
                        "-y",
                        "-i",
                        mainCachedFile,
                        "-t",
                        getVideoDuration(this, mainCachedFile).toString(),
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
                Log.d("VideoRes", "saveVideoWithFilter: ELSE")
                applyFilter()
            }
        } else {
            Toast.makeText(this, "Please select Effect.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun applyFilter() {

        val progressDialog = CustomWaitingDialog(this)
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            mp4Composer?.cancel()
        }
        progressDialog.show()
        progressDialog.setText("Please wait")

        Log.d("VideoRes", "saveVideoWithFilter: Apply Filter")

        val outputFile = createCacheTempFile(this)
        val filter = FilterType.createGlFilter(
            FilterType.createFilterList()[effectPosition], this
        )
        mp4Composer = Mp4Composer(mainCachedFile, outputFile).rotation(Rotation.NORMAL)
            .fillMode(FillMode.PRESERVE_ASPECT_FIT).filter(filter)
            .listener(object : Mp4Composer.Listener {
                override fun onProgress(progress: Double) {
                    this@EffectActivity.runOnUiThread {
                        progressDialog.setText("Filter Applied ${(progress * 100).toInt()}%")
                    }
                }

                override fun onCompleted() {
                    mainCachedFile = outputFile
                    runOnUiThread {
                        progressDialog.dismiss()
                        saveEditedVideo(this@EffectActivity)
                        effectPosition = 0
                    }
                }

                override fun onCanceled() {
                    progressDialog.dismiss()
                }

                override fun onFailed(exception: Exception) {
                    Log.d("exception", "onFailed: $exception")
                    progressDialog.dismiss()
                }
            }).start()
    }

    private fun executeFFMPEGCommand(command: Array<String>, outputFilePath: String) {
        val progressDialog = CustomWaitingDialog(this)
        progressDialog.setCloseButtonClickListener {
            progressDialog.dismiss()
            FFmpeg.cancel()
        }
        progressDialog.show()
        progressDialog.setText("Preparing Video")

        val ffmpegCommand: String = Utils.commandsGenerator(command)

        FFmpeg.executeAsync(
            ffmpegCommand
        ) { _, returnCode ->
            Config.printLastCommandOutput(Log.INFO)
            when (returnCode) {
                Config.RETURN_CODE_SUCCESS -> {
                    progressDialog.dismiss()
                    Config.resetStatistics()
                    mainCachedFile = outputFilePath
                    applyFilter()
                }
                Config.RETURN_CODE_CANCEL -> {
                    progressDialog.dismiss()
                    Config.resetStatistics()
                    try {
                        File(outputFilePath).delete()
                        Utils.deleteFromGallery(outputFilePath, this)
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
                        Utils.deleteFromGallery(outputFilePath, this)
                        Toast.makeText(this, "Error Creating Video", Toast.LENGTH_SHORT)
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

    private fun setUoGlPlayerView() {
        exoPLayerView = EPlayerView(this)
        exoPLayerView!!.setSimpleExoPlayer(player)

        Log.d("mainCachedFile", "setUoGlPlayerView: $mainCachedFile")

        val videoSize = getVideoSize(this, Uri.parse(mainCachedFile))
        if (videoSize != null) {
            val videoWidth = videoSize.first
            val videoHeight = videoSize.second

            val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val aspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
            val desiredHeight = (exoPLayerView!!.width / aspectRatio).toInt()

            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = desiredHeight

            exoPLayerView!!.layoutParams = layoutParams
        } else {
            exoPLayerView!!.layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        exoPLayerView!!.layoutParams = RelativeLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        binding.layoutMovieWrapper.addView(exoPLayerView)
        exoPLayerView!!.onResume()
    }

    override fun onItemClick(position: Int) {
        exoPLayerView!!.setGlFilter(
            FilterType.createGlFilter(
                filterTypes[position], this
            )
        )
        effectPosition = position

    }

    private fun updateSeekBar() {
        binding.seekBar.progress = player!!.currentPosition.toInt()

        handler.postDelayed(runnable, 1000) // Update every second (adjust as needed)
    }

    private fun startTrackingSeekBar() {
        handler.postDelayed(runnable, 0)
    }

    private fun stopTrackingSeekBar() {
        handler.removeCallbacks(runnable)
    }

}