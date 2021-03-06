package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.Employee;
import com.xiangshangban.device.bean.ReturnData;
import org.springframework.web.multipart.MultipartFile;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
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

    //关联门和人员
    void relateEmployeeAndDoor(String doorId, String doorName, String employeeId, String employeeName, String rangeFlagId, String deviceId);

    //关联人员门禁权限
    void relateEmployeeAndPermission(String rangeFlagId, String employeeId, String dayOfWeek, String isAllDay,
                                     String rangeStartTime, String rangeEndTime, String rangeDoorOpenType,
                                     String isDitto, String deviceId);

    //人员人脸、指纹、卡号信息上传存储
    Map<String, Object> saveEmployeeInputInfo(String employeeInputInfo, String deviceId, String style, String companyId);

    //删除设备上的人员的所有关联信息（设备模块调用）
    ReturnData deleteEmployeeInformationDev(String employeeIdCollection, String operatorEmployeeId);

    //删除设备上的人员的所有关联信息（人员模块调用）
    ReturnData deleteEmployeeInformationEmp(String employeeIdCollection, String operatorEmployeeId);

    //批量下发存为草稿的人员信息和人员权限信息
    void multipleHandOutEmployeePermission(String doorId);

    //根据人员id查找人的信息
    Employee findEmployeeById(String empId);

    //同步不同设备上，有开门权限的人员的权限信息（重新下发这个人的信息）
    void synchronizeEmployeePermissionForDevices(String jsonString, String employeeId);

    String deviceUploadPackage(String versionCode, MultipartFile uploadResource,
                               String fileType, String employeeId) throws IOException;

    //下发人员信息及门禁权限
    ReturnData handOutEmployeePermission(String employeePermission, HttpServletRequest request);
}
