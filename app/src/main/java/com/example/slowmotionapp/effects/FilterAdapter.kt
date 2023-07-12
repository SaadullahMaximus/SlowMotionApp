package com.example.slowmotionapp.effects

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.recyclerview.widget.RecyclerView
import com.example.slowmotionapp.R
import com.example.slowmotionapp.customviews.CircularImageView
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.utils.Utils.getVideoThumbnail
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.GPUImageToneCurveFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FilterAdapter(
    private val values: List<FilterType>,
    private val listener: OnItemClickListener,
    private val lifecycleScope: LifecycleCoroutineScope,
    private val context: Context
) : RecyclerView.Adapter<FilterAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView: TextView = view.findViewById(R.id.label)
        val circularImageView: CircularImageView = view.findViewById(R.id.imageViewCircular)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_text, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        lifecycleScope.launch(Dispatchers.Default) {
            val bitmap = context.getVideoThumbnail(mainCachedFile)

            val filteredBitmap = applyFilterAsync(bitmap!!, holder.itemView.context)

            withContext(Dispatchers.Main) {
                holder.circularImageView.setImageBitmap(filteredBitmap)
            }
        }
        holder.textView.text = values[position].name
    }

    override fun getItemCount(): Int {
        return values.size
    }

    private suspend fun applyFilterAsync(bitmap: Bitmap, context: Context): Bitmap =
        withContext(Dispatchers.Default) {
            val gpuImage = GPUImage(context)
            gpuImage.setImage(bitmap)

            val gpuFilter = GPUImageToneCurveFilter()

            val inputFilter = context.assets.open(("acv/tone_cuver_sample.acv"))

            gpuFilter.setFromCurveFileInputStream(inputFilter)
            inputFilter.close()

            gpuImage.setFilter(gpuFilter)
            gpuImage.bitmapWithFilterApplied
        }
}