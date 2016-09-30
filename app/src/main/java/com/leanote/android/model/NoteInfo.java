package com.leanote.android.model;

import com.google.gson.annotations.SerializedName;
import com.leanote.android.util.AppLog;
import com.leanote.android.util.StringUtils;

import java.io.Serializable;

/**
 * Created by binnchx on 10/18/15.
 */
public class NoteInfo implements Serializable {

    @SerializedName("Ok")
    private boolean isOk = true;
    @SerializedName("Msg")
    private String msg;

    private Long id;
    @SerializedName("NoteId")
    private String noteId;
    @SerializedName("NotebookId")
    private String noteBookId;
    private Long localNotebookId;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("Title")
    private String title;
    private String desc;
    @SerializedName("Tags")
    private String tags;
    private String noteAbstract;
    @SerializedName("Content")
    private String content;
    private String fileIds;
    @SerializedName("IsMarkdown")
    private boolean isMarkDown;
    @SerializedName("IsTrash")
    private boolean isTrash;
    private boolean isDeleted;
    private boolean isDirty;
    @SerializedName("IsBlog")
    private boolean isPublicBlog;
    @SerializedName("CreatedTime")
    private String createdTime;
    @SerializedName("UpdatedTime")
    private String updatedTime;
    @SerializedName("PublicTime")
    private String publicTime;
    @SerializedName("Usn")
    private int usn;
    private boolean isUploading;
    private boolean uploadSucc = true;

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getPublicTime() {
        return publicTime;
    }

    public void setPublicTime(String publicTime) {
        this.publicTime = publicTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNoteBookId() {
        return noteBookId;
    }

    public void setNoteBookId(String noteBookId) {
        this.noteBookId = noteBookId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }


    public boolean isMarkDown() {
        return isMarkDown;
    }

    public void setIsMarkDown(boolean isMarkDown) {
        this.isMarkDown = isMarkDown;
    }

    public boolean isTrash() {
        return isTrash;
    }

    public void setIsTrash(boolean isTrash) {
        this.isTrash = isTrash;
    }

    public int getUsn() {
        return usn;
    }

    public boolean isUploadSucc() {
        return uploadSucc;
    }

    public void setUploadSucc(boolean uploadSucc) {
        this.uploadSucc = uploadSucc;
    }

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }

    @Override
    public String toString() {
        return "NoteInfo{" +
                "id=" + id +
                ", noteId='" + noteId + '\'' +
                ", noteBookId='" + noteBookId + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", desc='" + desc + '\'' +
                ", tags='" + tags + '\'' +
                ", noteAbstract='" + noteAbstract + '\'' +
                ", content='" + content + '\'' +
                ", fileIds='" + fileIds + '\'' +
                ", isMarkDown=" + isMarkDown +
                ", isTrash=" + isTrash +
                ", isDeleted=" + isDeleted +
                ", isDirty=" + isDirty +
                ", isPublicBlog=" + isPublicBlog +
                ", createdTime='" + createdTime + '\'' +
                ", updatedTime='" + updatedTime + '\'' +
                ", publicTime='" + publicTime + '\'' +
                ", usn=" + usn +
                '}';
    }

    public boolean hasChanges(NoteInfo otherNote) {

        AppLog.i("title equals:" + !StringUtils.equals(title, otherNote.title));
        AppLog.i("content equals:" + !StringUtils.equals(content, otherNote.content));
        AppLog.i("notebookid equals:" + !(noteBookId.equals(otherNote.noteBookId)));
        AppLog.i("isMarkDown equal:" + (isMarkDown != otherNote.isMarkDown));
        AppLog.i("tags equals:" + !StringUtils.equals(tags, otherNote.tags));
        AppLog.i("isblog equals:" + (isPublicBlog != otherNote.isPublicBlog));

        return otherNote == null || !StringUtils.equals(title, otherNote.title)
                || !StringUtils.equals(content, otherNote.content)
                || !StringUtils.equals(noteBookId, otherNote.noteBookId)
                || isMarkDown != otherNote.isMarkDown
                || !StringUtils.equals(tags, otherNote.tags)
                || isPublicBlog != otherNote.isPublicBlog;
    }

    public boolean isPublicBlog() {
        return isPublicBlog;
    }

    public void setIsPublicBlog(boolean isPublicBlog) {
        this.isPublicBlog = isPublicBlog;
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

    public String getNoteAbstract() {
        return noteAbstract;
    }

    public void setNoteAbstract(String noteAbstract) {
        this.noteAbstract = noteAbstract;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getFileIds() {
        return fileIds;
    }

    public void setFileIds(String fileIds) {
        this.fileIds = fileIds;
    }

    public boolean isUploading() {
        return isUploading;
    }

    public void setIsUploading(boolean isUploading) {
        this.isUploading = isUploading;
    }

    public Long getLocalNotebookId() {
        return localNotebookId;
    }

    public void setLocalNotebookId(Long localNotebookId) {
        this.localNotebookId = localNotebookId;
    }
}
