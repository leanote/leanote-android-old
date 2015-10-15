package com.leanote.android.widget;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.leanote.android.util.AppLog;

/**
 * Created by binnchx on 10/14/15.
 */
public class CustomSwipeRefreshLayout extends SwipeRefreshLayout {
    public CustomSwipeRefreshLayout(Context context) {
        super(context);
    }

    public CustomSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        try{
            return super.onTouchEvent(event);
        } catch(IllegalArgumentException e) {
            // Fix for https://github.com/wordpress-mobile/WordPress-Android/issues/2373
            // Catch IllegalArgumentException which can be fired by the underlying SwipeRefreshLayout.onTouchEvent()
            // method.
            // When android support-v4 fixes it, we'll have to remove that custom layout completely.
            AppLog.e(AppLog.T.UTILS, e);
            return true;
        }
    }
}