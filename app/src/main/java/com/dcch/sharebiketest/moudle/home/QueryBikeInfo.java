package com.dcch.sharebiketest.moudle.home;

/**
 * Created by gao on 2017/7/17.
 */

public class QueryBikeInfo {

    /**
     * resultStatus : 1
     * bicycle : {"bicycleNo":"091701005","electricity":100,"latitude":"34.34247","longitude":"107.173485"}
     */

    private String resultStatus;
    private BicycleBean bicycle;

    public String getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(String resultStatus) {
        this.resultStatus = resultStatus;
    }

    public BicycleBean getBicycle() {
        return bicycle;
    }

    public void setBicycle(BicycleBean bicycle) {
        this.bicycle = bicycle;
    }

    public static class BicycleBean {
        /**
         * bicycleNo : 091701005
         * electricity : 100
         * latitude : 34.34247
         * longitude : 107.173485
         */

        private String bicycleNo;
        private int electricity;
        private String latitude;
        private String longitude;

        public String getBicycleNo() {
            return bicycleNo;
        }

        public void setBicycleNo(String bicycleNo) {
            this.bicycleNo = bicycleNo;
        }

        public int getElectricity() {
            return electricity;
        }

        public void setElectricity(int electricity) {
            this.electricity = electricity;
        }

        public String getLatitude() {
            return latitude;
        }

        public void setLatitude(String latitude) {
            this.latitude = latitude;
        }

        public String getLongitude() {
            return longitude;
        }

        public void setLongitude(String longitude) {
            this.longitude = longitude;
        }
    }
}
