package com.example.slowmotionapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.slowmotionapp.customviews.KnobView
import com.example.slowmotionapp.databinding.FragmentCropSpeedBinding
import com.example.slowmotionapp.ui.activities.EditorActivity

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
//        binding.trimVideoView.setVideoURI(Uri.parse(videoUri))
//        binding.trimVideoView.start()

        binding.knobView.setInitialKnobValue(7)


        binding.knobView.setOnKnobPositionChangeListener(object : KnobView.OnKnobPositionChangeListener {
            override fun onKnobPositionChanged(knobValue: Int) {
                // Handle the knob value change here
                // You can display it in a TextView or perform any other actions
                Log.d("Knob", "onKnobPositionChanged: $knobValue")
            }
        })


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}