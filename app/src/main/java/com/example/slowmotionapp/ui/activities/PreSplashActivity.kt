package com.example.slowmotionapp.ui.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.ActivityPreSplashBinding
import com.example.slowmotionapp.utils.Utils.makeTextLink
import com.example.slowmotionapp.utils.Utils.openURL

@SuppressLint("CustomSplashScreen")
class PreSplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPreSplashBinding

    private val handler = Handler(Looper.getMainLooper())


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_SlowMotionApp)

        binding = ActivityPreSplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        makeTextLink(binding.TV, "Privacy Policy", true, resources.getColor(R.color.baseColor)) { openURL(this) }

        makeTextLink(binding.TV, "Terms", true, resources.getColor(R.color.baseColor)) { openURL(this) }

        handler.postDelayed({
            binding.animationView.visibility = View.INVISIBLE
            binding.check.visibility = View.VISIBLE
        }, 2000)


        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.letsGo.visibility = View.VISIBLE // Hide the view when checkbox is checked
            } else {
                binding.letsGo.visibility =
                    View.INVISIBLE // Show the view when checkbox is unchecked
            }
        }

        binding.letsGo.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


    }
}