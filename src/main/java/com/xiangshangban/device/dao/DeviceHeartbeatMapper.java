package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceHeartbeat;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
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
    List<Map<String, Object>> selectLatestByDeviceId(@Param("companyName") String companyName,
                                               @Param("averageCpuUserUnilization") float averageCpuUserUnilization,
                                               @Param("averageCpuTemper") float averageCpuTemper,
                                               @Param("cpuUserUnilizationCondition") String cpuUserUnilizationCondition,
                                               @Param("cpuTemperCondition") String cpuTemperCondition);

    //查询最新的一条心跳信息的时间根据设备id
    List<Map<String, String>> selectLatestTimeByDeviceId(String deviceId);

    Map<String, String> temp(String deviceId);
}