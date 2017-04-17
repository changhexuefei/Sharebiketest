package com.dcch.sharebiketest.moudle.login.model;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public interface IUser {
    String getName();

    String getPasswd();

    int checkUserValidity(String name, String passwd);
}
