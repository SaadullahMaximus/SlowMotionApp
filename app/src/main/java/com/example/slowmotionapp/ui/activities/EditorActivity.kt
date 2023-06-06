package com.example.slowmotionapp.ui.activities

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityEditorBinding
import com.example.slowmotionapp.ui.fragments.VideoCheckFragment
import java.io.File

class EditorActivity : AppCompatActivity() {

    private var videoUri: String? = null
    private var type: Int = 0
    private lateinit var binding: ActivityEditorBinding
    private lateinit var trimmedVideoPath: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, VideoCheckFragment() as Fragment)
            .commit()


        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        type = intent.getIntExtra(Constants.TYPE, 0)
        Log.d("MaximusTech", "onCreate: $videoUri")
    }

    fun getVideoUri(): String? {
        // Return the video URI
        return videoUri
    }

    fun getType(): Int {
        return type
    }

    fun setTrimVideoPath(outputFile: File) {
        trimmedVideoPath = outputFile.toString()
    }

    fun getTrimmedPath(): String {
        return trimmedVideoPath
    }
}