package com.example.slowmotionapp.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.ui.activities.CropActivity
import com.example.slowmotionapp.ui.activities.EffectActivity
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.isFromTrim
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.playVideo
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.renamedName
import com.example.slowmotionapp.ui.activities.PlayerActivity
import com.example.slowmotionapp.ui.activities.TrimVideoActivity
import com.example.slowmotionapp.utils.Utils.deleteVideoFile
import com.example.slowmotionapp.utils.Utils.editVideo
import com.example.slowmotionapp.utils.Utils.milliSecondsToTimer
import com.example.slowmotionapp.utils.Utils.refreshGallery
import com.example.slowmotionapp.utils.Utils.shareVideo
import com.example.slowmotionapp.utils.Utils.showRenameDialog
import com.google.android.exoplayer2.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class VideoAdapter(
    val context: Context,
    private var videos: MutableList<File>
) :
    RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private lateinit var callback: AdapterCallback

    fun setAdapterCallback(callback: AdapterCallback) {
        this.callback = callback
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

        // Fetch thumbnail in a background thread using Kotlin coroutines
        CoroutineScope(Dispatchers.Main).launch {
            val thumbnail = withContext(Dispatchers.IO) {
                retrieveVideoThumbnail(videoFile.path)
            }
            thumbnailImageView.setImageBitmap(thumbnail)

            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(context, Uri.parse(videoFile.path))
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong()
                videoDuration.text = milliSecondsToTimer(time!!) + " min"
            } catch (e: Exception) {
                Log.e(
                    "MetadataRetriever",
                    "Failed to set data source for video file: ${videoFile.path}",
                    e
                )
                // Handle the error gracefully
            } finally {
                retriever.release()
            }
        }

        titleTextView.text = videoFile.nameWithoutExtension

        videoPlayer.setOnClickListener {
            playVideo = videoFile.path
            context.startActivity(Intent(context, PlayerActivity::class.java))
        }

        threeDots.setOnClickListener {
            playVideo = videoFile.path

            val popupMenu = PopupMenu(context, threeDots, Gravity.BOTTOM, 0, R.style.PopupMenuStyle)
            popupMenu.menuInflater.inflate(
                R.menu.three_dots_clicked,
                popupMenu.menu
            )
            popupMenu.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.editItem -> {
                        context.editVideo(videoFile.path)
                    }
                    R.id.shareItem -> {
                        context.shareVideo(videoFile.path)
                    }
                    R.id.renameItem -> {
                        context.showRenameDialog(videoFile.path) {
                            notifyItemChanged(position)
                            videos[position] = renamedName
                        }
                    }
                    R.id.deleteItem -> {
                        deleteVideoFile(videoFile.path)
                        deleteItem(position)
                        notifyItemChanged(position)
                        refreshGallery(videoFile.path, context)
                    }
                    R.id.trimItem -> {
                        val intent = Intent(context, TrimVideoActivity::class.java)
                        intent.putExtra("VideoUri", videoFile.path.toString())
                        intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                        isFromTrim = true
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                    R.id.cropItem -> {
                        val intent = Intent(context, CropActivity::class.java)
                        intent.putExtra("VideoUri", videoFile.path.toString())
                        intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                    R.id.effectItem -> {
                        val intent = Intent(context, EffectActivity::class.java)
                        intent.putExtra("VideoUri", videoFile.path.toString())
                        intent.putExtra(Constants.TYPE, Constants.VIDEO_GALLERY)
                        context.startActivity(intent)
                        (context as Activity).finish()
                    }
                }

                true
            }
            popupMenu.show()

        }

    }

    private fun retrieveVideoThumbnail(videoPath: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(videoPath)
            retriever.frameAtTime
        } catch (e: Exception) {
            Log.e("MetadataRetriever", "Failed to retrieve video thumbnail for path: $videoPath", e)
            null
        } finally {
            retriever.release()
        }
    }

    override fun getItemCount(): Int {
        return videos.size
    }

    class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    private fun deleteItem(
        position: Int
    ) {
        videos.removeAt(position)
        if (videos.isEmpty()) {
            callback.onFunctionCalled()
        }
        notifyItemRemoved(position)
    }


    interface AdapterCallback {
        fun onFunctionCalled()
    }

}