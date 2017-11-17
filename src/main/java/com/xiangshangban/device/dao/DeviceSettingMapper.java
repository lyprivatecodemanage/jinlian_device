package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceSetting;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceSettingMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(DeviceSetting record);

    int insertSelective(DeviceSetting record);

    DeviceSetting selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(DeviceSetting record);

    int updateByPrimaryKey(DeviceSetting record);
}