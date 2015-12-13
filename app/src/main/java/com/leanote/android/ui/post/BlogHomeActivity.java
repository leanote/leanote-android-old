package com.leanote.android.ui.post;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.leanote.android.R;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.ui.WebViewActivity;
import com.leanote.android.util.BlogWebViewClient;


public class BlogHomeActivity extends WebViewActivity {
    private String userid;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");

        setContentView(R.layout.webview);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // note: do NOT call mWebView.getSettings().setUserAgentString(WordPress.getUserAgent())
        // here since it causes problems with the browser-sniffing that some sites rely on to
        // format the page for mobile display
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

//        mWebView.setOnScrollChangedCallback(new ObservableWebView.OnScrollChangedCallback() {
//            public void onScroll(int dx, int dy) {
//                //这里我们根据dx和dy参数做自己想做的事情
//                Log.i("dxdy", "dx: " + dx + "dy: " + dy);
//            }
//        });

        // load URL if one was provided in the intent
        userid=AccountHelper.getDefaultAccount().getmUserName();
        String url = String.format("%s/blog/%s",
                AccountHelper.getDefaultAccount().getHost(),
                AccountHelper.getDefaultAccount().getmUserName());

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        BlogWebViewClient client = new BlogWebViewClient();
        mWebView.setWebViewClient(client);

        if (url != null) {
            loadUrl(url);
        }


    }
}





