package com.dcch.sharebiketest.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.dcch.sharebiketest.MainActivity;
import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.moudle.login.activity.LoginActivity;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.SPUtils;

import butterknife.BindView;


public class SplashActivity extends BaseActivity {

    @BindView(R.id.rl_splash_root)
    RelativeLayout mRlSplashRoot;

    private final static int SWITCH_LOGINACTIVITY = 1000;
    private final static int SWITCH_GUIDACTIVITY = 1001;


    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SWITCH_LOGINACTIVITY:
//                    goLogin();
                    switchPage();
                    break;
                case SWITCH_GUIDACTIVITY:
                    goGuide();
                    break;
            }
            super.handleMessage(msg);
        }
    };


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
        if (SPUtils.isFirst()) {
            handler.sendEmptyMessageDelayed(SWITCH_GUIDACTIVITY, 3000);
        } else {
            handler.sendEmptyMessageDelayed(SWITCH_LOGINACTIVITY, 3000);
        }
    }


    private void switchPage() {
        boolean isStartGuide = (boolean) SPUtils.get(SplashActivity.this, "isStartGuide", false);
        if (SPUtils.isLogin()) {
            LogUtils.e("已经登录...");
            goMain();
        } else {
            LogUtils.e("没有登录...");
            if (isStartGuide) {
                goLogin();
            } else {
                goGuide();
            }
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
        handler.removeCallbacksAndMessages(null);
        if (handler != null) {
            handler = null;
        }
    }
}
