package com.leanote.android.ui.post;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.ui.ObservableWebView;
import com.leanote.android.ui.WebViewActivity;



public class BlogHomeActivity extends WebViewActivity {
    private String username;
    private FrameLayout webview_wrapper ;
    private Toolbar toolbar ;
    private Boolean hasMeasured = false;
    private Boolean isTopHide = false;
    private Boolean isTop = true;
    static long TIME_ANIMATION =600;
    static String Url ;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.webview);
        setTitle("Blog");
        webview_wrapper =  (FrameLayout) findViewById(R.id.webview_wrapper);


        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // note: do NOT call mWebView.getSettings().setUserAgentString(WordPress.getUserAgent())
        // here since it causes problems with the browser-sniffing that some sites rely on to
        // format the page for mobile display

        mWebView = (WebView) this.findViewById(R.id.webView);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        ViewTreeObserver viewTreeObserver = webview_wrapper.getViewTreeObserver();
        viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {

                if (!hasMeasured) {
                    FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(FrameLayout
                            .LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    layoutParams.setMargins(0,toolbar.getHeight(), 0, 0);
                    mWebView.setLayoutParams(layoutParams);

                    hasMeasured = true;
                }
                return true;
            }
        });



        ((ObservableWebView)mWebView).setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
            public void onScroll(int dx, int dy) {

                //这里我们根据dx和dy参数做自己想做的事情
                if (dy > 100 && isTopHide == false) {

                    hideTop();

                }
            }

            @Override
            public void onPageEnd(int l, int t, int oldl, int oldt) {

            }

            @Override
            public void onPageTop(int l, int t, int oldl, int oldt) {
                if (isTopHide == true) {

                    showTop();

                }
            }
        });

        // load URL if one was provided in the intent
        username=AccountHelper.getDefaultAccount().getmUserName();
        Url = "http://blog.leanote.com/" + username;
        if (Url != null) {
            mWebView.loadUrl(Url);
        }
    }




    private void showTop() {
        //toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(toolbar, "y", toolbar.getY(),
                0);
        anim1.setDuration(TIME_ANIMATION);
        anim1.start();
//
//
//        ObjectAnimator anim4 = ObjectAnimator.ofFloat(mWebView, "y", mWebView.getY(),
//                0);
//        anim4.setDuration(TIME_ANIMATION);
//        anim4.start();


        final int topmatgin = toolbar.getHeight();
        Animation a = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mWebView.getLayoutParams();
                params.topMargin = (int)(topmatgin * interpolatedTime);
                mWebView.setLayoutParams(params);
            }
        };
        a.setDuration(TIME_ANIMATION); // in ms
        mWebView.startAnimation(a);


        isTopHide = false;
    }


    /**
     * 隐藏上部的布局
     */
    private void hideTop() {

        //toolbar.animate().translationY(-toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(2)).start();

        ObjectAnimator anim1 = ObjectAnimator.ofFloat(toolbar, "y", 0,
                -toolbar.getHeight());
        anim1.setDuration(TIME_ANIMATION);
        anim1.start();
//
//        Log.i(this.getClass().getSimpleName(), "para " + mWebView.getY() + " " + webview_wrapper.getY());
//        ObjectAnimator anim4 = ObjectAnimator.ofFloat(mWebView, "y",  mWebView.getY(),
//                0);
//        anim4.setDuration(TIME_ANIMATION);
//        anim4.start();

        final int topmatgin = 0;
        Animation a = new Animation() {

            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)mWebView.getLayoutParams();
                params.topMargin = (int)(topmatgin * interpolatedTime);
                mWebView.setLayoutParams(params);
            }
        };
        a.setDuration(TIME_ANIMATION); // in ms
        mWebView.startAnimation(a);

        isTopHide = true;
    }
}

