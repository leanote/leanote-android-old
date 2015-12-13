package com.leanote.android.util;

import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by binnchx on 12/9/15.
 */
public class BlogWebViewClient extends WebViewClient  {

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        view.loadUrl(url);
        return true;
    }
}
