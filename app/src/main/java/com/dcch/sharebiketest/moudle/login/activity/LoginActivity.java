package com.dcch.sharebiketest.moudle.login.activity;

import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dcch.sharebiketest.MainActivity;
import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.utils.ClickUtils;
import com.dcch.sharebiketest.utils.JsonUtils;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.NetUtils;
import com.dcch.sharebiketest.utils.SPUtils;
import com.dcch.sharebiketest.utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

/**
 * Created by Administrator on 2017/4/28 0028.
 */

public class LoginActivity extends BaseActivity {
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
        String userName = mUserName.getText().toString();
        String pwd = mPwd.getText().toString();
        if (TextUtils.isEmpty(userName) || TextUtils.isEmpty(pwd)) {
            Toast.makeText(getApplicationContext(), "账号或密码不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (NetUtils.isConnected(MyApp.getContext())) {
            login(userName, pwd);
        }

    }

    private void login(String userName, String pwd) {
        Map<String, String> map = new HashMap<>();
        map.put("username", userName);
        map.put("password", pwd);
        OkHttpUtils.post().url(Api.BASE_URL + Api.REPAIRLOGIN).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showShort(LoginActivity.this, "服务器正忙，请稍后再试");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d("登录结果", response);
                if (JsonUtils.isSuccess(response)) {
                    ToastUtils.showShort(LoginActivity.this, "登录成功");
                    SPUtils.put(LoginActivity.this, "islogin", true);
                    SPUtils.put(LoginActivity.this, "userDetail", response);
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    LoginActivity.this.finish();
                } else {
                    ToastUtils.showShort(LoginActivity.this, "用户名或者密码错误！");
                }
            }
        });
    }

}
