package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.Door;
import com.xiangshangban.device.bean.DoorCmd;

import java.util.List;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：用户管理
 */

public interface IEmployeeService {

    //人员模块命令生成器
    void employeeCommandGenerate(String action, List<String> userIdCollection);

    //关联门和人员（同一个人传入的数据不一样时执行更新操作）
    void relateEmployeeAndDoor(String doorId, String doorName, String employeeId, String employeeName);

    //根据公司id查门列表
    List<Door> findDoorIdByCompanyId(String companyId);

    //关联人员门禁权限（同一个人传入的数据不一样时执行更新操作）
    void relateEmployeeAndPermission(String employeeId, String dayOfWeek, String isAllDay,
                                     String rangeStartTime, String rangeEndTime, String rangeDoorOpenType);

    //添加or更新命令表（同一个人传入的数据不一样时执行更新操作）
    void insertEmployeeCommand(DoorCmd doorCmd);

    //添加人员信息

}
