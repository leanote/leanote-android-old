package com.leanote.android.accounts.helpers;

import com.leanote.android.R;
import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.retrofit.RetrofitUtil;
import com.leanote.android.networking.retrofit.imp.ImpLogin;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.AppLog.T;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by binnchx on 9/7/15.
 */
public class LoginLeanote extends LoginAbstract {

    @Override
    protected void login() {

        Map<String,String> map = new HashMap<>();
        map.put("email",mUsername);
        map.put("pwd",mPassword);
        RetrofitUtil.getInstance()
                .setBaseUrl(RetrofitUtil.RETROFITUTIL_BASE_URL)
                .setTimeout(10000)
                .build()
                .login(map,new ImpLogin(){
                    @Override
                    public void onSuccess(Account account) {
                        if (account.isOk()){
                            AccountHelper.getInstance().setAccount(account);
                            mCallback.onSuccess();
                        }else {
                            mCallback.onError();
                        }
                    }

                    @Override
                    public void onFail() {
                        mCallback.onError();
                    }
                });

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
