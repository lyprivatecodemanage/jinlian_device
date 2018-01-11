package com.xiangshangban.device.bean;

public class DeviceRebootRecord {
    private String rebootId;

    private String deviceId;

    private String rebootNumber;

    private String rebootTime;

    public String getRebootId() {
        return rebootId;
    }

    public void setRebootId(String rebootId) {
        this.rebootId = rebootId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getRebootNumber() {
        return rebootNumber;
    }

    public void setRebootNumber(String rebootNumber) {
        this.rebootNumber = rebootNumber;
    }

    public String getRebootTime() {
        return rebootTime;
    }

    public void setRebootTime(String rebootTime) {
        this.rebootTime = rebootTime;
    }
}