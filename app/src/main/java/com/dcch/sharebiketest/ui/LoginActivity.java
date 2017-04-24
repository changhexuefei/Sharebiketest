package com.dcch.sharebiketest.ui;

import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.moudle.login.bean.User;
import com.dcch.sharebiketest.moudle.login.presenter.LoginPresenter;
import com.dcch.sharebiketest.moudle.login.view.ILoginView;

import butterknife.BindView;
import butterknife.OnClick;

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
    TextView mLoginConfirm;
    private LoginPresenter mLoginPresenter;

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
        mLoginPresenter=new LoginPresenter(this);
    }

    @OnClick(R.id.login_confirm)
    public void onViewClicked() {
        mLoginConfirm.setEnabled(false);
        String userName = mUserName.getText().toString();
        String pwd = mPwd.getText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(getApplicationContext(), "账号或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }

        mLoginPresenter.login(new User(userName, pwd));

    }

    @Override
    public void onLoginSuccess() {
        Toast.makeText(getApplicationContext(), "登录成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLoginFailed(String error) {
        Toast.makeText(getApplicationContext(), "登录失败:" + error, Toast.LENGTH_SHORT).show();
    }
}
