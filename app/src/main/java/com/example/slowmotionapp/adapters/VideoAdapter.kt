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
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.PlayerActivity
import com.example.slowmotionapp.ui.activities.SavedActivity.Companion.positionClicked
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
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
        val videoDuration: TextView = holder.itemView.findViewById(R.id.videoDuration)
        val videoPlayer: CardView = holder.itemView.findViewById(R.id.videoPlayer)

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(videoFile.path))

        val thumbnail = retriever.frameAtTime
        thumbnailImageView.setImageBitmap(thumbnail)

        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

        videoDuration.text = milliSecondsToTimer(time!!) + " min"
        titleTextView.text = videoFile.nameWithoutExtension

        videoPlayer.setOnClickListener {
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