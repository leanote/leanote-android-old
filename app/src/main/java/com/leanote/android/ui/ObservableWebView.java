package com.leanote.android.ui;

import android.content.Context;
import android.util.AttributeSet;

import android.util.Log;
import android.webkit.WebView;

/**
 * Created by mashenjun on 3-12-15.
 */
public class ObservableWebView extends WebView {

    private OnScrollChangedCallback mOnScrollChangedCallback;

    public ObservableWebView(final Context context) {
        super(context);
    }


    public ObservableWebView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    public ObservableWebView(final Context context, final AttributeSet attrs,
                             final int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl,
                                   final int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);


        float webcontent = getContentHeight() * (getResources().getDisplayMetrics().density);// webview的高度
        float webnow = getHeight() + getScrollY();// 当前webview的高度
        Log.i(this.getClass().getSimpleName(),"para "+webcontent+" "+webnow);

        if (mOnScrollChangedCallback != null) {

            if (Math.abs(webcontent - webnow) < 1) {
                // 已经处于底端
                // Log.i("TAG1", "已经处于底端");
                mOnScrollChangedCallback.onPageEnd(l, t, oldl, oldt);
            } else if (getScrollY() == 0) {
                // Log.i("TAG1", "已经处于顶端");
                mOnScrollChangedCallback.onPageTop(l, t, oldl, oldt);
            }
            else {
                mOnScrollChangedCallback.onScroll(l - oldl, t - oldt);
            }

        }
    }

    public OnScrollChangedCallback getOnScrollChangedCallback() {
        return mOnScrollChangedCallback;
    }

    public void setOnScrollChangedCallback(
            final OnScrollChangedCallback onScrollChangedCallback) {
        mOnScrollChangedCallback = onScrollChangedCallback;
    }

    /**
     * Impliment in the activity/fragment/view that you want to listen to the webview
     */
    public static interface OnScrollChangedCallback {
        public void onScroll(int dx, int dy);
        public void onPageEnd(int l, int t, int oldl, int oldt);
        public void onPageTop(int l, int t, int oldl, int oldt);

    }
}
