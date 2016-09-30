package com.leanote.android.networking.retrofit;


import com.leanote.android.model.Account;
import com.leanote.android.networking.retrofit.bean.SuccessBean;
import com.leanote.android.networking.retrofit.imp.ImpLogin;
import com.leanote.android.networking.retrofit.imp.ImpRegister;
import com.leanote.android.networking.retrofit.imp.RetrofitService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * 网络请求工具类,所有请求均在此调用
 * 使用方法:
 * Retrofit.getInstance()
 * .setBaseUrl(baseUrl)
 * .setConnectTimeout(10 * 1000)
 * .setTimeout(10 * 1000)
 * .build()
 * .updateDeviceInfo();
 * <p>
 * Created by yuchuan
 * DATE 3/22/16
 * TIME 22:50
 */
public class RetrofitUtil {

    public static final int RETROFITUTIL_BASE_URL = 0;
    public static final int RETROFITUTIL_THEME_URL = 1;

    private static LgClient sLgClient;
    private static RetrofitService sRetrofitService;
    private LgClient.LgBuilder mLgBuilder;
    private HashMap<String, Call> mRequestMap;

    public RetrofitUtil() {
        mRequestMap = new HashMap<>();
        mLgBuilder = new LgClient.LgBuilder();
    }

    //获取单例
    public static RetrofitUtil getInstance() {
        return RetrofitUtilHolder.sRetrofitUtil;
    }

    private static class RetrofitUtilHolder {
        private static final RetrofitUtil sRetrofitUtil = new RetrofitUtil();
    }

    public RetrofitUtil setBaseUrl(String baseUrl) {
        if (mLgBuilder == null) {
            mLgBuilder = new LgClient.LgBuilder();
        }
        mLgBuilder.setBaseUrl(baseUrl);
        return this;
    }

    public RetrofitUtil setBaseUrl(int type) {
        if (mLgBuilder == null) {
            mLgBuilder = new LgClient.LgBuilder();
        }
        mLgBuilder.setBaseUrl(getBaseUrl(type));
        return this;
    }

    /**
     * 根据类型获取对应的url
     *
     * @param type 类型:RETROFITUTIL_BASE_URL,RETROFITUTIL_THEME_URL
     *
     * @return
     */
    private String getBaseUrl(int type) {
        switch (type) {
            case RETROFITUTIL_BASE_URL:
                return RetrofitService.BASE_URL;
            default:
                return RetrofitService.BASE_URL;
        }
    }

    public RetrofitUtil build() {
        sLgClient = mLgBuilder.build();
        sRetrofitService = sLgClient.getThemeService();
        return this;
    }

    /**
     * 设置连接超时
     *
     * @param connectTimeout 超时时间
     *
     * @return
     */
    public RetrofitUtil setConnectTimeout(int connectTimeout) {
        if (mLgBuilder == null) {
            mLgBuilder = new LgClient.LgBuilder();
        }
        mLgBuilder.setConnectTimeout(connectTimeout);
        return this;
    }

    /**
     * 设置读写超时
     *
     * @param timeout 超时时间
     *
     * @return
     */
    public RetrofitUtil setTimeout(int timeout) {
        if (mLgBuilder == null) {
            mLgBuilder = new LgClient.LgBuilder();
        }
        mLgBuilder.setTimeout(timeout);
        return this;
    }

    public void login(Map<String, String> map, final ImpLogin impLogin) {
        Call<Account> call = sRetrofitService.login(map);
        call.enqueue(new Callback<Account>() {
            @Override
            public void onResponse(Call<Account> call, Response<Account> response) {
                if (impLogin != null) {
                    if (response != null && response.body() != null) {
                        impLogin.onSuccess(response.body());
                    } else {
                        impLogin.onFail();
                    }
                }
            }

            @Override
            public void onFailure(Call<Account> call, Throwable t) {
                if (impLogin != null) {
                    impLogin.onFail();
                }
            }
        });
    }

    public void register(Map<String, String> map, final ImpRegister impRegister) {
        Call<SuccessBean> call = sRetrofitService.register(map);
        call.enqueue(new Callback<SuccessBean>() {
            @Override
            public void onResponse(Call<SuccessBean> call, Response<SuccessBean> response) {
                SuccessBean bean = response.body();
                if (bean != null && bean.isOk()) {
                    if (impRegister != null) {
                        impRegister.onSuccess(bean);
                    }
                } else {
                    if (impRegister != null) {
                        impRegister.onFail(bean == null ? "" : bean.getMsg());
                    }
                }
            }

            @Override
            public void onFailure(Call<SuccessBean> call, Throwable t) {
                if (impRegister != null) {
                    impRegister.onFail(t.getMessage());
                }
            }
        });
    }

    /**
     * upload note to server
     * 同步请求
     *
     * @param url 请求地址
     * @param map 参数
     *
     * @return
     */
    public String uploadNoteToServer(String url, Map<String, Object> map) {
        Call<ResponseBody> call = sRetrofitService.uploadNoteToServer(url, map);
        try {
            return call.execute().body().string();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 添加请求进行
     * <p>
     * 管理
     *
     * @param key  键
     * @param call 请求对象
     */
    private void addCallToMap(String key, Call call) {
        if (mRequestMap == null) {
            mRequestMap = new HashMap<>();
        }
        mRequestMap.put(key, call);
    }

    /**
     * 根据键值删除请求
     *
     * @param key 键
     */
    private void removeCallFromMap(String key) {
        if (mRequestMap == null || mRequestMap.isEmpty()) {
            return;
        }
        if (mRequestMap.containsKey(key)) {
            mRequestMap.remove(key);
        }
    }

    /**
     * 根据对象删除请求
     *
     * @param call 请求对象
     */
    private void removeCallFromMap(Call call) {
        if (mRequestMap == null || mRequestMap.isEmpty()) {
            return;
        }
        if (mRequestMap.containsValue(call)) {
            mRequestMap.remove(call);
        }
    }

}
