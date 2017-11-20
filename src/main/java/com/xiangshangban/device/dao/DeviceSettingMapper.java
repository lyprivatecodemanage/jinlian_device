package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceSetting;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceSettingMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(DeviceSetting record);

    int insertSelective(DeviceSetting record);

    DeviceSetting selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(DeviceSetting record);

    int updateByPrimaryKey(DeviceSetting record);

    /**
     * 非自动生成
     */
    List<Map<String, Object>> selectDeviceSettingByCondition(@Param("deviceId") String deviceId,
                                                             @Param("activeStatus") String activeStatus,
                                                             @Param("companyName") String companyName);
}