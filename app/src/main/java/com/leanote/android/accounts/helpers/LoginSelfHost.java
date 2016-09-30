package com.leanote.android.accounts.helpers;

import com.leanote.android.model.Account;
import com.leanote.android.model.AccountHelper;
import com.leanote.android.networking.retrofit.RetrofitUtil;
import com.leanote.android.networking.retrofit.imp.ImpLogin;
import com.leanote.android.util.AppLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by binnchx on 11/20/15.
 */
public class LoginSelfHost extends LoginAbstract {

    private String hostUrl;

    @Override
    protected void login() {
        String login_url = String.format("%s/api/auth/login?email=%s&pwd=%s", hostUrl, mUsername, mPassword);

        AppLog.i("login_url:" + login_url);

        Map<String, String> map = new HashMap<>();
        map.put("email", mUsername);
        map.put("pwd", mPassword);

        RetrofitUtil.getInstance()
                .setBaseUrl(hostUrl)
                .setTimeout(10000)
                .build()
                .login(map, new ImpLogin() {
                    @Override
                    public void onSuccess(Account account) {
                        if (account.isOk()) {
                            AccountHelper.getInstance().setAccount(account);
                            mCallback.onSuccess();
                        } else {
                            mCallback.onError();
                        }
                    }

                    @Override
                    public void onFail() {
                        mCallback.onError();
                    }
                });

    }

    public LoginSelfHost(String username, String password, String hostUrl) {
        super(username, password);
        this.hostUrl = hostUrl;
    }
}
