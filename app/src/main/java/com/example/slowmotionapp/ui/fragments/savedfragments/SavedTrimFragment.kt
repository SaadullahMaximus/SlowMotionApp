package com.example.slowmotionapp.ui.fragments.savedfragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.slowmotionapp.adapters.VideoAdapter
import com.example.slowmotionapp.databinding.FragmentSavedTrimBinding
import com.example.slowmotionapp.ui.activities.SavedActivity
import com.example.slowmotionapp.utils.Utils.fetchVideosFromDirectory
import com.example.slowmotionapp.utils.Utils.trimmedDir
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class SavedTrimFragment : Fragment(), VideoAdapter.AdapterCallback {

    private var _binding: FragmentSavedTrimBinding? = null
    private val binding get() = _binding!!

    private lateinit var videoAdapter: VideoAdapter

    private val trimmedFiles = mutableListOf<File>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedTrimBinding.inflate(inflater, container, false)


        binding.recyclerView.visibility = View.GONE
        binding.lottieAnimationView.visibility = View.VISIBLE
        binding.btnCreateNew.visibility = View.VISIBLE
        binding.title.visibility = View.VISIBLE

        trimmedFiles.clear()

        // Fetch data asynchronously
        GlobalScope.launch(Dispatchers.IO) {
            trimmedFiles.addAll(fetchVideosFromDirectory(trimmedDir))

            // Switch back to the main thread to update UI
            withContext(Dispatchers.Main) {
                if (trimmedFiles.isNotEmpty()) {
                    binding.recyclerView.visibility = View.VISIBLE
                    binding.lottieAnimationView.visibility = View.GONE
                    binding.btnCreateNew.visibility = View.GONE
                    binding.title.visibility = View.GONE

                    adapterSet()
                } else {
                    binding.lottieAnimationView.visibility = View.VISIBLE
                    binding.btnCreateNew.visibility = View.VISIBLE
                    binding.title.visibility = View.VISIBLE
                }
            }
        }

        binding.btnCreateNew.setOnClickListener {
            (activity as? SavedActivity)?.openGallery()
        }

        return binding.root

    }

    private fun adapterSet() {
        videoAdapter = VideoAdapter(requireContext(), trimmedFiles)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}