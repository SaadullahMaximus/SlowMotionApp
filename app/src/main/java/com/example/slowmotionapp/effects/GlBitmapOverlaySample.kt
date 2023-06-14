package com.example.slowmotionapp.effects

import android.graphics.Bitmap
import android.graphics.Canvas
import com.daasuu.mp4compose.filter.GlOverlayFilter

class GlBitmapOverlaySample(private val bitmap: Bitmap) : GlOverlayFilter() {
    override fun drawCanvas(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)
    }
}