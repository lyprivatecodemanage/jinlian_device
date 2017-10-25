package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Employee;
import org.springframework.stereotype.Component;

@Component
public interface EmployeeMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(Employee record);

    int insertSelective(Employee record);

    Employee selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(Employee record);

    int updateByPrimaryKey(Employee record);

}