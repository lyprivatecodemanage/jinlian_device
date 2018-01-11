package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.Door;
import com.xiangshangban.device.bean.ReturnData;

import javax.servlet.http.HttpServletRequest;
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
     */
    String addDevice(String deviceId);

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
                                                    String deviceId, String  isOnline, String activeStatus,
                                                    String employeeCompanyId);

    /**
     * 查询所有的设备信息
     * @param companyId
     * @return
     */
    List<Map> queryAllDeviceInfo(String companyId);

    /**
     * 查询所有的门信息
     * @param companyId
     * @return
     */
    List<Door> queryAllDoorInfoByCompanyId(String companyId);

    /**
     * 绑定设备
     * @param deviceId
     */
    void bindDevice(String deviceId, String companyId, String companyName);

    /**
     * 解绑设备
     * @param oldDeviceId
     * @param newDeviceId
     * @param unBindType
     * @return
     */
    ReturnData unBindDevice(String oldDeviceId, String newDeviceId, String unBindType, HttpServletRequest request);

    /**
     * 解绑设备时要执行的本地删除操作（假删除，改变数据状态位）
     * @param deviceId
     */
    void unBindDeviceDeleteOperation(String deviceId);

    /**
     * 上传设备重启记录
     * @param jsonString
     * @param deviceId
     */
    void deviceRebootRecordSave(String jsonString, String deviceId);

    /**
     * 上传设备运行日志
     * @param jsonString
     * @param deviceId
     */
    void deviceRunningLogSave(String jsonString, String deviceId);

    /**
     * 定时下载升级更新设备系统
     * @param deviceIdList
     * @param downloadTime
     * @param updateTime
     */
    void updateDeviceSystem(List<String> deviceIdList, String downloadTime, String updateTime);

    /**
     * 定时下载升级更新设备应用
     * @param deviceIdList
     * @param downloadTime
     * @param updateTime
     * @return
     */
    void updateDeviceApplication(List<String> deviceIdList, String downloadTime, String updateTime);

    /**
     * CRC16校验设备id
     * @param deviceId
     * @return
     */
    boolean checkCrc16DeviceId(String deviceId);

    /**
     * 解绑门和设备时，同步当前门的信息到新设备上
     * @param newDeviceId
     */
    void synchronizationDoorInfoToNewDevice(String doorId, String newDeviceId, String operatorEmployeeId);

    /**
     * 下发设备系统设置
     * @param jsonString
     * @return
     */
    ReturnData handOutDeviceSetting(String jsonString);
}
