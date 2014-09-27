package com.leanote.model;

import android.content.ContentValues;
import android.net.Uri;
import android.os.Parcelable;

/**
 * Created by jerrychoi on 2014-9-26.
 */
public interface IModel extends Parcelable {

    public abstract ContentValues getContentValues();

    public abstract Uri getContentUri();

}
