package com.dcch.sharebiketest.http;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public class Api {
    public static final String BASE_URL = "http://114.112.86.38/MavenSSM/mobile/";
    //请求车辆信息
    public static final String FINDBICYCLE = "findBicycle.do?";
    public static final String FINDBICYCLEEXCEPTION = "findBicycleException.do?";
    public static final String FINDBICYCLETROUBLE = "findBicycleTrouble.do?";
    //扫码开锁
    public static final String OPENSCAN = "openScan.do?";
    //查询车辆编号
    public static final String CHECKBICYCLENO = "checkBicycleNo.do?";
}
