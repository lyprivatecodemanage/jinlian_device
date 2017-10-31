package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.*;

import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：门禁管理
 */
public interface IEntranceGuardService {

    //添加or更新门禁命令表（同一个人传入的数据不一样时执行更新操作）
    void insertCommand(DoorCmd doorCmd);

    //TODO 基础信息部分
    /**
     * 添加门信息
     */
    boolean addDoorInfo(Door door);

    /**
     * 删除门信息
     */
    boolean deleteDoorInfo(Door door);

    /**
     * 批量删除门信息
     */
    boolean delDoorInfoByBatch(List<String> doorList);

    /**
     * 更新门信息
     */
    boolean updateDoorInfo(Door door);

    /**
     * 根据门名称查询门信息[动态查询]
     */
    List<Map> queryAllDoorInfo(Door door);

    /**
     * 根据id查询门信息
     */
    Door queryDoorInfo(String doorId);


    //TODO 授权中心

    /**
     * 根据门名称查询门和门关联的人员的信息
     */
    List<Map> authoQueryAllDoor(DoorEmployee doorEmployee);

    /**
     * 查询门命令下发时间
     * @param doorName
     * @return
     */
   List<Map> querySendTime(String doorName);


    /**
     * 查询门关联的员工信息（姓名、部门、开门时间、开门方式）
     * @param relateEmpPermissionCondition
     * @return
     */
   List<Map> queryRelateEmpPermissionInfo(RelateEmpPermissionCondition relateEmpPermissionCondition);

    /**
     * 查询设备命令信息（下方时间、下发状态、下发数据）
     * @param relateEmpPermissionCondition
     * @return
     */
   List<Map> queryCMDInfo(RelateEmpPermissionCondition relateEmpPermissionCondition);


    //TODO 门禁记录

    //1）出入记录
    public List<DoorRecord> queryPunchCardRecord(DoorRecordCondition doorRecordCondition);

    //2）门禁异常
    public List<DoorException> queryDoorExceptionRecord(DoorExceptionCondition doorExceptionCondition);

    //门禁配置---功能配置（身份验证失败次数、非法入侵、报警时长、密码、开门事件记录）
    void doorCommonSetupAdditional(String doorId, String countLimitAuthenticationFailed, String enableAlarm,
                                   String alarmTimeLength, String publicPassword1, String publicPassword2, String threatenPassword,
                                   String deviceManagePassword, String enableDoorOpenRecord, List oneWeekTimeDoorKeepList,
                                   String enableDoorKeepOpen, String enableFirstCardKeepOpen, String enableDoorCalendar);

    //门禁配置---功能配置（首卡权限配置）
    void handOutFirstCard(String doorId, String enableFirstCardKeepOpen, List<String> employeeIdList, List oneWeekTimeFirstCardList);

    //门禁配置---功能配置（门禁日历）
    void handOutDoorCalendar(String doorId, String enableDoorCalendar, List accessCalendar);

}
