package com.example.slowmotionapp.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.slowmotionapp.R

class KnobView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    interface OnKnobPositionChangeListener {
        fun onKnobPositionChanged(knobValue: Int)
        fun onKnobStopped(knobValue: Int)
    }

    // Inside KnobView class
    var knobPositionX: Float
        get() = knobPosition.x
        set(value) {
            val valueInt = (value / 100).toInt()
            knobValue = valueInt.coerceIn(1, 13)
            val scaleWidth = width.toFloat() - 2 * knobRadius
            val positionRatio = (knobValue - 1) / 12f
            val knobPositionX = knobRadius + positionRatio * scaleWidth
            knobPosition.x = knobPositionX  // Update the x coordinate only
            invalidate()
        }

    private val knobRadius = 40f
    private val knobColor = 0xFFE91E63.toInt()

    private val scaleImageResId = R.drawable.speed_scale
    private val knobImageResId = R.drawable.knob

    private var scaleImageWidth = 0
    private var scaleImageHeight = 0

    private var knobImage: Drawable
    private var knobImageWidth = 0
    private var knobImageHeight = 0

    private var knobPosition = PointF(0f, 0f)
    private var knobValue = 0

    private val paint = Paint().apply {
        color = knobColor
        isAntiAlias = true
    }

    private var knobPositionChangeListener: OnKnobPositionChangeListener? = null

    init {
        // Load the scale image dimensions
        val res = resources
        val scaleImage = res.getDrawable(scaleImageResId, null)
        scaleImageWidth = scaleImage.intrinsicWidth
        scaleImageHeight = scaleImage.intrinsicHeight - 30

        // Load the knob image and dimensions
        knobImage = res.getDrawable(knobImageResId, null)
        knobImageWidth = 140
        knobImageHeight = 120
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)

        // Calculate the desired height based on the scale image's aspect ratio
        val desiredHeight = (desiredWidth * scaleImageHeight) / scaleImageWidth

        // Add the knob image height to the desired height
        val totalHeight = desiredHeight + knobImageHeight

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width: Int
        val height: Int

        when {
            widthMode == MeasureSpec.EXACTLY && heightMode == MeasureSpec.EXACTLY -> {
                width = widthSize
                height = heightSize
            }
            widthMode == MeasureSpec.EXACTLY -> {
                width = widthSize
                height = totalHeight
            }
            heightMode == MeasureSpec.EXACTLY -> {
                width = desiredWidth
                height = heightSize
            }
            else -> {
                width = desiredWidth
                height = totalHeight
            }
        }

        val widthMeasureSpecFinal = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY)
        val heightMeasureSpecFinal = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)

        super.onMeasure(widthMeasureSpecFinal, heightMeasureSpecFinal)
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the knob image
        val left = (knobPosition.x - knobImageWidth / 2) - dpToPx(1)
        val top = knobPosition.y - knobImageHeight / 2
        val right = (knobPosition.x + knobImageWidth / 2) + dpToPx(2)
        val bottom = knobPosition.y + knobImageHeight / 2
        knobImage.setBounds(left.toInt(), top.toInt(), right.toInt(), bottom.toInt())
        knobImage.draw(canvas)


        // Draw the scale image
        val res = resources
        val scaleImage = res.getDrawable(scaleImageResId, null)
        val scaleTop = bottom + dpToPx(2)
        scaleTop + height
        scaleImage.setBounds(0, scaleTop.toInt(), width, height)
        scaleImage.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                // Handle the knob movement
                val newX = event.x.coerceIn(knobRadius, width.toFloat() - knobRadius)
                knobPosition.set(newX, knobPosition.y)
                knobValue = calculateKnobValue()
                knobPositionChangeListener?.onKnobPositionChanged(knobValue)
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                // Handle the event where the user stops moving
                val newX = event.x.coerceIn(knobRadius, width.toFloat() - knobRadius)
                knobPosition.set(newX, knobPosition.y)
                knobValue = calculateKnobValue()
                knobPositionChangeListener?.onKnobPositionChanged(knobValue)
                knobPositionChangeListener?.onKnobStopped(knobValue)
                invalidate()
            }
        }
        return true
    }

    private fun calculateKnobValue(): Int {
        val scaleWidth = width.toFloat() - 2 * knobRadius
        val valueRange = 1..13
        val positionRatio = (knobPosition.x - knobRadius) / scaleWidth
        val value = (positionRatio * valueRange.count()).toInt() + valueRange.first
        return value.coerceIn(valueRange)
    }


    fun setOnKnobPositionChangeListener(listener: OnKnobPositionChangeListener) {
        knobPositionChangeListener = listener
    }

    private fun dpToPx(dp: Int): Float {
        return dp * resources.displayMetrics.density
    }


}
