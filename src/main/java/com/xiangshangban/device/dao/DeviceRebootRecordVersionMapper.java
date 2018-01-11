package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRebootRecordVersion;

import java.util.List;

public interface DeviceRebootRecordVersionMapper {
    int deleteByPrimaryKey(String rebootId);

    int insert(DeviceRebootRecordVersion record);

    int insertSelective(DeviceRebootRecordVersion record);

    List<DeviceRebootRecordVersion> selectByPrimaryKey(String rebootId);

    int updateByPrimaryKeySelective(DeviceRebootRecordVersion record);

    int updateByPrimaryKey(DeviceRebootRecordVersion record);
}