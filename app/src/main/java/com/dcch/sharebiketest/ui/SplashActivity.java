package com.dcch.sharebiketest.ui;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;

import com.dcch.sharebiketest.MainActivity;
import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.base.BaseActivity;
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
                    goLogin();
//                    switchPage();
                    break;
                case SWITCH_GUIDACTIVITY:
                    goGuide();
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void goLogin() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        SplashActivity.this.startActivity(intent);
        SplashActivity.this.finish();
    }

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
        boolean isStartGuide = (boolean) SPUtils.get(MyApp.getContext(), "isStartGuide", false);
        if (SPUtils.isLogin()) {
            LogUtils.e("已经登录...");
            Intent login = new Intent(MyApp.getContext(), MainActivity.class);
            startActivity(login);
            SplashActivity.this.finish();
        } else {
            LogUtils.e("没有登录...");
            if (isStartGuide) {
                startActivity(new Intent(MyApp.getContext(), MainActivity.class));
                finish();
            } else {
                startActivity(new Intent(MyApp.getContext(), GuideActivity.class));
                finish();
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

}
