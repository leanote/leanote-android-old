package com.leanote.android.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by binnchx on 11/10/15.
 */
public class LeaAutoResizeTextView extends AutoResizeTextView {

    public LeaAutoResizeTextView(Context context) {
        super(context);
        TypefaceCache.setCustomTypeface(context, this, null);
    }

    public LeaAutoResizeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypefaceCache.setCustomTypeface(context, this, attrs);
    }

    public LeaAutoResizeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypefaceCache.setCustomTypeface(context, this, attrs);
    }

}
