package com.xiangshangban.device.bean;

public class DoorException {
    private String doorExceptionId;

    private String alarmType;

    private String alarmDate;

    private String employeeId;

    public String getDoorExceptionId() {
        return doorExceptionId;
    }

    public void setDoorExceptionId(String doorExceptionId) {
        this.doorExceptionId = doorExceptionId;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }
}