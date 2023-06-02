package com.example.slowmotionapp.ui.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import androidx.fragment.app.Fragment
import com.example.slowmotionapp.customviews.KnobView
import com.example.slowmotionapp.databinding.FragmentSpeedBinding

class SpeedFragment : Fragment() {

    private var _binding: FragmentSpeedBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSpeedBinding.inflate(inflater, container, false)

        binding.knobView.setInitialKnobValue(6)
        animateKnob(700F)



        binding.knobView.setOnKnobPositionChangeListener(object :
            KnobView.OnKnobPositionChangeListener {
            override fun onKnobPositionChanged(knobValue: Int) {
                // Handle the knob value change here
                // You can display it in a TextView or perform any other actions
                Log.d("Knob", "onKnobPositionChanged: $knobValue")
            }
        })



        return binding.root
    }

    fun animateKnob(interval: Float) {
        val knobAnimator = ValueAnimator.ofFloat(binding.knobView.knobPositionX, interval)
        knobAnimator.addUpdateListener { animator ->
            val animatedValue = animator.animatedValue as Float
            binding.knobView.knobPositionX = animatedValue
        }
        // Set the interpolator for the bounce effect
        knobAnimator.interpolator = BounceInterpolator()

        // Set the duration of the animation in milliseconds
        knobAnimator.duration = 1000

        // Start the animation
        knobAnimator.start()
    }
}