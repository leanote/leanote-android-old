package com.leanote.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.leanote.android.R;

/**
 * Created by binnchx on 8/27/15.
 */
public class LeaLinearLayoutSizeBound extends LinearLayout {
    private final int mMaxWidth;
    private final int mMaxHeight;

    public LeaLinearLayoutSizeBound(Context context) {
        super(context);
        mMaxWidth = 0;
        mMaxHeight = 0;
    }

    public LeaLinearLayoutSizeBound(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs,
                R.styleable.LeaLinearLayoutSizeBound);
        mMaxWidth = a.getDimensionPixelSize(R.styleable.LeaLinearLayoutSizeBound_maxWidth,
                Integer.MAX_VALUE);
        mMaxHeight = a.getDimensionPixelSize(R.styleable.LeaLinearLayoutSizeBound_maxHeight,
                Integer.MAX_VALUE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = MeasureSpec.getSize(widthMeasureSpec);
        if (mMaxWidth > 0 && mMaxWidth < measuredWidth) {
            int measureMode = MeasureSpec.getMode(widthMeasureSpec);
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, measureMode);
        }
        int measuredHeight = MeasureSpec.getSize(heightMeasureSpec);
        if (mMaxHeight > 0 && mMaxHeight < measuredHeight) {
            int measureMode = MeasureSpec.getMode(heightMeasureSpec);
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxHeight, measureMode);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
