package com.xiangshangban.device.bean;

/**
 * author : Administrator
 * date: 2017/10/24 16:13
 * describe: TODO 门关联人员权限信息的查询条件封装类
 */
public class RelateEmpPermissionCondition {
    private String doorId;
    private String empName;
    private String deptName;

    //开门方式(ID)
    private String openType;
    //下发状态(ID)
    private String issueState;
    //设备id
    private String deviceId;

    public String getDoorId() {
        return doorId;
    }

    public void setDoorId(String doorName) {
        this.doorId = doorName;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getIssueState() {
        return issueState;
    }

    public void setIssueState(String issueState) {
        this.issueState = issueState;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }
}
