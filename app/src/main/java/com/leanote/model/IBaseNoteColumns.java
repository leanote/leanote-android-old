package com.leanote.model;

import android.provider.BaseColumns;

import com.leanote.util.Constants;

/**
 * Base columns for notebook and note
 * <p/>
 * Created by jerrychoi on 2014-9-26.
 */
public interface IBaseNoteColumns extends BaseColumns {

    public static final String AUTHORITY = Constants.PACKAGE_NAME + ".provider";

    /**
     * Notebook or note id
     */
    public static final String NID = "nid";

    /**
     * User id
     */
    public static final String USER_ID = "user_id";

    /**
     * The title of the notebook or note
     */
    public static final String TITLE = "title";

    /**
     * Created time of the notebook or note
     */
    public static final String CREATED_TIME = "created_time";

    /**
     * Last updated time of the notebook or note
     */
    public static final String UPDATED_TIME = "updated_time";

}
