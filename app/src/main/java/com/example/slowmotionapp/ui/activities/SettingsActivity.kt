package com.example.slowmotionapp.ui.activities

import android.content.res.Resources
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.ActivitySettingsBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.quit.setOnClickListener {
            showCustomBottomSheet()
        }
    }

    private fun showCustomBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.custom_bottom_sheet, null)
        dialog.setContentView(view)

        val btnYes = view.findViewById<Button>(R.id.btnYes)
        val btnNo = view.findViewById<Button>(R.id.btnNo)

        btnYes.setOnClickListener {
            finishAffinity()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }

        view.setOnTouchListener { _, event ->
            val screenHeight = Resources.getSystem().displayMetrics.heightPixels
            val deltaY = screenHeight - event.rawY
            val threshold = 200

            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    if (deltaY > threshold) {
                        // Move the bottom sheet downward
                        dialog.behavior.peekHeight = deltaY.toInt()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (deltaY > threshold) {
                        // Close the bottom sheet if moved below the threshold
                        dialog.dismiss()
                    } else {
                        // Reset the bottom sheet to its original position
                        dialog.behavior.peekHeight = 0

                        // Perform click if the touch didn't exceed the threshold
                        view.performClick()
                    }
                }
            }

            true
        }


        dialog.show()
    }


}