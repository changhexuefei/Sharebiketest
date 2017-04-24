package com.dcch.sharebiketest.moudle.login.model;


import com.dcch.sharebiketest.moudle.login.bean.User;

public interface IUser {
    /**
     * 登录
     *
     * @param user
     * @return 约定返回"true"为登录成功，其他为登录失败的错误信息
     */
    String login(User user);
}
