package com.technion.vedibarta.utilities

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class CustomViewPager(context: Context, attrs: AttributeSet): ViewPager(context, attrs) {

    var pageEnabled = true
    private var custonBehavior : () -> Unit = {};

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return pageEnabled && super.onTouchEvent(ev)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        custonBehavior.invoke()
        return pageEnabled && super.onInterceptTouchEvent(ev)
    }

    fun setPagingEnabled(enabled: Boolean) {
        pageEnabled = enabled
    }

    fun setOnInterceptTouchEventCustomBehavior(function : ()->Unit){
        custonBehavior = function
    }


}