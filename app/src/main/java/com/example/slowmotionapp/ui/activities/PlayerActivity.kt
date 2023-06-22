package com.example.slowmotionapp.ui.activities

import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityPlayerBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.adapterShowing
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.positionClicked
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.Utils.deleteVideoFile
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
import java.io.File

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
        pauseVideo()

        val btnEdit = dialog.findViewById<TextView>(R.id.btnEdit)
        val btnShare = dialog.findViewById<TextView>(R.id.btnShare)
        val btnRename = dialog.findViewById<TextView>(R.id.btnRename)
        val btnDelete = dialog.findViewById<TextView>(R.id.btnDelete)
        val overLayout = dialog.findViewById<FrameLayout>(R.id.overlay_layout)

        btnEdit.setOnClickListener {
            val uri = Uri.parse(playVideo)
            val intent = Intent(this, TrimVideoActivity::class.java)
            intent.putExtra("VideoUri", playVideo)
            intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
            intent.putExtra(
                "VideoDuration",
                Utils.getMediaDuration(this, uri)
            )
            finish()
            startActivity(intent)
            dialog.dismiss()
        }

        btnShare.setOnClickListener {
            shareVideo(playVideo)
            dialog.dismiss()
        }

        btnRename.setOnClickListener {
            showRenameDialog()
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deleteVideoFile(playVideo)
            adapterShowing.deleteItem(positionClicked)

            dialog.dismiss()
            finish()
        }


        overLayout.setOnClickListener {
            binding.playBtn.setImageResource(R.drawable.baseline_pause)
            binding.videoView.start()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showRenameDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.rename_dialog)

        val fileName = dialog.findViewById<EditText>(R.id.fileName)
        val btnOk = dialog.findViewById<TextView>(R.id.okBtn)
        val btnCancel = dialog.findViewById<TextView>(R.id.cancelBtn)


        btnOk.setOnClickListener {
            val text = fileName.text.toString()
            if (text.isNotEmpty()) {
                // The EditText has non-empty text
                // Perform your desired actions here
                File(playVideo).renameTo(File(File(playVideo).parent, "$text.mp4"))
            } else {
                Toast.makeText(this, "Please enter a valid name!", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
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

    fun shareVideo(videoPath: String) {
        // Create the intent

        // Create the intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"

        // Set the path of the video file

        // Set the path of the video file
        val videoUri = Uri.parse(videoPath)
        shareIntent.putExtra(Intent.EXTRA_STREAM, videoUri)

        // Optionally, you can set a subject for the shared video

        // Optionally, you can set a subject for the shared video
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Video")

        // Start the activity for sharing

        // Start the activity for sharing
        startActivity(Intent.createChooser(shareIntent, "Share Video"))

    }
}

