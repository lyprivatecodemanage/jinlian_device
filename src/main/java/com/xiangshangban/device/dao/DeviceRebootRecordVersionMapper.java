package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRebootRecordVersion;

public interface DeviceRebootRecordVersionMapper {
    int deleteByPrimaryKey(String rebootId);

    int insert(DeviceRebootRecordVersion record);

    int insertSelective(DeviceRebootRecordVersion record);

    DeviceRebootRecordVersion selectByPrimaryKey(String rebootId);

    int updateByPrimaryKeySelective(DeviceRebootRecordVersion record);

    int updateByPrimaryKey(DeviceRebootRecordVersion record);
}