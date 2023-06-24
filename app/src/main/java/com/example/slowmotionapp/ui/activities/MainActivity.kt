package com.example.slowmotionapp.ui.activities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.constants.Constants.Companion.VIDEO_LIMIT
import com.example.slowmotionapp.constants.Constants.Companion.VIDEO_MIN_LIMIT
import com.example.slowmotionapp.databinding.ActivityMainBinding
import com.example.slowmotionapp.ui.fragments.MyDialogFragment
import com.example.slowmotionapp.utils.Utils.convertDurationInSec
import com.example.slowmotionapp.utils.Utils.createCacheTempFile
import com.example.slowmotionapp.utils.Utils.createVideoFile
import com.example.slowmotionapp.utils.Utils.getFileExtension
import com.example.slowmotionapp.utils.Utils.getMediaDuration
import com.example.slowmotionapp.utils.Utils.getVideoDuration
import com.example.slowmotionapp.utils.Utils.refreshGalleryAlone
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isExpanded = false
    private var videoFile: File? = null
    private var videoUri: Uri? = null
    private var masterVideoFile: File? = null
    private var playbackPosition: Long = 0
    private var currentWindow: Int = 0

    companion object {

        var isFromTrim: Boolean = false

        var knobPosition: Float = 700F

        lateinit var trimFilePath: String

        lateinit var tempCacheName: String

        lateinit var mainCachedFile: String

        var trimOrCrop = false

        lateinit var myMusicUri: String

        var myMusic = false

        var musicReady = false

        lateinit var playVideo: String

        var filterPosition = 0

        var MusicApplied = false

        var justEffects = false

        var backSave = false

        var permissionAllowed = false

        var cameraPermission = false

        var wannaGoBack = false

        var wannaGoBackCheckViewModel: MutableLiveData<Boolean> = MutableLiveData(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createCacheTempFile(this)

        binding.animationView.setOnClickListener {
            binding.imageCreate.visibility = View.VISIBLE
            binding.animationView.visibility = View.GONE

            // Create the ObjectAnimator for rotation animation
            val animator = ObjectAnimator.ofFloat(binding.imageCreate, View.ROTATION, 0f, 90f)
            animator.duration = 300 // Animation duration in milliseconds

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
            checkPermissionGallery()
        }

        binding.btnSaved.setOnClickListener {
            requestPermissions(
                Constants.PERMISSION_GALLERY, 100
            )
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

        binding.btnEffects.setOnClickListener {
            justEffects = true
            val myDialogFragment = MyDialogFragment()
            myDialogFragment.show(supportFragmentManager, "MyDialogFragment")
        }

        binding.settingBtn.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        collapseButtons()
        // Create the ObjectAnimator for rotation animation
        val animator = ObjectAnimator.ofFloat(binding.imageCreate, View.ROTATION, 0f, -180f)
        animator.duration = 100 // Animation duration in milliseconds

        // Start the animation
        animator.start()

        binding.imageCreate.visibility = View.GONE
        binding.animationView.visibility = View.VISIBLE

        // Start the animation from the beginning
        binding.animationView.progress = 0f
        binding.animationView.playAnimation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (allPermissionsGranted) {
            when (requestCode) {
                Constants.GALLERY_PERMISSION_CODE -> {
                    startActivity(Intent(this, SavedActivity::class.java))
                }
                Constants.VIDEO_GALLERY -> {
                    //call the gallery intent
                    refreshGalleryAlone(this)
                    val i = Intent(Intent.ACTION_PICK)
                    i.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")

                    i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    permissionAllowed = true
                    startActivityForResult(i, Constants.VIDEO_GALLERY)
                }
                Constants.RECORD_VIDEO -> {
                    val cameraIntent = Intent(MediaStore.ACTION_VIDEO_CAPTURE)
                    videoFile = createVideoFile()
                    videoUri = FileProvider.getUriForFile(
                        this, Constants.provider, videoFile!!
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 240) //4 minutes
                    cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1)
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, videoUri)
                    cameraPermission = true
                    startActivityForResult(cameraIntent, Constants.RECORD_VIDEO)
                }
            }
        } else {
            showPermissionDeniedMessage(requestCode)
        }
    }

    private fun showPermissionDeniedMessage(requestCode: Int) {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.permission_denied_dialog)

        val btnYes = dialog.findViewById<TextView>(R.id.yesBtn)
        val btnNo = dialog.findViewById<TextView>(R.id.noBtn)

        btnYes.setOnClickListener {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", this.applicationContext.packageName, null)
            intent.data = uri
            startActivityForResult(intent, requestCode)
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun openCameraDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.camera_video_dialog)

        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnProceed = dialog.findViewById<TextView>(R.id.btnProceed)

        btnProceed.setOnClickListener {
            requestPermissions(Constants.PERMISSION_CAMERA, Constants.RECORD_VIDEO)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun startCamera() {
        openCameraDialog()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {

            Constants.VIDEO_GALLERY -> {
                if (permissionAllowed) {
                    data?.let {
                        setFilePath(it)
                    }
                } else {
                    requestPermissions(
                        Constants.PERMISSION_GALLERY, Constants.VIDEO_GALLERY
                    )
                }

            }

            Constants.RECORD_VIDEO -> {
                if (cameraPermission) {
                    if (justEffects) {
                        val intent = Intent(this, EffectActivity::class.java)
                        intent.putExtra("VideoUri", videoUri.toString())
                        intent.putExtra(Constants.TYPE, Constants.RECORD_VIDEO)
                        startActivity(intent)
                    } else {
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
                } else {
                    requestPermissions(Constants.PERMISSION_CAMERA, Constants.RECORD_VIDEO)
                }
            }

            Constants.GALLERY_PERMISSION_CODE -> {
                startActivity(Intent(this, SavedActivity::class.java))
            }
        }

    }

    private fun setFilePath(data: Intent) {

        try {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor =
                this.contentResolver.query(selectedImage!!, filePathColumn, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                val filePath = cursor.getString(columnIndex)
                cursor.close()
                masterVideoFile = File(filePath)

                val extension = getFileExtension(masterVideoFile!!.absolutePath)

                val timeInMillis = getVideoDuration(this, masterVideoFile!!)
                val duration = convertDurationInSec(timeInMillis)

                //check if video is more than 4 minutes
                if (duration in (VIDEO_MIN_LIMIT + 1) until VIDEO_LIMIT) {
                    //check video format before playing into exoplayer
                    if (extension == Constants.AVI_FORMAT) {
                        convertAviToMp4() //avi format is not supported in exoplayer
                    } else {
                        if (justEffects) {
                            justEffects = false
                            val intent = Intent(this, EffectActivity::class.java)
                            intent.putExtra("VideoUri", filePath)
                            intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                            startActivity(intent)
                        } else {
                            if (trimOrCrop) {
                                val intent = Intent(this, CropActivity::class.java)
                                intent.putExtra("VideoUri", filePath)
                                intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                                startActivity(intent)
                            } else {
                                playbackPosition = 0
                                currentWindow = 0
                                val uri = Uri.fromFile(masterVideoFile)
                                val intent = Intent(this, TrimVideoActivity::class.java)
                                intent.putExtra("VideoUri", filePath)
                                intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                                intent.putExtra(
                                    "VideoDuration", getMediaDuration(this, uri)
                                )
                                startActivity(intent)
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this, "Video duration should be between 4-240 seconds", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (_: Exception) {

        }
    }

    private fun expandButtons() {
        isExpanded = true
        binding.expandedButtonsContainer.visibility = View.VISIBLE
        binding.expandedButtonsContainer.alpha = 0f
        binding.expandedButtonsContainer.animate().alpha(1f).setDuration(600).setListener(null)
            .start()
    }

    private fun collapseButtons() {
        isExpanded = false
        binding.expandedButtonsContainer.animate().alpha(0f).setDuration(600)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.expandedButtonsContainer.visibility = View.GONE
                }
            }).start()
    }

    private fun openGallery() {
        requestPermissions(
            Constants.PERMISSION_GALLERY, Constants.VIDEO_GALLERY
        )
    }

    fun checkPermissionGallery() {
        openGalleryDialog()
    }

    private fun openGalleryDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.gallery_video_dialog)

        val btnCancel = dialog.findViewById<TextView>(R.id.btnCancel)
        val btnProceed = dialog.findViewById<TextView>(R.id.btnProceed)

        btnProceed.setOnClickListener {
            openGallery()
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun convertAviToMp4() {

    }

    private fun openExitDialog() {
        val dialog = Dialog(this, R.style.FullScreenDialogStyle)
        dialog.setContentView(R.layout.main_exit_dialog)

        val btnNo = dialog.findViewById<TextView>(R.id.btnNo)
        val btnYes = dialog.findViewById<TextView>(R.id.btnYes)

        btnYes.setOnClickListener {
            finishAffinity()
            dialog.dismiss()
        }

        btnNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onBackPressed() {
        openExitDialog()
    }

}