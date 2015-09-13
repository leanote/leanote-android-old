package com.leanote.android.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by binnchx on 8/26/15.
 */
public class LeaMainActivity extends Activity {


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
//        if (AccountUtils.isSignedIn()) {
//
//
//        } else {
//            ActivityLauncher.showSignInForResult(this);
//        }
        ActivityLauncher.showSignInForResult(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.ADD_ACCOUNT && resultCode == RESULT_OK) {

        }
    }
}