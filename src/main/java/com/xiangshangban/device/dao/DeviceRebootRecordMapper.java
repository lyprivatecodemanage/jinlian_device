package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceRebootRecord;
import org.apache.ibatis.annotations.Param;

public interface DeviceRebootRecordMapper {
    int deleteByPrimaryKey(String rebootId);

    int insert(DeviceRebootRecord record);

    int insertSelective(DeviceRebootRecord record);

    DeviceRebootRecord selectByPrimaryKey(String rebootId);

    int updateByPrimaryKeySelective(DeviceRebootRecord record);

    int updateByPrimaryKey(DeviceRebootRecord record);

    /**
     * 非自动生成
     */
    DeviceRebootRecord selectByRebootIdAndDeviceId(@Param("rebootId") String rebootId,
                                                   @Param("deviceId") String deviceId);
}