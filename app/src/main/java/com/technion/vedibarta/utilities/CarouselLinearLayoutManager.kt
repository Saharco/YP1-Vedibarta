package com.technion.vedibarta.utilities

import android.content.Context
import android.graphics.PointF
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView


class CarouselLinearLayoutManager(val context: Context, orientation: Int, reverseLayout: Boolean) :
    LinearLayoutManager(context, orientation, reverseLayout) {

    private val linearSmoothScroller = object : LinearSmoothScroller(context) {
        // This controls the scrolling speed. Bigger value = slower scroll
        private val MILLISECONDS_PER_INCH = 100f

        override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
            return this@CarouselLinearLayoutManager
                .computeScrollVectorForPosition(targetPosition)
        }

        override fun calculateSpeedPerPixel(displayMetrics: DisplayMetrics?): Float {
            return MILLISECONDS_PER_INCH / displayMetrics!!.densityDpi
        }
    }

    override fun smoothScrollToPosition(
        recyclerView: RecyclerView?,
        state: RecyclerView.State?,
        position: Int
    ) {
        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }
}