package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Employee;

public interface EmployeeMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(Employee record);

    int insertSelective(Employee record);

    Employee selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(Employee record);

    int updateByPrimaryKey(Employee record);

    /**
     * 非自动生成
     */

    //按照卡号查找是否有人员占用了该卡号
    Employee selectByEmployeeNfc(String employeeNfc);

    //根据companyId查找人员信息
    Employee selectOneByCompanyId(String companyId);
}