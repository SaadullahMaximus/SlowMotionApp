package com.example.slowmotionapp.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.PlayerActivity
import java.io.File

class VideoAdapter(val context: Context, private val videos: List<File>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val videoFile = videos[position]
        val thumbnailImageView: ImageView = holder.itemView.findViewById(R.id.videoThumbnail)
        val titleTextView: TextView = holder.itemView.findViewById(R.id.videoTitle)

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(videoFile.path)

        val thumbnail = retriever.frameAtTime
        thumbnailImageView.setImageBitmap(thumbnail)

        val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            ?: getVideoTitleFromMediaStore(videoFile)

        Log.d("TITLE", "onBindViewHolder: $title")
        titleTextView.text = title ?: "Untitled"

        holder.itemView.setOnClickListener {
            // Handle video playback
            playVideo = videoFile.path
            context.startActivity(Intent(context, PlayerActivity::class.java))
        }
    }



    override fun getItemCount(): Int {
        return videos.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun getVideoTitleFromMediaStore(videoFile: File): String? {
        val projection = arrayOf(MediaStore.Video.Media.TITLE)
        val selection = "${MediaStore.Video.Media.DATA} = ?"
        val selectionArgs = arrayOf(videoFile.path)
        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )

        var title: String? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndex(MediaStore.Video.Media.TITLE)
                title = it.getString(columnIndex)
            }
        }

        cursor?.close()
        return title
    }

}
