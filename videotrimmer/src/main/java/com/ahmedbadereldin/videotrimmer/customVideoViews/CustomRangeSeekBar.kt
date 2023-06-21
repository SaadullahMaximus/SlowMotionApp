package com.ahmedbadereldin.videotrimmer.customVideoViews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.ahmedbadereldin.videotrimmer.R

class CustomRangeSeekBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet?,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var mHeightTimeLine = 0
    private var mBarThumbs: List<BarThumb>? = null
    private var mListeners: MutableList<OnRangeSeekBarChangeListener>? = null
    private var mMaxWidth = 0f
    private var mThumbWidth = 0f
    private var mThumbHeight = 0f
    private var mViewWidth = 0
    private var mPixelRangeMin = 0f
    private var mPixelRangeMax = 0f
    private var mScaleRangeMax = 0f
    private var mFirstRun = false
    private val mShadow = Paint()
    private val mLine = Paint()
    private fun init() {
        mBarThumbs = BarThumb.initThumbs(resources)
        mThumbWidth = BarThumb.getWidthBitmap(mBarThumbs!!).toFloat()
        mThumbHeight = BarThumb.getHeightBitmap(mBarThumbs!!).toFloat()
        mScaleRangeMax = 100f
        mHeightTimeLine = context.resources.getDimensionPixelOffset(R.dimen.frames_video_height)
        isFocusable = true
        isFocusableInTouchMode = true
        mFirstRun = true
        val shadowColor = ContextCompat.getColor(context, R.color.shadow_color)
        mShadow.isAntiAlias = true
        mShadow.color = shadowColor
        mShadow.alpha = 177
        val lineColor = ContextCompat.getColor(context, R.color.line_color)
        mLine.isAntiAlias = true
        mLine.color = lineColor
        mLine.alpha = 200
    }

    fun initMaxWidth() {
        mMaxWidth = mBarThumbs!![1].pos - mBarThumbs!![0].pos
        onSeekStop(this, 0, mBarThumbs!![0].getVal())
        onSeekStop(this, 1, mBarThumbs!![1].getVal())
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val minW = paddingLeft + paddingRight + suggestedMinimumWidth
        mViewWidth = resolveSizeAndState(minW, widthMeasureSpec, 1)
        val minH = paddingBottom + paddingTop + mThumbHeight.toInt()
        val viewHeight = resolveSizeAndState(minH, heightMeasureSpec, 1)
        setMeasuredDimension(mViewWidth, viewHeight)
        mPixelRangeMin = 0f
        mPixelRangeMax = mViewWidth - mThumbWidth
        if (mFirstRun) {
            for (i in mBarThumbs!!.indices) {
                val th = mBarThumbs!![i]
                th.setVal(mScaleRangeMax * i)
                th.pos = mPixelRangeMax * i
            }
            onCreate(this, currentThumb, getThumbValue(currentThumb))
            mFirstRun = false
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawShadow(canvas)
        drawThumbs(canvas)
    }

    private var currentThumb = 0

    init {
        init()
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        val mBarThumb: BarThumb
        val mBarThumb2: BarThumb
        val coordinate = ev.x
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                currentThumb = getClosestThumb(coordinate)
                if (currentThumb == -1) {
                    return false
                }
                mBarThumb = mBarThumbs!![currentThumb]
                mBarThumb.lastTouchX = coordinate
                onSeekStart(this, currentThumb, mBarThumb.getVal())
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (currentThumb == -1) {
                    return false
                }
                mBarThumb = mBarThumbs!![currentThumb]
                onSeekStop(this, currentThumb, mBarThumb.getVal())
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                mBarThumb = mBarThumbs!![currentThumb]
                mBarThumb2 = mBarThumbs!![if (currentThumb == 0) 1 else 0]
                val dx = coordinate - mBarThumb.lastTouchX
                val newX = mBarThumb.pos + dx
                if (currentThumb == 0) {
                    if (newX + mBarThumb.widthBitmap >= mBarThumb2.pos) {
                        mBarThumb.pos = mBarThumb2.pos - mBarThumb.widthBitmap
                    } else if (newX <= mPixelRangeMin) {
                        mBarThumb.pos = mPixelRangeMin
                        if (mBarThumb2.pos - (mBarThumb.pos + dx) > mMaxWidth) {
                            mBarThumb2.pos = mBarThumb.pos + dx + mMaxWidth
                            setThumbPos(1, mBarThumb2.pos)
                        }
                    } else {
                        if (mBarThumb2.pos - (mBarThumb.pos + dx) > mMaxWidth) {
                            mBarThumb2.pos = mBarThumb.pos + dx + mMaxWidth
                            setThumbPos(1, mBarThumb2.pos)
                        }
                        mBarThumb.pos = mBarThumb.pos + dx
                        mBarThumb.lastTouchX = coordinate
                    }
                } else {
                    if (newX <= mBarThumb2.pos + mBarThumb2.widthBitmap) {
                        mBarThumb.pos = mBarThumb2.pos + mBarThumb.widthBitmap
                    } else if (newX >= mPixelRangeMax) {
                        mBarThumb.pos = mPixelRangeMax
                        if (mBarThumb.pos + dx - mBarThumb2.pos > mMaxWidth) {
                            mBarThumb2.pos = mBarThumb.pos + dx - mMaxWidth
                            setThumbPos(0, mBarThumb2.pos)
                        }
                    } else {
                        if (mBarThumb.pos + dx - mBarThumb2.pos > mMaxWidth) {
                            mBarThumb2.pos = mBarThumb.pos + dx - mMaxWidth
                            setThumbPos(0, mBarThumb2.pos)
                        }
                        mBarThumb.pos = mBarThumb.pos + dx
                        mBarThumb.lastTouchX = coordinate
                    }
                }
                setThumbPos(currentThumb, mBarThumb.pos)
                invalidate()
                return true
            }
        }
        return false
    }

    private fun pixelToScale(index: Int, pixelValue: Float): Float {
        val scale = pixelValue * 100 / mPixelRangeMax
        return if (index == 0) {
            val pxThumb = scale * mThumbWidth / 100
            scale + pxThumb * 100 / mPixelRangeMax
        } else {
            val pxThumb = (100 - scale) * mThumbWidth / 100
            scale - pxThumb * 100 / mPixelRangeMax
        }
    }

    private fun scaleToPixel(index: Int, scaleValue: Float): Float {
        val px = scaleValue * mPixelRangeMax / 100
        return if (index == 0) {
            val pxThumb = scaleValue * mThumbWidth / 100
            px - pxThumb
        } else {
            val pxThumb = (100 - scaleValue) * mThumbWidth / 100
            px + pxThumb
        }
    }

    private fun calculateThumbValue(index: Int) {
        if (index < mBarThumbs!!.size && mBarThumbs!!.isNotEmpty()) {
            val th = mBarThumbs!![index]
            th.setVal(pixelToScale(index, th.pos))
            onSeek(this, index, th.getVal())
        }
    }

    private fun calculateThumbPos(index: Int) {
        if (index < mBarThumbs!!.size && mBarThumbs!!.isNotEmpty()) {
            val th = mBarThumbs!![index]
            th.pos = scaleToPixel(index, th.getVal())
        }
    }

    private fun getThumbValue(index: Int): Float {
        return mBarThumbs!![index].getVal()
    }

    fun setThumbValue(index: Int, value: Float) {
        mBarThumbs!![index].setVal(value)
        calculateThumbPos(index)
        invalidate()
    }

    private fun setThumbPos(index: Int, pos: Float) {
        mBarThumbs!![index].pos = pos
        calculateThumbValue(index)
        invalidate()
    }

    private fun getClosestThumb(coordinate: Float): Int {
        var closest = -1
        if (mBarThumbs!!.isNotEmpty()) {
            for (i in mBarThumbs!!.indices) {
                val v = mBarThumbs!![i].pos + mThumbWidth
                if (coordinate >= mBarThumbs!![i].pos && coordinate <= v) {
                    closest = mBarThumbs!![i].index
                }
            }
        }
        return closest
    }

    private fun drawShadow(canvas: Canvas) {
        if (mBarThumbs!!.isNotEmpty()) {
            for (barThumb in mBarThumbs!!) {
                val x = barThumb.pos
                if (barThumb.index == 0) {
                    if (x > mPixelRangeMin) {
                        val mRect = Rect(
                            0,
                            (mThumbHeight - mHeightTimeLine).toInt() / 2,
                            (x + mThumbWidth / 2).toInt(),
                            mHeightTimeLine + (mThumbHeight - mHeightTimeLine).toInt() / 2
                        )
                        canvas.drawRect(mRect, mShadow)
                    }
                } else {
                    if (x < mPixelRangeMax) {
                        val mRect = Rect(
                            (x + mThumbWidth / 2).toInt(),
                            (mThumbHeight - mHeightTimeLine).toInt() / 2,
                            mViewWidth,
                            mHeightTimeLine + (mThumbHeight - mHeightTimeLine).toInt() / 2
                        )
                        canvas.drawRect(mRect, mShadow)
                    }
                }
            }
        }
    }

    private fun drawThumbs(canvas: Canvas) {
        if (mBarThumbs!!.isNotEmpty()) {
            for (th in mBarThumbs!!) {
                if (th.index == 0) {
                    canvas.drawBitmap(th.bitmap, th.pos + paddingLeft, paddingTop.toFloat(), null)
                } else {
                    canvas.drawBitmap(th.bitmap, th.pos - paddingRight, paddingTop.toFloat(), null)
                }
            }
        }
    }

    fun addOnRangeSeekBarListener(listener: OnRangeSeekBarChangeListener) {
        if (mListeners == null) {
            mListeners = ArrayList()
        }
        mListeners!!.add(listener)
    }

    private fun onCreate(CustomRangeSeekBar: CustomRangeSeekBar, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onCreate(CustomRangeSeekBar, index, value)
        }
    }

    private fun onSeek(CustomRangeSeekBar: CustomRangeSeekBar, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onSeek(CustomRangeSeekBar, index, value)
        }
    }

    private fun onSeekStart(CustomRangeSeekBar: CustomRangeSeekBar, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onSeekStart(CustomRangeSeekBar, index, value)
        }
    }

    private fun onSeekStop(CustomRangeSeekBar: CustomRangeSeekBar, index: Int, value: Float) {
        if (mListeners == null) return
        for (item in mListeners!!) {
            item.onSeekStop(CustomRangeSeekBar, index, value)
        }
    }
}