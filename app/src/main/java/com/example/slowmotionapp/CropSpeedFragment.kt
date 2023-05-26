package com.example.slowmotionapp

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.slowmotionapp.databinding.FragmentCropSpeedBinding

class CropSpeedFragment : Fragment() {

    private var _binding: FragmentCropSpeedBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropSpeedBinding.inflate(inflater, container, false)



        val videoUri: String = (activity as EditorActivity?)!!.getTrimmedPath()
        Log.d("Hello", "onCreateView: $videoUri")
        binding.trimVideoView.setVideoURI(Uri.parse(videoUri))
        binding.trimVideoView.start()


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}