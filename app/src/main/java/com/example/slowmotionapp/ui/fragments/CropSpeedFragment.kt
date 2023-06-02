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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCropSpeedBinding.inflate(inflater, container, false)

        childFragmentManager = getChildFragmentManager()

        currentChildFragment = MainFragment()
        childFragmentManager!!.beginTransaction()
            .add(R.id.fragment_container_main, currentChildFragment as MainFragment)
            .commit()


        val videoUri: String = (activity as EditorActivity?)!!.getTrimmedPath()
        Log.d("Hello", "onCreateView: $videoUri")
//        binding.trimVideoView.setVideoURI(Uri.parse(videoUri))
//        binding.trimVideoView.start()

//        binding.knobView.setInitialKnobValue(6)

//        val knobAnimator = ValueAnimator.ofFloat(binding.knobView.knobPositionX, 700F)
//        knobAnimator.addUpdateListener { animator ->
//            val animatedValue = animator.animatedValue as Float
//            binding.knobView.knobPositionX = animatedValue
//        }
//        // Set the interpolator for the bounce effect
//        knobAnimator.interpolator = BounceInterpolator()
//
//        // Set the duration of the animation in milliseconds
//        knobAnimator.duration = 1000
//
//        // Start the animation
//        knobAnimator.start()


//        binding.knobView.setOnKnobPositionChangeListener(object : KnobView.OnKnobPositionChangeListener {
//            override fun onKnobPositionChanged(knobValue: Int) {
//                // Handle the knob value change here
//                // You can display it in a TextView or perform any other actions
//                Log.d("Knob", "onKnobPositionChanged: $knobValue")
//            }
//        })


        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

//    // Create the bounce animation
//    fun createBounceAnimation(view: View) {
//        val animatorSet = AnimatorSet()
//
//        // Translation animation
//        val translationX = ObjectAnimator.ofFloat(view, "translationX", -200f, 0f)
//        translationX.repeatCount = 2
//        translationX.repeatMode = ObjectAnimator.REVERSE
//
//        // Scale animation
//        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 1.2f, 0.9f, 1.1f, 1f)
//        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.8f, 1.1f, 0.9f, 1f)
//
//        // Set interpolator for the animations
//        translationX.interpolator = BounceInterpolator()
//        scaleX.interpolator = BounceInterpolator()
//        scaleY.interpolator = BounceInterpolator()
//
//        // Set the duration for the animations
//        translationX.duration = 500
//        scaleX.duration = 500
//        scaleY.duration = 500
//
//        // Add the animations to the animator set
//        animatorSet.playTogether(translationX, scaleX, scaleY)
//
//        // Start the animation
//        animatorSet.start()
//    }

}