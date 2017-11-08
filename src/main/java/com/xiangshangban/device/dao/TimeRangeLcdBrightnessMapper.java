package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.TimeRangeLcdBrightness;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TimeRangeLcdBrightnessMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(TimeRangeLcdBrightness record);

    int insertSelective(TimeRangeLcdBrightness record);

    List<TimeRangeLcdBrightness> selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(TimeRangeLcdBrightness record);

    int updateByPrimaryKey(TimeRangeLcdBrightness record);
}