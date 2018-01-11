package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRunningLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceRunningLogMapper {
    int deleteByPrimaryKey(String logId);

    int insert(DeviceRunningLog record);

    int insertSelective(DeviceRunningLog record);

    DeviceRunningLog selectByPrimaryKey(String logId);

    int updateByPrimaryKeySelective(DeviceRunningLog record);

    int updateByPrimaryKey(DeviceRunningLog record);

    /**
     * 非自动生成
     */
    DeviceRunningLog selectByLogIdAndDeviceId(@Param("logId") String logId,
                                              @Param("deviceId") String deviceId);
}