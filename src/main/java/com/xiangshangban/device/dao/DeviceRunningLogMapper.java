package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRunningLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceRunningLogMapper {
    int deleteByPrimaryKey(String logId);

    int insert(DeviceRunningLog record);

    int insertSelective(DeviceRunningLog record);

    DeviceRunningLog selectByPrimaryKey(String logId);

    int updateByPrimaryKeySelective(DeviceRunningLog record);

    int updateByPrimaryKey(DeviceRunningLog record);
}