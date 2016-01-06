
package com.leanote.android.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.leanote.android.R;
import com.leanote.android.util.BlogWebViewClient;

public abstract class WebViewActivity extends AppCompatActivity {
    /** Primary webview used to display content. */

    private static final String URL = "url";

    protected ProgressBar progressBar;
    protected WebView mWebView;
    protected TextView progressTv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_PROGRESS);

        super.onCreate(savedInstanceState);

        // clear title text so there's no title until actual web page title can be shown
        // this is done here rather than in the manifest to automatically handle descendants
        // such as AuthenticatedWebViewActivity
        setTitle("");

        setContentView(R.layout.webview);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        progressTv = (TextView) findViewById(R.id.progress_tv);
        mWebView = (WebView) findViewById(R.id.webView);
        mWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);

        mWebView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int progress) {

                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                    progressTv.setVisibility(View.VISIBLE);
                }
                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                    progressTv.setVisibility(View.GONE);
                }

            }
        });

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);

        mWebView.setWebViewClient(new BlogWebViewClient());
        String url = getIntent().getStringExtra(URL);
        if (url != null) {
            loadUrl(url);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Flash video may keep playing if the webView isn't paused here
        pauseWebView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeWebView();
    }

    private void pauseWebView() {
        if (mWebView != null) {
            mWebView.onPause();
        }
    }

    private void resumeWebView() {
        if (mWebView != null) {
            mWebView.onResume();
        }
    }

    /**
     * Load the specified URL in the webview.
     *
     * @param url URL to load in the webview.
     */
    protected void loadUrl(String url) {
        mWebView.loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (mWebView != null && mWebView.canGoBack())
            mWebView.goBack();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
