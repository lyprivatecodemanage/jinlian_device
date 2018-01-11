package com.xiangshangban.device.bean;

public class Device {
    private String deviceId;
    private String deviceName;
    private String companyId;
    private String companyName;
    private String deviceNumber;
    private String macAddress;
    private String isOnline;
    private String activeStatus;
    private String devicePlace;
    private String deviceUsages;
    private String totalServerTime;
    private String haveUsedTime;
    private String remainServerTime;
    private String isUnbind;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public void setDeviceNumber(String deviceNumber) {
        this.deviceNumber = deviceNumber;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }

    public String getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(String activeStatus) {
        this.activeStatus = activeStatus;
    }

    public String getDevicePlace() {
        return devicePlace;
    }

    public void setDevicePlace(String devicePlace) {
        this.devicePlace = devicePlace;
    }

    public String getDeviceUsages() {
        return deviceUsages;
    }

    public void setDeviceUsages(String deviceUsages) {
        this.deviceUsages = deviceUsages;
    }

    public String getTotalServerTime() {
        return totalServerTime;
    }

    public void setTotalServerTime(String totalServerTime) {
        this.totalServerTime = totalServerTime;
    }

    public String getHaveUsedTime() {
        return haveUsedTime;
    }

    public void setHaveUsedTime(String haveUsedTime) {
        this.haveUsedTime = haveUsedTime;
    }

    public String getRemainServerTime() {
        return remainServerTime;
    }

    public void setRemainServerTime(String remainServerTime) {
        this.remainServerTime = remainServerTime;
    }

    public String getIsUnbind() {
        return isUnbind;
    }

    public void setIsUnbind(String isUnbind) {
        this.isUnbind = isUnbind;
    }

    @Override
    public String toString() {
        return "Device{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", companyId='" + companyId + '\'' +
                ", companyName='" + companyName + '\'' +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", macAddress='" + macAddress + '\'' +
                ", isOnline='" + isOnline + '\'' +
                ", activeStatus='" + activeStatus + '\'' +
                ", devicePlace='" + devicePlace + '\'' +
                ", deviceUsages='" + deviceUsages + '\'' +
                ", totalServerTime='" + totalServerTime + '\'' +
                ", haveUsedTime='" + haveUsedTime + '\'' +
                ", remainServerTime='" + remainServerTime + '\'' +
                ", isUnbind='" + isUnbind + '\'' +
                '}';
    }

    //模板模块（生成二维码的时候，二维码的内容）
    public String personalToString() {
        return "Device{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceNumber='" + deviceNumber + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}