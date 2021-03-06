package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.*;

import java.io.OutputStream;
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
     * 删除门信息(解除门和设备的关联关系)
     */
    boolean delDoorInfo(String doorId);

    /**
     * 查询door_表主键的最大值
     */
    String queryPrimaryKeyFromDoor();

    /**
     * 更新门信息
     */
    boolean updateDoorInfo(Door door);

    /**
     * 根据门名称查询门信息[动态查询]
     */
    List<Map> queryAllDoorInfo(Map map);


    //TODO 日志管理
    /**
     * 条件查询日志
     * @param requestParam
     * @return
     */
    Map queryLogCommand(String requestParam,String companyId);

    /**
     * 批量删除日志----->web端
     */
    boolean clearLogCommand(String requestParam);

    /**
     * 删除指定设备上的日志信息（设备解绑的时候要清空的数据）
     */
    boolean clearDeviceLog(String deviceId);

    //TODO 授权中心
    /**
     * 根据门名称查询门和门关联的人员的信息
     */
    List<Map> authoQueryAllDoor(Map map);

    /**
     * 查询当前门关于人员（下发删除）部分命令的下发时间
     * @param doorId
     * @return
     */
     String querySendTime(String doorId);


    /**
     * 查询门关联的员工信息（姓名、部门、开门时间、开门方式、下发时间、下发状态）
     * @param relateEmpPermissionCondition
     * @return
     */
   List<Map> queryRelateEmpPermissionInfo(Map relateEmpPermissionCondition,String companyId);

    /**
     * 根据门的ID查询门的名称
     */
    String queryDoorNameByDoorId(String doorId);

    /**
     * 查询有门禁权限的人员一周的开门时间段
     * @param empId
     * @return
     */
   List<Map> queryAWeekOpenTime(String empId,String doorId);


    /**
     * 根据门的id查询门的定时常开信息
     * @param doorId
     * @return
     */
   List<DoorTimingKeepOpen> queryKeepOpenInfo(String doorId);

    /**
     * 根据门的id查询门的首卡常开信息
     * @param doorId
     * @return
     */
    List<Map> queryFirstCardKeepOpenInfo(String doorId,String companyId);

    /**
     * 根据门的id查询门禁日历信息
     * @return
     */
   List<DoorCalendar> queryDoorCalendarInfo(String doorId);

    /**
     * 根据门的id查询门的设置信息
     * @param doorId
     * @return
     */
   List<Map> queryDoorSettingInfo(String doorId);

    //TODO 门禁记录

    //1）出入记录和门禁异常
    public List<Map> queryPunchCardRecord(DoorRecordCondition doorRecordCondition,int flag);

    //2）查询一个人在某段时间内的最早和最晚的打卡时间
    public List<String> queryPunchCardTime(String empId,String companyId,String startTime,String endTime);

    //3 ) 查询当前公司人员的签到签退情况
    public Map querySignInAndOutRecord(String requestParam,String companyId);

    /**
     * 导出记录到Excel
     * @param excelName 导出的Excel表的名称
     * @param out 输出流
     * @param companyId 公司ID
     */
    public void exportRecordToExcel(String requestParam,String excelName, OutputStream out, String companyId);

    //门禁配置---功能配置（身份验证失败次数、非法入侵、报警时长、密码、开门事件记录）
    void doorCommonSetupAdditional(String doorId, String countLimitAuthenticationFailed, String enableAlarm,
                                   String alarmTimeLength, String publicPassword1, String publicPassword2, String threatenPassword,
                                   String deviceManagePassword, String enableDoorOpenRecord, List oneWeekTimeDoorKeepList,
                                   String enableDoorKeepOpen, String enableFirstCardKeepOpen, String enableDoorCalendar,
                                   String operatorEmployeeId);

    //门禁配置---功能配置（首卡权限配置）
    void handOutFirstCard(String doorId, String enableFirstCardKeepOpen, List<String> employeeIdList, List oneWeekTimeFirstCardList,
                          String operatorEmployeeId);

    //门禁配置---功能配置（门禁日历）
    void handOutDoorCalendar(String doorId, String enableDoorCalendar, List accessCalendar, String operatorEmployeeId);

    //门禁记录上传存储
    void doorRecordSave(String doorRecordMap);

    //根据公司id查门列表
    List<Door> findDoorIdByCompanyId(String companyId);

    /**
     *  TODO APP接口（获取员工的打卡记录）
     */
    List<Map> queryEmpPunchCardRecord(String requestParam,String companyId);

    ReturnData handOutDoorFeaturesSetup(String doorFeaturesSetup, String operatorEmployeeId);

    //下发首卡常开命令
    void handOutFirstCardCmd(String deviceId, String operatorEmployeeId, List<String> employeeIdList, List oneWeekTimeFirstCardListTemp);

    //下发门禁基本设置命令
    void doorCommonSetupAdditionalCmd(String deviceId, String operatorEmployeeId, Map<String, Object> doorSetupMap);

    //下发门禁日历命令
    void handOutDoorCalendarCmd(String deviceId, String operatorEmployeeId, List accessCalendar);
}
