package com.leanote.android.ui;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.leanote.android.R;

public class SignInActivity extends Activity {

    public static final int SIGN_IN_REQUEST = 1;
    public static final int REQUEST_CODE = 5000;
    public static final int ADD_SELF_HOSTED_BLOG = 2;
    public static final int CREATE_ACCOUNT_REQUEST = 3;
    public static final int SHOW_CERT_DETAILS = 4;
    public static String START_FRAGMENT_KEY = "start-fragment";
    public static final String ARG_JETPACK_SITE_AUTH = "ARG_JETPACK_SITE_AUTH";
    public static final String ARG_JETPACK_MESSAGE_AUTH = "ARG_JETPACK_MESSAGE_AUTH";
    public static final String ARG_IS_AUTH_ERROR = "ARG_IS_AUTH_ERROR";

    private SignInFragment mSignInFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_sign_in);

        FragmentManager fragmentManager = getFragmentManager();
        mSignInFragment = (SignInFragment) fragmentManager.findFragmentById(R.id.sign_in_fragment);
//        actionMode(getIntent().getExtras());
//        ActivityId.trackLastActivity(ActivityId.LOGIN);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sign_in, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
