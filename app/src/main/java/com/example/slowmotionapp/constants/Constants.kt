package com.example.slowmotionapp.constants

import android.Manifest
import android.os.Build

class Constants {

    companion object {
        val PERMISSION_CAMERA = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val PERMISSION_GALLERY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        const val provider = "com.example.slowmotionapp.provider"
        const val APP_NAME = "SlowMotionApp"

        const val TYPE = "TYPE"

        const val VIDEO_GALLERY = 101
        const val RECORD_VIDEO = 102
        const val MAIN_VIDEO_TRIM = 107

        const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val VIDEO_FORMAT = ".mp4"
        const val AVI_FORMAT = ".avi"

        const val VIDEO_LIMIT = 4 //4 minutes

    }
}