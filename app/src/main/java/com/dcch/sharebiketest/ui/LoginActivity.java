package com.dcch.sharebiketest.ui;

import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.moudle.login.presenter.ILoginPresenter;
import com.dcch.sharebiketest.moudle.login.presenter.LoginPresenterCompl;
import com.dcch.sharebiketest.moudle.login.view.ILoginView;

import butterknife.BindView;
import butterknife.OnClick;

public class LoginActivity extends BaseActivity implements ILoginView {
    ILoginPresenter loginPresenter;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.userPhone)
    EditText mUserPhone;
    @BindView(R.id.pwd)
    EditText mPwd;
    @BindView(R.id.login_confirm)
    TextView mLoginConfirm;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_login;
    }

    @Override
    protected void initData() {
        mToolbar.setTitle("");
        mTitle.setText(getResources().getString(R.string.phone_verification));
        setSupportActionBar(mToolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                EventBus.getDefault().post(new MessageEvent(), "show");
                finish();
            }
        });
        loginPresenter = new LoginPresenterCompl(this);
    }

    @OnClick(R.id.login_confirm)
    public void onViewClicked() {
        mLoginConfirm.setEnabled(false);
        loginPresenter.doLogin(mUserPhone.getText().toString(), mPwd.getText().toString());
    }

    @Override
    public void onClearText() {
        mUserPhone.setText("");
        mPwd.setText("");
    }

    @Override
    public void onLoginResult(Boolean result, int code) {
        mLoginConfirm.setEnabled(true);
        if (result) {
            Toast.makeText(this, "登录成功", Toast.LENGTH_SHORT).show();
        } else
            Toast.makeText(this, "登录失败, code = " + code, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSetProgressBarVisibility(int visibility) {

    }
}
