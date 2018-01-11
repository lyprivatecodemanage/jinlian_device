package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.TimeRangeCommonEmployee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TimeRangeCommonEmployeeMapper {
    int deleteByPrimaryKey(String rangeFlagId);

    int insert(TimeRangeCommonEmployee record);

    int insertSelective(TimeRangeCommonEmployee record);

    TimeRangeCommonEmployee selectByPrimaryKey(String rangeFlagId);

    int updateByPrimaryKeySelective(TimeRangeCommonEmployee record);

    int updateByPrimaryKey(TimeRangeCommonEmployee record);

    /**
     * 非自动生成
     */

    TimeRangeCommonEmployee findIfExist(TimeRangeCommonEmployee timeRangeCommonEmployee);

    int updateByEmployeeId(TimeRangeCommonEmployee timeRangeCommonEmployee);

    List<TimeRangeCommonEmployee> selectExistByEmployeeId(String employeeId);

    int deleteByEmployeeId(String employeeId);

    /**
     * 删除门相关的人员的开门时间段信息
     */
    int delCommonEmpOpenTime(@Param("doorId") String doorId);
}