package com.example.slowmotionapp.customviews

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class NonSwipeableViewPager(context: Context, attrs: AttributeSet) : ViewPager(context, attrs) {

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        // Disable touch events
        return false
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Disable touch events
        return false
    }
}
