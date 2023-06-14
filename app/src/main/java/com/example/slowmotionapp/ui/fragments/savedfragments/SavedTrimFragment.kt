package com.example.slowmotionapp.ui.fragments.savedfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.slowmotionapp.adapters.VideoAdapter
import com.example.slowmotionapp.databinding.FragmentSavedTrimBinding
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.trimmedFiles
import com.example.slowmotionapp.utils.Utils.fetchVideosFromDirectory
import com.example.slowmotionapp.utils.Utils.trimmedDir

class SavedTrimFragment : Fragment() {

    private var _binding: FragmentSavedTrimBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedTrimBinding.inflate(inflater, container, false)

        val videoAdapter = VideoAdapter(requireContext(), trimmedFiles)

        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoAdapter
        }

        return binding.root

    }
}