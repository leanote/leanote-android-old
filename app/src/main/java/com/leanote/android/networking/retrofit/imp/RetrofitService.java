package com.leanote.android.networking.retrofit.imp;


import com.leanote.android.model.Account;
import com.leanote.android.networking.retrofit.bean.SuccessBean;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;


/**
 * 网络数据请求接口,所有网络请求的方法均在此添加
 * <p>
 * Created by yuchuan
 * DATE 3/22/16
 * TIME 22:36
 */
public interface RetrofitService {
    String BASE_URL = "https://leanote.com/";

    String LEA_URL_LOGIN = "api/auth/login?";
    String LEA_URL_REGISTER = "api/auth/register?";

    @GET(LEA_URL_LOGIN)
    Call<Account> login(@QueryMap Map<String, String> map);

    @GET(LEA_URL_REGISTER)
    Call<SuccessBean> register(@QueryMap Map<String, String> map);

    /**
     * POST方法数据请求,Call中的参数为对象形式,由于加了转换器,所以使用对象形式
     * "https://leanote.com/api/note/addNote?imeiNum=113"
     * "https://leanote.com/api/note/updateNote?imeiNum=113"
     *
     * @param map 请求中要发送的数据表
     *
     * @return 返回需要的对象
     */
    @FormUrlEncoded
    @POST("api/note/{url}")


    Call<ResponseBody> uploadNoteToServer(@Path("url") String url, @FieldMap Map<String, Object> map);
}
