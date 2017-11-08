package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceHeartbeat;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceHeartbeatMapper {
    int insert(DeviceHeartbeat record);

    int insertSelective(DeviceHeartbeat record);
}