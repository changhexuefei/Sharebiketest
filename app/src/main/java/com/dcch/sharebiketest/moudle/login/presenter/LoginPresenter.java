package com.dcch.sharebiketest.moudle.login.presenter;

import android.os.Handler;

import com.dcch.sharebiketest.moudle.login.model.ILogin;
import com.dcch.sharebiketest.moudle.login.model.Login;
import com.dcch.sharebiketest.moudle.login.model.LoginListener;
import com.dcch.sharebiketest.moudle.login.view.ILoginView;

/**
 * Created by gao on 2017/8/16.
 */

public class LoginPresenter {

    ILogin mILogin;
    ILoginView mILoginView;
    Handler handler = new Handler();

    public LoginPresenter(ILoginView ILoginView) {
        mILoginView = ILoginView;
        mILogin = new Login();
    }

    public void login() {
        mILogin.login(mILoginView.getUsername(), mILoginView.getPassword(), new LoginListener() {
            @Override
            public void loginSuccess() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mILoginView.loginSuccess();
                    }
                });
            }

            @Override
            public void loginFailed() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        mILoginView.loginFailed();
                    }
                });
            }
        });
    }

}
