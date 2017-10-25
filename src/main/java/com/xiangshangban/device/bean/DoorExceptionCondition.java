package com.xiangshangban.device.bean;

/**
 * author : Administrator
 * date: 2017/10/23 15:51
 * describe: TODO 门禁异常记录查询条件封装类
 */
public class DoorExceptionCondition {
    private String name;
    private String department;
    private String alarmType;
    private String alarmTime;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(String alarmType) {
        this.alarmType = alarmType;
    }

    public String getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(String alarmTime) {
        this.alarmTime = alarmTime;
    }
}
