package com.leanote.android.model;

import com.google.gson.annotations.SerializedName;
import com.leanote.android.db.LeanoteDbManager;

/**
 * Created by binnchx on 8/26/15.
 */
public class AccountModel {

    @SerializedName("Ok")
    private boolean isOk;
    @SerializedName("LocalUserId")
    private long localUserId;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("Username")
    private String userName;
    @SerializedName("Email")
    private String email;
    @SerializedName("Verified")
    private boolean verified;
    @SerializedName("Avatar")
    private String avatar;
    @SerializedName("Token")
    private String accessToken;
    @SerializedName("UseMarkdown")
    private boolean useMarkdown;
    @SerializedName("LastSyncUsn")
    private int lastSyncUsn;
    @SerializedName("Host")
    private String host;
    @SerializedName("Msg")
    private String msg;

    public AccountModel() {
        localUserId = 0L;
    }

    public void init() {
        userId = "";
        userName = "";
        email = "";
        verified = false;
        avatar = "";
        accessToken = "";
        lastSyncUsn = 0;
    }

    public boolean isOk() {
        return isOk;
    }

    public void setOk(boolean ok) {
        isOk = ok;
    }

    public void clearToken() {
        accessToken = "";
    }

    public long getLocalUserId() {
        return localUserId;
    }

    public void setLocalUserId(long localUserId) {
        this.localUserId = localUserId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String mUserId) {
        this.userId = mUserId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getLastSyncUsn() {
        return LeanoteDbManager.getInstance().getAccountUsn(userId);
    }

    public void setLastSyncUsn(int lastSyncUsn) {
        this.lastSyncUsn = lastSyncUsn;
    }

    public boolean isUseMarkdown() {
        return useMarkdown;
    }

    public void setUseMarkdown(boolean useMarkdown) {
        this.useMarkdown = useMarkdown;
    }

    public String getHost() {
        return "https://leanote.com";
//        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "AccountModel{" +
                "isOk=" + isOk +
                ", localUserId=" + localUserId +
                ", userId='" + userId + '\'' +
                ", userName='" + userName + '\'' +
                ", email='" + email + '\'' +
                ", verified=" + verified +
                ", avatar='" + avatar + '\'' +
                ", accessToken='" + accessToken + '\'' +
                ", useMarkdown=" + useMarkdown +
                ", lastSyncUsn=" + lastSyncUsn +
                ", host='" + host + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
