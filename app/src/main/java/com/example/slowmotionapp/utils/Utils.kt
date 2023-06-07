package com.example.slowmotionapp.utils

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.tempCacheName
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {
    fun createVideoFile(): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"
        val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/SlowMotionApp/"
        val storageDir = File("$filepath/Recordings/")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, storageDir)
    }

    fun createTrimmedFile(): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"
        val filepath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/SlowMotionApp/"
        val storageDir = File("$filepath/Trimmed/")
        if (!storageDir.exists()) storageDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, storageDir)
    }

    fun saveEditedVideo(context: Context, videoFile: File) {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"

        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
                .toString() + "/SlowMotionApp/"
        val editedDir = File("$downloadsDir/Edited/")

        // Create the "Edited" directory if it doesn't exist
        if (!editedDir.exists()) {
            editedDir.mkdirs()
        }

        val destinationFile = File(editedDir, "$imageFileName${Constants.VIDEO_FORMAT}")

        try {
            // Open the source video file
            val sourceStream = FileInputStream(videoFile)

            // Create the output stream in the "Edited" directory
            val outputStream = FileOutputStream(destinationFile)

            // Copy the video data from source to destination file
            val buffer = ByteArray(1024)
            var bytesRead = sourceStream.read(buffer)
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = sourceStream.read(buffer)
            }

            // Close the streams
            sourceStream.close()
            outputStream.close()

            // Delete the original file from the cache directory
            videoFile.delete()

            Toast.makeText(context, "Video Saved", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Can't Video Saved", Toast.LENGTH_SHORT).show()
        }
    }

    fun createCacheTempFile(context: Context): String {
        val timeStamp: String =
            SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(Date())

        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"

        val storageDir = File(context.cacheDir, "Temp")
        if (!storageDir.exists()) storageDir.mkdirs()

        tempCacheName =
            File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, storageDir).toString()
        return tempCacheName
    }

    fun convertContentUriToFilePath(contentUriString: String): String {
        val startIndex = contentUriString.lastIndexOf("/") + 1
        val endIndex = contentUriString.length
        return "/storage/emulated/0/Movies/SlowMotionApp/Recordings/${
            contentUriString.substring(
                startIndex,
                endIndex
            )
        }"
    }

    fun refreshGalleryAlone(context: Context) {
        try {
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            context.sendBroadcast(mediaScanIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getFileExtension(filePath: String): String {
        return filePath.substring(filePath.lastIndexOf("."))
    }

    fun getVideoDuration(context: Context, file: File): Long {
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

    fun commandsGenerator(args: Array<String>): String {
        val sb = StringBuffer()
        for (i in args.indices) {
            if (i == (args.size - 1)) {
                sb.append("\"")
                sb.append(args[i])
                sb.append("\"")
            } else {
                sb.append("\"")
                sb.append(args[i])
                sb.append("\" ")
            }
        }
        val str = sb.toString()
        println(str)
        return str
    }

    fun deleteFromGallery(str: String, context: Context) {
        val strArr = arrayOf("_id")
        val strArr2 = arrayOf(str)
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val contentResolver: ContentResolver = context.contentResolver
        val query = contentResolver.query(uri, strArr, "_data = ?", strArr2, null)
        if (query!!.moveToFirst()) {
            try {
                contentResolver.delete(
                    ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, query.getLong(
                            query.getColumnIndexOrThrow("_id")
                        )
                    ), null, null
                )
            } catch (e2: IllegalArgumentException) {
                e2.printStackTrace()
            }
        } else {
            try {
                File(str).delete()
                refreshGallery(str, context)
            } catch (e3: Exception) {
                e3.printStackTrace()
            }
        }
        query.close()
    }

    private fun refreshGallery(str: String?, context: Context) {
        val intent = Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE")
        intent.data = Uri.fromFile(File(str!!))
        context.sendBroadcast(intent)
    }

    fun createCacheCopy(context: Context, videoPath: String): File? {
        // Create a file in the cache directory
        val cacheDir = context.cacheDir
        val cacheFile = File(cacheDir, "cachedEditedFile.mp4")

        try {
            // Open the source video file
            val sourceFile = File(videoPath)
            val sourceStream = FileInputStream(sourceFile)

            // Create the output stream
            val outputStream = FileOutputStream(cacheFile)

            // Copy the video data from source to cache file
            val buffer = ByteArray(1024)
            var bytesRead = sourceStream.read(buffer)
            while (bytesRead != -1) {
                outputStream.write(buffer, 0, bytesRead)
                bytesRead = sourceStream.read(buffer)
            }

            // Close the streams
            sourceStream.close()
            outputStream.close()

            return cacheFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getVideoDuration(context: Context, videoPath: String): Int {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(videoPath))

        val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
        val duration = durationStr!!.toLong()

        return (duration / 1000).toInt()
    }

}