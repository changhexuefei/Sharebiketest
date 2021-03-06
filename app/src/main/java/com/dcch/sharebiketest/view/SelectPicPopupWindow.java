package com.dcch.sharebiketest.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.moudle.home.BikeInfo;


/**
 * Created by Administrator on 2016/6/16 0016.
 */
public class SelectPicPopupWindow extends PopupWindow {

    private BikeInfo mBikeInfo;
    TextView mBikeLocationInfo;
    TextView mUnitPrice;
    public TextView mDistance;
    public TextView mArrivalTime;
    private View mMenuView;

    public SelectPicPopupWindow(Context context, BikeInfo mBikeInfo) {
        super(context);
        this.mBikeInfo = mBikeInfo;

        //创建布局反射器
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //加载布局
        mMenuView = inflater.inflate(R.layout.item_popwindow, null);

        //初始化控件
        mBikeLocationInfo = (TextView) mMenuView.findViewById(R.id.bike_location_info);
        mUnitPrice = (TextView) mMenuView.findViewById(R.id.unitPrice);
        mDistance = (TextView) mMenuView.findViewById(R.id.distance);
        mArrivalTime = (TextView) mMenuView.findViewById(R.id.arrivalTime);

        //为控件赋值
        if (mBikeInfo != null && !mBikeInfo.equals("")) {
            String address = mBikeInfo.getAddress();
            if (address != null && !address.equals("")) {
                mBikeLocationInfo.setText(mBikeInfo.getAddress());
            } else {
                mBikeLocationInfo.setText("未知地址");
            }
            mUnitPrice.setText(String.valueOf(mBikeInfo.getUnitPrice()) + "元");
        }

        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWinow弹出窗体可点击
        this.setFocusable(false);
        // 设置SelectPicPopupWindow弹出窗体动画效果
//        this.setAnimationStyle(R.style.PopupAnimation);
//         实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
//         设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

}
