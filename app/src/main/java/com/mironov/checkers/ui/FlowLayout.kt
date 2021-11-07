package com.mironov.checkers.ui

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import kotlin.math.max

class FlowLayout : ViewGroup {
    private var lineHeightSpace = 0
    private var nextLine=false
    private val numberViewsInLine=8

    class LayoutParams
    /**
     * @param horizontal_spacing Pixels between items, horizontally
     * @param vertical_spacing   Pixels between items, vertically
     */(var horizontal_spacing: Int, var vertical_spacing: Int) :
        ViewGroup.LayoutParams(0, 0) {
    }

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        assert(MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED)
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        var height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom
        val count = childCount
        var lineHeightSpace =0
        var xpos = paddingLeft
        var ypos = paddingTop
        val childHeightMeasureSpec: Int = if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        } else {
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        }
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val lp = child.layoutParams as LayoutParams
                child.measure(
                    MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),
                    childHeightMeasureSpec
                )
                val childWidth = child.measuredWidth
                lineHeightSpace =
                    max(lineHeightSpace, child.measuredHeight + lp.vertical_spacing)

                if (i%numberViewsInLine==0&&i!=0) {
                    xpos = paddingLeft
                    ypos += lineHeightSpace
                    nextLine=false
                }
                xpos += childWidth + lp.horizontal_spacing
            }
        }
        this.lineHeightSpace = lineHeightSpace
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + lineHeightSpace
        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + lineHeightSpace < height) {
                height = ypos + lineHeightSpace
            }
        }
        setMeasuredDimension(width, height)
    }

    override fun generateDefaultLayoutParams(): ViewGroup.LayoutParams {
        return LayoutParams(0, 0)
    }

    override fun checkLayoutParams(p: ViewGroup.LayoutParams): Boolean {
        return p is LayoutParams
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        val count = childCount
        val width = r - l
        var xpos = paddingLeft
        var ypos = paddingTop
        for (i in 0 until count) {
            val child = getChildAt(i)
            if (child.visibility != GONE) {
                val childWidth = child.measuredWidth
                val childHeight = child.measuredHeight
                val lp = child.layoutParams as LayoutParams

                if (i%numberViewsInLine==0&&i!=0) {
                    xpos = paddingLeft
                    ypos += lineHeightSpace
                    nextLine=false
                }

                child.layout(xpos, ypos, xpos + childWidth, ypos + childHeight)
                xpos += childWidth + lp.horizontal_spacing
            }
        }
    }
}