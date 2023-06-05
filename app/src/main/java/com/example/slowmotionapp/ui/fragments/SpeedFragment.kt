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
import com.example.slowmotionapp.sqlite.DatabaseManager
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.dataBasePosition
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.knobPosition
import com.example.slowmotionapp.utils.Utils

class SpeedFragment : Fragment() {

    private var _binding: FragmentSpeedBinding? = null
    private val binding get() = _binding!!

    private lateinit var databaseManager: DatabaseManager


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        databaseManager = DatabaseManager(requireContext())
        // Inflate the layout for this fragment
        _binding = FragmentSpeedBinding.inflate(inflater, container, false)

        animateKnob(knobPosition)



        binding.knobView.setOnKnobPositionChangeListener(object :
            KnobView.OnKnobPositionChangeListener {
            override fun onKnobPositionChanged(knobValue: Int) {
                // Handle the knob value change here
                // You can display it in a TextView or perform any other actions
                Log.d("Knob", "onKnobPositionChanged: $knobValue")
            }

            override fun onKnobStopped(knobValue: Int) {
                handleKnobStopMoving(knobValue)
            }
        })



        return binding.root
    }

    private fun handleKnobStopMoving(knobValue: Int) {
        // Handle the event when the user stops moving the knob
        Log.d("Saad", "handleKnobStopMoving: Stop Moving")
        val tempFile = Utils.createCacheTempFile(requireContext())
        databaseManager.insertData(dataBasePosition, tempFile.toString(), knobValue)
        dataBasePosition += 1
        knobPosition = knobValue * 100F
        Log.d("Saad", "handleKnobStopMoving: FileName $tempFile")
        Log.d("Saad", "handleKnobStopMoving: KnobValue $knobValue")

    }


    private fun animateKnob(interval: Float) {
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