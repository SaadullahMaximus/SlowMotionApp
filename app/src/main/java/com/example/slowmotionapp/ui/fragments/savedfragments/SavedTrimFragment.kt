package com.example.slowmotionapp.ui.fragments.savedfragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.slowmotionapp.R
import com.example.slowmotionapp.adapters.VideoAdapter
import com.example.slowmotionapp.databinding.FragmentSavedTrimBinding
import com.example.slowmotionapp.ui.activities.SavedActivity
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.trimmedFiles
import com.example.slowmotionapp.utils.Utils.deleteVideoFile
import com.example.slowmotionapp.utils.Utils.editVideo
import com.example.slowmotionapp.utils.Utils.fetchVideosFromDirectory
import com.example.slowmotionapp.utils.Utils.refreshGallery
import com.example.slowmotionapp.utils.Utils.shareVideo
import com.example.slowmotionapp.utils.Utils.showRenameDialog
import com.example.slowmotionapp.utils.Utils.trimmedDir

class SavedTrimFragment : Fragment(), VideoAdapter.VideoItemClickListener {

    private var _binding: FragmentSavedTrimBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoAdapter: VideoAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedTrimBinding.inflate(inflater, container, false)

        if (trimmedFiles.isNotEmpty()) {

            binding.recyclerView.visibility = View.VISIBLE

            binding.lottieAnimationView.visibility = View.GONE
            binding.btnCreateNew.visibility = View.GONE
            binding.title.visibility = View.GONE

            adapterSet()

        }

        binding.btnCreateNew.setOnClickListener {
            (activity as? SavedActivity)?.openGallery()
        }

        return binding.root

    }

    private fun adapterSet() {
        videoAdapter = VideoAdapter(requireContext(), trimmedFiles)

        videoAdapter.setVideoItemClickListener(this)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoAdapter
        }
    }

    override fun onButtonClicked(videoPath: String, position: Int) {
        showFullScreenDialog(videoPath, position)
    }

    private fun showFullScreenDialog(videoPath: String, position: Int) {

        val dialog = Dialog(requireContext(), R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.player_long_pressed_dialog)

        val btnEdit = dialog.findViewById<TextView>(R.id.btnEdit)
        val btnShare = dialog.findViewById<TextView>(R.id.btnShare)
        val btnRename = dialog.findViewById<TextView>(R.id.btnRename)
        val btnDelete = dialog.findViewById<TextView>(R.id.btnDelete)
        val overLayout = dialog.findViewById<FrameLayout>(R.id.overlay_layout)

        btnEdit.setOnClickListener {
            requireActivity().editVideo(videoPath)
            dialog.dismiss()
        }

        btnShare.setOnClickListener {
            requireActivity().shareVideo(videoPath)
            dialog.dismiss()
        }

        btnRename.setOnClickListener {
            requireActivity().showRenameDialog(videoPath) {
                videoAdapter.notifyItemChanged(position)
                trimmedFiles = fetchVideosFromDirectory(trimmedDir)
                adapterSet()
            }
            dialog.dismiss()
        }

        btnDelete.setOnClickListener {
            deleteVideoFile(videoPath)
            videoAdapter.deleteItem(position) { recyclerViewGone() }
            videoAdapter.notifyItemChanged(position)
            refreshGallery(videoPath, requireContext())
            dialog.dismiss()
        }

        overLayout.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun recyclerViewGone() {
        binding.recyclerView.visibility = View.GONE

        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.btnCreateNew.visibility = View.VISIBLE
        binding.title.visibility = View.VISIBLE
    }
}