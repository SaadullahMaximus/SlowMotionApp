package com.example.slowmotionapp.constants

import android.Manifest
import android.os.Build

class Constants {

    companion object {
        val PERMISSION_CAMERA = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        val PERMISSION_GALLERY = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_VIDEO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val PERMISSION_AUDIO = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }

        const val provider = "com.example.slowmotionapp.provider"
        const val APP_NAME = "SlowMotionApp"

        const val TYPE = "TYPE"

        const val GALLERY_PERMISSION_CODE = 100
        const val VIDEO_GALLERY = 101
        const val RECORD_VIDEO = 102
        const val AUDIO_GALLERY = 103

        const val DATE_FORMAT = "yyyyMMdd_HHmmss"
        const val VIDEO_FORMAT = ".mp4"
        const val AVI_FORMAT = ".avi"

        const val VIDEO_LIMIT = 240 //4 minutes
        const val VIDEO_MIN_LIMIT = 3 //3 sec

        const val PRIVACY_URL = "https://sites.google.com/view/slowmotionvideomakereditorpp/home"

    }
}