package com.dcch.sharebiketest.moudle.login.view;

/**
 * Created by gao on 2017/8/16.
 */

public interface ILoginView {
    String getUsername();
    String getPassword();
    void loginSuccess();
    void loginFailed();
}
