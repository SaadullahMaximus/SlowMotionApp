package com.example.slowmotionapp.ui.fragments.savedfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.slowmotionapp.adapters.VideoAdapter
import com.example.slowmotionapp.databinding.FragmentSavedEditedBinding
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.croppedFiles
import com.example.slowmotionapp.utils.Utils

class SavedEditedFragment : Fragment() {

    private var _binding: FragmentSavedEditedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val videoAdapter =
            VideoAdapter(requireContext(), croppedFiles)

        _binding = FragmentSavedEditedBinding.inflate(inflater, container, false)
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 3)
            adapter = videoAdapter
        }

        return binding.root
    }
}