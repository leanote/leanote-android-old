package com.leanote.android.networking.retrofit.imp;

import com.leanote.android.model.Account;

/**
 * Created by yuchuan
 * DATE 7/22/16
 * TIME 14:44
 */
public interface ImpLogin {
    void onSuccess(Account account);

    void onFail();

}
