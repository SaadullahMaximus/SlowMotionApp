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

        const val provider = "com.example.slowmotionapp.provider"
        const val RECORD_VIDEO = 102

        const val APP_NAME = "SlowMotionApp"

        const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val VIDEO_FORMAT = ".mp4"

    }
}