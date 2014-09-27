package com.leanote.model;

import android.net.Uri;

/**
 * Created by jerrychoi on 2014-9-26.
 */
public interface NotebookColumns extends IBaseNoteColumns {

    /**
     * Table name
     */
    public static final String TABLE_NAME = "notebook";

    /**
     * The parent notebook id
     */
    public static final String PARENT_NOTEBOOK_ID = "parent_notebook_id";

    /**
     * The sequence of the notebook
     */
    public static final String SEQ = "seq";

    /**
     * Whether the notebook in trash
     */
    public static final String IS_TRASH = "is_trash";

    /**
     * Whether the notebook is a blog
     */
    public static final String IS_BLOG = "is_blog";

    public static final String CREATE_TABLE = "CREATE TALBLE " + TABLE_NAME + " ("
            + _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + NID + " TEXT, "
            + PARENT_NOTEBOOK_ID + " TEXT, "
            + USER_ID + " TEXT, "
            + SEQ + " INTEGER, "
            + TITLE + " TEXT, "
            + IS_TRASH + " INTEGER DEFAULT 0, "
            + IS_BLOG + " INTEGER DEFAULT 0, "
            + CREATED_TIME + " REAL"
            + UPDATED_TIME + " REAL"
            + ");";

    /**
     * Common notebook content uri
     */
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);

}
