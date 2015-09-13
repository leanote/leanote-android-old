package com.leanote.android.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * Created by binnchx on 8/27/15.
 */
public class OpenSansEditText extends PersistentEditText {
    public OpenSansEditText(Context context) {
        super(context, null);
        TypefaceCache.setCustomTypeface(context, this, null);
    }

    public OpenSansEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypefaceCache.setCustomTypeface(context, this, attrs);
    }

    public OpenSansEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypefaceCache.setCustomTypeface(context, this, attrs);
    }
}