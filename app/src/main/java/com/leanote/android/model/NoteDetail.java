package com.leanote.android.model;

import com.leanote.android.util.StringUtils;

import java.io.Serializable;

/**
 * Created by binnchx on 10/18/15.
 */
public class NoteDetail implements Serializable {


    private Long id;
    private String noteId;
    private String noteBookId;
    private String userId;
    private String title;
    private String desc;
    private String tags;
    private String noteAbstract;
    private String content;
    private String fileIds;
    private boolean isMarkDown;
    private boolean isTrash;
    private boolean isDeleted;
    private boolean isDirty;
    private boolean isPublicBlog;
    private String createdTime;
    private String updatedTime;
    private String publicTime;
    private int usn;
    private boolean isUploading;



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

    @Override
    public String toString() {
        return "NoteDetail{" +
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

    public void setUsn(int usn) {
        this.usn = usn;
    }

    public String getNoteId() {
        return noteId;
    }

    public void setNoteId(String noteId) {
        this.noteId = noteId;
    }


    public boolean hasChanges(NoteDetail otherNote) {

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
}
