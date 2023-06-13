package com.example.slowmotionapp.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.example.slowmotionapp.ui.fragments.MyDialogFragment
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

    companion object {

        var isFromTrim: Boolean = false

        // Define properties and functions here
        var knobPosition: Float = 700F

        lateinit var trimFilePath: String

        lateinit var tempCacheName: String

        lateinit var mainCachedFile: String

        var trimOrCrop = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Utils.createCacheTempFile(this)

        preferences = this.getSharedPreferences("fetch_permission", Context.MODE_PRIVATE)

        requestPermissions(
            arrayOf(
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.READ_MEDIA_VIDEO",
                "android.permission.READ_MEDIA_AUDIO"
            ), 101
        )

        binding.animationView.setOnClickListener {
            binding.imageCreate.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE

            // Create the ObjectAnimator for rotation animation
            val animator = ObjectAnimator.ofFloat(binding.imageCreate, View.ROTATION, 0f, 90f)
            animator.duration = 800 // Animation duration in milliseconds

            // Start the animation
            animator.start()

            // Delay the visibility change using a Handler
            Handler(Looper.getMainLooper()).postDelayed({
                expandButtons()
            }, 100)

        }


        binding.imageCreate.setOnClickListener {
            collapseButtons()
            // Create the ObjectAnimator for rotation animation
            val animator = ObjectAnimator.ofFloat(binding.imageCreate, View.ROTATION, 0f, -180f)
            animator.duration = 800 // Animation duration in milliseconds

            // Start the animation
            animator.start()

            // Delay the visibility change using a Handler
            Handler(Looper.getMainLooper()).postDelayed({
                binding.imageCreate.visibility = View.GONE
                binding.animationView.visibility = View.VISIBLE

                // Start the animation from the beginning
                binding.animationView.progress = 0f
                binding.animationView.playAnimation()
            }, 500)
        }

        binding.captureVideoBtn.setOnClickListener {
            isFromTrim = false
            startCamera()
        }

        binding.selectVideoBtn.setOnClickListener {
            isFromTrim = false
            checkPermissionGallery(Constants.PERMISSION_GALLERY)
        }

        binding.btnSaved.setOnClickListener {
            startActivity(Intent(this, SavedActivity::class.java))
        }

        binding.btnTrim.setOnClickListener {
            trimOrCrop = false
            val myDialogFragment = MyDialogFragment()
            myDialogFragment.show(supportFragmentManager, "MyDialogFragment")
        }

        binding.btnCrop.setOnClickListener {
            trimOrCrop = true
            val myDialogFragment = MyDialogFragment()
            myDialogFragment.show(supportFragmentManager, "MyDialogFragment")
        }

    }

    fun checkPermissionGallery(permissionGallery: Array<String>) {
        openGallery(permissionGallery)
    }


    private fun expandButtons() {
        isExpanded = true
        binding.expandedButtonsContainer.visibility = View.VISIBLE
        binding.expandedButtonsContainer.alpha = 0f
        binding.expandedButtonsContainer.animate()
            .alpha(1f)
            .setDuration(600)
            .setListener(null)
            .start()
    }

    private fun collapseButtons() {
        isExpanded = false
        binding.expandedButtonsContainer.animate()
            .alpha(0f)
            .setDuration(600)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.expandedButtonsContainer.visibility = View.GONE
                }
            })
            .start()
    }


    fun startCamera() {
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

        Log.d("LETSGO", "setFilePath: Trim Lets Go RequestCode $requestCode")


        when (requestCode) {

            Constants.VIDEO_GALLERY -> {
                data?.let {
                    setFilePath(resultCode, it, Constants.VIDEO_GALLERY)
                }
            }

            Constants.RECORD_VIDEO -> {
                if (trimOrCrop) {
                    val intent = Intent(this, CropActivity::class.java)
                    intent.putExtra("VideoUri", videoUri.toString())
                    intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
                    startActivity(intent)
                } else {
                    val intent = Intent(this, TrimVideoActivity::class.java)
                    intent.putExtra("VideoUri", videoUri.toString())
                    intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
                    startActivity(intent)
                }
            }
        }

    }

    private fun setFilePath(resultCode: Int, data: Intent, mode: Int) {

        Log.d("LETSGO", "setFilePath: Trim Lets Go ResultCode $resultCode")
        Log.d("LETSGO", "setFilePath: Trim Lets Go Mode $mode")


        if (resultCode == RESULT_OK) {
            try {
                val selectedImage = data.data
                Log.e("selectedImage==>", "" + selectedImage)
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
                            Log.d("LETSGO", "setFilePath: Trim Lets Go Almost extension $extension")
                            //check video format before playing into exoplayer
                            if (extension == Constants.AVI_FORMAT) {
                                convertAviToMp4() //avi format is not supported in exoplayer
                            } else {
                                if (trimOrCrop) {
                                    val intent = Intent(this, CropActivity::class.java)
                                    intent.putExtra("VideoUri", filePath)
                                    intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                                    startActivity(intent)
                                } else {
                                    playbackPosition = 0
                                    currentWindow = 0
                                    Log.d("LETSGO", "setFilePath: Trim Lets Go")
                                    val uri = Uri.fromFile(masterVideoFile)
                                    val intent = Intent(this, TrimVideoActivity::class.java)
                                    intent.putExtra("VideoUri", filePath)
                                    intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                                    intent.putExtra(
                                        "VideoDuration",
                                        Utils.getMediaDuration(this, uri)
                                    )
                                    startActivity(intent)
                                }
                            }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.error_select_smaller_video),
                                Toast.LENGTH_SHORT
                            ).show()

                            if (trimOrCrop) {
                                val intent = Intent(this, CropActivity::class.java)
                                intent.putExtra("VideoUri", filePath)
                                intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                                startActivity(intent)
                            } else {

                                isLargeVideo = true
                                Log.d("LETSGO", "setFilePath: Trim Lets Go")
                                val uri = Uri.fromFile(masterVideoFile)
                                val intent = Intent(this, TrimVideoActivity::class.java)
                                intent.putExtra("VideoPath", filePath)
                                intent.putExtra("VideoDuration", Utils.getMediaDuration(this, uri))
                                startActivityForResult(intent, Constants.MAIN_VIDEO_TRIM)
                            }
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