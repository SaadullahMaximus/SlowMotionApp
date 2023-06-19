package com.example.slowmotionapp.ui.activities

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.ActivityPlayerBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.adapterShowing
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.positionClicked
import com.example.slowmotionapp.utils.Utils.deleteVideoFile
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer

class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding

    private lateinit var handler: Handler
    private lateinit var progressUpdateRunnable: Runnable

    private var isSeeking = false

    private var visible = true

    private var duration: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the handler and runnable for hiding views
        handler = Handler(Looper.getMainLooper())
        val hideViewsRunnable = Runnable {
            hideViews()
        }
        // Start the delayed runnable to hide views after 5 seconds
        handler.postDelayed(hideViewsRunnable, 5000)

        binding.videoView.setVideoURI(Uri.parse(playVideo))

        binding.videoView.setOnPreparedListener { mediaPlayer ->
            duration = mediaPlayer.duration
            binding.seekBar.max = duration
            binding.TotalDuration.text = milliSecondsToTimer(duration.toLong())
            binding.videoView.start()
            binding.playBtn.setImageResource(R.drawable.baseline_pause)
        }

        // Update seek bar progress continuously
        progressUpdateRunnable = Runnable {
            updateSeekBarProgress()
        }

        handler.postDelayed(progressUpdateRunnable, 100)

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    // Seek video to the selected position
                    binding.videoView.seekTo(progress)
                    handler.removeCallbacks(hideViewsRunnable)
                    showViews()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                isSeeking = true
                handler.removeCallbacks(progressUpdateRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                isSeeking = false
                handler.postDelayed(progressUpdateRunnable, 100)
                handler.postDelayed(hideViewsRunnable, 5000)
            }
        })

        binding.overlayLayoutPlayer.setOnClickListener {
            if (visible) {
                hideViews()
            } else {
                showViews()
                handler.removeCallbacks(hideViewsRunnable)
                handler.postDelayed(hideViewsRunnable, 5000)
            }
        }

        binding.overlayLayoutPlayer.setOnLongClickListener {
            showFullScreenDialog()
            true
        }

        binding.playBtn.setOnClickListener {
            if (binding.videoView.isPlaying) {
                binding.playBtn.setImageResource(R.drawable.baseline_play_arrow)
                binding.videoView.pause()
            } else {
                binding.playBtn.setImageResource(R.drawable.baseline_pause)
                binding.videoView.start()
            }
        }

        binding.videoView.setOnCompletionListener {
            binding.seekBar.progress = 0
            binding.videoView.seekTo(0)
            binding.playBtn.setImageResource(R.drawable.baseline_play_arrow)
        }

        binding.forwardBtn.setOnClickListener {
            val currentPosition = binding.videoView.currentPosition
            val newPosition =
                currentPosition + 5000 // Jump forward by 5 seconds (5000 milliseconds)
            if (newPosition <= duration) {
                binding.videoView.seekTo(newPosition)
                binding.seekBar.progress = newPosition
                binding.currentTime.text = milliSecondsToTimer(newPosition.toLong())
            }
        }

        binding.backwardBtn.setOnClickListener {
            val currentPosition = binding.videoView.currentPosition
            val newPosition =
                currentPosition - 5000 // Jump backward by 5 seconds (5000 milliseconds)
            if (newPosition >= 0) {
                binding.videoView.seekTo(newPosition)
                binding.seekBar.progress = newPosition
                binding.currentTime.text = milliSecondsToTimer(newPosition.toLong())
            }
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    private fun showFullScreenDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.player_long_pressed_dialog)

        val btnEdit = dialog.findViewById<TextView>(R.id.btnEdit)
        val btnShare = dialog.findViewById<TextView>(R.id.btnShare)
        val btnRename = dialog.findViewById<TextView>(R.id.btnRename)
        val btnDelete = dialog.findViewById<TextView>(R.id.btnDelete)
        val overLayout = dialog.findViewById<FrameLayout>(R.id.overlay_layout)

        btnEdit.setOnClickListener {
            Toast.makeText(this, "Edit", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        btnShare.setOnClickListener {
            Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show()
            pauseVideo()
            dialog.dismiss()
        }

        btnRename.setOnClickListener {
            Toast.makeText(this, "Rename", Toast.LENGTH_SHORT).show()
            pauseVideo()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            pauseVideo()
            deleteVideoFile(playVideo)
            adapterShowing.deleteItem(positionClicked)

            dialog.dismiss()
            finish()
        }


        overLayout.setOnClickListener {
            Toast.makeText(this, "Over layout", Toast.LENGTH_SHORT).show()
            pauseVideo()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun pauseVideo() {
        if (binding.videoView.isPlaying) {
            binding.playBtn.setImageResource(R.drawable.baseline_play_arrow)
            binding.videoView.pause()
        }
    }

    private fun updateSeekBarProgress() {
        if (!isSeeking) {
            binding.seekBar.progress = binding.videoView.currentPosition
            handler.postDelayed(progressUpdateRunnable, 100)
            binding.currentTime.text =
                milliSecondsToTimer(binding.videoView.currentPosition.toLong())
        }
    }

    private fun hideViews() {
        binding.forwardBtn.visibility = View.GONE
        binding.playBtn.visibility = View.GONE
        binding.backwardBtn.visibility = View.GONE
        visible = false
    }

    private fun showViews() {
        binding.forwardBtn.visibility = View.VISIBLE
        binding.playBtn.visibility = View.VISIBLE
        binding.backwardBtn.visibility = View.VISIBLE
        visible = true
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        binding.videoView.stopPlayback()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}

