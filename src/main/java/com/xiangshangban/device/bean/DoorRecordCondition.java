package com.xiangshangban.device.bean;

/**
 * author : Administrator
 * date: 2017/10/23 15:51
 * describe: TODO 门禁记录查询条件封装类
 */
public class DoorRecordCondition {

    private String name;
    private String department;
    private String punchCardType;
    private String punchCardTime;

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

    public String getPunchCardType() {
        return punchCardType;
    }

    public void setPunchCardType(String punchCardType) {
        this.punchCardType = punchCardType;
    }

    public String getPunchCardTime() {
        return punchCardTime;
    }

    public void setPunchCardTime(String punchCardTime) {
        this.punchCardTime = punchCardTime;
    }
}
