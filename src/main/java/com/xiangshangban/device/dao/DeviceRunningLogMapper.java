package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRunningLog;

public interface DeviceRunningLogMapper {
    int deleteByPrimaryKey(String logId);

    int insert(DeviceRunningLog record);

    int insertSelective(DeviceRunningLog record);

    DeviceRunningLog selectByPrimaryKey(String logId);

    int updateByPrimaryKeySelective(DeviceRunningLog record);

    int updateByPrimaryKey(DeviceRunningLog record);
}