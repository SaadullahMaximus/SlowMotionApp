package com.example.slowmotionapp.ui.activities

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.constants.Constants.Companion.VIDEO_LIMIT
import com.example.slowmotionapp.constants.Constants.Companion.VIDEO_MIN_LIMIT
import com.example.slowmotionapp.databinding.ActivitySavedBinding
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.permissionAllowed
import com.example.slowmotionapp.ui.fragments.savedfragments.SavedCropFragment
import com.example.slowmotionapp.ui.fragments.savedfragments.SavedEditedFragment
import com.example.slowmotionapp.ui.fragments.savedfragments.SavedTrimFragment
import com.example.slowmotionapp.utils.Utils
import com.example.slowmotionapp.utils.Utils.convertDurationInSec
import com.google.android.material.tabs.TabLayout
import java.io.File

class SavedActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySavedBinding
    private var masterVideoFile: File? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySavedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewPagerAdapter = ViewPagerSetter(supportFragmentManager)

        viewPagerAdapter.addFragment(SavedEditedFragment(), "Edited")
        viewPagerAdapter.addFragment(SavedTrimFragment(), "Trimmed")
        viewPagerAdapter.addFragment(SavedCropFragment(), "Cropped")

        binding.viewpager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewpager)
        binding.tabLayout.setTabTextColors(R.color.baseColor, R.color.baseColor)

        val tab1: TabLayout.Tab? = binding.tabLayout.getTabAt(0)
        val tab2: TabLayout.Tab? = binding.tabLayout.getTabAt(1)
        val tab3: TabLayout.Tab? = binding.tabLayout.getTabAt(2)

        tab1?.customView = createTabView("Edited")

        binding.tabLayout.getTabAt(0)?.customView?.findViewById<TextView>(R.id.tabText)
            ?.setTextColor(resources.getColor(R.color.baseColor))

        tab2?.customView = createTabView("Trimmed")
        tab3?.customView = createTabView("Cropped")

        binding.viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                // Set the title color for the selected position
                for (i in 0 until viewPagerAdapter.count) {
                    val title = viewPagerAdapter.getPageTitle(i) as? String
                    val textView =
                        binding.tabLayout.getTabAt(i)?.customView?.findViewById<TextView>(R.id.tabText)
                    if (title != null && textView != null) {
                        if (i == position) {
                            // Set the selected title color to yellow
                            textView.setTextColor(resources.getColor(R.color.baseColor))
                        } else {
                            // Set other titles to white
                            textView.setTextColor(Color.WHITE)
                        }
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {
                // Not used in this example
            }
        })

        binding.backBtn.setOnClickListener {
            finish()
        }

    }

    private class ViewPagerSetter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        private val fragmentList = mutableListOf<Fragment>()
        private val fragmentTitleList = mutableListOf<String>()

        override fun getCount(): Int {
            return fragmentList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragmentList.add(fragment)
            fragmentTitleList.add(title)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    private fun createTabView(text: String): View {
        val tabView =
            LayoutInflater.from(this).inflate(R.layout.custome_saved_layout, null)
        val textView = tabView.findViewById<TextView>(R.id.tabText)

        textView.text = text
        return tabView
    }

    fun openGallery() {
        requestPermissions(
            Constants.PERMISSION_GALLERY, Constants.VIDEO_GALLERY
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val allPermissionsGranted = grantResults.all { it == PackageManager.PERMISSION_GRANTED }

        if (allPermissionsGranted) {
            when (requestCode) {
                Constants.VIDEO_GALLERY -> {
                    //call the gallery intent
                    Utils.refreshGalleryAlone(this)
                    val i = Intent(Intent.ACTION_PICK)
                    i.setDataAndType(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, "video/*")

                    i.putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
                    permissionAllowed = true
                    startActivityForResult(i, Constants.VIDEO_GALLERY)
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
        }

    }

    private fun setFilePath(data: Intent) {

        try {
            val selectedImage = data.data
            val filePathColumn = arrayOf(MediaStore.MediaColumns.DATA)
            val cursor = this.contentResolver
                .query(selectedImage!!, filePathColumn, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val columnIndex = cursor
                    .getColumnIndex(filePathColumn[0])
                val filePath = cursor.getString(columnIndex)
                cursor.close()
                masterVideoFile = File(filePath)

                val extension = Utils.getFileExtension(masterVideoFile!!.absolutePath)

                val timeInMillis = Utils.getVideoDuration(this, masterVideoFile!!)
                val duration = convertDurationInSec(timeInMillis)

                if (duration in (VIDEO_MIN_LIMIT + 1) until VIDEO_LIMIT) {
                    //check video format before playing into exoplayer
                    if (extension == Constants.AVI_FORMAT) {
                        convertAviToMp4() //avi format is not supported in exoplayer
                    } else {
                        val uri = Uri.fromFile(masterVideoFile)
                        val intent = Intent(this, TrimVideoActivity::class.java)
                        intent.putExtra("VideoUri", filePath)
                        intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                        intent.putExtra(
                            "VideoDuration",
                            Utils.getMediaDuration(this, uri)
                        )
                        finish()
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Video duration should be between 4-240 seconds",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } catch (_: Exception) {
        }
    }

    private fun convertAviToMp4() {

    }

}