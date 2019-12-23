package com.technion.vedibarta.views

import android.animation.ArgbEvaluator
import android.content.Context
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.technion.vedibarta.R
import com.technion.vedibarta.utilities.CarouselLinearLayoutManager
import kotlin.math.pow


/**
 * A horizontal recycler view which visually acts as a carousel.
 * Uses a gaussian curve to scale the list items' sizes upon scrolling
 */
class HorizontalCarouselRecyclerView(context: Context, attrs: AttributeSet) :
    RecyclerView(context, attrs) {

    private val TAG = "carousel"

    private val activeColor = ContextCompat.getColor(context, R.color.colorPrimary)
    private val inactiveColor = ContextCompat.getColor(context, R.color.colorAccent)
    private var viewsToChangeColor = listOf<Int>()

    var position = 0
    var scrollingPosition = 0

    private val scrollSpeedRatio = 0.125f
    private val listFreezeDuration = 200L

    fun <T : ViewHolder> initialize(newAdapter: Adapter<T>) {
        layoutManager = CarouselLinearLayoutManager(context, HORIZONTAL, false)
        newAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                post {
                    val sidePadding = (width / 2) - (getChildAt(0).width / 2)
                    setPadding(sidePadding, 0, sidePadding, 0)
                    scrollToPosition(0)
                    addOnScrollListener(object : OnScrollListener() {
                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            onScrollChanged()
                        }
                    })
                }
            }
        })
        adapter = newAdapter
        smoothScrollToPosition(position)
        isNestedScrollingEnabled = false

        Handler().postDelayed({
            val child = getChildAt(position)
            if (childCount == 1) {
                child.scaleX = 2f
                child.scaleY = 2f
                colorView(child, 2f)
            }
            isNestedScrollingEnabled = true
        }, listFreezeDuration)
    }

    private fun onScrollChanged() {
        post {
            (0 until childCount).forEach { position ->
                val child = getChildAt(position)
                val childCenterX = (child.left + child.right) / 2
                val scaleValue = getGaussianScale(childCenterX, 1f, 1f, 150.toDouble())
                child.scaleX = scaleValue
                child.scaleY = scaleValue
                colorView(child, scaleValue)
            }
        }
    }

    /**
     * Gaussian curve:
     * f(x) = ae^(-((x-b)^2) / (2c^2))
     * where:
     *  a - magnitude of the curve (scale for the view in the center of the carousel)
     *  x - child's horizontal center,
     *  b - carousel's center,
     *  c - spread of the curve: the higher it is, the wider the curve
     */
    private fun getGaussianScale(
        childCenterX: Int,
        minScaleOffset: Float,
        scaleFactor: Float,
        spreadFactor: Double
    ): Float {
        val recyclerCenterX = (left + right) / 2
        return (Math.E.pow(
            -(childCenterX - recyclerCenterX.toDouble()).pow(2.toDouble()) / (2 * spreadFactor.pow(2.toDouble()))
        ) * scaleFactor + minScaleOffset).toFloat()
    }

    fun setViewsToChangeColor(viewIds: List<Int>) {
        viewsToChangeColor = viewIds
    }

    private fun colorView(child: View, scaleValue: Float) {
        val saturationPercent = (scaleValue - 1) / 1f
        val alphaPercent = scaleValue / 2f
        val matrix = ColorMatrix()
        matrix.setSaturation(saturationPercent)

        viewsToChangeColor.forEach { viewId ->
            when (val viewToChangeColor = child.findViewById<View>(viewId)) {
                is ImageView -> {
                    viewToChangeColor.colorFilter = ColorMatrixColorFilter(matrix)
                    viewToChangeColor.imageAlpha = (255 * alphaPercent).toInt()
                }
                is TextView -> {
                    val textColor = ArgbEvaluator().evaluate(
                        saturationPercent,
                        inactiveColor,
                        activeColor
                    ) as Int
                    viewToChangeColor.setTextColor(textColor)
                }
            }
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        if (state == SCROLL_STATE_IDLE) {
            updatePosition()
        }
        scrollingPosition =
            (layoutManager as CarouselLinearLayoutManager).findFirstVisibleItemPosition()
    }

    private fun updatePosition() {
        position = (layoutManager as CarouselLinearLayoutManager).findFirstVisibleItemPosition()
        scrollingPosition = position
    }

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        return super.fling((velocityX * scrollSpeedRatio).toInt(), velocityY)
    }
}