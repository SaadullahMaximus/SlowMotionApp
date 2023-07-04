package com.example.slowmotionapp.ui.activities

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.slowmotionapp.R
import com.example.slowmotionapp.databinding.ActivitySettingsBinding
import com.example.slowmotionapp.utils.Utils.openURL
import com.example.slowmotionapp.utils.Utils.singleClick
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var appUpdateManager: AppUpdateManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize the AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(this)

        binding.quit.setOnClickListener {
            singleClick {
                showCustomBottomSheet()
            }
        }

        binding.policy.setOnClickListener {
            singleClick {
                openURL(this)
            }
        }

        binding.checkForUpdates.setOnClickListener {
            singleClick {
                checkForUpdates()
            }
        }

        binding.rateApp.setOnClickListener {
            singleClick {
                showCustomRatingBottomSheet()
            }
        }

        binding.moreApps.setOnClickListener {
            singleClick {
                openPlayStoreAllApps()
            }
        }

        binding.share.setOnClickListener {
            singleClick {
                shareApp()
            }
        }

        binding.contactUs.setOnClickListener {
            singleClick {
                contactUs()
            }
        }

        binding.backBtn.setOnClickListener {
            singleClick {
                finish()
            }
        }
    }

    private fun contactUs() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("maximustoolsapp@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Slow-Motion Video Maker")

        try {
            startActivity(Intent.createChooser(emailIntent, "Contact Us"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No email app available", Toast.LENGTH_SHORT).show()
        }
    }


    private fun shareApp() {
        val packageName = this.packageName
        val appName = this.applicationInfo.loadLabel(this.packageManager).toString()

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Check out this app!")
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "Create  slow-motion videos effortlessly with our amazing app. Download it from the Play Store: https://play.google.com/store/apps/details?id=$packageName"
        )

        val chooserIntent = Intent.createChooser(shareIntent, "Share $appName")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No app available to handle the share action", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun openPlayStoreAllApps() {
        val packageName = this.packageName
        val uri = Uri.parse("market://search?q=pub:$packageName")
        val playStoreIntent = Intent(Intent.ACTION_VIEW, uri)

        // Set the flags to ensure the Play Store opens your developer page directly
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            startActivity(playStoreIntent)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the Play Store app is not installed on the device
            // You can open the Play Store website instead
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(webIntent)
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
        dialog.show()
    }

    private fun showCustomRatingBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.rate_us_bottom_sheet, null)
        dialog.setContentView(view)

        val rateUs = view.findViewById<Button>(R.id.rateUs)

        rateUs.setOnClickListener {
            openPlayStoreForRating()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openPlayStoreForRating() {
        val uri = Uri.parse("market://details?id=${this.packageName}")
        val playStoreIntent = Intent(Intent.ACTION_VIEW, uri)

        // Set the flags to ensure the Play Store opens your app's page directly
        playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)

        try {
            startActivity(playStoreIntent)
        } catch (e: ActivityNotFoundException) {
            // Handle the case where the Play Store app is not installed on the device
            // You can open the Play Store website instead
            val webIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            startActivity(webIntent)
        }
    }

    private fun checkForUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
            ) {
                // An update is available, show a dialog
                val alertDialogBuilder = AlertDialog.Builder(this)
                alertDialogBuilder.setTitle("Update Available")
                    .setMessage("An update is available. Do you want to update the app?")
                    .setPositiveButton("Yes") { dialog, _ ->
                        // Open the Play Store for app update
                        val appPackageName = packageName
                        try {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("market://details?id=$appPackageName")
                                )
                            )
                        } catch (e: ActivityNotFoundException) {
                            startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                                )
                            )
                        }
                        dialog.dismiss()
                    }
                    .setNegativeButton("No") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .setCancelable(false)
                    .show()
            } else {
                // No update available or update type not allowed
                // Handle accordingly (e.g., show a message)
                Toast.makeText(this, "Your app is updated.", Toast.LENGTH_SHORT).show()
            }
        }
    }


}