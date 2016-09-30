package com.leanote.android.networking.retrofit.bean;


import com.google.gson.annotations.SerializedName;

/**
 * Created by yuchuan
 * DATE 16/4/2
 * TIME 15:05
 */
public class SuccessBean {

    @SerializedName("Ok")
    public boolean isOk;
    @SerializedName("Msg")
    public String msg;

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "SuccessBean{" +
                "isOk=" + isOk +
                ", msg='" + msg + '\'' +
                '}';
    }
}
