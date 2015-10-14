package com.leanote.android.ui;

import com.leanote.android.util.AppLog;

/**
 * Created by binnchx on 10/13/15.
 */
public enum ActivityId {
    UNKNOWN("Unknown"),
    READER("Reader"),
    NOTIFICATIONS("Notifications"),
    ME("Me"),
    MY_SITE("My Site"),
    //POSTS("Post List"),
    MEDIA("Media Library"),
    PAGES("Page List"),
    COMMENTS("Comments"),
    COMMENT_DETAIL("Comment Detail"),
    COMMENT_EDITOR("Comment Editor"),
    SITE_PICKER("Site Picker"),
    THEMES("Themes"),
    STATS("Stats"),
    STATS_VIEW_ALL("Stats View All"),
    STATS_POST_DETAILS("Stats Post Details"),
    VIEW_SITE("View Site"),
    POST_EDITOR("Post Editor"),
    LOGIN("Login Screen"),
    HELP_SCREEN("Help Screen"),
    NOTE("Note"),
    POST("Post"),
    CATEGORY("Category");

    private final String mStringValue;

    private ActivityId(final String stringValue) {
        mStringValue = stringValue;
    }

    public String toString() {
        return mStringValue;
    }

    public static void trackLastActivity(ActivityId activityId) {
        AppLog.v(AppLog.T.UTILS, "trackLastActivity, activityId: " + activityId);
        if (activityId != null) {
            AppPrefs.setLastActivityStr(activityId.name());
        }
    }

    public static ActivityId getActivityIdFromName(String activityString) {
        if (activityString == null) {
            return ActivityId.UNKNOWN;
        }
        try {
            return ActivityId.valueOf(activityString);
        } catch (IllegalArgumentException e) {
            // default to UNKNOWN in case the activityString is bogus
            return ActivityId.UNKNOWN;
        }
    }
}