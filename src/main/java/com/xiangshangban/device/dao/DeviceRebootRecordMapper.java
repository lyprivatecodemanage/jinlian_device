package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRebootRecord;

public interface DeviceRebootRecordMapper {
    int deleteByPrimaryKey(String rebootId);

    int insert(DeviceRebootRecord record);

    int insertSelective(DeviceRebootRecord record);

    DeviceRebootRecord selectByPrimaryKey(String rebootId);

    int updateByPrimaryKeySelective(DeviceRebootRecord record);

    int updateByPrimaryKey(DeviceRebootRecord record);
}