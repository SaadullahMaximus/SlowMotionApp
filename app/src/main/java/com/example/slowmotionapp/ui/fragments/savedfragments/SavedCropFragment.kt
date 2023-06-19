package com.example.slowmotionapp.ui.fragments.savedfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.slowmotionapp.adapters.VideoAdapter
import com.example.slowmotionapp.databinding.FragmentSavedCropBinding
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.adapterShowing
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.croppedFiles

class SavedCropFragment : Fragment() {

    private var _binding: FragmentSavedCropBinding? = null
    private val binding get() = _binding!!

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

        }

        return binding.root
    }

    private fun adapterSet(){
        val videoAdapter = VideoAdapter(requireContext(), croppedFiles)

        adapterShowing = videoAdapter

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoAdapter
        }
    }

}