package com.example.slowmotionapp.ui.activities

import android.app.Dialog
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityEditorBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.fragments.CropSpeedFragment
import com.example.slowmotionapp.viewmodel.SharedViewModel

class EditorActivity : AppCompatActivity() {

    private var videoUri: String? = null
    private var type: Int = 0
    private lateinit var binding: ActivityEditorBinding

    private lateinit var sharedViewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]

        supportFragmentManager.beginTransaction().remove(CropSpeedFragment()).commit()

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, CropSpeedFragment() as Fragment)
            .commit()


        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        mainCachedFile = videoUri!!
        type = intent.getIntExtra(Constants.TYPE, 0)
    }

    override fun onBackPressed() {
        exitDialog()
    }

    private fun exitDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.exit_dialog)

        val noBtn = dialog.findViewById<TextView>(R.id.noBtn)
        val yesBtn = dialog.findViewById<TextView>(R.id.yesBtn)

        yesBtn.setOnClickListener {
            finish()
            sharedViewModel.stopAllMusic(true)
            dialog.dismiss()
        }

        noBtn.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onPause() {
        super.onPause()
        sharedViewModel.musicSelectPauseEveryThing(true)
    }

}