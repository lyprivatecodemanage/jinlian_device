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
    private String punchCardStartTime;
    private String punchCardEndTime;
    private String companyId;
    private String deviceName;

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

    public String getPunchCardStartTime() {
        return punchCardStartTime;
    }

    public void setPunchCardStartTime(String punchCardStartTime) {
        this.punchCardStartTime = punchCardStartTime;
    }

    public String getPunchCardEndTime() {
        return punchCardEndTime;
    }

    public void setPunchCardEndTime(String punchCardEndTime) {
        this.punchCardEndTime = punchCardEndTime;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    @Override
    public String toString() {
        return "DoorRecordCondition{" +
                "name='" + name + '\'' +
                ", department='" + department + '\'' +
                ", punchCardType='" + punchCardType + '\'' +
                ", punchCardStartTime='" + punchCardStartTime + '\'' +
                ", punchCardEndTime='" + punchCardEndTime + '\'' +
                ", companyId='" + companyId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                '}';
    }
}
