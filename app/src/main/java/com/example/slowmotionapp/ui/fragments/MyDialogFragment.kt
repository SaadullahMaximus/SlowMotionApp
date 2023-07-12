package com.example.slowmotionapp.ui.fragments

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.DialogFragment
import com.example.slowmotionapp.R
import com.example.slowmotionapp.ui.activities.MainActivity
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.isFromTrim
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.justEffects
import com.example.slowmotionapp.ui.activities.MainActivity.Companion.trimOrCrop

class MyDialogFragment : DialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.MyDialogTheme)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_my_dialog, container, false)

        val overlayLayout = view.findViewById<FrameLayout>(R.id.overlay_layout)
        val captureVideo = view.findViewById<CardView>(R.id.startCamera)
        val openGallery = view.findViewById<CardView>(R.id.openGallery)

        overlayLayout.setOnClickListener {
            justEffects = false
            trimOrCrop = false
            // Dismiss the fragment when the overlay layout is clicked
            dismiss()
        }

        captureVideo.setOnClickListener {
            (activity as? MainActivity)?.startCamera()
            isFromTrim = true
            dismiss()
        }

        openGallery.setOnClickListener {
            (activity as? MainActivity)?.checkPermissionGallery()
            isFromTrim = true
            dismiss()
        }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.setCancelable(true)
    }
}
