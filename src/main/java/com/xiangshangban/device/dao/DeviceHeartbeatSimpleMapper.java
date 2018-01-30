package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceHeartbeatSimple;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceHeartbeatSimpleMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(DeviceHeartbeatSimple record);

    int insertSelective(DeviceHeartbeatSimple record);

    DeviceHeartbeatSimple selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(DeviceHeartbeatSimple record);

    int updateByPrimaryKey(DeviceHeartbeatSimple record);

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
}