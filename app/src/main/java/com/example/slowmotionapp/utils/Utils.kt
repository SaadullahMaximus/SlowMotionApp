package com.example.slowmotionapp.utils

import android.content.Context
import android.os.Environment
import com.example.slowmotionapp.constants.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object Utils {

    fun createVideoFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"
        val storageDir: File = context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)!!
        if (!storageDir.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, storageDir)
    }

}