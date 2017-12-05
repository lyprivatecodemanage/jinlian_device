package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.TimeRangePrivilegeEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TimeRangePrivilegeEmployeeMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(TimeRangePrivilegeEmployee record);

    int insertSelective(TimeRangePrivilegeEmployee record);

    TimeRangePrivilegeEmployee selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(TimeRangePrivilegeEmployee record);

    int updateByPrimaryKey(TimeRangePrivilegeEmployee record);

    //根据人员id查询是否存在数据
    List<TimeRangePrivilegeEmployee> selectExitByEmployeeId(String employeeId);

    //根据人员id删除查到的数据
    int deleteByEmployeeId(String employeeId);
    /**
     * 根据门的id查询首卡常开信息
     */
    List<Map> selectFirstCardKeepOpenInfo(@Param("doorId") String doorId);
}