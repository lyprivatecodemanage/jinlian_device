package com.xiangshangban.device.bean;

public class DoorEmployeePermission {

    private String employeeId;
    private String isManager;
    private String haveFirstCardPermission;
    private String doorOpenStartTime;
    private String doorOpenEndTime;

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getIsManager() {
        return isManager;
    }

    public void setIsManager(String isManager) {
        this.isManager = isManager;
    }

    public String getHaveFirstCardPermission() {
        return haveFirstCardPermission;
    }

    public void setHaveFirstCardPermission(String haveFirstCardPermission) {
        this.haveFirstCardPermission = haveFirstCardPermission;
    }

    public String getDoorOpenStartTime() {
        return doorOpenStartTime;
    }

    public void setDoorOpenStartTime(String doorOpenStartTime) {
        this.doorOpenStartTime = doorOpenStartTime;
    }

    public String getDoorOpenEndTime() {
        return doorOpenEndTime;
    }

    public void setDoorOpenEndTime(String doorOpenEndTime) {
        this.doorOpenEndTime = doorOpenEndTime;
    }
}