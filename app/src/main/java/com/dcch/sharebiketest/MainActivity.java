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
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.baidu.mapapi.map.MapPoi;
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
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
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
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.DistanceUtil;
import com.dcch.sharebiketest.app.MyApp;
import com.dcch.sharebiketest.base.AppManager;
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.base.CodeEvent;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.libzxing.zxing.activity.CaptureActivity;
import com.dcch.sharebiketest.moudle.home.BikeInfo;
import com.dcch.sharebiketest.moudle.home.QueryBikeInfo;
import com.dcch.sharebiketest.moudle.listener.MyOrientationListener;
import com.dcch.sharebiketest.moudle.login.activity.LoginActivity;
import com.dcch.sharebiketest.overlayutil.OverlayManager;
import com.dcch.sharebiketest.overlayutil.WalkingRouteOverlay;
import com.dcch.sharebiketest.utils.ClickUtils;
import com.dcch.sharebiketest.utils.JsonUtils;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.MapUtil;
import com.dcch.sharebiketest.utils.NetUtils;
import com.dcch.sharebiketest.utils.SPUtils;
import com.dcch.sharebiketest.utils.ToastUtils;
import com.google.gson.Gson;
import com.hss01248.dialog.StyledDialog;
import com.hss01248.dialog.interfaces.MyDialogListener;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.simple.eventbus.EventBus;
import org.simple.eventbus.Subscriber;
import org.simple.eventbus.ThreadMode;

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


@SuppressWarnings("ALL")
@RuntimePermissions
public class MainActivity extends BaseActivity implements OnGetRoutePlanResultListener, OnGetGeoCoderResultListener, BaiduMap.OnMapStatusChangeListener {
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
    @BindView(R.id.current_addr)
    TextView mCurrentAddr;
    @BindView(R.id.unitPrice)
    TextView mUnitPrice;
    @BindView(R.id.distance)
    TextView mDistance;
    @BindView(R.id.arrivalTime)
    TextView mArrivalTime;
    @BindView(R.id.bike_layout)
    LinearLayout mBikeLayout;
    @BindView(R.id.bike_number)
    TextView mBikeNumber;
    @BindView(R.id.electric_quantity)
    TextView mElectricQuantity;
    @BindView(R.id.centerIcon)
    ImageView mCenterIcon;
    @BindView(R.id.lookingFor)
    TextView mLookingFor;
    private BaiduMap mMap;
    private LocationClient mLocationClient;//定位的客户端
    private float mCurrentAccracy;//当前的精度
    private int mXDirection;//方向传感器X方向的值
    private final MyLocationConfiguration.LocationMode mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;//当前定位的模式
    private volatile boolean isFristLocation = true;//是否是第一次定位
    private MyOrientationListener myOrientationListener;//方向传感器的监听器
    private double mCurrentLantitude;//最新一次的经纬度
    private double mCurrentLongitude;
    private MyLocationListener mMyLocationListener;//定位的监听器
    private List<BikeInfo> bikeInfos;
    private BikeInfo bikeInfo;
    private Marker mMarker = null;
    private PlanNode startNodeStr;
    private OverlayManager routeOverlay = null;//该类提供一个能够显示和管理多个Overlay的基类
    private RoutePlanSearch mRPSearch = null;    // 搜索模块，也可去掉地图模块独立使用
    private final boolean useDefaultIcon = false;
    private long mExitTime; //退出时间
    private String mToken;
    private String mUID;
    private String result;
    private GeoCoder mSearch = null;//地理编码
    private LatLng mMCenterLatLng;
    private double mChangeLatitude;
    private double mChangeLongitude;
    private boolean isShowBikeInfo = false;
    private String mBikeNo;

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
        ImageView userIcon = (ImageView) headerView.findViewById(R.id.userIcon);
        //获得用户名
        TextView userName = (TextView) headerView.findViewById(R.id.userName);
        mRPSearch = RoutePlanSearch.newInstance();
        mRPSearch.setOnGetRoutePlanResultListener(this);
        mSearch = GeoCoder.newInstance();
        mSearch.setOnGetGeoCodeResultListener(this);
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

                switch (item.getItemId()) {
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
//                                        SPUtils.put(MyApp.getContext(), "isStartGuide", true);
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
        mMap.setOnMapStatusChangeListener(this);
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
        clickBaiduMapMark();
        clickBaiduMap();
    }

    @OnClick({R.id.userCenter, R.id.scan, R.id.btn_my_location, R.id.lookingFor})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.scan:
                if (ClickUtils.isFastClick()) {
                    return;
                }
                MainActivityPermissionsDispatcher.showCameraWithCheck(MainActivity.this);
                Intent i1 = new Intent(MainActivity.this, CaptureActivity.class);
                i1.putExtra("msg", "main");
                startActivity(i1);
                break;
            case R.id.btn_my_location:
                if (ClickUtils.isFastClick()) {
                    return;
                }
                if (routeOverlay != null) {
                    routeOverlay.removeFromMap();
                    mBikeLayout.setVisibility(View.GONE);
                    mCenterIcon.setVisibility(View.VISIBLE);
                    isShowBikeInfo = false;
                    mSubclauses.setVisibility(View.VISIBLE);
                    mMap.clear();
                    if (mAll.isChecked()) {
                        getBikeInfo(mCurrentLantitude, mCurrentLongitude);
                    } else if (mTrouble.isChecked()) {
                        getTroubleBikeInfo(mCurrentLantitude, mCurrentLongitude);
                    } else if (mException.isChecked()) {
                        getExceptionBikeInfo(mCurrentLantitude, mCurrentLongitude);
                    }
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

            case R.id.lookingFor:
                if (ClickUtils.isFastClick()) {
                    return;
                }
                StyledDialog.buildNormalInput(MainActivity.this, "寻车", "请输入车辆编号", "", "确定", "取消", new MyDialogListener() {
                    @Override
                    public void onFirst() {

                    }

                    @Override
                    public void onSecond() {
                    }

                    @Override
                    public void onGetInput(CharSequence input1, CharSequence input2) {
                        super.onGetInput(input1, input2);
                        if (input1 != null && !input1.equals("")) {
                            mBikeNo = input1.toString();
                            queryBikeNo(mBikeNo);
                        }
                    }
                }).show();
                break;
        }
    }

    private void queryBikeNo(String bikeNo) {
        Map<String, String> map = new HashMap<>();
        map.put("bicycleNo", bikeNo);
        OkHttpUtils.post().url(Api.BASE_URL + Api.SEARCHBICYCLE).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {

            }

            @Override
            public void onResponse(String response, int id) {
                if (JsonUtils.isSuccess(response)) {
                    LogUtils.d("信息", response);
                    Gson gson = new Gson();
                    QueryBikeInfo queryBikeInfo = gson.fromJson(response, QueryBikeInfo.class);
                    mSubclauses.setVisibility(View.GONE);
                    mBikeLayout.setVisibility(View.VISIBLE);
                    mCenterIcon.setVisibility(View.GONE);
                    mMap.clear();
                    isShowBikeInfo = true;
                    mElectricQuantity.setText(String.valueOf(queryBikeInfo.getBicycle().getElectricity()) + "%");
                    mBikeNumber.setText(queryBikeInfo.getBicycle().getBicycleNo());
                    if (!queryBikeInfo.getBicycle().getLatitude().equals("") && !queryBikeInfo.getBicycle().getLongitude().equals("")) {
                        forLocationAddMark(Double.valueOf(queryBikeInfo.getBicycle().getLatitude()), Double.valueOf(queryBikeInfo.getBicycle().getLongitude()));
                        reverseGeoCoder(transform(Double.valueOf(queryBikeInfo.getBicycle().getLatitude()), Double.valueOf(queryBikeInfo.getBicycle().getLongitude())));
                        PlanNode endNodeStr = PlanNode.withLocation(transform(Double.valueOf(queryBikeInfo.getBicycle().getLatitude()), Double.valueOf(queryBikeInfo.getBicycle().getLongitude())));
                        drawPlanRoute(endNodeStr);
                    }
                } else {


                }
            }
        });
    }

    private void CheckRepairBicycleNo(final String result) {
        Map<String, String> map = new HashMap<>();
        map.put("lockremark", result);
        OkHttpUtils.post().url(Api.BASE_URL + Api.CHECKREPAIRBICYCLENO).params(map).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showShort(MainActivity.this, "服务器正忙，请稍后再试！");
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.d("检查", response);
                try {
                    JSONObject object = new JSONObject(response);
                    String resultStatus = object.optString("resultStatus");
                    switch (resultStatus) {
                        case "0":
                            ToastUtils.showLong(MyApp.getContext(), "车辆编号有误");
                            break;
                        case "1":
                            openScan(mUID, result, mToken);
                            break;
                        case "3":
                            ToastUtils.showLong(MyApp.getContext(), "我正在被使用！");
                            break;
                        case "5":
                            ToastUtils.showLong(MyApp.getContext(), "我是预约车！");
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
            LogUtils.d("开锁", result + "\n" + uID + "\n" + mToken);
            OkHttpUtils.post().url(Api.BASE_URL + Api.OPENSCAN).params(map).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    ToastUtils.showShort(MainActivity.this, "服务器忙，请稍后重试");
                }

                @Override
                public void onResponse(String response, int id) {
                    Log.d("开锁", response);
                    try {
                        JSONObject object = new JSONObject(response);
                        String resultStatus = object.optString("resultStatus");
                        switch (resultStatus) {
                            case "0":
                                ToastUtils.showLong(MyApp.getContext(), "开锁失败！");
                                break;
                            case "1":
                                ToastUtils.showLong(MyApp.getContext(), "开锁成功！");
                                break;
                            case "2":
                                ToastUtils.showLong(MyApp.getContext(), "您的账号在其他设备上登录，您已被迫下线");
                                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                SPUtils.put(MyApp.getContext(), "islogin", false);
                                MainActivity.this.finish();
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
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

    @Override
    public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

    }

    @Override
    public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
        if (reverseGeoCodeResult == null
                || reverseGeoCodeResult.error != SearchResult.ERRORNO.NO_ERROR) {
            // 没有检测到结果
            Toast.makeText(MainActivity.this, "抱歉，未能找到结果",
                    Toast.LENGTH_LONG).show();
        }
        String reverseGeoCodeResultAddress = reverseGeoCodeResult != null ? reverseGeoCodeResult.getAddress() : null;
        if (reverseGeoCodeResultAddress != null && !reverseGeoCodeResultAddress.equals("")) {
            mCurrentAddr.setText(reverseGeoCodeResultAddress);

        } else {
            mCurrentAddr.setText("未知地址");
        }
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }


    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {
        if (!isShowBikeInfo) {
            updateMapStatus(mapStatus);
        }
    }

    private void updateMapStatus(MapStatus mapStatus) {
//        mMap.clear();
        mMCenterLatLng = mapStatus.target;
        mChangeLatitude = mMCenterLatLng.latitude;
        mChangeLongitude = mMCenterLatLng.longitude;
        Log.i("中心点坐标", mChangeLatitude + "," + mChangeLongitude);
        WindowManager wm = this.getWindowManager();
        startNodeStr = PlanNode.withLocation(new LatLng(mChangeLatitude, mChangeLongitude));
        LogUtils.d("谁选中了", mAll.isChecked() + "\n" + mTrouble.isChecked() + "\n" + mException.isChecked());
        if (mAll.isChecked()) {
            getBikeInfo(mChangeLatitude, mChangeLongitude);
        } else if (mTrouble.isChecked()) {
            getTroubleBikeInfo(mChangeLatitude, mChangeLongitude);
        } else if (mException.isChecked()) {
            getExceptionBikeInfo(mChangeLatitude, mChangeLongitude);
        }

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
                mChangeLatitude = location.getLatitude();
                mChangeLongitude = location.getLongitude();
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
                                bikeInfo.setBicycleNo(jsonObject.getString("bicycleNo"));
                                bikeInfo.setLatitude(jsonObject.getDouble("latitude"));
                                bikeInfo.setLongitude(jsonObject.getDouble("longitude"));
                                bikeInfo.setUnitPrice(jsonObject.getString("unitPrice"));
                                bikeInfo.setElectricity(jsonObject.getDouble("electricity"));
                                bikeInfos.add(bikeInfo);
                            }
                            addOverlay(bikeInfos);
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
                                bikeInfo.setLatitude(jsonObject.getDouble("latitude"));
                                bikeInfo.setLongitude(jsonObject.getDouble("longitude"));
                                bikeInfo.setUnitPrice(jsonObject.getString("unitPrice"));
                                bikeInfo.setBicycleNo(jsonObject.getString("bicycleNo"));
                                bikeInfo.setElectricity(jsonObject.getDouble("electricity"));
                                bikeInfos.add(bikeInfo);
                            }
                            addOverlay(bikeInfos);
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
                                bikeInfo = new BikeInfo();
                                bikeInfo.setAddress(jsonObject.getString("address"));
                                bikeInfo.setLatitude(jsonObject.getDouble("latitude"));
                                bikeInfo.setLongitude(jsonObject.getDouble("longitude"));
                                bikeInfo.setUnitPrice(jsonObject.getString("unitPrice"));
                                bikeInfo.setBicycleNo(jsonObject.getString("bicycleNo"));
                                bikeInfo.setElectricity(jsonObject.getDouble("electricity"));
                                bikeInfos.add(bikeInfo);
                            }
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
            for (int i = 0; i < bikeInfos.size(); i++) {
                bikeInfo = (BikeInfo) bikeInfos.get(i);
                double lat = bikeInfo.getLatitude();
                double lng = bikeInfo.getLongitude();
                LatLng latLng = transform(lat, lng);
                double distance = DistanceUtil.getDistance(latLng, mMCenterLatLng);
                if (mTrouble.isChecked()) {
                    forLocationAddMark(lat, lng);
                } else {
                    if (distance <= 400) {
                        forLocationAddMark(lat, lng);
                    }
                }
            }
        } else {
            ToastUtils.showLong(this, "当前周围没有车辆");
        }
    }

    private LatLng transform(double lat, double lng) {
        LatLng sourceLatLng = new LatLng(lat, lng);
        CoordinateConverter converter = new CoordinateConverter();
        converter.from(CoordinateConverter.CoordType.COMMON);
        // sourceLatLng待转换坐标
        converter.coord(sourceLatLng);
        return converter.convert();
    }

    //添加覆盖物的方法
    private void forLocationAddMark(double lat, double lng) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.mipmap.bike_icon);
        OverlayOptions options;
        LatLng latLng = transform(lat, lng);
        LogUtils.d("所有的经纬度", latLng.latitude + "\n" + latLng.longitude);
        options = new MarkerOptions()
                .position(latLng)//设置位置
                .icon(bitmap)//设置图标样式
                .zIndex(9) // 设置marker所在层级
                .draggable(true)// 设置手势拖拽;
                .animateType(MarkerOptions.MarkerAnimateType.grow);
        //添加marker
        mMarker = (Marker) mMap.addOverlay(options);
        Bundle bundle = new Bundle();
//                // bikeInfo必须实现序列化接口
        bundle.putSerializable("bikeInfo", bikeInfo);
        mMarker.setExtraInfo(bundle);
//        }
    }

    //百度地图的覆盖物点击方法
    private void clickBaiduMapMark() {
        final TreeSet<Integer> integers = new TreeSet<>();
        mMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {

                if (marker.getExtraInfo() != null) {
                    StyledDialog.buildMdLoading(MainActivity.this, "路线规划中..", true, false).show();
                    Bundle bundle = marker.getExtraInfo();
                    bikeInfo = (BikeInfo) bundle.getSerializable("bikeInfo");
                    if (bikeInfo != null) {
                        mSubclauses.setVisibility(View.GONE);
                        mBikeLayout.setVisibility(View.VISIBLE);
                        mCenterIcon.setVisibility(View.GONE);
                        isShowBikeInfo = true;
                        mElectricQuantity.setText(String.valueOf(bikeInfo.getElectricity()) + "%");
                        mBikeNumber.setText(bikeInfo.getBicycleNo());
                        mCurrentAddr.setText(bikeInfo.getAddress());
                        mUnitPrice.setText(bikeInfo.getUnitPrice() + "元");
                        updateBikeInfo(bikeInfo);
                    }
                }
                return true;
            }
        });
    }

    //点击百度地图的方法
    private void clickBaiduMap() {
        mMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (isShowBikeInfo) {
                    if (routeOverlay != null) {
                        routeOverlay.removeFromMap();
//                        mMap.clear();
                        mBikeLayout.setVisibility(View.GONE);
                        mCenterIcon.setVisibility(View.VISIBLE);
                        isShowBikeInfo = false;
                        mSubclauses.setVisibility(View.VISIBLE);
//                        if (mAll.isChecked()) {
//                            addOverlay(bikeInfos);
//                        } else if (mTrouble.isChecked()) {
//                            addOverlay(bikeInfos);
//                        } else if (mException.isChecked()) {
//                            addOverlay(bikeInfos);
//                        }
                        setUserMapCenter(mCurrentLantitude, mCurrentLongitude);
                    }
                }
            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {
                return false;
            }
        });

    }

    private void updateBikeInfo(BikeInfo bikeInfo) {
        boolean hasPlanRoute = false;
        this.bikeInfo = bikeInfo;
        Double doulat = bikeInfo.getLatitude();
        Double doulon = bikeInfo.getLongitude();
        reverseGeoCoder(transform(doulat, doulon));
        PlanNode endNodeStr = PlanNode.withLocation(transform(doulat, doulon));
        StyledDialog.dismissLoading();
        drawPlanRoute(endNodeStr);
    }

    private void drawPlanRoute(PlanNode endNodeStr) {
        if (routeOverlay != null) {
            routeOverlay.removeFromMap();
        }
        if (endNodeStr != null) {
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
        mMap.setMyLocationEnabled(false);
        mTestMapView.onDestroy();
        EventBus.getDefault().unregister(this);
        StyledDialog.dismiss();

        if (mLocationClient != null) {
            mLocationClient.unRegisterLocationListener(mMyLocationListener);
            mLocationClient.stop();
        }
        if (mRPSearch != null) {
            mRPSearch.destroy();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mTestMapView.onResume();
        if (SPUtils.isLogin()) {
            mUID = (String) SPUtils.get(MyApp.getContext(), "id", "");
            mToken = (String) SPUtils.get(MyApp.getContext(), "token", "");
        }
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
        if ((walkingRouteResult != null ? walkingRouteResult.error : null) == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            //起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//                    result.getSuggestAddrInfo();
            return;
        }

        if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
            if (walkingRouteResult.getRouteLines().size() > 0) {
                WalkingRouteLine walkingRouteLine = walkingRouteResult.getRouteLines().get(0);
                int distance = walkingRouteLine.getDistance();
                int walkTime = walkingRouteLine.getDuration() / 60;
                LogUtils.d("gao", distance + "\n" + walkTime + "\n" + walkingRouteLine);
                String distance1 = MapUtil.distanceFormatter(distance);
                String castTime = String.valueOf(walkTime);
                if (!distance1.equals("")) {
                    mDistance.setText(distance1);
                }
                if (!castTime.equals("")) {
                    mArrivalTime.setText(castTime + "分钟");
                }

            }
            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mMap);
            mMap.setOnMarkerClickListener(overlay);
            routeOverlay = overlay;
            if (!overlay.equals("")) {
                overlay.setData(walkingRouteResult.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();
            }
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
                AppManager.AppExit(this);
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Subscriber(tag = "bikeNo", mode = ThreadMode.MAIN)
    private void receiveFromManual(CodeEvent info) {
        LogUtils.d("输入", info.getBikeNo());
        result = info.getBikeNo();
        if (mUID != null && result != null && mToken != null) {
            openScan(mUID, result, mToken);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    //手机的扫码消息
    @Subscriber(tag = "iphone", mode = ThreadMode.MAIN)
    private void receiveFromUnlockProgressforPhone(CodeEvent info) {
        if (info != null) {
            result = info.getBikeNo();
            if (result != null) {
                result = result.substring(result.length() - 9, result.length());
                LogUtils.d("锁号", result);
                if (NetUtils.isConnected(MyApp.getContext())) {
//                    CheckRepairBicycleNo(result);
                    openScan(mUID, result, mToken);
                }
            }
        }
    }

    private void reverseGeoCoder(LatLng latlng) {
        mSearch.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
    }

}
