package uk.co.sundroid.util.view

import uk.co.sundroid.R
import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout

class BoundedLinearLayout(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    private val mBoundedWidth: Int
    private val mBoundedHeight: Int

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // Adjust width as necessary
        var widthSpec = widthMeasureSpec
        val measuredWidth = MeasureSpec.getSize(widthSpec)
        if (mBoundedWidth in 1..(measuredWidth - 1)) {
            val measureMode = MeasureSpec.getMode(widthSpec)
            widthSpec = MeasureSpec.makeMeasureSpec(mBoundedWidth, measureMode)
        }
        // Adjust height as necessary
        var heightSpec = heightMeasureSpec
        val measuredHeight = MeasureSpec.getSize(heightSpec)
        if (mBoundedHeight in 1..(measuredHeight - 1)) {
            val measureMode = MeasureSpec.getMode(heightSpec)
            heightSpec = MeasureSpec.makeMeasureSpec(mBoundedHeight, measureMode)
        }
        super.onMeasure(widthSpec, heightSpec)
    }

    init {
        val array = getContext().obtainStyledAttributes(attrs, R.styleable.BoundedLinearLayout)
        mBoundedWidth = array.getDimensionPixelSize(R.styleable.BoundedLinearLayout_bounded_width, 0)
        mBoundedHeight = array.getDimensionPixelSize(R.styleable.BoundedLinearLayout_bounded_height, 0)
        array.recycle()
    }
}
