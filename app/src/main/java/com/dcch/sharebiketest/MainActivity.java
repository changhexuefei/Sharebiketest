package com.dcch.sharebiketest;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteLine;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.libzxing.zxing.activity.CaptureActivity;
import com.dcch.sharebiketest.moudle.home.BikeInfo;
import com.dcch.sharebiketest.moudle.listener.MyOrientationListener;
import com.dcch.sharebiketest.moudle.login.activity.LoginActivity;
import com.dcch.sharebiketest.overlayutil.OverlayManager;
import com.dcch.sharebiketest.overlayutil.WalkingRouteOverlay;
import com.dcch.sharebiketest.utils.ClickUtils;
import com.dcch.sharebiketest.utils.JsonUtils;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.MapUtil;
import com.dcch.sharebiketest.utils.SPUtils;
import com.dcch.sharebiketest.utils.ToastUtils;
import com.dcch.sharebiketest.view.SelectPicPopupWindow;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;


@RuntimePermissions
public class MainActivity extends BaseActivity implements OnGetRoutePlanResultListener {
    @BindView(R.id.testMapView)
    MapView mTestMapView;
    @BindView(R.id.userCenter)
    ImageView mUserCenter;
    @BindView(R.id.scan)
    TextView mScan;
    @BindView(R.id.btn_my_location)
    ImageButton mBtnMyLocation;
    @BindView(R.id.all)
    RadioButton mAll;
    @BindView(R.id.exception)
    RadioButton mException;
    @BindView(R.id.trouble)
    RadioButton mTrouble;
    @BindView(R.id.subclauses)
    RadioGroup mSubclauses;
    @BindView(R.id.nav)
    NavigationView mNav;
    @BindView(R.id.activity_na)
    DrawerLayout mActivityNa;
    private BaiduMap mMap;
    private LocationClient mLocationClient;//定位的客户端
    private float mCurrentAccracy;//当前的精度
    private int mXDirection;//方向传感器X方向的值
    private MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//当前定位的模式
    private volatile boolean isFristLocation = true;//是否是第一次定位
    private MyOrientationListener myOrientationListener;//方向传感器的监听器
    private double mCurrentLantitude;//最新一次的经纬度
    private double mCurrentLongitude;
    public MyLocationListener mMyLocationListener;//定位的监听器
    private double changeLatitude;
    private List<BikeInfo> bikeInfos;
    private BikeInfo bikeInfo;
    Marker mMarker = null;
    private LatLng clickMarkLatlng;
    private boolean isFirst = true;
    private String bicycleNo;
    private PlanNode startNodeStr, endNodeStr;
    OverlayManager routeOverlay = null;//该类提供一个能够显示和管理多个Overlay的基类
    RoutePlanSearch mRPSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private SelectPicPopupWindow menuWindow = null; // 自定义弹出框
    boolean useDefaultIcon = false;
    private long mExitTime; //退出时间
    private String mToken;
    private String mUID;
    private ImageView mUserIcon;
    private TextView mUserName;


    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        MainActivityPermissionsDispatcher.initPermissionWithCheck(this);
        showCamera();
        initPermission();
        mMap = mTestMapView.getMap();
        View headerView = mNav.getHeaderView(0);//获取头布局
        //获得头像
        mUserIcon = (ImageView) headerView.findViewById(R.id.userIcon);
        //获得用户名
        mUserName = (TextView) headerView.findViewById(R.id.userName);
        mRPSearch = RoutePlanSearch.newInstance();
        mRPSearch.setOnGetRoutePlanResultListener(this);
        LogUtils.d("地图", mMap + "");
        bikeInfos = new ArrayList<>();
        mAll.setChecked(true);
        // 初始化定位
        initMyLocation();
        // 初始化传感器
        initOritationListener();//.LOCK_MODE_LOCKED_CLOSED
        mActivityNa.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);//关闭手势滑动，只通过点击按钮来滑动
        mNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    case R.id.credit:

                        break;
                    case R.id.tickling:

                        break;
                    case R.id.friend:

                        break;
                    case R.id.record:

                        break;
                    case R.id.exit:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("退出登录")
                                .setMessage("确定退出登录吗？")
                                .setNegativeButton("取消", null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        ToastUtils.showShort(MainActivity.this, "" + which);
                                        Intent i1 = new Intent(MainActivity.this, LoginActivity.class);
                                        startActivity(i1);
                                        SPUtils.clear(MyApp.getContext());
                                        SPUtils.put(MyApp.getContext(), "islogin", false);
                                        SPUtils.put(MyApp.getContext(), "isfirst", false);
                                        SPUtils.put(MyApp.getContext(), "isStartGuide", true);
                                        finish();
                                    }
                                }).create()
                                .show();
                        break;
                }
                item.setChecked(true);
                mActivityNa.closeDrawer(mNav);
                return true;
            }
        });
    }

    @Override
    protected void initListener() {
        super.initListener();
        mSubclauses.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i) {
                    case R.id.all:
                        mMap.clear();
                        getBikeInfo(mCurrentLantitude, mCurrentLongitude);
                        break;
                    case R.id.exception:
                        mMap.clear();
                        getExceptionBikeInfo(mCurrentLantitude, mCurrentLongitude);
                        break;
                    case R.id.trouble:
                        mMap.clear();
                        getTroubleBikeInfo(mCurrentLantitude, mCurrentLongitude);
                        break;
                }
            }
        });
    }

    private void initOritationListener() {
        myOrientationListener = new MyOrientationListener(getApplicationContext());
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mXDirection = (int) x;
                // 构造定位数据
                MyLocationData locData = new MyLocationData.Builder()
                        .accuracy(mCurrentAccracy)
                        // 此处设置开发者获取到的方向信息，顺时针0-360
                        .direction(mXDirection)
                        .latitude(mCurrentLantitude)
                        .longitude(mCurrentLongitude)
                        .build();
                // 设置定位数据
                mMap.setMyLocationData(locData);
//                 设置自定义图标
//                BitmapDescriptor mCurrentMarker =
//                        fromResource(R.mipmap.search_center_ic);
                MyLocationConfiguration config = new MyLocationConfiguration(
                        mCurrentMode, true, null);
                mMap.setMyLocationConfigeration(config);
            }
        });
    }

    private void initMyLocation() {
        // 定位初始化
        mLocationClient = new LocationClient(this);
        // 开启定位图层
        mMap.setMyLocationEnabled(true);
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);
        // 设置定位的相关配置
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);// 设置定位模式
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(5000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setIsNeedLocationDescribe(true);//可选，设置是否需要地址描述
        option.setNeedDeviceDirect(false);//可选，设置是否需要设备方向结果
        option.setLocationNotify(false);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        mLocationClient.setLocOption(option);
    }

    @OnClick({R.id.userCenter, R.id.scan, R.id.btn_my_location})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan:
                if (ClickUtils.isFastClick()) {
                    return;
                }
                MainActivityPermissionsDispatcher.showCameraWithCheck(MainActivity.this);
                Intent i1 = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(i1, 0);
                break;
            case R.id.btn_my_location:
                if (ClickUtils.isFastClick()) {
                    return;
                }
                if (routeOverlay != null)
                    routeOverlay.removeFromMap();
                if (menuWindow != null) {
                    menuWindow.dismiss();
                }
                setUserMapCenter(mCurrentLantitude, mCurrentLongitude);
                break;

            case R.id.userCenter:
                if (ClickUtils.isFastClick()) {
                    return;
                }
                if (mActivityNa.isDrawerOpen(mNav)) {
                    mActivityNa.closeDrawer(mNav);
                } else {
                    mActivityNa.openDrawer(mNav);
                }
                break;
        }
    }

    //扫一扫二维码时的回调
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            Bundle bundle = data.getExtras();
            switch (requestCode) {
                case 0:
                    if (bundle != null) {
                        String result = bundle.getString("result");
                        openScan(mUID, result, mToken);
                        ToastUtils.showLong(this, result);
                    }
                    break;
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    void showCamera() {
    }

    @NeedsPermission({Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    void initPermission() {

    }

    //扫码开锁的方法
    private void openScan(final String uID, final String result, final String mToken) {
        if (uID != null && !uID.equals("") && result != null && !result.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.clear();
            map.put("userId", uID);
            map.put("bicycleNo", result);
            map.put("token", mToken);
            LogUtils.d("开锁", result);
            LogUtils.d("开锁", uID);
            LogUtils.d("开锁", mToken);
            OkHttpUtils.post().url(Api.BASE_URL + Api.OPENSCAN).params(map).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    if (!e.equals("")) {
                        LogUtils.e(e.getMessage());
                    }
                    ToastUtils.showShort(MainActivity.this, "服务器忙，请稍后重试");
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("开锁", response);
                    ToastUtils.showShort(MainActivity.this, response);
                    if (JsonUtils.isSuccess(response)) {
                        LogUtils.d("开锁", "什么情况！");
                        ToastUtils.showShort(MainActivity.this, "开锁成功！");
                    } else {
                        ToastUtils.showShort(MainActivity.this, "开锁失败！");
                    }
                }
            });
        }
    }


    //设置中心点
    private void setUserMapCenter(Double Lantitude, Double Longitude) {
        LatLng ll = new LatLng(Lantitude, Longitude);
        MapStatus.Builder builder = new MapStatus.Builder();
        //地图缩放比设置为18
        builder.target(ll).zoom(18.0f);
        mMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }


    //实现定位回调监听
    private class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mTestMapView == null)
                return;
            // 构造定位数据
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(mXDirection).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mCurrentAccracy = location.getRadius();
            // 设置定位数据
            mMap.setMyLocationData(locData);
            mCurrentLantitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();
            LatLng currentLatLng = new LatLng(mCurrentLantitude, mCurrentLongitude);
            startNodeStr = PlanNode.withLocation(new LatLng(mCurrentLantitude, mCurrentLongitude));
//            BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
//                    .fromResource(R.mipmap.search_center_ic);
            //不设置bitmapDescriptor时代表默认使用百度地图图标
            MyLocationConfiguration config = new MyLocationConfiguration(
                    mCurrentMode, true, null);
            mMap.setMyLocationConfigeration(config);
            // 第一次定位时，将地图位置移动到当前位置
            if (isFristLocation) {
                isFristLocation = false;
//                mMap.clear();
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                //地图缩放比设置为18
                builder.target(ll).zoom(18.0f);
                mMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                changeLatitude = location.getLatitude();
                double changeLongitude = location.getLongitude();
                setUserMapCenter(mCurrentLantitude, mCurrentLongitude);
                //根据手机定位地点，得到车辆信息的方法
                getBikeInfo(mCurrentLantitude, mCurrentLongitude);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    //获得全部自行车信息的方法
    private void getBikeInfo(double Lantitude, double Longitude) {
        String lat = Lantitude + "";
        String lng = Longitude + "";
        if (!lat.equals("") && !lng.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.put("lng", lng);
            map.put("lat", lat);
            OkHttpUtils.post().url(Api.BASE_URL + Api.FINDBICYCLE).params(map).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    LogUtils.e(e.getMessage());
                    ToastUtils.showShort(MainActivity.this, "抱歉，服务器正忙！");
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("所有的数据", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        bikeInfos.clear();
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Log.d("自行车", jsonObject + "");
                                bikeInfo = new BikeInfo();
                                bikeInfo.setAddress(jsonObject.getString("address"));
                                bikeInfo.setBicycleId(jsonObject.getInt("bicycleId"));
                                bikeInfo.setBicycleNo(jsonObject.getInt("bicycleNo"));
                                bikeInfo.setLatitude(jsonObject.getString("latitude"));
                                bikeInfo.setLongitude(jsonObject.getString("longitude"));
                                bikeInfo.setUnitPrice(jsonObject.getInt("unitPrice"));
                                bikeInfo.setBicycleNo(jsonObject.getInt("bicycleNo"));
                                bikeInfos.add(bikeInfo);

                            }
                            addOverlay(bikeInfos);
                            LogUtils.d("数量", bikeInfos.size() + "1");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //获得异常自行车信息的方法
    private void getExceptionBikeInfo(double Lantitude, double Longitude) {
        String lat = Lantitude + "";
        String lng = Longitude + "";
        if (!lat.equals("") && !lng.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.put("lng", lng);
            map.put("lat", lat);
            OkHttpUtils.post().url(Api.BASE_URL + Api.FINDBICYCLEEXCEPTION).params(map).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    LogUtils.e(e.getMessage());
                    ToastUtils.showShort(MainActivity.this, "抱歉，服务器正忙！");
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("所有的数据", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        bikeInfos.clear();
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Log.d("自行车", jsonObject + "");
                                bikeInfo = new BikeInfo();
                                bikeInfo.setAddress(jsonObject.getString("address"));
                                bikeInfo.setBicycleId(jsonObject.getInt("bicycleId"));
                                bikeInfo.setBicycleNo(jsonObject.getInt("bicycleNo"));
                                bikeInfo.setLatitude(jsonObject.getString("latitude"));
                                bikeInfo.setLongitude(jsonObject.getString("longitude"));
                                bikeInfo.setUnitPrice(jsonObject.getInt("unitPrice"));
                                bikeInfo.setBicycleNo(jsonObject.getInt("bicycleNo"));
                                bikeInfos.add(bikeInfo);

                            }
                            addOverlay(bikeInfos);
                            LogUtils.d("数量", bikeInfos.size() + "2");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    //获得故障自行车信息的方法
    private void getTroubleBikeInfo(double Lantitude, double Longitude) {
        String lat = Lantitude + "";
        String lng = Longitude + "";
        if (!lat.equals("") && !lng.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.put("lng", lng);
            map.put("lat", lat);
            LogUtils.d("所有的数据", lng + "\n" + lat);
            OkHttpUtils.post().url(Api.BASE_URL + Api.FINDBICYCLETROUBLE).params(map).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    LogUtils.e(e.getMessage());
                    ToastUtils.showShort(MainActivity.this, "抱歉，服务器正忙！");
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("所有的数据", response);
                    try {
                        JSONArray jsonArray = new JSONArray(response);
                        bikeInfos.clear();
                        if (jsonArray.length() > 0) {
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Log.d("自行车", jsonObject + "");
                                bikeInfo = new BikeInfo();
                                bikeInfo.setAddress(jsonObject.getString("address"));
                                bikeInfo.setBicycleId(jsonObject.getInt("bicycleId"));
                                bikeInfo.setBicycleNo(jsonObject.getInt("bicycleNo"));
                                bikeInfo.setLatitude(jsonObject.getString("latitude"));
                                bikeInfo.setLongitude(jsonObject.getString("longitude"));
                                bikeInfo.setUnitPrice(jsonObject.getInt("unitPrice"));
                                bikeInfo.setBicycleNo(jsonObject.getInt("bicycleNo"));
                                bikeInfos.add(bikeInfo);
                            }
                            LogUtils.d("数量", bikeInfos.size() + "3");
                            addOverlay(bikeInfos);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }


    //百度地图添加覆盖物的方法
    private void addOverlay(List bikeInfos) {
        if (bikeInfos.size() > 0) {
            //清空地图
            mMap.clear();
            //创建marker的显示图标
            BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
            LatLng latLng;
            List<Double> doubles = new ArrayList<>();
            for (int i = 0; i < bikeInfos.size(); i++) {
                bikeInfo = (BikeInfo) bikeInfos.get(i);
                String lat = bikeInfo.getLatitude();
                String lng = bikeInfo.getLongitude();
                double lat1 = Double.parseDouble(lat);
                double lng1 = Double.parseDouble(lng);
                latLng = new LatLng(lat1, lng1);
//                //两点之间直线距离的算法
//                double distance1 = DistanceUtil.getDistance(latLng, currentLatLng);
//                doubles.add(distance1);
                //设置marker
                OverlayOptions options = new MarkerOptions()
                        .position(latLng)//设置位置
                        .icon(bitmap)//设置图标样式
                        .zIndex(i) // 设置marker所在层级
                        .draggable(true); // 设置手势拖拽;
                //添加marker
                mMarker = (Marker) mMap.addOverlay(options);
                //使用marker携带info信息，当点击事件的时候可以通过marker获得info信息
                Bundle bundle = new Bundle();
                // bikeInfo必须实现序列化接口
                bundle.putSerializable("bikeInfo", bikeInfo);
                mMarker.setExtraInfo(bundle);
            }

        } else {
            ToastUtils.showLong(this, "当前周围没有车辆");
        }
    }

    //百度地图的覆盖物点击方法
    private void clickBaiduMapMark() {
        final TreeSet<Integer> integers = new TreeSet<>();
        mMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                if (marker.getExtraInfo() != null && marker != null) {
                    int zIndex = marker.getZIndex();
                    integers.add(Integer.valueOf(zIndex));
                    LogUtils.d("覆盖物", zIndex + "\n" + integers.size());
                    Bundle bundle = marker.getExtraInfo();
                    clickMarkLatlng = marker.getPosition();
                    bikeInfo = (BikeInfo) bundle.getSerializable("bikeInfo");
                    if (bikeInfo != null) {
                        bicycleNo = bikeInfo.getBicycleNo() + "";
                        if (menuWindow == null || !menuWindow.isShowing()) {
                            showMenuWindow(bikeInfo);
                        }
                        updateBikeInfo(bikeInfo);
                    }
                }
                mMap.clear();
                addOverlay(bikeInfos);//
                return true;
            }

        });

    }

    private void updateBikeInfo(BikeInfo bikeInfo) {
        boolean hasPlanRoute = false;
        if (!hasPlanRoute) {
            this.bikeInfo = bikeInfo;
            Double doulat = Double.valueOf(bikeInfo.getLatitude());
            Double doulon = Double.valueOf(bikeInfo.getLongitude());
            endNodeStr = PlanNode.withLocation(new LatLng(doulat, doulon));
            drawPlanRoute(endNodeStr);
        }
    }

    private void showMenuWindow(BikeInfo bikeInfo) {
        if (menuWindow == null) {
            menuWindow = new SelectPicPopupWindow(MainActivity.this, bikeInfo);
        }
        menuWindow.setFocusable(false);
        menuWindow.setOutsideTouchable(false);
        menuWindow.showAsDropDown(findViewById(R.id.top));
    }

    private void drawPlanRoute(PlanNode endNodeStr) {
        if (routeOverlay != null)
            routeOverlay.removeFromMap();
        if (endNodeStr != null) {
            Log.d("gao", "changeLatitude-----startNode--------" + startNodeStr.getLocation().latitude);
            Log.d("gao", "changeLongitude-----startNode--------" + startNodeStr.getLocation().longitude);
            Log.d("gao", "changeLatitude-----startNode--------" + endNodeStr.getLocation().latitude);
            Log.d("gao", "changeLongitude-----startNode--------" + endNodeStr.getLocation().longitude);
            mRPSearch.walkingSearch((new WalkingRoutePlanOption()).from(startNodeStr).to(endNodeStr));
        }
    }

    @Override
    protected void onStart() {
        // 开启图层定位
        mMap.setMyLocationEnabled(true);
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
        // 开启方向传感器
        myOrientationListener.start();
        super.onStart();
        Log.d("实验", "onStart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mTestMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mTestMapView.onResume();
        if (SPUtils.isLogin()) {
            String userDetail = (String) SPUtils.get(MyApp.getContext(), "userDetail", "");
            JSONObject object;
            try {
                object = new JSONObject(userDetail);
                int id = object.optInt("id");
                mToken = object.optString("token");
                mUID = String.valueOf(id);
                Log.d("实验", "onResume");
                LogUtils.d("实验", mToken + "\n" + mUID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        clickBaiduMapMark();
    }


    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mTestMapView.onPause();
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {
        if (walkingRouteResult == null || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MainActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        }
        if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//                    result.getSuggestAddrInfo();
            return;
        }

        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            WalkingRouteLine walkingRouteLine = walkingRouteResult.getRouteLines().get(0);
            int distance = walkingRouteLine.getDistance();
            int walkTime = walkingRouteLine.getDuration() / 60;
            LogUtils.d("gao", distance + "\n" + walkTime + "\n" + walkingRouteLine);
//            mWalkTime = distance / 60;
            String distance1 = MapUtil.distanceFormatter(distance);
            String castTime = String.valueOf(walkTime);
            if (!distance1.equals("")) {
                menuWindow.mDistance.setText(distance1);
            }
            if (!castTime.equals("")) {
                menuWindow.mArrivalTime.setText(castTime + "分钟");
            }

        }
        WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mMap);
//        /**
//         * 设置地图 Marker 覆盖物点击事件监听者
//         * 需要实现的方法：     onMarkerClick(Marker marker)
//         * */
        mMap.setOnMarkerClickListener(overlay);
        routeOverlay = overlay;

        if (!overlay.equals("")) {
//            /**
//             * public void setData(WalkingRouteLine line)设置路线数据。
//             * 参数:line - 路线数据
//             * */
            overlay.setData(walkingRouteResult.getRouteLines().get(0));
//            /**
//             * public final void addToMap()将所有Overlay 添加到地图上
//             * */
            overlay.addToMap();
//            /**
//             * public void zoomToSpan()
//             * 缩放地图，使所有Overlay都在合适的视野内
//             * 注： 该方法只对Marker类型的overlay有效
//             * */
            overlay.zoomToSpan();
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult massTransitRouteResult) {

    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {

    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

    }

    //WalkingRouteOverlay已经实现了BaiduMap.OnMarkerClickListener接口
    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {
        MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        //返回:起点图标
        @Override
        public BitmapDescriptor getStartMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.unchecked);
            }
            return null;
        }

        //返回:终点图标
        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (useDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.unchecked);
            }
            return null;
        }
    }

    //点击手机上的返回键退出App的方法
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        //按下的如果是BACK键，同时没有重复
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            if ((System.currentTimeMillis() - mExitTime) > 2000) {
                Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                mExitTime = System.currentTimeMillis();
            } else {
                MyApp.getInstance().exit();
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


}
