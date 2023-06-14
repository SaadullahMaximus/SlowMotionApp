package com.example.slowmotionapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.models.Mp3Store

class Mp3StoreAdapter(private var mp3Stores: List<Mp3Store>, private val onItemClick: (String) -> Unit) :
    RecyclerView.Adapter<Mp3StoreAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMp3StoreName: TextView = itemView.findViewById(R.id.musicTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_mp3_store, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mp3Store = mp3Stores[position]
        holder.tvMp3StoreName.text = mp3Store.name
        holder.itemView.setOnClickListener {
            onItemClick(mp3Store.link)
        }
    }

    override fun getItemCount(): Int {
        return mp3Stores.size
    }

    fun setData(data: List<Mp3Store>) {
        mp3Stores = data
        notifyDataSetChanged()
    }
}
