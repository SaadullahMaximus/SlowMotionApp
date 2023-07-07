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
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
import java.io.File

class VideoAdapter(val context: Context, private val videos: MutableList<File>) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private var videoItemClickListener: VideoItemClickListener? = null

    fun setVideoItemClickListener(listener: VideoItemClickListener) {
        videoItemClickListener = listener
    }

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
        val threeDots: ImageView = holder.itemView.findViewById(R.id.threeDots)

        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, Uri.parse(videoFile.path))

        val thumbnail = retriever.frameAtTime
        val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong()

        retriever.release()

//        try {
//            retriever.setDataSource(context, Uri.parse(videoFile.path))
//            // Proceed with extracting metadata or further processing
//        } catch (e: Exception) {
//            Log.e("MetadataRetriever", "Failed to set data source for video file: ${videoFile.path}", e)
//            // Handle the error gracefully
//        } finally {
//            retriever.release()
//        }

        thumbnailImageView.setImageBitmap(thumbnail)
        videoDuration.text = milliSecondsToTimer(time!!) + " min"
        titleTextView.text = videoFile.nameWithoutExtension

        videoPlayer.setOnClickListener {
            playVideo = videoFile.path
            context.startActivity(Intent(context, PlayerActivity::class.java))
        }

        threeDots.setOnClickListener {
            playVideo = videoFile.path
            videoItemClickListener?.onButtonClicked(videoFile.path, position)
        }

    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun deleteItem(
        position: Int, action: (() -> Unit)? = null
    ) {
        videos.removeAt(position)

        if (videos.isEmpty()) {
            action!!.invoke()
        }

        notifyItemRemoved(position)
    }

    interface VideoItemClickListener {
        fun onButtonClicked(videoPath: String, position: Int)
    }


}