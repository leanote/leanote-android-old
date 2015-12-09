package com.leanote.android.util;

/**
 * Created by binnchx on 12/3/15.
 */
public enum NoteSyncResultEnum {
    FAIL(0, "fail"),
    SUCCESS(1, "success"),
    CONFLICT(2, "conflict");

    private int code;
    private String msg;

    NoteSyncResultEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
