package com.technion.vedibarta.utilities

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet): ViewPager(context, attrs) {

    private var pageEnabled = true

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return pageEnabled && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return pageEnabled && super.onInterceptTouchEvent(ev)
    }

    fun setPagingEnabled(enabled: Boolean) {
        pageEnabled = enabled
    }
}