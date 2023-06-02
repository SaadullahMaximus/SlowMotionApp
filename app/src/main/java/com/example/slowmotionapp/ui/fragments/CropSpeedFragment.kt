package com.example.slowmotionapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.FragmentCropSpeedBinding
import com.example.slowmotionapp.ui.activities.EditorActivity


class CropSpeedFragment : Fragment() {

    private var _binding: FragmentCropSpeedBinding? = null
    private val binding get() = _binding!!

    private var childFragmentManager: FragmentManager? = null
    private var currentChildFragment: Fragment? = null
    private var effectMusicFragment: Fragment? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropSpeedBinding.inflate(inflater, container, false)

        childFragmentManager = getChildFragmentManager()

        currentChildFragment = MainFragment()
        effectMusicFragment = EffectMusicFragment()

        binding.backBtn.setOnClickListener {
            requireActivity().finish()
        }

        childFragmentManager!!.beginTransaction()
            .add(R.id.fragment_container_main, currentChildFragment as MainFragment)
            .commit()

        binding.enhanceBtn.setOnClickListener {
            binding.enhanceBtn.visibility = View.GONE
            binding.backTextBtn.visibility = View.VISIBLE
            childFragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container_main, effectMusicFragment as EffectMusicFragment)
                .commit()
        }

        binding.backTextBtn.setOnClickListener {
            binding.enhanceBtn.visibility = View.VISIBLE
            binding.backTextBtn.visibility = View.GONE
            childFragmentManager!!.beginTransaction()
                .replace(R.id.fragment_container_main, currentChildFragment as MainFragment)
                .commit()
        }


        val videoUri: String = (activity as EditorActivity?)!!.getTrimmedPath()
        Log.d("Hello", "onCreateView: $videoUri")
//        binding.trimVideoView.setVideoURI(Uri.parse(videoUri))
//        binding.trimVideoView.start()


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}