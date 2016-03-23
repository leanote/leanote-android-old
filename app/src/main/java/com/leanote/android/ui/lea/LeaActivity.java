package com.leanote.android.ui.lea;

import android.os.Bundle;
import android.view.MenuItem;

import com.leanote.android.R;
import com.leanote.android.ui.WebViewActivity;

public class LeaActivity extends WebViewActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getText(R.string.Lea));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
        }
        return true;
    }
}
