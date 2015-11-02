package com.leanote.android.util.helper;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.CharacterStyle;
import android.text.style.UpdateAppearance;

/**
 * Created by binnchx on 10/26/15.
 */
public class LeaUnderlineSpan extends CharacterStyle
        implements UpdateAppearance, ParcelableSpan {

    public LeaUnderlineSpan() {
    }

    public LeaUnderlineSpan(Parcel src) {
    }

    public int getSpanTypeId() {
        return 6;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        ds.setUnderlineText(true);
    }
}