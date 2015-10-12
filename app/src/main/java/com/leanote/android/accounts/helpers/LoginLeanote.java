package com.leanote.android.accounts.helpers;

import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.leanote.android.Leanote;
import com.leanote.android.R;
import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.CustomRequest;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.AppLog.T;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by binnchx on 9/7/15.
 */
public class LoginLeanote extends LoginAbstract {

    @Override
    protected void login() {

        String login_url = String.format("http://leanote.com/api/auth/login?email=%s&pwd=%s", mUsername, mPassword);

        CustomRequest login_req = new CustomRequest(Request.Method.GET, login_url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                Log.d("json:", response.toString());
                try {
                    boolean isOk = (boolean) response.get("Ok");
                    if (isOk) {
                        Account account = AccountHelper.getDefaultAccount();
                        String token = (String) response.get("Token");
                        account.setAccessToken(token.toString());
                        account.setUserName(mUsername);
                        account.save();
                        //account.fetchAccountDetails();

                        mCallback.onSuccess();
                    } else {
                        //mCallback.onError(errorMsgId, errorMsgId == R.string.account_two_step_auth_enabled, false, false);
                        mCallback.onError();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }


                // Once we have a token, start up Simperium
                //SimperiumUtils.configureSimperium(Leanote.getContext(), token.toString());
            }
        }, new Response.ErrorListener(){

            @Override
            public void onErrorResponse(VolleyError error) {
                mCallback.onError();

            }
        });

        Leanote.requestQueue.add(login_req);


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


    public LoginLeanote(String username, String password) {
        super(username, password);
    }


}
