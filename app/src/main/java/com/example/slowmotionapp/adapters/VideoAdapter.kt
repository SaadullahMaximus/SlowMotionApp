package com.example.slowmotionapp.adapters

import android.content.Context
import android.content.Intent
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.PlayerActivity
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.positionClicked
import java.io.File

class VideoAdapter(val context: Context, private val videos: MutableList<File>) :
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
        retriever.setDataSource(context, Uri.parse(videoFile.path))

        val thumbnail = retriever.frameAtTime
        thumbnailImageView.setImageBitmap(thumbnail)

        titleTextView.text = videoFile.nameWithoutExtension

        holder.itemView.setOnClickListener {
            playVideo = videoFile.path
            positionClicked = position
            context.startActivity(Intent(context, PlayerActivity::class.java))
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun deleteItem(position: Int) {
        videos.removeAt(position)
        notifyItemRemoved(position)
    }

}