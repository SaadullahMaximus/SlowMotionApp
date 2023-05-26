package com.example.slowmotionapp.utils

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import com.example.slowmotionapp.constants.Constants
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


object Utils {

    fun createVideoFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"
        val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/SlowMotionApp/"
        val storageDir = File("$filepath/Recordings/")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, storageDir)
    }

    fun createTrimmedFile(context: Context): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"
        val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/SlowMotionApp/"
        val storageDir = File("$filepath/Trimmed/")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, storageDir)
    }

    fun convertContentUriToFilePath(contentUriString: String): String {
        val startIndex = contentUriString.lastIndexOf("/") + 1
        val endIndex = contentUriString.length
        return "/storage/emulated/0/Movies/SlowMotionApp/Recordings/${contentUriString.substring(startIndex, endIndex)}"
    }

    fun refreshGalleryAlone(context: Context) {
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFileExtension(filePath: String): String? {
        return filePath.substring(filePath.lastIndexOf("."))
    }

    fun getVideoDuration(context: Context, file: File): Long{
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.fromFile(file))
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val timeInMillis = time!!.toLong()
        retriever.release()
        return timeInMillis
    }

    fun convertDurationInMin(duration: Long): Long {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(duration)
        return if (minutes > 0) {
            minutes
        } else {
            0
        }
    }

    fun getMediaDuration(context: Context?, uriOfFile: Uri?): Int {
        val mp = MediaPlayer.create(context, uriOfFile)
        return mp.duration
    }

}