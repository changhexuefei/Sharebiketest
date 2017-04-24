package com.dcch.sharebiketest.moudle.login.model;


import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.moudle.login.bean.User;
import com.dcch.sharebiketest.utils.JsonUtils;
import com.dcch.sharebiketest.utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;


public class UserModel implements IUser {
    @Override
    public String login(User user) {
        boolean networkError = false; //网络是否异常
        Map<String, String> map = new HashMap<>();
        map.put("username", user.getUsername());
        map.put("password", user.getPassword());
        OkHttpUtils.post().url(Api.BASE_URL + Api.REPAIRLOGIN).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showShort(MyApp.getContext(), "服务器正忙！");
            }

            @Override
            public void onResponse(String response, int id) {
                if (JsonUtils.isSuccess(response)) {
                    ToastUtils.showLong(MyApp.getContext(), "登录成功");

                } else {
                    ToastUtils.showLong(MyApp.getContext(), "用户名或者密码错误，请重新登录！");
                }
            }
        });

        return null;
    }
}
