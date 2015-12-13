package com.leanote.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.style.ImageSpan;

/**
 * Created by binnchx on 10/22/15.
 */
public class LeaImageSpan extends ImageSpan implements Parcelable {
    protected Uri mImageSource = null;
    protected boolean mNetworkImageLoaded = false;
    protected MediaFile mMediaFile;
    protected int mStartPosition, mEndPosition;

    protected LeaImageSpan() {
        super((Bitmap) null);
    }

    public LeaImageSpan(Context context, Bitmap b, Uri src) {
        super(context, b);
        this.mImageSource = src;
        mMediaFile = new MediaFile();
    }

    public LeaImageSpan(Context context, int resId, Uri src) {
        super(context, resId);
        this.mImageSource = src;
        mMediaFile = new MediaFile();
    }

    public void setPosition(int start, int end) {
        mStartPosition = start;
        mEndPosition = end;
    }

    public int getStartPosition() {
        return mStartPosition >= 0 ? mStartPosition : 0;
    }

    public int getEndPosition() {
        return mEndPosition < getStartPosition() ? getStartPosition() : mEndPosition;
    }

    public MediaFile getMediaFile() {
        return mMediaFile;
    }

    public void setMediaFile(MediaFile mMediaFile) {
        this.mMediaFile = mMediaFile;
    }

    public void setImageSource(Uri mImageSource) {
        this.mImageSource = mImageSource;
    }

    public Uri getImageSource() {
        return mImageSource;
    }

    public boolean isNetworkImageLoaded() {
        return mNetworkImageLoaded;
    }

    public void setNetworkImageLoaded(boolean networkImageLoaded) {
        this.mNetworkImageLoaded = networkImageLoaded;
    }

    protected void setupFromParcel(Parcel in) {
        MediaFile mediaFile = new MediaFile();

        boolean[] booleans = new boolean[2];
        in.readBooleanArray(booleans);
        setNetworkImageLoaded(booleans[0]);

        setImageSource(Uri.parse(in.readString()));
        mediaFile.setMediaId(in.readString());

        mediaFile.setNoteID(in.readString());
        mediaFile.setCaption(in.readString());
        mediaFile.setDescription(in.readString());
        mediaFile.setTitle(in.readString());
        mediaFile.setMimeType(in.readString());
        mediaFile.setFileName(in.readString());
        mediaFile.setThumbnailURL(in.readString());

        mediaFile.setFileURL(in.readString());
        mediaFile.setFilePath(in.readString());
        mediaFile.setDateCreatedGMT(in.readLong());
        mediaFile.setWidth(in.readInt());
        mediaFile.setHeight(in.readInt());
        setPosition(in.readInt(), in.readInt());

        setMediaFile(mediaFile);
    }

    public static final Parcelable.Creator<LeaImageSpan> CREATOR
            = new Parcelable.Creator<LeaImageSpan>() {
        public LeaImageSpan createFromParcel(Parcel in) {
            LeaImageSpan imageSpan = new LeaImageSpan();
            imageSpan.setupFromParcel(in);
            return imageSpan;
        }

        public LeaImageSpan[] newArray(int size) {
            return new LeaImageSpan[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeBooleanArray(new boolean[] {mNetworkImageLoaded, false});
        parcel.writeString(mImageSource.toString());
        parcel.writeString(mMediaFile.getMediaId());
        parcel.writeString(mMediaFile.getNoteID());
        parcel.writeString(mMediaFile.getCaption());
        parcel.writeString(mMediaFile.getDescription());
        parcel.writeString(mMediaFile.getTitle());
        parcel.writeString(mMediaFile.getMimeType());
        parcel.writeString(mMediaFile.getFileName());
        parcel.writeString(mMediaFile.getThumbnailURL());

        parcel.writeString(mMediaFile.getFileURL());
        parcel.writeString(mMediaFile.getFilePath());
        parcel.writeLong(mMediaFile.getDateCreatedGMT());
        parcel.writeInt(mMediaFile.getWidth());
        parcel.writeInt(mMediaFile.getHeight());
        parcel.writeInt(getStartPosition());
        parcel.writeInt(getEndPosition());
    }
}