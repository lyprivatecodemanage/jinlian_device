package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Device;

import java.util.List;
import java.util.Map;

public interface DeviceMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(Device record);

    int insertSelective(Device record);

    Device selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(Device record);

    int updateByPrimaryKey(Device record);

    List<Map<String, Object>> findByCondition(Device device);

    List<String> findDeviceIdByCompanyId(String companyId);

    /**
     * 查询所有的设备信息
     */
    List<Device> selectAllDeviceInfo();
}