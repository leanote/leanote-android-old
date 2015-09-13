package com.leanote.android.accounts.helpers;

import android.accounts.Account;
import android.annotation.SuppressLint;

import com.android.volley.VolleyError;
import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.util.VolleyUtils;
import com.wordpress.rest.Oauth;

import org.json.JSONObject;
import com.wordpress.rest.Oauth.Listener;

/**
 * Created by binnchx on 9/7/15.
 */
public class LoginLeanote extends LoginAbstract {

    @Override
    protected void login() {
        Leanote.requestQueue.add(makeOAuthRequest(mUsername, mPassword, new Oauth.Listener() {
            @SuppressLint("CommitPrefEdits")
            @Override
            public void onResponse(final Oauth.Token token) {

                Account account = AccountHelper.getDefaultAccount();

                // Once we have a token, start up Simperium
                SimperiumUtils.configureSimperium(WordPress.getContext(), token.toString());

                mCallback.onSuccess();
            }
        }, new Oauth.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                JSONObject errorObject = VolleyUtils.volleyErrorToJSON(volleyError);
                int errorMsgId = restLoginErrorToMsgId(errorObject);
                mCallback.onError(errorMsgId, errorMsgId == R.string.account_two_step_auth_enabled, false, false);
            }
        }));

    }

    private Request makeOAuthRequest(final String username, final String password, final Listener listener,
                                     final ErrorListener errorListener) {
        Oauth oauth = new Oauth(org.wordpress.android.BuildConfig.OAUTH_APP_ID,
                org.wordpress.android.BuildConfig.OAUTH_APP_SECRET,
                org.wordpress.android.BuildConfig.OAUTH_REDIRECT_URI);
        Request oauthRequest;
        oauthRequest = oauth.makeRequest(username, password, mTwoStepCode, mShouldSendTwoStepSMS, listener, errorListener);
        return oauthRequest;
    }


    public LoginLeanote(String username, String password) {
        super(username, password);
    }


}
