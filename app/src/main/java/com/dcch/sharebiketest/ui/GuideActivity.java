package com.dcch.sharebiketest.ui;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dcch.sharebiketest.R;
import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.moudle.login.activity.LoginActivity;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.SPUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;


public class GuideActivity extends BaseActivity {

    @BindView(R.id.viewpager_guide)
    ViewPager viewpager_guide;
    @BindView(R.id.btn_start_main)
    Button btn_start_main;
    @BindView(R.id.linearlayout_dot)
    LinearLayout mLinearlayoutDot;
    private boolean istete = false;
    private ArrayList imageViews = new ArrayList();
    private List<ImageView> mDots;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_guide;
    }

    @Override
    protected void initData() {
        //设置全屏显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        int[] ids = {R.drawable.qilinone, R.drawable.qilintwo, R.drawable.qilinthere,
                R.drawable.qilinfour};
        imageViews = new ArrayList();
        for (int i = 0; i < ids.length; i++) {
            ImageView imageView = new ImageView(this);
            imageView.setBackgroundResource(ids[i]);//一定要设置为背景
            //加入到集合中
            imageViews.add(imageView);
        }

        //通过循环动态的添加点。
        mDots = new ArrayList<ImageView>();
        for (int i = 0; i < imageViews.size(); i++) {
            ImageView imageView = new ImageView(this);
            int width = Dp2Px(this, 6);
            int heigth = Dp2Px(this, 6);
            int margin = Dp2Px(this, 5);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, heigth);
            params.setMargins(margin, margin, margin, margin);//设置margin,也就是外边距。
            imageView.setLayoutParams(params);//传入参数params设置宽和高
            imageView.setImageResource(R.drawable.dot_normal);//设置图片
            mLinearlayoutDot.addView(imageView);//将图片添加到布局中
            //将dot添加到dots集合中
            mDots.add(imageView);
        }
        mDots.get(0).setImageResource(R.drawable.dot_focus);//设置启动后显示的第一个点
        btn_start_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GuideActivity.this, LoginActivity.class);
                startActivity(intent);
                SPUtils.put(MyApp.getContext(), "isStartGuide", true);
                SPUtils.put(MyApp.getContext(), "isfirst", false);
                LogUtils.d("kankan", SPUtils.get(MyApp.getContext(), "isfirst", false) + "");
                finish();
            }
        });

    }


    @Override
    protected void initListener() {
        super.initListener();
        viewpager_guide.setAdapter(new MyPagerAdapter());
        viewpager_guide.addOnPageChangeListener(new MyOnPageChangeListener());

    }


    class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return imageViews.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        /**
         * @param container ViewPager
         * @param position  当前添加到哪个页面
         * @return
         */
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = (ImageView) imageViews.get(position);
            container.addView(imageView);// 把页面添加到ViewPager中
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }


    class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {

        /**
         * 当页面滚动了的时候回调这个方法
         *
         * @param position             当前滚动页面的位置
         * @param positionOffset       当前页面滑动的百分比
         * @param positionOffsetPixels 当前页面滑动了多少像数
         */
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            if (positionOffset != 0) {
                istete = true;
            } else {
                istete = false;
            }
        }

        @Override
        public void onPageSelected(int position) {
            //for-each循环将所有的dot设置为dot_normal
            for (ImageView imageView : mDots) {
                imageView.setImageResource(R.drawable.dot_normal);
            }
            //设置当前显示的页面的dot设置为dot_focused
            mDots.get(position).setImageResource(R.drawable.dot_focus);
            if (position == imageViews.size() - 1 && istete) {// 最后一个页面才显示
                // 显示按钮
                btn_start_main.setVisibility(View.VISIBLE);
            } else {
                // 隐藏按钮
                btn_start_main.setVisibility(View.GONE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    }

    /*
       将dp转化为px
        */
    public int Dp2Px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
