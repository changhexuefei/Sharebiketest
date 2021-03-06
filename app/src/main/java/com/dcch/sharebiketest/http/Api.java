package com.dcch.sharebiketest.http;

/**
 * Created by Administrator on 2017/4/17 0017.
 */

public class Api {
    public static final String BASE_URL = "http://www.70bikes.cn/MavenSSM/mobile/";
//    public static final String BASE_URL = "http://114.112.86.38:8080/MavenSSM/mobile/";
//    public static final String BASE_URL = "http://192.168.1.142:8080/MavenSSM/mobile/";
    //请求全部车辆信息
    public static final String FINDBICYCLE = "findBicycle.do?";
    //异常车辆信息
    public static final String FINDBICYCLEEXCEPTION = "findBicycleException.do?";
    //故障车辆信息
    public static final String FINDBICYCLETROUBLE = "findBicycleTrouble.do?";
    //扫码开锁
    public static final String OPENSCAN = "openScanRepair.do?";
    //查询车辆编号
    public static final String CHECKBICYCLENO = "checkBicycleNo.do?";
    //登录
    public static final String REPAIRLOGIN = "repairLogin.do?";
    //检查车锁是否存在，是否开锁，是否预约
    public static final String CHECKREPAIRBICYCLENO = "CheckRepairBicycleNo.do?";
    //手工输入车辆编号，查询车辆状态
    public static final String SEARCHBICYCLE = "searchBicycle.do?";
    //寻车铃
    public static final String FINDBIKERING = "FindBikeRing.do?";

}
