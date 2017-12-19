package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorEmployeePermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
     * @param rangeFlagId
     * @return
     */
    int deleteByRangeFlagId(String rangeFlagId);

    /**
     * 联查人和门关联表以及人员开门权限表
     */
    Map<String, String> selectEmployeePressionByLeftJoin(@Param("employeeId") String employeeId,
                                                         @Param("doorId") String doorId);
}