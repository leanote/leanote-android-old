package com.leanote.android.ui.post;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.leanote.android.model.AccountHelper;

public class BlogHomeActivity extends AppCompatActivity {
    String userid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        //this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        WebView webview = new WebView(this);

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }
        });

        webview.getSettings().setJavaScriptEnabled(true);
        userid = AccountHelper.getDefaultAccount().getmUserName();
        Log.i("userid",userid);
        webview.loadUrl("http://blog.leanote.com/"+userid);
        setContentView(webview);
    }
}
