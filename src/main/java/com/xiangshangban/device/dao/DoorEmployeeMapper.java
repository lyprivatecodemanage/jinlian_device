package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorEmployee;
import com.xiangshangban.device.bean.RelateEmpPermissionCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
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
    List<Map> queryDoorEmployeeInfo(@Param("doorEmployee") DoorEmployee doorEmployee);

    /**
     * 查询命令下发时间
     */
    List<Map> selectSendTime(@Param("doorName") String doorName);

    /**
     * 根据设备id查询门id
     */
    String selectDoorIdByDeviceId(@Param("deviceId") String deviceId);


    //点击门图标，显示门关联的人员的权限信息

    /**
     *获取门关联人员权限信息（姓名、部门、开门时间，开门方式）
     */
    List<Map> selectRelateEmpPermissionInfo(@Param("relateEmpPermissionCondition")RelateEmpPermissionCondition relateEmpPermissionCondition);

    /**
     * 获取设备命令信息(下发时间，下发状态，下发的数据)
     */
    List<Map> selectCMDInfo(@Param("relateEmpPermissionCondition")RelateEmpPermissionCondition relateEmpPermissionCondition);

    //批量删除人员权限信息

    /**
     * 批量删除
     */

}