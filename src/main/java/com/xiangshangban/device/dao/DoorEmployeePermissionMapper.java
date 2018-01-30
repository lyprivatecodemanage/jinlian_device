package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorEmployeePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DoorEmployeePermissionMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(DoorEmployeePermission record);

    int insertSelective(DoorEmployeePermission record);

    DoorEmployeePermission selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(DoorEmployeePermission record);

    int updateByPrimaryKey(DoorEmployeePermission record);


    /**
     * 非自动生成
     */

    /**
     * 根据rangeFlagId删除所有数据
     *
     * @param rangeFlagId
     * @return
     */
    int deleteByRangeFlagId(String rangeFlagId);

    /**
     * 联查人和门关联表以及人员开门权限表
     */
    Map<String, String> selectEmployeePressionByLeftJoin(@Param("employeeId") String employeeId,
                                                         @Param("doorId") String doorId);

    /**
     * 删除门关联的人员权限信息（是否首卡常开、以及有效开始时间和结束时间）
     */
    int delDoorEmpPermission(@Param("doorId") String doorId);

    /**
     * 根据rangeFlagId查询人员开门权限有效年月日时间
     * @param rangeFlagId
     * @return
     */
    DoorEmployeePermission selectByRangeFlagId(String rangeFlagId);

    int updateByRangeFlagIdSelective(DoorEmployeePermission doorEmployeePermission);

    int deleteByEmployeeIdAndDeviceId(@Param("employeeId") String employeeId,
                                      @Param("deviceId") String deviceId);

    List<DoorEmployeePermission> temp();

    List<DoorEmployeePermission> temp1(String deviceId);
}