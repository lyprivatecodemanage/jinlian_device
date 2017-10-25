package com.xiangshangban.device.service;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：设备管理
 */

public interface IDeviceService {

    void addDevice(String companyId, String deviceNumber, String macAddress);

    void findDeviceInformation(String companyName, String deviceName, String deviceNumber,
                               String  isOnline, String activeStatus);

}
