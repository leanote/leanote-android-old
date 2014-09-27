package com.leanote.util;

import android.content.Context;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by jerrychoi on 2014-9-26.
 */
public class ImageHelper {

    /**
     * Get default DisplayImageOptions
     *
     * @return
     */
    private static DisplayImageOptions getDefaultDisplayImageOptions() {
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cacheInMemory(true).cacheOnDisk(true);

        return builder.build();
    }

    /**
     * Get default ImageLoaderConfiguration
     *
     * @param context
     * @return
     */
    public static ImageLoaderConfiguration getDefaultImageLoaderConfiguration(Context context) {
        ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context);
        builder.defaultDisplayImageOptions(getDefaultDisplayImageOptions());
        builder.denyCacheImageMultipleSizesInMemory();

        return builder.build();
    }

}
