package com.example.slowmotionapp.customviews

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import com.example.slowmotionapp.R

class CustomWaitingDialog(context: Context) : Dialog(context) {

    private var closeButtonClickListener: (() -> Unit)? = null

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setCancelable(false)

        val contentView =
            LayoutInflater.from(context).inflate(R.layout.custom_waiting_dialog, null)
        setContentView(contentView)

        window?.setGravity(Gravity.CENTER)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        val closeButton: ImageView = contentView.findViewById(R.id.closeButton)
        closeButton.setOnClickListener {
            closeButtonClickListener?.invoke()
            dismiss()
        }
    }

    fun setCloseButtonClickListener(listener: () -> Unit) {
        closeButtonClickListener = listener
    }
}
