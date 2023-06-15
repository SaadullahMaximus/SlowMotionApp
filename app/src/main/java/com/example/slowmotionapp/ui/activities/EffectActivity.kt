package com.example.slowmotionapp.ui.activities

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.daasuu.mp4compose.FillMode
import com.daasuu.mp4compose.Rotation
import com.daasuu.mp4compose.composer.Mp4Composer
import com.example.slowmotionapp.R
import com.example.slowmotionapp.constants.Constants
import com.example.slowmotionapp.databinding.ActivityEffectBinding
import com.example.slowmotionapp.effects.EPlayerView
import com.example.slowmotionapp.effects.FilterAdapter
import com.example.slowmotionapp.effects.FilterType
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.mainCachedFile
import com.example.slowmotionapp.utils.Utils

class EffectActivity : AppCompatActivity(), FilterAdapter.OnItemClickListener {

    private lateinit var binding: ActivityEffectBinding

    private var videoUri: String? = null
    private var type: Int = 0

    private lateinit var adapter: FilterAdapter
    private lateinit var filterTypes: List<FilterType>

    companion object {
        var exoPLayerView: EPlayerView? = null
        var effectPosition = 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEffectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fetch the videoUri from the intent
        videoUri = intent.getStringExtra("VideoUri")
        type = intent.getIntExtra(Constants.TYPE, 0)

        mainCachedFile = videoUri!!

        binding.backBtn.setOnClickListener {
            finish()
        }

        Utils.setUpSimpleExoPlayer(this)
        setUoGlPlayerView()

        filterTypes = FilterType.createFilterList()

        // Initialize RecyclerView
        binding.recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = FilterAdapter(filterTypes, this, lifecycleScope)
        binding.recyclerView.adapter = adapter

        binding.saveBtn.setOnClickListener {
            saveVideoWithFilter()
        }

    }

    private fun saveVideoWithFilter() {

        val progressDialog =
            ProgressDialog(this, R.style.CustomDialog)
        progressDialog.window!!.setBackgroundDrawableResource(R.color.transparent)
        progressDialog.isIndeterminate = true
        progressDialog.setCancelable(false)
        progressDialog.setMessage("Please Wait")
        progressDialog.show()

        if (effectPosition != 0) {
            val outputFile = Utils.createCacheTempFile(this)
            val filter = FilterType.createGlFilter(
                FilterType.createFilterList()[effectPosition],
                this
            )
            Mp4Composer(mainCachedFile, outputFile)
                .rotation(Rotation.NORMAL)
                .fillMode(FillMode.PRESERVE_ASPECT_FIT)
                .filter(filter)
                .listener(object : Mp4Composer.Listener {
                    override fun onProgress(progress: Double) {
                        Log.d(Constants.APP_NAME, "onProgress Filter = " + progress * 100)
                    }

                    override fun onCompleted() {
                        Log.d(Constants.APP_NAME, "onCompleted() Filter : $outputFile")
                        mainCachedFile = outputFile
                        progressDialog.dismiss()
                        Utils.saveEditedVideo(this@EffectActivity)
                    }

                    override fun onCanceled() {
                        progressDialog.dismiss()
                        Log.d(Constants.APP_NAME, "onCanceled")
                    }

                    override fun onFailed(exception: Exception) {
                        progressDialog.dismiss()
                        Log.e(Constants.APP_NAME, "onFailed() Filter", exception)
                    }
                })
                .start()
        } else {
            progressDialog.dismiss()
        }
    }


    private fun setUoGlPlayerView() {
        exoPLayerView =
            EPlayerView(this)
        exoPLayerView!!.setSimpleExoPlayer(Utils.player)

        val videoSize = Utils.getVideoSize(this, Uri.parse(mainCachedFile))
        if (videoSize != null) {
            val videoWidth = videoSize.first
            val videoHeight = videoSize.second

            val layoutParams = RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Calculate the desired height based on the video aspect ratio
            val aspectRatio = videoWidth.toFloat() / videoHeight.toFloat()
            val desiredHeight = (exoPLayerView!!.width / aspectRatio).toInt()

            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            layoutParams.height = desiredHeight

            exoPLayerView!!.layoutParams = layoutParams
        } else {
            exoPLayerView!!.layoutParams =
                RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
        }

        exoPLayerView!!.layoutParams =
            RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        binding.frameLayout.addView(exoPLayerView)
        exoPLayerView!!.onResume()
    }

    override fun onItemClick(position: Int) {
        exoPLayerView!!.setGlFilter(
            FilterType.createGlFilter(
                filterTypes[position],
                this
            )
        )
        effectPosition = position

    }

}