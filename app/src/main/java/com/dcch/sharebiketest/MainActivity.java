package com.dcch.sharebiketest;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

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
import com.dcch.sharebiketest.base.BaseActivity;
import com.dcch.sharebiketest.http.Api;
import com.dcch.sharebiketest.libzxing.zxing.activity.CaptureActivity;
import com.dcch.sharebiketest.moudle.home.BikeInfo;
import com.dcch.sharebiketest.moudle.listener.MyOrientationListener;
import com.dcch.sharebiketest.utils.JsonUtils;
import com.dcch.sharebiketest.utils.LogUtils;
import com.dcch.sharebiketest.utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnClick;
import okhttp3.Call;

public class MainActivity extends BaseActivity {
    @BindView(R.id.testMapView)
    MapView mTestMapView;
    @BindView(R.id.MyCenter)
    ImageView mMyCenter;
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
    private LatLng currentLatLng;
    private double changeLatitude, changeLongitude;
    private List<BikeInfo> bikeInfos;
    private BikeInfo bikeInfo;
    private double mLat1;
    private double mLng1;
    Marker mMarker = null;
    private String result;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void initData() {
        mMap = mTestMapView.getMap();
        LogUtils.d("地图", mMap + "");
        bikeInfos = new ArrayList<BikeInfo>();
        mAll.setChecked(true);
        // 初始化定位
        initMyLocation();
        // 初始化传感器
        initOritationListener();
    }

    @Override
    protected void initListener() {
        super.initListener();
        mSubclauses.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                switch (i){
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

    @OnClick({R.id.MyCenter, R.id.scan, R.id.btn_my_location})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.MyCenter:
                break;
            case R.id.scan:
                Intent i1 = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(i1, 0);
                break;
            case R.id.btn_my_location:
                setUserMapCenter(mCurrentLantitude, mCurrentLongitude);
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
                        result = bundle.getString("result");
//                        openScan(uID, phone, result, mToken);
//                        Intent intent = new Intent(MainActivity.this, UnlockProgressActivity.class);
//                        startActivity(intent);
                        ToastUtils.showLong(this, result);
                    }
                    break;

            }

        }
    }
    //扫码开锁的方法
    private void openScan(final String uID, String phone, final String result, final String mToken) {
        if (phone != null && !phone.equals("") && result != null && !result.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.clear();
            map.put("userId", uID);
            map.put("phone", phone);
            map.put("bicycleNo", result);
            map.put("token", mToken);
            LogUtils.d("开锁", phone);
            LogUtils.d("开锁", result);
            LogUtils.d("开锁", uID);
            OkHttpUtils.post().url(Api.BASE_URL + Api.OPENSCAN).params(map).build().execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    if (!e.equals("") && e != null) {
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
//                        EventBus.getDefault().post(new MessageEvent(), "on");

                    } else {
//                        EventBus.getDefault().post(new MessageEvent(), "off");
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
    public class MyLocationListener implements BDLocationListener {
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
            currentLatLng = new LatLng(mCurrentLantitude, mCurrentLongitude);
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
                changeLongitude = location.getLongitude();
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
        if (lat != null && !lat.equals("") && lng != null && !lng.equals("")) {
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
                            LogUtils.d("数量",bikeInfos.size()+"1");
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
        if (lat != null && !lat.equals("") && lng != null && !lng.equals("")) {
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
                            LogUtils.d("数量",bikeInfos.size()+"2");
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
        if (lat != null && !lat.equals("") && lng != null && !lng.equals("")) {
            Map<String, String> map = new HashMap<>();
            map.put("lng", lng);
            map.put("lat", lat);
            LogUtils.d("所有的数据",lng+"\n"+lat);
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
                            LogUtils.d("数量",bikeInfos.size()+"3");
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
            LatLng latLng = null;
            List<Double> doubles = new ArrayList<>();
            for (int i = 0; i < bikeInfos.size(); i++) {
                bikeInfo = (BikeInfo) bikeInfos.get(i);
                String lat = bikeInfo.getLatitude();
                String lng = bikeInfo.getLongitude();
                mLat1 = Double.parseDouble(lat);
                mLng1 = Double.parseDouble(lng);
                latLng = new LatLng(mLat1, mLng1);
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

//    public void getMyLocation() {
//        MyLocationData data = new MyLocationData.Builder()
//                .accuracy(1000)//范围半径，单位：米
//                .latitude(mCurrentLantitude)//
//                .longitude(mCurrentLongitude).build();
//        mMap.setMyLocationData(data);
//    }


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
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mTestMapView.onPause();
    }
}
