package com.example.slowmotionapp.ui

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.slowmotionapp.EditorActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentTrimVideoBinding
import java.util.*
import java.util.concurrent.TimeUnit

class TrimVideoFragment : Fragment() {

    private var _binding: FragmentTrimVideoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentTrimVideoBinding.inflate(inflater, container, false)

        val videoUri: Uri = (activity as EditorActivity?)!!.getVideoUri()!!

        Log.d("MaximusFragment", "onCreateView:2 $videoUri")


        val progressDialog = ProgressDialog(requireContext())
        progressDialog.setMessage("Preparing Video...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        binding.trimVideoView.setVideoURI(videoUri)

        binding.trimVideoView.setOnPreparedListener {
            val totalDuration = it.duration
            binding.seekBar.max = totalDuration
            binding.totalDurationTextView.text = formatDuration(totalDuration)
            progressDialog.dismiss()
            it.start()

            binding.trimVideoView.pause()

            it.setOnCompletionListener {
                binding.seekBar.progress = totalDuration
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            }

            // Update progress of SeekBar and duration as video plays
            Timer().scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentPosition = it.currentPosition
                    activity?.runOnUiThread {
                        binding.seekBar.progress = currentPosition
                        binding.totalDurationTextView.text = formatDuration(currentPosition)
                    }
                }
            }, 0, 100)

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        it.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {
                    // Not needed for your specific requirement, but you can implement any required functionality here
                }

                override fun onStopTrackingTouch(seekBar: SeekBar) {
                    // Not needed for your specific requirement, but you can implement any required functionality here
                }
            })

        }


        binding.playPauseButton.setOnClickListener {
            if (binding.trimVideoView.isPlaying) {
                binding.trimVideoView.pause()
                binding.playPauseButton.setImageResource(R.drawable.baseline_play_arrow)
            } else {
                binding.trimVideoView.start()
                binding.playPauseButton.setImageResource(R.drawable.baseline_pause)
            }
        }

        binding.backBtn.setOnClickListener {
            Toast.makeText(requireContext(), "Back Btn", Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun formatDuration(duration: Int): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration.toLong())
        val seconds = TimeUnit.MILLISECONDS.toSeconds(duration.toLong()) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }


}