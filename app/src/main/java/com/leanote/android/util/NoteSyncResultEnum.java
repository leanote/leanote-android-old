package com.leanote.android.util;

import com.leanote.android.Leanote;
import com.leanote.android.R;

/**
 * Created by binnchx on 12/3/15.
 */
public enum NoteSyncResultEnum {

    FAIL(0, Leanote.getContext().getString(R.string.upload_fail)),
    SUCCESS(1, Leanote.getContext().getString(R.string.upload_successfully)),
    CONFLICT(2, Leanote.getContext().getString(R.string.upload_conflict));

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
