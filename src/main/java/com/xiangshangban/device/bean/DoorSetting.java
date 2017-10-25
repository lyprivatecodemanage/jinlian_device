package com.xiangshangban.device.bean;

public class DoorSetting {
    private String doorId;

    private String doorOpenLimitTime;

    private String lockOpenLimitTime;

    private String enableDoorEventRecord;

    private String faultCountAuthentication;

    private String alarmTimeLengthTrespass;

    private String firstPublishPassword;

    private String secondPublishPassword;

    private String threatenPublishPasswrod;

    private String managerPassword;

    public String getDoorId() {
        return doorId;
    }

    public void setDoorId(String doorId) {
        this.doorId = doorId;
    }

    public String getDoorOpenLimitTime() {
        return doorOpenLimitTime;
    }

    public void setDoorOpenLimitTime(String doorOpenLimitTime) {
        this.doorOpenLimitTime = doorOpenLimitTime;
    }

    public String getLockOpenLimitTime() {
        return lockOpenLimitTime;
    }

    public void setLockOpenLimitTime(String lockOpenLimitTime) {
        this.lockOpenLimitTime = lockOpenLimitTime;
    }

    public String getEnableDoorEventRecord() {
        return enableDoorEventRecord;
    }

    public void setEnableDoorEventRecord(String enableDoorEventRecord) {
        this.enableDoorEventRecord = enableDoorEventRecord;
    }

    public String getFaultCountAuthentication() {
        return faultCountAuthentication;
    }

    public void setFaultCountAuthentication(String faultCountAuthentication) {
        this.faultCountAuthentication = faultCountAuthentication;
    }

    public String getAlarmTimeLengthTrespass() {
        return alarmTimeLengthTrespass;
    }

    public void setAlarmTimeLengthTrespass(String alarmTimeLengthTrespass) {
        this.alarmTimeLengthTrespass = alarmTimeLengthTrespass;
    }

    public String getFirstPublishPassword() {
        return firstPublishPassword;
    }

    public void setFirstPublishPassword(String firstPublishPassword) {
        this.firstPublishPassword = firstPublishPassword;
    }

    public String getSecondPublishPassword() {
        return secondPublishPassword;
    }

    public void setSecondPublishPassword(String secondPublishPassword) {
        this.secondPublishPassword = secondPublishPassword;
    }

    public String getThreatenPublishPasswrod() {
        return threatenPublishPasswrod;
    }

    public void setThreatenPublishPasswrod(String threatenPublishPasswrod) {
        this.threatenPublishPasswrod = threatenPublishPasswrod;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }
}