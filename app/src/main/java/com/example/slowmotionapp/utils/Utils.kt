package com.example.slowmotionapp.utils

import android.app.Activity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.interfaces.MyListener
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.backSave
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.tempCacheName
import com.example.slowmotionapp.ui.activities.PlayerActivity
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object Utils {

    var player: ExoPlayer? = null

    private val filepath =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
            .toString() + "/SlowMotionApp/"
    val trimmedDir = File("$filepath/Trimmed/")

    val croppedDir = File("$filepath/Cropped/")

    val editedDir = File("$filepath/Edited/")

    private var listener: MyListener? = null

    fun setListener(listener: MyListener) {
        this.listener = listener
    }

    fun fetchVideosFromDirectory(dir: File): MutableList<File> {
        val videosList = mutableListOf<File>()

        if (dir.exists() && dir.isDirectory) {
            val files = dir.listFiles()

            if (files != null) {
                for (file in files) {
                    if (file.isFile && file.extension == "mp4") {
                        videosList.add(file)
                    }
                }
            }
        }

        return videosList
    }

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
        if (!trimmedDir.exists()) trimmedDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, trimmedDir)
    }

    fun createCroppedFile(): File {
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"

        if (!croppedDir.exists()) croppedDir.mkdirs()
        return File.createTempFile(imageFileName, Constants.VIDEO_FORMAT, croppedDir)
    }

    fun saveEditedVideo(context: Context) {
        val videoFile = File(mainCachedFile)
        val timeStamp: String = SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault()).format(
            Date()
        )
        val imageFileName: String = Constants.APP_NAME + timeStamp + "_"


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
            if (backSave) {
                mainCachedFile = destinationFile.toString()
                // Call the listener function
                listener?.onUtilityFunctionCalled()
                backSave = false
            } else {
                playVideo = destinationFile.toString()
                context.startActivity(Intent(context, PlayerActivity::class.java))
                (context as Activity).finish()
            }

        } catch (e: Exception) {
            e.printStackTrace()
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

    fun refreshAudioGallery(context: Context) {
        try {
            val audioContentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, audioContentUri)
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

    fun getScreenWidth(): Int {
        return Resources.getSystem().displayMetrics.widthPixels
    }

    fun setUpSimpleExoPlayer(context: Context) {
        // Produces DataSource instances through which media data is loaded.
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(context, Util.getUserAgent(context, Constants.APP_NAME))

        // SimpleExoPlayer
        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(ProgressiveMediaSource.Factory(dataSourceFactory))
            .build()
        player!!.addMediaItem(MediaItem.fromUri(Uri.parse(mainCachedFile)))
        player!!.prepare()
    }

    fun getVideoSize(context: Context, uri: Uri): Pair<Int, Int>? {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, uri)

        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            ?.toIntOrNull()
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            ?.toIntOrNull()

        return if (width != null && height != null) {
            Pair(width, height)
        } else {
            null
        }
    }

    fun milliSecondsToTimer(milliseconds: Long): String {
        var finalTimerString = ""
        val secondsString: String
        val minutesString: String
        val hours = (milliseconds / (1000 * 60 * 60)).toInt()
        val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
        val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()

        if (hours > 0) {
            finalTimerString = "$hours:"
        }

        secondsString = if (seconds < 10) {
            "0$seconds"
        } else {
            "" + seconds
        }
        minutesString = if (minutes < 10) {
            "0$minutes"
        } else {
            "" + minutes
        }
        finalTimerString = "$finalTimerString$minutesString:$secondsString"

        return finalTimerString
    }

    fun getAudioFilePathFromUri(context: Context, uri: Uri): String? {
        var filePath: String? = null
        val projection = arrayOf(MediaStore.Audio.Media.DATA)
        val cursor = context.contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
                filePath = it.getString(columnIndex)
            }
        }
        return filePath
    }

    fun deleteVideoFile(filePath: String) {
        val videoFile = File(filePath)
        if (videoFile.exists()) {
            videoFile.delete()
        }
    }


}