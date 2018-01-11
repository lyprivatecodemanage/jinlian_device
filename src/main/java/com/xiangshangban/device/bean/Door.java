package com.xiangshangban.device.bean;

public class Door {

    private String doorId;
    private String doorName;
    private String deviceId;
    private String operateTime;
    private String operateEmployee;
    private String companyId;
    private String bindDate;

    public String getDoorId() {
        return doorId;
    }

    public void setDoorId(String doorId) {
        this.doorId = doorId;
    }

    public String getDoorName() {
        return doorName;
    }

    public void setDoorName(String doorName) {
        this.doorName = doorName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getOperateTime() {
        return operateTime;
    }

    public void setOperateTime(String operateTime) {
        this.operateTime = operateTime;
    }

    public String getOperateEmployee() {
        return operateEmployee;
    }

    public void setOperateEmployee(String operateEmployee) {
        this.operateEmployee = operateEmployee;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getBindDate() {
        return bindDate;
    }

    public void setBindDate(String bindDate) {
        this.bindDate = bindDate;
    }

    @Override
    public String toString() {
        return "Door{" +
                "doorId='" + doorId + '\'' +
                ", doorName='" + doorName + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", operateTime='" + operateTime + '\'' +
                ", operateEmployee='" + operateEmployee + '\'' +
                ", companyId='" + companyId + '\'' +
                ", bindDate='" + bindDate + '\'' +
                '}';
    }
}