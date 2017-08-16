package com.dcch.sharebiketest.moudle.login.activity;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dcch.sharebiketest.MainActivity;
import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.moudle.login.presenter.LoginPresenter;
import com.dcch.sharebiketest.moudle.login.view.ILoginView;
import com.dcch.sharebiketest.utils.ClickUtils;
import com.dcch.sharebiketest.utils.NetUtils;
import com.dcch.sharebiketest.utils.ToastUtils;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Administrator on 2017/4/28 0028.
 */

public class LoginActivity extends BaseActivity implements ILoginView {
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.userName)
    EditText mUserName;
    @BindView(R.id.pwd)
    EditText mPwd;
    @BindView(R.id.login_confirm)
    Button mLoginConfirm;
    LoginPresenter loginPresenter = new LoginPresenter(this);

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        mToolbar.setTitle("");
        mTitle.setText(getResources().getString(R.string.user_login));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @OnClick(R.id.login_confirm)
    public void onViewClicked() {
        if (ClickUtils.isFastClick()) {
            return;
        }

        if (NetUtils.isConnected(MyApp.getContext())) {
            loginPresenter.login();
        } else {
            ToastUtils.showShort(this, "当前无网络连接，请检查网络后登录！");
        }

    }

    @Override
    public String getUsername() {
        if (TextUtils.isEmpty(mUserName.getText().toString().trim())) {
            ToastUtils.showShort(this,"账号不能为空");
        }
        return mUserName.getText().toString().trim();
    }

    @Override
    public String getPassword() {
        if (TextUtils.isEmpty(mPwd.getText().toString().trim())) {
            ToastUtils.showShort(this,"密码不能为空");
        }
        return mPwd.getText().toString().trim();
    }

    @Override
    public void loginSuccess() {
        ToastUtils.showShort(this, "登录成功");
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        LoginActivity.this.finish();
    }

    @Override
    public void loginFailed() {
        ToastUtils.showShort(this, "用户名或者密码错误！");
    }

}
