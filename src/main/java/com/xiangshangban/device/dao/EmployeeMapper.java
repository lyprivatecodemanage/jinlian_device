package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Employee;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    //查询指定人员在职的公司ID
    Employee selectByEmployeeIdAndCompanyId(@Param("employeeId") String employeeId,
                                            @Param("employeeCompanyId") String employeeCompanyId);

    //按照人员id和公司id更新人员信息
    int updateByEmployeeIdAndCompanyIdSelective(Employee employee);

    /**
     * 按照人员Id和公司的id查询人员的名称
     */
    String selectEmpNameByComIdAndEmpId(Map map);

    //临时方法-查询人脸非null非空字符串的所有人
    List<Employee> temp();

    //临时方法-根据公司id查询所有员工信息
    List<Employee> selectAllByCompanyId(String employeeCompanyId);

    List<Employee> selectAllByEmployeeId(String employeeId);
}