package com.leanote;

import android.app.Application;

import com.leanote.util.ImageHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * leanote application
 * <p/>
 * Created by jerrychoi on 2014-9-24.
 */
public class LeanoteApplication extends Application {

    private static final String TAG = LeanoteApplication.class.getSimpleName();

    private LeanoteApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        this.instance = this;
        ImageLoader.getInstance().init(ImageHelper.getDefaultImageLoaderConfiguration(this));
    }

    public LeanoteApplication getInstance() {
        return instance;
    }

}
