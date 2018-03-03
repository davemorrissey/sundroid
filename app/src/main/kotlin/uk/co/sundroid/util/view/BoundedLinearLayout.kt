package uk.co.sundroid.util.view

import uk.co.sundroid.R
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class BoundedLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val boundedWidth: Int
    private val boundedHeight: Int

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Adjust width as necessary
        var widthSpec = widthMeasureSpec
        val measuredWidth = MeasureSpec.getSize(widthSpec)
        if (boundedWidth in 1..(measuredWidth - 1)) {
            val measureMode = MeasureSpec.getMode(widthSpec)
            widthSpec = MeasureSpec.makeMeasureSpec(boundedWidth, measureMode)
        }
        // Adjust height as necessary
        var heightSpec = heightMeasureSpec
        val measuredHeight = MeasureSpec.getSize(heightSpec)
        if (boundedHeight in 1..(measuredHeight - 1)) {
            val measureMode = MeasureSpec.getMode(heightSpec)
            heightSpec = MeasureSpec.makeMeasureSpec(boundedHeight, measureMode)
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    init {
        val array = context.obtainStyledAttributes(attrs, R.styleable.BoundedLinearLayout)
        boundedWidth = array.getDimensionPixelSize(R.styleable.BoundedLinearLayout_bounded_width, 0)
        boundedHeight = array.getDimensionPixelSize(R.styleable.BoundedLinearLayout_bounded_height, 0)
        array.recycle()
    }
}
