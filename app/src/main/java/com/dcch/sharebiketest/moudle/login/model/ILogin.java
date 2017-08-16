package com.dcch.sharebiketest.moudle.login.model;

/**
 * Created by gao on 2017/8/16.
 */

public interface ILogin {
    void login(String username, String password,LoginListener loginListener);

}
