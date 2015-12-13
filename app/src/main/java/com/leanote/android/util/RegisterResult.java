package com.leanote.android.util;

/**
 * Created by binnchx on 12/9/15.
 */
public class RegisterResult {


    private boolean success;
    private String msg;

    public RegisterResult(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
