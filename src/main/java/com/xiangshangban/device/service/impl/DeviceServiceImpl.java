package com.xiangshangban.device.service.impl;

import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.dao.DeviceMapper;
import com.xiangshangban.device.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 设备管理实现类
 */

@Service
public class DeviceServiceImpl implements IDeviceService{

    @Autowired
    private DeviceMapper deviceMapper;

    @Override
    public void addDevice(String companyId, String deviceNumber, String macAddress) {

        Device device = new Device();

        //新增设备信息
        device.setCompanyId(companyId);
        device.setDeviceNumber(deviceNumber);
        device.setMacAddress(macAddress);

        deviceMapper.insert(device);

    }

    @Override
    public void findDeviceInformation(String companyName, String deviceName, String deviceNumber,
                                      String isOnline, String activeStatus) {

        Device device = new Device();

        //设备信息筛选条件
        device.setCompanyName(companyName);
        device.setDeviceName(deviceName);
        device.setDeviceNumber(deviceNumber);
        device.setIsOnline(isOnline);
        device.setActiveStatus(activeStatus);

        deviceMapper.findByCondition(device);

    }

}
