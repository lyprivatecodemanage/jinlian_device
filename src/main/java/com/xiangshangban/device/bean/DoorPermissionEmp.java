package com.xiangshangban.device.bean;

/**
 * author :Yonghui Wang
 * date: 2017/12/4 11:23
 * describe: TODO 具有门禁权限人员信息实体类
 */
public class DoorPermissionEmp {

     private String day_of_week;
     private String range_door_open_type;
     private String status;
     private String lasttime;
     private String range_end_time;
     private String employee_department_name;
     private String range_start_time;
     private String employee_id;
     private String employee_name;
     private String deviceName;
     private String isHistoryDevice;

    public String getDay_of_week() {
        return day_of_week;
    }

    public void setDay_of_week(String day_of_week) {
        this.day_of_week = day_of_week;
    }

    public String getRange_door_open_type() {
        return range_door_open_type;
    }

    public void setRange_door_open_type(String range_door_open_type) {
        this.range_door_open_type = range_door_open_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLasttime() {
        return lasttime;
    }

    public void setLasttime(String lasttime) {
        this.lasttime = lasttime;
    }

    public String getRange_end_time() {
        return range_end_time;
    }

    public void setRange_end_time(String range_end_time) {
        this.range_end_time = range_end_time;
    }

    public String getEmployee_department_name() {
        return employee_department_name;
    }

    public void setEmployee_department_name(String employee_department_name) {
        this.employee_department_name = employee_department_name;
    }

    public String getRange_start_time() {
        return range_start_time;
    }

    public void setRange_start_time(String range_start_time) {
        this.range_start_time = range_start_time;
    }

    public String getEmployee_id() {
        return employee_id;
    }

    public void setEmployee_id(String employee_id) {
        this.employee_id = employee_id;
    }

    public String getEmployee_name() {
        return employee_name;
    }

    public void setEmployee_name(String employee_name) {
        this.employee_name = employee_name;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getIsHistoryDevice() {
        return isHistoryDevice;
    }

    public void setIsHistoryDevice(String isHistoryDevice) {
        this.isHistoryDevice = isHistoryDevice;
    }

    @Override
    public String toString() {
        return "DoorPermissionEmp{" +
                "day_of_week='" + day_of_week + '\'' +
                ", range_door_open_type='" + range_door_open_type + '\'' +
                ", status='" + status + '\'' +
                ", lasttime='" + lasttime + '\'' +
                ", range_end_time='" + range_end_time + '\'' +
                ", employee_department_name='" + employee_department_name + '\'' +
                ", range_start_time='" + range_start_time + '\'' +
                ", employee_id='" + employee_id + '\'' +
                ", employee_name='" + employee_name + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", isHistoryDevice='" + isHistoryDevice + '\'' +
                '}';
    }
}