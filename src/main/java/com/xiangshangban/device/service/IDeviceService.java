package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.Device;

import java.util.List;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：设备管理
 */

public interface IDeviceService {

    void addDevice(String companyId, String deviceNumber, String macAddress);

    void findDeviceInformation(String companyName, String deviceName, String deviceNumber,
                               String  isOnline, String activeStatus);

    /**
     * 查询所有的设备信息
     */
    List<Device> queryAllDeviceInfo();

    /**
     * 绑定设备
     */
    void bindDevice();

    /**
     * 解绑设备
     */
    void unBindDevice();

}
