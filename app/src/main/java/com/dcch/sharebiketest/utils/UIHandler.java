package com.dcch.sharebiketest.utils;

import android.os.Handler;
import android.os.Looper;

/**
 * 在UI线程进行处理
 * <p/>
 */
public class UIHandler extends Handler {
    private UIHandler() {
        super(Looper.getMainLooper());
    }

    private static UIHandler sInstance;

    public static UIHandler get() {
        if (sInstance == null) {
            sInstance = new UIHandler();
        }
        return sInstance;
    }
}
