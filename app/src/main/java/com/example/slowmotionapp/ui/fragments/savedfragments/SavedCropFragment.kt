package com.example.slowmotionapp.ui.fragments.savedfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.slowmotionapp.adapters.VideoAdapter
import com.example.slowmotionapp.databinding.FragmentSavedCropBinding
import com.example.slowmotionapp.ui.activities.SavedActivity
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.croppedFiles

class SavedCropFragment : Fragment(), VideoAdapter.AdapterCallback {

    private var _binding: FragmentSavedCropBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoAdapter: VideoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedCropBinding.inflate(inflater, container, false)


        if (croppedFiles.isNotEmpty()) {

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
        videoAdapter = VideoAdapter(requireContext(), croppedFiles)
        videoAdapter.setAdapterCallback(this)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoAdapter
        }
    }

    override fun onFunctionCalled() {
        binding.recyclerView.visibility = View.GONE

        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.btnCreateNew.visibility = View.VISIBLE
        binding.title.visibility = View.VISIBLE
    }
}