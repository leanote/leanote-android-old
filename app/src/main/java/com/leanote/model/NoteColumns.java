package com.leanote.model;

import android.net.Uri;

/**
 * Created by jerrychoi on 2014-9-26.
 */
public interface NoteColumns extends IBaseNoteColumns {

    public static final String TABLE_NAME = "note";

    /**
     * The created user id. <p/>
     * Used for share only. It isn't equal to user id when the note is shared.
     * Otherwise the value is empty.
     */
    public static final String CREATED_USER_ID = "created_user_id";

    /**
     * The notebook id which the note belongs to
     */
    public static final String NOTEBOOK_ID = "notebook_id";

    /**
     * The description. No html labels.
     */
    public static final String DESC = "desc";

    /**
     * Whether the content is writted by markdown or html
     */
    public static final String IS_MARKDOWN = "is_markdown";

    /**
     * The content of the note
     */
    public static final String CONTENT = "content";

    /**
     * The summary of the note. It's shorter than content. Used for blog only.</p>
     * May contains html labels.
     */
    public static final String SUMMARY = "summary";

    /**
     * The source of the first thumbnails
     */
    public static final String THUMBNAILS_SRC = "thumbnails_src";

    /**
     * Tags of the note
     */
    public static final String TAGS = "tags";

    /**
     * Whether the note is in trash
     */
    public static final String IS_TRASH = "is_trash";

    /**
     * Whether the note is a blog
     */
    public static final String IS_BLOG = "is_blog";

    /**
     * Whether the note is ordered to top
     */
    public static final String IS_TOP = "is_top";

    /**
     * Last user id who modified the note if it's shared and could be written
     */
    public static final String UPDATED_USER_ID = "updated_user_id";

    /**
     * Sql of creating note table
     */
    public static final String CREATE_TABLE = "CREATE TALBLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NID + " TEXT, "
            + USER_ID + " TEXT, "
            + CREATED_USER_ID + " TEXT, "
            + NOTEBOOK_ID + " TEXT, "
            + TITLE + " TEXT, "
            + DESC + " TEXT, "
            + IS_MARKDOWN + " INTEGER DEFAULT 0, "
            + CONTENT + " TEXT, "
            + SUMMARY + " TEXT, "
            + THUMBNAILS_SRC + " TEXT, "
            + TAGS + " TEXT, "
            + IS_TRASH + " INTEGER DEFAULT 0, "
            + IS_BLOG + " INTEGER DEFAULT 0, "
            + IS_TOP + " INTEGER DEFAULT 0, "
            + CREATED_TIME + " REAL, "
            + UPDATED_TIME + " REAL, "
            + UPDATED_USER_ID + " TEXT, "
            + ");";

    /**
     * Common note content uri
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

    /**
     * Simple note content uri
     */
    public static final Uri SIMPLE_NOTE_CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + TABLE_NAME + "/simple");

}
