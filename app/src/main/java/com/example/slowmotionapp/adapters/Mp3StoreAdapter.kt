package com.example.slowmotionapp.adapters

import android.app.ProgressDialog
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.models.Mp3Store

class Mp3StoreAdapter(
    private var mp3Stores: List<Mp3Store>,
    private val onItemClick: (String, Int) -> Unit,
    private val onApplyBtnClick: (String) -> Unit
) :
    RecyclerView.Adapter<Mp3StoreAdapter.ViewHolder>() {

    private var selectedPosition: Int = RecyclerView.NO_POSITION

    private var currentMediaPlayer: MediaPlayer? = null
    private var progressDialog: ProgressDialog? = null

    private var isClickable = true
    private val clickDelay = 500 // Set the desired delay in milliseconds
    private val clickHandler = Handler(Looper.getMainLooper())

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMp3StoreName: TextView = itemView.findViewById(R.id.musicTitle)
        val btnApply: Button = itemView.findViewById(R.id.btnApply)
        val selected: ImageView = itemView.findViewById(R.id.selected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_mp3_store, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mp3Store = mp3Stores[position]
        holder.tvMp3StoreName.text = mp3Store.name

        if (position == selectedPosition) {
            holder.btnApply.visibility = View.VISIBLE
        } else {
            holder.btnApply.visibility = View.GONE
        }

        holder.btnApply.setOnClickListener {
            val finalPosition = selectedPosition
            selectedPosition = -1

            holder.btnApply.visibility = View.GONE

            if (position == finalPosition) {
                holder.selected.setImageResource(R.drawable.music_select)
            } else {
                holder.selected.setImageResource(R.drawable.music_unselect)
            }

            onApplyBtnClick(mp3Store.link)
        }

        holder.itemView.setOnClickListener {

            if (isClickable) {
                isClickable = false

                val previousPosition = selectedPosition
                selectedPosition = position
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                progressDialog?.dismiss() // Dismiss any previous progress dialog

                progressDialog = ProgressDialog(holder.itemView.context)
                progressDialog?.setMessage("Preparing music...")
                progressDialog?.setCancelable(false)
                progressDialog?.show()

                onItemClick(mp3Store.link, position)

                clickHandler.postDelayed({
                    isClickable = true
                }, clickDelay.toLong())
            }
        }

    }

    override fun getItemCount(): Int {
        return mp3Stores.size
    }

    fun setData(data: List<Mp3Store>) {
        mp3Stores = data
        notifyDataSetChanged()
    }

    fun getCurrentMediaPlayer(): MediaPlayer? {
        return currentMediaPlayer
    }

    fun setCurrentMediaPlayer(mediaPlayer: MediaPlayer?) {
        currentMediaPlayer = mediaPlayer
    }

    fun dialogDismiss() {
        progressDialog!!.dismiss()
    }

}
