package com.dcch.sharebiketest.moudle.login.model;

import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.utils.JsonUtils;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.SPUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by gao on 2017/8/16.
 */

public class Login implements ILogin {

    @Override
    public void login(String username, String password, final LoginListener loginListener) {
        Map<String, String> map = new HashMap<>();
        map.put("username", username);
        map.put("password", password);
        OkHttpUtils.post().url(Api.BASE_URL + Api.REPAIRLOGIN).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d("登录结果", response);
                if (JsonUtils.isSuccess(response)) {
                    try {
                        JSONObject object = new JSONObject(response);
                        SPUtils.put(MyApp.getContext(), "islogin", true);
                        SPUtils.put(MyApp.getContext(), "token", object.optString("token"));
                        SPUtils.put(MyApp.getContext(), "id", object.optString("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    loginListener.loginSuccess();
                } else {
                    loginListener.loginFailed();
                }
            }
        });
    }
}
