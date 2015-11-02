package com.leanote.android.util.helper;

import android.content.Context;
import android.text.style.ImageSpan;

/**
 * Created by binnchx on 10/26/15.
 */
public class MediaGalleryImageSpan extends ImageSpan {
    private MediaGallery mMediaGallery;

    public MediaGalleryImageSpan(Context context, MediaGallery mediaGallery, int placeHolder) {
        super(context, placeHolder);
        setMediaGallery(mediaGallery);
    }

    public MediaGallery getMediaGallery() {
        return mMediaGallery;
    }

    public void setMediaGallery(MediaGallery mediaGallery) {
        this.mMediaGallery = mediaGallery;
    }
}
