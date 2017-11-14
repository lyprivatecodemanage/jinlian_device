package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceHeartbeat;
import org.apache.ibatis.annotations.Mapper;

import java.util.Map;

@Mapper
public interface DeviceHeartbeatMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(DeviceHeartbeat record);

    int insertSelective(DeviceHeartbeat record);

    DeviceHeartbeat selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(DeviceHeartbeat record);

    int updateByPrimaryKey(DeviceHeartbeat record);

    /**
     * 非自动生成
     */

    //查询最新的一条心跳信息根据设备Id
    Map<String, Object> selectLatestByDeviceId(String deviceId);
}