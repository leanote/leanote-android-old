package com.leanote.android.util;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by binnchx on 12/27/15.
 */
public class DateUtils {

    public static String formatDate(String dateStr) {
        if (TextUtils.isEmpty(dateStr)) {
            return "";
        }

        Pattern pattern = Pattern.compile("(\\d{4}-\\d{2}-\\d{2})T(\\d{2}:\\d{2}:\\d{2}).*");
        Matcher m = pattern.matcher(dateStr);
        String formatDate = "";
        if (m.find()) {
            formatDate = m.group(1) + " " + m.group(2);
        }

        return formatDate;
    }
}
