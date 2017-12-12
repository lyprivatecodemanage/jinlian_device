package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DoorEmployeeMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(DoorEmployee record);

    int insertSelective(DoorEmployee record);

    DoorEmployee selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(DoorEmployee record);

    int updateByPrimaryKey(DoorEmployee record);

    /**
     * 查询所有字段是否存在
     */
    DoorEmployee findIfExist(DoorEmployee doorEmployee);

    //TODO ==============门禁管理(授权中心)=================

    //首页搜索显示部分
    /**
     * 获取门信息，以及门关联的人员信息(根据门名称)
     */
    List<Map> queryDoorEmployeeInfo(Map map);

    /**
     * 查询命令下发时间
     */
    String selectSendTime(@Param("doorId") String doorId);

    /**
     * 根据设备id查询门id
     */
   /* String selectDoorIdByDeviceId(@Param("deviceId") String deviceId);*/


    //点击门图标，显示门关联的人员的权限信息

    /**
     *获取门关联人员相关联的指令的最新下发时间和状态
     */
    List<Map> selectRelateEmpCommand(Map map);

    /**
     * 获取门关联的人员基本信息以及周一最早的打卡时间、打卡方式
     */
    List<Map> selectMondayPunchCardTimeAndEmpInfo(Map map);

    /**
     *查询有门禁权限的人员一周的开门时间段
     */
    List<Map> selectAWeekOpenTime(Map map);

    /**
     *通过人员id和门id查询
     */
    DoorEmployee selectByEmployeeIdAndDoorId(@Param("employeeId") String employeeId,
                                             @Param("doorId") String doorId);

    /**
     * 删除人员和门的关联通过rangeFlagId
     */
    int deleteByRangeFlagId(String rangeFlagId);

    /**
     * 删除人员和门的关联通过doorId和employeeId
     */
    int deleteByDoorIdAndEmployeeId(Map map);

    /**
     * 判断当前门当前人员信息是否在door_employee表中存在
     */
    String getDoorEmpByDoorIdAndEmpId(Map map);

    /**
     * 添加人员权限信息到door_employee表中
     */
    int insertEmpInfoToDoorEmployee(Map map);

    /**
     * 更新人员权限信息
     */
    int updateEmpInfoToDoorEmployee(Map map);
}