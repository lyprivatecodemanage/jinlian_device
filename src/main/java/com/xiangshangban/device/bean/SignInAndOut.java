package com.xiangshangban.device.bean;

/**
 * TODO 签到签退记录实体类
 */
public class SignInAndOut {
    private String empId;
    private String empName;
    private String empDept;
    private String signIn;
    private String signOut;

    public String getEmpId() {
        return empId;
    }

    public void setEmpId(String empId) {
        this.empId = empId;
    }

    public String getEmpName() {
        return empName;
    }

    public void setEmpName(String empName) {
        this.empName = empName;
    }

    public String getEmpDept() {
        return empDept;
    }

    public void setEmpDept(String empDept) {
        this.empDept = empDept;
    }

    public String getSignIn() {
        return signIn;
    }

    public void setSignIn(String signIn) {
        this.signIn = signIn;
    }

    public String getSignOut() {
        return signOut;
    }

    public void setSignOut(String signOut) {
        this.signOut = signOut;
    }

    @Override
    public String toString() {
        return "SignInAndOut{" +
                "empId='" + empId + '\'' +
                ", empName='" + empName + '\'' +
                ", empDept='" + empDept + '\'' +
                ", signIn='" + signIn + '\'' +
                ", signOut='" + signOut + '\'' +
                '}';
    }
}
