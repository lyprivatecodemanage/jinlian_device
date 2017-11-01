package com.xiangshangban.device.controller;

import com.xiangshangban.device.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import com.alibaba.fastjson.JSONArray;
import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.dao.DoorMapper;

import java.util.List;

/**
 * 控制层：设备操作
 */

@Controller
@RequestMapping(value = "/device")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private DoorMapper doorMapper;

    /**
     * 平台新增设备，无绑定的设备
     * @param companyId
     * @param deviceNumber
     * @param macAddress
     */
    @ResponseBody
    @RequestMapping(value = "/addDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void addDevice(String companyId, String deviceNumber, String macAddress){

        deviceService.addDevice(companyId, deviceNumber, macAddress);

    }

    /**
     * 查找当前公司的设备信息(包括筛选功能，无参传入查询全部设备)
     * @param companyName
     * @param deviceName
     * @param deviceNumber
     * @param isOnline
     * @param activeStatus
     */
    @ResponseBody
    @RequestMapping(value = "/findDeviceInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void findDeviceInformation(String companyName, String deviceName, String deviceNumber,
                                      String  isOnline, String activeStatus){

        deviceService.findDeviceInformation(companyName, deviceName, deviceNumber, isOnline, activeStatus);

    }

    /**
     * 重启指定的设备(下发重启设备的命令)
     * @param deviceId
     */
    @ResponseBody
    @RequestMapping(value = "/rebootDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void rebootDevice(String deviceId){

    }

    /**
     * 查询所有的设备信息
     */
    @ResponseBody
    @RequestMapping("/getAllDevice.do")
    public String getAllDeviceInfo(){
        List<Device> devices = deviceService.queryAllDeviceInfo();
        return JSONArray.toJSONString(devices);
    }

    /**
     * 绑定设备
     */
    @ResponseBody
    @RequestMapping("/bindDevice")
    public void bindDevice(String companyId, String companyName, String deviceId){

        deviceService.bindDevice(companyId, companyName, deviceId);

    }

    /**
     * 解绑设备
     */
    @ResponseBody
    @RequestMapping("/unBindDevice")
    public void unBindDevice(String doorId){

        //查找设备id
        String deviceId = doorMapper.findAllByDoorId(doorId).getDeviceId();

        deviceService.unBindDevice(deviceId);

    }

}
