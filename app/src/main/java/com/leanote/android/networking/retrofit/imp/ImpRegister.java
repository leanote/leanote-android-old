package com.leanote.android.networking.retrofit.imp;

import com.leanote.android.networking.retrofit.bean.SuccessBean;

/**
 * Created by yuchuan
 * DATE 7/27/16
 * TIME 16:38
 */
public interface ImpRegister {

    void onSuccess(SuccessBean bean);

    void onFail(String msg);

}
