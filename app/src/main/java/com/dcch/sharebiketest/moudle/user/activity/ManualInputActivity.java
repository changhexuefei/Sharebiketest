package com.dcch.sharebiketest.moudle.user.activity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.dcch.sharebiketest.MainActivity;
import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.base.CodeEvent;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.utils.DensityUtils;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.ToastUtils;
import com.dcch.sharebiketest.view.CodeInputEditText;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;


public class ManualInputActivity extends BaseActivity {

    @BindView(R.id.back)
    ImageView mBack;
    @BindView(R.id.inputFlashLight)
    ToggleButton mOpenFlashLight;
    @BindView(R.id.manualInputArea)
    CodeInputEditText mManualInputArea;
    @BindView(R.id.ensure)
    Button mEnsure;
    private String bikeNo = "";
    private String mTag;
    private Camera camera = null;
    private Camera.Parameters parameters = null;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_manual_input;
    }

    @Override
    protected void initData() {
        Intent intent = getIntent();
        if (intent != null) {
            mTag = intent.getStringExtra("tag");
            LogUtils.d("空的", mTag);
        }

    }

    //将TextView中的图片转化为规定大小的方法
    public void initDrawable(TextView v) {
        Drawable drawable = v.getCompoundDrawables()[1];
        drawable.setBounds(0, 0, DensityUtils.dp2px(ManualInputActivity.this, 50), DensityUtils.dp2px(ManualInputActivity.this, 50));
        v.setCompoundDrawables(null, drawable, null, null);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initDrawable(mOpenFlashLight);
        mManualInputArea.initStyle(R.drawable.edit_num_bg, 9, 0.5f, R.color.colorTitle, R.color.lineColor, 15);
        mOpenFlashLight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    camera = Camera.open();
                    parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 开启
                    camera.setParameters(parameters);
                    camera.startPreview();
                } else {
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);// 关闭
                    camera.setParameters(parameters);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mManualInputArea.setOnTextFinishListener(new CodeInputEditText.OnTextFinishListener() {
            @Override
            public void onFinish(String str) {
                bikeNo = str;
                mEnsure.setEnabled(true);
                mEnsure.setBackgroundColor(Color.parseColor("#F8941D"));
            }
        });

    }

    @OnClick({R.id.back, R.id.ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back:
                finish();
                break;
            case R.id.ensure:
                Map<String, String> map = new HashMap<>();
                map.put("lockremark", bikeNo);
                OkHttpUtils.post().url(Api.BASE_URL + Api.CHECKBICYCLENO).params(map).build().execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        ToastUtils.showShort(ManualInputActivity.this, "服务器正忙，请稍后再试！");
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        LogUtils.d("锁号", response);
                        //{"resultStatus":"0"}
                        try {
                            JSONObject object = new JSONObject(response);
                            String resultStatus = object.optString("resultStatus");
                            switch (resultStatus) {
                                case "1":
                                    if (mTag.equals("main")) {
                                        Intent bikeNoIntent = new Intent(ManualInputActivity.this, MainActivity.class);
                                        startActivity(bikeNoIntent);
                                        EventBus.getDefault().post(new CodeEvent(bikeNo), "bikeNo");
                                    }
                                    finish();
                                    break;
                                case "0":
                                    clearTextView();
                                    ToastUtils.showLong(ManualInputActivity.this, "车辆编号不存在。");


                                    break;
                                case "2":
                                    clearTextView();
                                    ToastUtils.showLong(ManualInputActivity.this, "您的账号在其他设备上登录，您被迫下线！");

                                    break;
                                case "3":
                                    clearTextView();
                                    ToastUtils.showLong(ManualInputActivity.this, "正在被使用");
                                    break;
                                case "4":
                                    clearTextView();
                                    ToastUtils.showLong(ManualInputActivity.this, "我是故障车");
                                    break;

                                case "5":
                                    clearTextView();
                                    ToastUtils.showLong(ManualInputActivity.this, "我已经被预约");
                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
                break;
        }
    }

    private void clearTextView() {
        mManualInputArea.clearText();
        mEnsure.setEnabled(false);
        mEnsure.setBackgroundColor(getColor(R.color.input_btn_color));
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
