package com.dcch.sharebiketest.moudle.home;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/2/17 0017.
 */

public class BikeInfo implements Serializable {


    /**
     * bicycleId : 21
     * bicycleNo : 1201000011
     * bicycleName : null
     * bicycleTypeId : null
     * bicycleTypeNo : null
     * unitPrice : 1
     * price : 0
     * discount : 0
     * exdiscount : 0
     * startDate : null
     * endDate : null
     * manufacturer : null
     * organization_ID : 0
     * organization_Name : null
     * address : 北京市上地五街
     * usestate : 0
     * anomaly : 1
     * reservestate : 0
     * delflag : null
     * createTime : null
     * releaseTime : null
     * userId : null
     * lockRemark : null
     * resultStatus : null
     * systemTime : 2017-03-14 11:06:19.0
     * bicycletime : null
     * sleephour : null
     * positiontype : null
     * locknum : 0
     * electricity : 0
     * online : null
     * longitude : 116.30816476027793
     * latitude : 40.050456963226345
     * minLat : null
     * maxLat : null
     * minLng : null
     * maxLng : null
     */

    private int bicycleId;
    private int bicycleNo;
    private String unitPrice;
    private String address;
    private String systemTime;
    private double longitude;
    private double latitude;


    public int getBicycleId() {
        return bicycleId;
    }

    public void setBicycleId(int bicycleId) {
        this.bicycleId = bicycleId;
    }

    public int getBicycleNo() {
        return bicycleNo;
    }

    public void setBicycleNo(int bicycleNo) {
        this.bicycleNo = bicycleNo;
    }

    public String getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(String unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getSystemTime() {
        return systemTime;
    }

    public void setSystemTime(String systemTime) {
        this.systemTime = systemTime;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

}
