package com.xiangshangban.device.bean;

public class OperateLog {
    private String id;

    private String operateEmpId;

    private String operateEmpCompany;

    private String operateType;

    private String operateContent;

    private String operateDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOperateEmpId() {
        return operateEmpId;
    }

    public void setOperateEmpId(String operateEmpId) {
        this.operateEmpId = operateEmpId;
    }

    public String getOperateEmpCompany() {
        return operateEmpCompany;
    }

    public void setOperateEmpCompany(String operateEmpCompany) {
        this.operateEmpCompany = operateEmpCompany;
    }

    public String getOperateType() {
        return operateType;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public String getOperateContent() {
        return operateContent;
    }

    public void setOperateContent(String operateContent) {
        this.operateContent = operateContent;
    }

    public String getOperateDate() {
        return operateDate;
    }

    public void setOperateDate(String operateDate) {
        this.operateDate = operateDate;
    }

    @Override
    public String toString() {
        return "OperateLog{" +
                "id='" + id + '\'' +
                ", operateEmpId='" + operateEmpId + '\'' +
                ", operateEmpCompany='" + operateEmpCompany + '\'' +
                ", operateType='" + operateType + '\'' +
                ", operateContent='" + operateContent + '\'' +
                ", operateDate='" + operateDate + '\'' +
                '}';
    }
}