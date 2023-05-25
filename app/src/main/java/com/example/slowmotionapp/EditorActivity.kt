package com.example.slowmotionapp

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.slowmotionapp.databinding.ActivityEditorBinding
import com.example.slowmotionapp.ui.VideoCheckFragment


class EditorActivity : AppCompatActivity() {

    private var videoUri: String? = null
    private lateinit var binding: ActivityEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, VideoCheckFragment() as Fragment)
            .commit()


        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        Log.d("MaximusTech", "onCreate: $videoUri")

    }

    fun getVideoUri(): String? {
        // Return the video URI
        return videoUri
    }


}