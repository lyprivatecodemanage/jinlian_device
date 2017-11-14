package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceSettingUpdate;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceSettingUpdateMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(DeviceSettingUpdate record);

    int insertSelective(DeviceSettingUpdate record);

    DeviceSettingUpdate selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(DeviceSettingUpdate record);

    int updateByPrimaryKey(DeviceSettingUpdate record);
}