package com.example.slowmotionapp.ui.activities

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityEffectBinding
import com.example.slowmotionapp.effects.EPlayerView
import com.example.slowmotionapp.effects.FilterAdapter
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.Utils.player
import com.google.android.exoplayer2.Player

class EffectActivity : AppCompatActivity(), FilterAdapter.OnItemClickListener {

    private lateinit var binding: ActivityEffectBinding

    private var videoUri: String? = null
    private var type: Int = 0

    private lateinit var adapter: FilterAdapter
    private lateinit var filterTypes: List<FilterType>

    private val mHandler = Handler(Looper.getMainLooper())
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    private var mStartPosition = 0
    private var mDuration = 0
    private var mEndPosition = 0
    private var mTimeVideo = 0
    private val mMaxDuration = 120
    private var mDurationWithoutEdit = 0

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

        mainCachedFile = videoUri!!

        binding.backBtn.setOnClickListener {
            finish()
        }

        Utils.setUpSimpleExoPlayer(this)
        setUoGlPlayerView()

        filterTypes = FilterType.createFilterList()

        // Initialize RecyclerView
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = FilterAdapter(filterTypes, this, lifecycleScope)
        binding.recyclerView.adapter = adapter

        player!!.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_READY && playWhenReady) {
                    // ExoPlayer is ready to play
                    // You can start playing the media here
                    binding.seekBar.max = player!!.duration.toInt()

                    player!!.playWhenReady = true

                    // Initialize handler and runnable
                    handler = Handler(Looper.getMainLooper())
                    runnable = Runnable { updateSeekBar() }
                }
                if (playbackState == Player.STATE_ENDED) {
                    binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
                    player!!.seekTo(0)
                    player!!.pause()
                }
            }
        })

        binding.seekBar.isEnabled = false

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
            saveVideoWithFilter()
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)
                binding.seekBar.max = mTimeVideo * 1000
                binding.seekBar.progress = 0
                player?.seekTo((mStartPosition * 1000).toLong())
                player?.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask)

                player?.seekTo((mStartPosition * 1000 + seekBar.progress).toLong())
            }
        })

        binding.playPauseButton.setOnClickListener {
            if (player?.isPlaying!!) {
                player?.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                videoPlay()
            }
        }

    }

    private val mUpdateTimeTask: Runnable = object : Runnable {
        @SuppressLint("SetTextI18n")
        override fun run() {
            if (binding.seekBar.progress >= binding.seekBar.max) {
                binding.seekBar.progress =
                    (player?.currentPosition!! - mStartPosition * 1000).toInt()
                player?.seekTo((mStartPosition * 1000).toLong())
                player?.pause()
                binding.seekBar.progress = 0
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.seekBar.progress =
                    (player?.currentPosition?.minus(mStartPosition * 1000))!!.toInt()
                mHandler.postDelayed(this, 100)
            }
        }
    }

    private fun saveVideoWithFilter() {

        val progressDialog =
            ProgressDialog(this, R.style.CustomDialog)
        progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")
        progressDialog.show()

        if (effectPosition != 0) {
            val outputFile = Utils.createCacheTempFile(this)
            val filter = FilterType.createGlFilter(
                FilterType.createFilterList()[effectPosition],
                this
            )
            Mp4Composer(mainCachedFile, outputFile)
                .rotation(Rotation.NORMAL)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(filter)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        Log.d(Constants.APP_NAME, "onProgress Filter = " + progress * 100)
                    }

                    override fun onCompleted() {
                        Log.d(Constants.APP_NAME, "onCompleted() Filter : $outputFile")
                        mainCachedFile = outputFile
                        progressDialog.dismiss()
                        Utils.saveEditedVideo(this@EffectActivity)
                    }

                    override fun onCanceled() {
                        progressDialog.dismiss()
                        Log.d(Constants.APP_NAME, "onCanceled")
                    }

                    override fun onFailed(exception: Exception) {
                        progressDialog.dismiss()
                        Log.e(Constants.APP_NAME, "onFailed() Filter", exception)
                    }
                })
                .start()
        } else {
            progressDialog.dismiss()
        }
    }

    private fun videoPlay() {
        player?.play()
        binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
        updateProgressBar()
    }

    private fun updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100)
    }

    private fun setUoGlPlayerView() {
        exoPLayerView =
            EPlayerView(this)
        exoPLayerView!!.setSimpleExoPlayer(player)

        val videoSize = Utils.getVideoSize(this, Uri.parse(mainCachedFile))
        if (videoSize != null) {
            val videoWidth = videoSize.first
            val videoHeight = videoSize.second

            val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Calculate the desired height based on the video aspect ratio
            val aspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
            val desiredHeight = (exoPLayerView!!.width / aspectRatio).toInt()

            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = desiredHeight

            exoPLayerView!!.layoutParams = layoutParams
        } else {
            exoPLayerView!!.layoutParams =
                RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
        }

        exoPLayerView!!.layoutParams =
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        binding.frameLayout.addView(exoPLayerView)
        exoPLayerView!!.onResume()
    }

    override fun onItemClick(position: Int) {
        exoPLayerView!!.setGlFilter(
            FilterType.createGlFilter(
                filterTypes[position],
                this
            )
        )
        effectPosition = position

    }

    private fun updateSeekBar() {
        // Update the seek bar progress with the current position of the player
        binding.seekBar.progress = player!!.currentPosition.toInt()

        // Schedule the next update after a certain delay
        handler.postDelayed(runnable, 1000) // Update every second (adjust as needed)
    }

    private fun startTrackingSeekBar() {
        // Start updating the seek bar progress
        handler.postDelayed(runnable, 0)
    }

    private fun stopTrackingSeekBar() {
        // Stop updating the seek bar progress
        handler.removeCallbacks(runnable)
    }

}