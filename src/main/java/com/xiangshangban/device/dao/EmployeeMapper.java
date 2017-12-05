package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;

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
    List<Employee> selectByEmployeeNfc(String employeeNfc);

    //根据companyId查找公司编号
    List<String> selectCompanyNoByCompanyId(String employeeCompanyId);

    //根据人员的ID查询人员的名称
    String selectEmpNameByEmpId(@Param("empId") String empId);

    Employee selectByEmployeeIdAndCompanyId(@Param("employeeId") String employeeId,
                                            @Param("employeeCompanyId") String employeeCompanyId);
}