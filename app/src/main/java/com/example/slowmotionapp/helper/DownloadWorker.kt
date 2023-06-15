package com.example.slowmotionapp.helper

import android.app.ProgressDialog
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class DownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private var progressDialog: ProgressDialog? = null

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val downloadUrl =
            inputData.getString(KEY_DOWNLOAD_URL) ?: return@withContext Result.failure()

        try {
            val file = downloadFile(downloadUrl)
            val outputData = workDataOf(KEY_DOWNLOAD_PATH to file.absolutePath)
            Result.success(outputData)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            progressDialog?.dismiss() // Dismiss the progress dialog regardless of the result
        }
    }

    private fun downloadFile(downloadUrl: String): File {
        val url = URL(downloadUrl)
        val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
        connection.connect()

        val inputStream = BufferedInputStream(connection.inputStream)
        val totalSize = connection.contentLength
        var downloadedSize = 0

        val outputFile = File(context.cacheDir, "downloaded_file.mp3")
        val outputStream = FileOutputStream(outputFile)

        val data = ByteArray(1024)
        var count: Int
        while (inputStream.read(data).also { count = it } != -1) {
            outputStream.write(data, 0, count)
            downloadedSize += count

            val progress = (downloadedSize.toFloat() / totalSize.toFloat() * 100).toInt()
            setProgressAsync(workDataOf(PROGRESS_KEY to progress))

            progressDialog?.progress = progress // Update the progress dialog
        }

        outputStream.flush()
        outputStream.close()
        inputStream.close()
        connection.disconnect()

        return outputFile
    }

    companion object {
        const val KEY_DOWNLOAD_URL = "download_url"
        const val KEY_DOWNLOAD_PATH = "download_path"
        const val PROGRESS_KEY = "progress"
    }
}
