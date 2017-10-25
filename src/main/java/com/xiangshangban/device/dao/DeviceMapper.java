package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Device;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DeviceMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(Device record);

    int insertSelective(Device record);

    Device selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(Device record);

    int updateByPrimaryKey(Device record);

    List<Device> findByCondition(Device device);

    List<String> findDeviceIdByCompanyId(String companyId);
}