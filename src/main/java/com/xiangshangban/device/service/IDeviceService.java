package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.Device;

import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 业务层：设备管理
 */

public interface IDeviceService {

    /**
     * 平台新增设备
     * @param deviceId
     * @param macAddress
     */
    void addDevice(String deviceId, String macAddress);

    /**
     * 查找当前公司的设备信息(包括筛选功能，无参传入查询全部设备)
     * @param companyName
     * @param deviceName
     * @param deviceId
     * @param isOnline
     * @param activeStatus
     * @return
     */
    List<Map<String, String>> findDeviceInformation(String companyId, String companyName, String deviceName,
                                                    String deviceId, String  isOnline, String activeStatus);

    /**
     * 平台管理员编辑当前设备的信息
     * @param deviceId
     * @param deviceName
     * @param doorName
     * @param devicePlace
     * @param deviceUsages
     * @return
     */
    int editorDeviceInformation(String deviceId, String deviceName, String doorName,
                                String devicePlace, String deviceUsages);

    /**
     * 下发重启命令，重启指定的设备
     * @param deviceId
     */
    void rebootDevice(String deviceId);

    /**
     * 查询所有的设备信息
     */
    List<Device> queryAllDeviceInfo(String companyId);

    /**
     * 绑定设备
     */
    void bindDevice(String companyId, String companyName, String deviceId);

    /**
     * 解绑设备
     */
    void unBindDevice(String deviceId);

    /**
     * 上传设备重启记录
     */
    void deviceRebootRecordSave(String jsonString, String deviceId);

    /**
     * 上传设备运行日志
     */
    void deviceRunningLogSave(String jsonString, String deviceId);

    /**
     * CRC16校验设备id
     */
    boolean checkCrc16DeviceId(String deviceId);

}
