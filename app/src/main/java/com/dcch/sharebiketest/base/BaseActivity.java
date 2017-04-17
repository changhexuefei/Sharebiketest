package com.dcch.sharebiketest.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

import com.dcch.sharebiketest.app.MyApp;

import butterknife.ButterKnife;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutId());
        ButterKnife.bind(this);
        MyApp.getInstance().addActivity(this);
        initData();
        initListener();
    }

    protected void initListener() {
    }

    protected abstract int getLayoutId();

    protected abstract void initData();

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        App.getInstance().exit();
    }

    @Override
    protected void onResume() {
        super.onResume();
//
    }

}
