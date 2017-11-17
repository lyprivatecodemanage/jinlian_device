package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.TimeRangeLcdOff;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TimeRangeLcdOffMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(TimeRangeLcdOff record);

    int insertSelective(TimeRangeLcdOff record);

    List<TimeRangeLcdOff> selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(TimeRangeLcdOff record);

    int updateByPrimaryKey(TimeRangeLcdOff record);
}