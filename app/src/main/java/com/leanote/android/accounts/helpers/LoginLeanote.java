package com.leanote.android.accounts.helpers;

import android.annotation.SuppressLint;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.leanote.android.BuildConfig;
import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.AppLog.T;
import com.leanote.android.util.VolleyUtils;
import com.wordpress.rest.Oauth;
import com.wordpress.rest.Oauth.ErrorListener;
import com.wordpress.rest.Oauth.Listener;

import org.json.JSONException;
import org.json.JSONObject;

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
                //SimperiumUtils.configureSimperium(Leanote.getContext(), token.toString());

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

    public static int restLoginErrorToMsgId(JSONObject errorObject) {
        // Default to generic error message
        int errorMsgId = R.string.nux_cannot_log_in;

        // Map REST errors to local error codes
        if (errorObject != null) {
            try {
                String error = errorObject.optString("error", "");
                String errorDescription = errorObject.getString("error_description");
                if (error.equals("invalid_request")) {
                    if (errorDescription.contains("Incorrect username or password.")) {
                        errorMsgId = R.string.username_or_password_incorrect;
                    }
                } else if (error.equals("needs_2fa")) {
                    errorMsgId = R.string.account_two_step_auth_enabled;
                } else if (error.equals("invalid_otp")) {
                    errorMsgId = R.string.invalid_verification_code;
                }
            } catch (JSONException e) {
                AppLog.e(T.NUX, e);
            }
        }
        return errorMsgId;
    }

    private Request makeOAuthRequest(final String username, final String password, final Listener listener,
                                     final ErrorListener errorListener) {
        Oauth oauth = new Oauth(com.leanote.android.BuildConfig.OAUTH_APP_ID,
                BuildConfig.OAUTH_APP_SECRET,
                BuildConfig.OAUTH_REDIRECT_URI);
        Request oauthRequest;
        oauthRequest = oauth.makeRequest(username, password, mTwoStepCode, mShouldSendTwoStepSMS, listener, errorListener);
        return oauthRequest;
    }


    public LoginLeanote(String username, String password) {
        super(username, password);
    }


}
