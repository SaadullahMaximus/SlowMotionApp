package com.example.slowmotionapp.ui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.slowmotionapp.EditorActivity
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityMainBinding
import com.example.slowmotionapp.utils.Utils
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isExpanded = false
    private var videoFile: File? = null
    private var videoUri: Uri? = null
    private var permissionList: ArrayList<String> = ArrayList()
    private lateinit var preferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = this.getSharedPreferences("fetch_permission", Context.MODE_PRIVATE)

        requestPermissions(
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO"
            ), 101
        )

        binding.fabCreate.setOnClickListener {
            if (isExpanded) {
                collapseButtons()
            } else {
                expandButtons()
            }
        }

        binding.captureVideoBtn.setOnClickListener {
            startCamera()
        }

    }

    private fun expandButtons() {
        isExpanded = true
        binding.fabCreate.animate().rotation(45f).setInterpolator(AccelerateInterpolator()).start()
        binding.expandedButtonsContainer.visibility = View.VISIBLE
        binding.expandedButtonsContainer.alpha = 0f
        binding.expandedButtonsContainer.animate()
            .alpha(1f)
            .setDuration(200)
            .setListener(null)
            .start()
    }

    private fun collapseButtons() {
        isExpanded = false
        binding.fabCreate.animate().rotation(0f).setInterpolator(DecelerateInterpolator()).start()
        binding.expandedButtonsContainer.animate()
            .alpha(0f)
            .setDuration(200)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.expandedButtonsContainer.visibility = View.GONE
                }
            })
            .start()
    }


    private fun startCamera() {
        val blockedPermission = checkHasPermission(this, Constants.PERMISSION_CAMERA)
        if (blockedPermission != null && blockedPermission.size > 0) {
            Log.d("Maximus", "startCamera: If $blockedPermission")
            val isBlocked = isPermissionBlocked(this, blockedPermission)
            if (isBlocked) {
                callPermissionSettings()
            } else {
                requestPermissions(Constants.PERMISSION_CAMERA, Constants.RECORD_VIDEO)
            }
        } else {
            Log.d("Maximus", "startCamera: else")

            val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            videoFile = Utils.createVideoFile(this)
            Log.v("Maximus", "videoPath1: " + videoFile!!.absolutePath)
            videoUri = FileProvider.getUriForFile(
                this,
                Constants.provider, videoFile!!
            )
            cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240) //4 minutes
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
            startActivityForResult(cameraIntent, Constants.RECORD_VIDEO)
        }
    }

    private var isFirstTimePermission: Boolean
        get() = preferences.getBoolean("isFirstTimePermission", false)
        set(isFirstTime) = preferences.edit().putBoolean("isFirstTimePermission", isFirstTime)
            .apply()

    private fun checkHasPermission(
        context: Activity?,
        permissions: Array<String>?
    ): ArrayList<String> {
        permissionList = ArrayList()
        if (context != null && permissions != null) {
            for (permission in permissions) {
                if (ContextCompat.checkSelfPermission(
                        context,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    permissionList.add(permission)
                }
            }
        }
        return permissionList
    }

    private fun isPermissionBlocked(context: Activity?, permissions: ArrayList<String>?): Boolean {
        if (context != null && permissions != null && isFirstTimePermission) {
            for (permission in permissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(context, permission)) {
                    return true
                }
            }
        }
        return false
    }

    private fun callPermissionSettings() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", this.applicationContext.packageName, null)
        intent.data = uri
        startActivityForResult(intent, 300)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Toast.makeText(this, "Test is going to Start", Toast.LENGTH_SHORT).show()
        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == Constants.RECORD_VIDEO && resultCode == Activity.RESULT_OK) {
            // Start the new activity
            val intent = Intent(this, EditorActivity::class.java)
            intent.putExtra("VideoUri", videoUri.toString())
            startActivity(intent)
//        }
    }



}