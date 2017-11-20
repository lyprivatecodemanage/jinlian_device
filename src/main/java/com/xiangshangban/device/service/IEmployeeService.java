package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.Employee;

import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：用户管理
 */

public interface IEmployeeService {

    //人员模块人员信息同步
    void employeeCommandGenerate(List<Map<String, Object>> userIdCollection);

    //关联门和人员（同一个人传入的数据不一样时执行更新操作）
    void relateEmployeeAndDoor(String doorId, String doorName, String employeeId, String employeeName);

    //关联人员门禁权限（同一个人传入的数据不一样时执行更新操作）
    void relateEmployeeAndPermission(String employeeId, String dayOfWeek, String isAllDay,
                                     String rangeStartTime, String rangeEndTime, String rangeDoorOpenType);

    //人员人脸、指纹、卡号信息上传存储
    Map<String, Object> saveEmployeeInputInfo(String employeeInputInfo, String deviceId);

    //删除设备上的人员的所有关联信息
    void deleteEmployeeInformation(String employeeIdCollection);

    //批量下发存为草稿的人员信息和人员权限信息
    void multipleHandOutEmployeePermission(String doorId);

    //根据人员id查找人的信息
    Employee findEmployeeById(String empId);

}
