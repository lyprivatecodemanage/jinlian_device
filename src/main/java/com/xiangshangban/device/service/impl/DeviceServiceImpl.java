package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.Device;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.dao.DeviceMapper;
import com.xiangshangban.device.service.IDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.service.IEntranceGuardService;

import java.util.*;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 设备管理实现类
 */

@Service
public class DeviceServiceImpl implements IDeviceService {

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private IEntranceGuardService entranceGuardService;

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

    public List<Device> queryAllDeviceInfo() {
        List<Device> devices = deviceMapper.selectAllDeviceInfo();
        return devices;
    }

    @Override
    public void bindDevice(String companyId, String companyName, String deviceId) {

        Map<String, Object> bindInformation = new LinkedHashMap<String, Object>();
        bindInformation.put("companyId", companyId);
        bindInformation.put("companyName", companyName);

        //绑定关系存入数据库
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setCompanyId(companyId);
        device.setCompanyName(companyName);
        //根据deviceId判断设备信息是否存在
        Device deviceExist = deviceMapper.selectByPrimaryKey(deviceId);
        if (deviceExist == null){
            //新增设备信息
            deviceMapper.insertSelective(device);
        }else {
            //更新设备信息
            deviceMapper.updateByPrimaryKeySelective(device);
        }

        //构造命令格式
        DoorCmd doorCmdBindDevice = new DoorCmd();
        doorCmdBindDevice.setServerId("001");
        doorCmdBindDevice.setDeviceId(deviceId);
        doorCmdBindDevice.setFileEdition("v1.3");
        doorCmdBindDevice.setCommandMode("C");
        doorCmdBindDevice.setCommandType("single");
        doorCmdBindDevice.setCommandTotal("1");
        doorCmdBindDevice.setCommandIndex("1");
        doorCmdBindDevice.setSubCmdId("");
        doorCmdBindDevice.setAction("BIND_DEVICE");
        doorCmdBindDevice.setActionCode("1001");
        doorCmdBindDevice.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdBindDevice.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdBindDevice.setSuperCmdId(FormatUtil.createUuid());
        doorCmdBindDevice.setData(JSON.toJSONString(bindInformation));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdBindDevice, "bindInformation", bindInformation, "C");
        //命令状态设置为: 发送中
        doorCmdBindDevice.setStatus("1");
        //设置md5校验值
        doorCmdBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdBindDevice);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);

    }

    @Override
    public void unBindDevice(String deviceId) {

        //构造命令格式
        DoorCmd doorCmdUnBindDevice = new DoorCmd();
        doorCmdUnBindDevice.setServerId("001");
        doorCmdUnBindDevice.setDeviceId(deviceId);
        doorCmdUnBindDevice.setFileEdition("v1.3");
        doorCmdUnBindDevice.setCommandMode("C");
        doorCmdUnBindDevice.setCommandType("single");
        doorCmdUnBindDevice.setCommandTotal("1");
        doorCmdUnBindDevice.setCommandIndex("1");
        doorCmdUnBindDevice.setSubCmdId("");
        doorCmdUnBindDevice.setAction("UNBIND_DEVICE");
        doorCmdUnBindDevice.setActionCode("1002");
        doorCmdUnBindDevice.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdUnBindDevice.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdUnBindDevice.setSuperCmdId(FormatUtil.createUuid());
        doorCmdUnBindDevice.setData("");

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdUnBindDevice, "", "", "NULLDATA");
        //命令状态设置为: 发送中
        doorCmdUnBindDevice.setStatus("1");
        //设置md5校验值
        doorCmdUnBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdUnBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdUnBindDevice);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);

    }

}
