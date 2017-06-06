package com.dcch.sharebiketest.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;
import com.dcch.sharebiketest.http.HttpUtils;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public class MyApp extends Application {
    private List<Activity> activityList = new LinkedList();
    private static MyApp instance;
    private static Context mContext;
    private static RefWatcher sRefWatcher;

    public MyApp() {
    }

    //单例模式中获取唯一的Application实例
    public static MyApp getInstance() {
        if (null == instance) {
            instance = new MyApp();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //注意该方法要再setContentView方法之前实现
        mContext = getApplicationContext();
        SDKInitializer.initialize(mContext);
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        sRefWatcher = LeakCanary.install(this);

        //初始化OkHttp
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new LoggerInterceptor("HttpUtils"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        HttpUtils.init(okHttpClient);
    }
    public static Context getContext() {
        return mContext;
    }
}
