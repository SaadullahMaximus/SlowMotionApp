package com.example.slowmotionapp.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class CircularImageView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val shapeDrawable: ShapeDrawable = ShapeDrawable(OvalShape())

    init {
        scaleType = ScaleType.CENTER_CROP
    }

    override fun onDraw(canvas: Canvas) {
        shapeDrawable.setBounds(0, 0, width, height)
        canvas.save()
        canvas.clipPath(getClipPath())
        super.onDraw(canvas)
        canvas.restore()
    }

    private fun getClipPath(): Path {
        val path = Path()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        path.addOval(rect, Path.Direction.CW)
        return path
    }
}
