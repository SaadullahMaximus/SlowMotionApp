package com.example.slowmotionapp.ui.activities

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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityMainBinding
import com.example.slowmotionapp.sqlite.DatabaseManager
import com.example.slowmotionapp.utils.Utils
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isExpanded = false
    private var videoFile: File? = null
    private var videoUri: Uri? = null
    private var permissionList: ArrayList<String> = ArrayList()
    private lateinit var preferences: SharedPreferences
    private var masterVideoFile: File? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0
    private var isLargeVideo: Boolean? = false

    private lateinit var databaseManager: DatabaseManager


    companion object {
        // Define properties and functions here
        var dataBasePosition: Int = 1
        var knobPosition: Float = 700F
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseManager = DatabaseManager(this)


        preferences = this.getSharedPreferences("fetch_permission", Context.MODE_PRIVATE)

        databaseManager.clearTable()

        requestPermissions(
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO"
            ), 101
        )

        binding.animationView.setOnClickListener {
            expandButtons()
            binding.animationView.visibility = View.GONE
            binding.imageCreate.visibility = View.VISIBLE
        }


        binding.imageCreate.setOnClickListener {
            collapseButtons()
            binding.animationView.visibility = View.VISIBLE
            binding.imageCreate.visibility = View.GONE
        }

        binding.captureVideoBtn.setOnClickListener {
            startCamera()
        }

        binding.selectVideoBtn.setOnClickListener {
            checkPermissionGallery(Constants.PERMISSION_GALLERY)

        }

    }

    override fun onResume() {
        super.onResume()
        databaseManager.clearTable()
    }

    private fun checkPermissionGallery(permissionGallery: Array<String>) {
        openGallery(permissionGallery)
    }


    private fun expandButtons() {
        isExpanded = true
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
            videoFile = Utils.createVideoFile()
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

    private fun openGallery(permissionGallery: Array<String>) {
        for (permission in permissionGallery) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this as Activity, permission)) {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                break
            } else {
                if (ActivityCompat.checkSelfPermission(
                        this as Activity,
                        permission
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    //call the gallery intent
                    Utils.refreshGalleryAlone(this)
                    val i = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    i.type = "video/*"
                    i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    startActivityForResult(i, Constants.VIDEO_GALLERY)
                } else {
                    callPermissionSettings()
                }
            }
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
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            Constants.VIDEO_GALLERY -> {
                data?.let {
                    setFilePath(resultCode, it, Constants.VIDEO_GALLERY)
                }
            }

            Constants.RECORD_VIDEO -> {
                val intent = Intent(this, EditorActivity::class.java)
                intent.putExtra("VideoUri", videoUri.toString())
                intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
                startActivity(intent)
            }
        }

    }

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {

        if (resultCode == RESULT_OK) {
            try {
                val selectedImage = data.data
                //  Log.e("selectedImage==>", "" + selectedImage)
                val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor = this.contentResolver
                    .query(selectedImage!!, filePathColumn, null, null, null)
                if (cursor != null) {
                    cursor.moveToFirst()
                    val columnIndex = cursor
                        .getColumnIndex(filePathColumn[0])
                    val filePath = cursor.getString(columnIndex)
                    cursor.close()
                    if (mode == Constants.VIDEO_GALLERY) {
                        Log.v("GalleryVideo", "filePath: $filePath")
                        masterVideoFile = File(filePath)

                        val extension = Utils.getFileExtension(masterVideoFile!!.absolutePath)

                        val timeInMillis = Utils.getVideoDuration(this, masterVideoFile!!)
                        val duration = Utils.convertDurationInMin(timeInMillis)

                        //check if video is more than 4 minutes
                        if (duration < Constants.VIDEO_LIMIT) {
                            //check video format before playing into exoplayer
                            if (extension == Constants.AVI_FORMAT) {
                                convertAviToMp4() //avi format is not supported in exoplayer
                            } else {
                                playbackPosition = 0
                                currentWindow = 0
                                val uri = Uri.fromFile(masterVideoFile)
                                val intent = Intent(this, EditorActivity::class.java)
                                intent.putExtra("VideoUri", filePath)
                                intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                                intent.putExtra("VideoDuration", Utils.getMediaDuration(this, uri))
                                startActivity(intent)
                            }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.error_select_smaller_video),
                                Toast.LENGTH_SHORT
                            ).show()

                            isLargeVideo = true
                            val uri = Uri.fromFile(masterVideoFile)
                            val intent = Intent(this, EditorActivity::class.java)
                            intent.putExtra("VideoPath", filePath)
                            intent.putExtra("VideoDuration", Utils.getMediaDuration(this, uri))
                            startActivityForResult(intent, Constants.MAIN_VIDEO_TRIM)
                        }
                    }
                }
            } catch (_: Exception) {

            }
        }
    }

    private fun convertAviToMp4() {

    }

}