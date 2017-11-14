package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.CRC16;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IDeviceService;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    @Autowired
    private DeviceRebootRecordMapper deviceRebootRecordMapper;

    @Autowired
    private DeviceRebootRecordVersionMapper deviceRebootRecordVersionMapper;

    @Autowired
    private DeviceRunningLogMapper deviceRunningLogMapper;

    @Override
    public void addDevice(String deviceId, String macAddress) {

        Device device = new Device();

        //新增设备信息
        device.setDeviceId(deviceId);
        device.setMacAddress(macAddress);

        Device deviceExist = deviceMapper.selectByPrimaryKey(deviceId);

        if (deviceExist == null){
            System.out.println(deviceMapper.insert(device));
        }else {
            System.out.println("设备已存在");
        }

    }

    @Override
    public List<Map<String, String>> findDeviceInformation(String companyId, String companyName, String deviceName, String deviceId,
                                      String isOnline, String activeStatus) {

        Device device = new Device();

        //设备信息筛选条件
        device.setCompanyId(companyId);
        device.setCompanyName(companyName);
        device.setDeviceName(deviceName);
        device.setDeviceId(deviceId);//设备编号就是deviceId
        device.setIsOnline(isOnline);
        device.setActiveStatus(activeStatus);

        List<Map<String, String>> mapList = deviceMapper.findByCondition(device);

        System.out.println(JSON.toJSONString(mapList));
        for (Map<String, String> map : mapList) {
            String isOnlineTemp = map.get("is_online");
            String activeStatusTemp = map.get("active_status");

            if (isOnlineTemp.equals("0")){
                map.put("is_online", "在线");
            }else if (isOnline.equals("1")){
                map.put("is_online", "离线");
            }

            if (activeStatusTemp.equals("0")){
                map.put("active_status", "待激活");
            }else if (activeStatusTemp.equals("1")){
                map.put("active_status", "待完善");
            }else if (activeStatusTemp.equals("2")){
                map.put("active_status", "使用中");
            }else if (activeStatusTemp.equals("3")){
                map.put("active_status", "已欠费");
            }else if (activeStatusTemp.equals("4")){
                map.put("active_status", "故障中");
            }
        }

        System.out.println("------------"+JSON.toJSONString(mapList));

        return mapList;

    }

    @Override
    public int editorDeviceInformation(String deviceId, String deviceName, String doorName,
                                       String devicePlace, String deviceUsages) {

        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setDeviceName(deviceName);
        device.setDevicePlace(devicePlace);
        device.setDeviceUsages(deviceUsages);

        Door door = new Door();
        door.setDoorId(doorMapper.findDoorIdByDeviceId(deviceId).getDoorId());
        door.setDoorName(doorName);

        deviceMapper.updateByPrimaryKeySelective(device);

        doorMapper.updateByPrimaryKeySelective(door);

        return 0;
    }

    @Override
    public void rebootDevice(String deviceId) {

        //构造命令格式
        DoorCmd doorCmdRebootDevice = new DoorCmd();
        doorCmdRebootDevice.setServerId("001");
        doorCmdRebootDevice.setDeviceId(deviceId);
        doorCmdRebootDevice.setFileEdition("v1.3");
        doorCmdRebootDevice.setCommandMode("C");
        doorCmdRebootDevice.setCommandType("single");
        doorCmdRebootDevice.setCommandTotal("1");
        doorCmdRebootDevice.setCommandIndex("1");
        doorCmdRebootDevice.setSubCmdId("");
        doorCmdRebootDevice.setAction("REBOOT_DEVICE");
        doorCmdRebootDevice.setActionCode("1007");
        doorCmdRebootDevice.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdRebootDevice.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdRebootDevice.setSuperCmdId(FormatUtil.createUuid());
        doorCmdRebootDevice.setData("");

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdRebootDevice, "", "", "NULLDATA");
        //命令状态设置为: 发送中
        doorCmdRebootDevice.setStatus("1");
        //设置md5校验值
        doorCmdRebootDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdRebootDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdRebootDevice);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);

    }

    public List<Device> queryAllDeviceInfo(String companyId) {

        return deviceMapper.selectAllDeviceInfo(companyId);
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

    @Override
    public void deviceRebootRecordSave(String jsonString, String deviceId) {

        //解析json数据
        Map<String, Object> mapJson = JSONObject.fromObject(jsonString);
        List<Map<String, Object>> rebootRecordList = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> rebootRecordVersionList = new ArrayList<Map<String, Object>>();
        String rebootId;
        List<String> rebootIdList = new ArrayList<>();
        String resultCode;
        String resultMessage;

        rebootRecordList = (List<Map<String, Object>>) mapJson.get("record");
        for (Map<String, Object> singleRecordMap : rebootRecordList) {

            rebootId = (String) singleRecordMap.get("rebootId");
            rebootIdList.add(rebootId);

            DeviceRebootRecord deviceRebootRecord = new DeviceRebootRecord();
            deviceRebootRecord.setRebootId(rebootId);
            deviceRebootRecord.setDeviceId((String) singleRecordMap.get("deviceId"));
            deviceRebootRecord.setRebootNumber((String) singleRecordMap.get("rebootNumber"));
            deviceRebootRecord.setRebootTime((String) singleRecordMap.get("rebootTime"));

            DeviceRebootRecord deviceRebootRecordExist = deviceRebootRecordMapper.selectByPrimaryKey(rebootId);
            if (deviceRebootRecordExist == null){
                deviceRebootRecordMapper.insertSelective(deviceRebootRecord);
            }else {
                deviceRebootRecordMapper.updateByPrimaryKeySelective(deviceRebootRecord);
            }

            //删除该记录对应的所有硬件版本记录信息
            List<DeviceRebootRecordVersion> deviceRebootRecordVersionExist = deviceRebootRecordVersionMapper.selectByPrimaryKey(rebootId);
            if (deviceRebootRecordVersionExist.size() > 0){
                deviceRebootRecordVersionMapper.deleteByPrimaryKey(rebootId);
            }
            rebootRecordVersionList = (List<Map<String, Object>>) singleRecordMap.get("version");
            for (Map<String, Object> singleRecordVersionMap : rebootRecordVersionList) {

                DeviceRebootRecordVersion deviceRebootRecordVersion = new DeviceRebootRecordVersion();
                deviceRebootRecordVersion.setRebootId(rebootId);
                deviceRebootRecordVersion.setName((String) singleRecordVersionMap.get("name"));
                deviceRebootRecordVersion.setValue((String) singleRecordVersionMap.get("value"));

                //重新插入该记录对应的所有硬件版本记录信息
                deviceRebootRecordVersionMapper.insertSelective(deviceRebootRecordVersion);
            }
        }

        //回复设备重启记录上传
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        resultCode = "0";
        resultMessage = "执行成功";
        resultData.put("resultCode", resultCode);
        resultData.put("resultMessage", resultMessage);
        resultData.put("returnObj", rebootIdList);
        resultMap.put("result", resultData);

        //构造命令格式
        DoorCmd doorCmdRecord = new DoorCmd();
        doorCmdRecord.setServerId("001");
        doorCmdRecord.setDeviceId(deviceId);
        doorCmdRecord.setFileEdition("v1.3");
        doorCmdRecord.setCommandMode("R");
        doorCmdRecord.setCommandType("S");
        doorCmdRecord.setCommandTotal("1");
        doorCmdRecord.setCommandIndex("1");
        doorCmdRecord.setSubCmdId("");
        doorCmdRecord.setAction("UPLOAD_DEVICE_REBOOT_RECORD");
        doorCmdRecord.setActionCode("1004");
        doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdRecord.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
        doorCmdRecord.setData(JSON.toJSONString(resultMap));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorRecordAll =  rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
        //命令状态设置为: 已回复
        doorCmdRecord.setStatus("5");
        //设置md5校验值
        doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
        doorCmdRecord.setResultCode(resultCode);
        doorCmdRecord.setResultMessage(resultMessage);
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdRecord);
        //立即下发回复数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorRecordAll);
        System.out.println("重启记录已回复");
    }

    @Override
    public void deviceRunningLogSave(String jsonString, String deviceId) {

        //解析json数据
        Map<String, Object> mapJson = JSONObject.fromObject(jsonString);
        List<Map<String, String>> deviceRunningLogList = (List<Map<String, String>>) mapJson.get("log");
        List<String> logIdList = new ArrayList<String>();
        String resultCode;
        String resultMessage;

        for (Map<String, String> deviceRunningLogMap : deviceRunningLogList) {

            String logId = deviceRunningLogMap.get("logId");
            logIdList.add(logId);

            DeviceRunningLog deviceRunningLog = new DeviceRunningLog();
            deviceRunningLog.setLogId(logId);
            deviceRunningLog.setLogLevel(deviceRunningLogMap.get("logLevel"));
            deviceRunningLog.setLogType(deviceRunningLogMap.get("logType"));
            deviceRunningLog.setLogContent(deviceRunningLogMap.get("logContent"));
            deviceRunningLog.setLogTime(deviceRunningLogMap.get("logTime"));

            DeviceRunningLog deviceRunningLogExist = deviceRunningLogMapper.selectByPrimaryKey(logId);
            if (deviceRunningLogExist == null){
                deviceRunningLogMapper.insertSelective(deviceRunningLog);
            }else {
                deviceRunningLogMapper.updateByPrimaryKeySelective(deviceRunningLog);
            }
        }

        //回复设备重启记录上传
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        resultCode = "0";
        resultMessage = "执行成功";
        resultData.put("resultCode", resultCode);
        resultData.put("resultMessage", resultMessage);
        resultData.put("returnObj", logIdList);
        resultMap.put("result", resultData);

        //构造命令格式
        DoorCmd doorCmdRecord = new DoorCmd();
        doorCmdRecord.setServerId("001");
        doorCmdRecord.setDeviceId(deviceId);
        doorCmdRecord.setFileEdition("v1.3");
        doorCmdRecord.setCommandMode("R");
        doorCmdRecord.setCommandType("S");
        doorCmdRecord.setCommandTotal("1");
        doorCmdRecord.setCommandIndex("1");
        doorCmdRecord.setSubCmdId("");
        doorCmdRecord.setAction("UPLOAD_DEVICE_RUNNING_LOG");
        doorCmdRecord.setActionCode("1006");
        doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdRecord.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
        doorCmdRecord.setData(JSON.toJSONString(resultMap));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorRecordAll =  rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
        //命令状态设置为: 已回复
        doorCmdRecord.setStatus("5");
        //设置md5校验值
        doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
        doorCmdRecord.setResultCode(resultCode);
        doorCmdRecord.setResultMessage(resultMessage);
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdRecord);
        //立即下发回复数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorRecordAll);
        System.out.println("运行日志上传已回复");
    }

    /**
     * CRC16校验设备id
     */
    @Override
    public boolean checkCrc16DeviceId(String deviceId) {

        //去除deviceId尾部的-Crc16得到的字符串
        String deviceIdAhead = deviceId.substring(0, deviceId.length()-5);

        //对方的Crc16
        String otherCrc16= deviceId.substring(deviceId.length()-4, deviceId.length());

        //我方的Crc16
        String myCrc16 = String.format("%04x", CRC16.calcCrc16(deviceIdAhead.getBytes()));

        //比对双方的Crc16
        if (myCrc16.equals(otherCrc16)){
            System.out.println("CRC16校验成功，数据完好无损");
            return true;
        }else {
            System.out.println("CRC16校验失败，数据已被修改");
            return false;
        }

    }

    public static void main(String[] args) {

        String deviceId = "0f1a21d4e6fd3cb8-1-2-6277";

        String deviceIdAhead = deviceId.substring(0, deviceId.length()-5);
        String deviceIdBehind = deviceId.substring(deviceId.length()-5, deviceId.length());
        String deviceIdTruth = deviceId.substring(0, 16);

        System.out.println(deviceIdAhead+"*******"+deviceIdBehind);
        System.out.println(deviceId.length()+"*******"+(deviceId.length()-5));
        System.out.println(deviceIdTruth);

        System.out.println("十六进制："+String.format("%04x", CRC16.calcCrc16(deviceIdAhead.getBytes())));
    }

}
