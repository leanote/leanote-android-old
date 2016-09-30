package com.leanote.android.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by binnchx on 11/1/15.
 */
public class NotebookInfo {

    @SerializedName("Ok")
    private boolean isOk = true;
    @SerializedName("Msg")
    private String msg;

    private long id;
    @SerializedName("NotebookId")
    private String notebookId;
    @SerializedName("ParentNotebookId")
    private String parentNotebookId;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("Title")
    private String title;
    private String urlTitle;
    @SerializedName("Seq")
    private int seq;
    @SerializedName("IsBlog")
    private boolean isBlog;
    @SerializedName("CreatedTime")
    private String createTime;
    @SerializedName("UpdatedTime")
    private String updateTime;
    private boolean isDirty;
    @SerializedName("IsDeleted")
    private boolean isDeleted;
    private boolean isTrash;
    @SerializedName("ParentNotebookId")
    private int usn;

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isBlog() {
        return isBlog;
    }

    public void setIsBlog(boolean isBlog) {
        this.isBlog = isBlog;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    public boolean isDirty() {
        return isDirty;
    }

    public void setIsDirty(boolean isDirty) {
        this.isDirty = isDirty;
    }

    public String getNotebookId() {
        return notebookId;
    }

    public void setNotebookId(String notebookId) {
        this.notebookId = notebookId;
    }

    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getUrlTitle() {
        return urlTitle;
    }

    public void setUrlTitle(String urlTitle) {
        this.urlTitle = urlTitle;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getUsn() {
        return usn;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public String getParentNotebookId() {
        return parentNotebookId;
    }

    public void setParentNotebookId(String parentNotebookId) {
        this.parentNotebookId = parentNotebookId;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setIsTrash(boolean isTrash) {
        this.isTrash = isTrash;
    }


}
