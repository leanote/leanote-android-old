package com.leanote.android.accounts.helpers;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.leanote.android.Leanote;
import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.CustomRequest;
import com.leanote.android.util.AppLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by binnchx on 11/20/15.
 */
public class LoginSelfHost extends LoginAbstract {

    private String hostUrl;

    @Override
    protected void login() {
        String login_url = String.format("%s/api/auth/login?email=%s&pwd=%s", hostUrl, mUsername, mPassword);

        AppLog.i("login_url" + login_url);
        CustomRequest login_req = new CustomRequest(Request.Method.GET, login_url, null, new Response.Listener<JSONObject>(){

            @Override
            public void onResponse(JSONObject response) {
                try {
                    AppLog.i("response:" + response.toString());
                    boolean isOk = (boolean) response.get("Ok");
                    if (isOk) {
                        Account account = AccountHelper.getDefaultAccount();
                        String token = response.getString("Token");
                        account.setmAccessToken(token);
                        account.setmUserId(response.getString("UserId"));
                        account.setmUserName(response.getString("Username"));
                        account.setmEmail(response.getString("Email"));
                        account.setHost(hostUrl);
                        account.save();

                        mCallback.onSuccess();
                    } else {
                        //mCallback.onError(errorMsgId, errorMsgId == R.string.account_two_step_auth_enabled, false, false);
                        mCallback.onError();
                    }

                } catch (JSONException e) {
                    mCallback.onError();
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

    public LoginSelfHost(String username, String password, String hostUrl) {
        super(username, password);
        this.hostUrl = hostUrl;
    }
}
