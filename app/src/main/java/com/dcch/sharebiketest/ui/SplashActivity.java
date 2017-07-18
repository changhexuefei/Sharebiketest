package com.dcch.sharebiketest.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.dcch.sharebiketest.MainActivity;
import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.base.MessageEvent;
import com.dcch.sharebiketest.moudle.login.activity.LoginActivity;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.SPUtils;

import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

import butterknife.BindView;


public class SplashActivity extends BaseActivity {

    @BindView(R.id.rl_splash_root)
    RelativeLayout mRlSplashRoot;

    private final static int SWITCH_SWITCHPAGE = 1000;
    private final static int SWITCH_GUIDACTIVITY = 1001;


//    private Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            switch (msg.what) {
//                case SWITCH_SWITCHPAGE:
//                    switchPage();
//                    break;
//                case SWITCH_GUIDACTIVITY:
//                    goGuide();
//                    break;
//            }
//            super.handleMessage(msg);
//        }
//    };


    @Override
    protected int getLayoutId() {
        return R.layout.activity_splash;
    }

    @Override
    protected void initData() {
        //设置全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        AlphaAnimation animation = new AlphaAnimation(0, 1);
        animation.setDuration(1000);
        animation.setFillAfter(true);
        mRlSplashRoot.startAnimation(animation);
    }

    private void switchPage() {
        if (SPUtils.isLogin()) {
            goMain();
        } else {
            goLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void goGuide() {
        Intent intent = new Intent(SplashActivity.this, GuideActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    private void goLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    private void goMain() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
//        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        if (SPUtils.isFirst()) {
//            handler.sendEmptyMessageDelayed(SWITCH_GUIDACTIVITY, 2000);
            EventBus.getDefault().post(new MessageEvent(), "guide");
        } else {
//            handler.sendEmptyMessageDelayed(SWITCH_SWITCHPAGE, 2000);
            EventBus.getDefault().post(new MessageEvent(), "switch");
        }
    }


    @Subscriber(tag = "guide", mode = ThreadMode.POST)
    private void receiveMessageToGuide(MessageEvent info) {
        LogUtils.d("输入", info.toString() + "guide");
        goGuide();
    }

    //gotoMain
    @Subscriber(tag = "switch", mode = ThreadMode.POST)
    private void receiveMessageSwitch(MessageEvent info) {
        LogUtils.d("输入", info.toString() + "switch");
        switchPage();
    }


}
