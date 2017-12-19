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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Value("${serverId}")
    String serverId;

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

    @Autowired
    private DeviceSettingUpdateMapper deviceSettingUpdateMapper;

    @Autowired
    private DeviceUpdatePackSysMapper deviceUpdatePackSysMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Transactional
    @Override
    public String addDevice(String deviceId) {

        Device device = new Device();

        //新增设备信息
        device.setDeviceId(deviceId);
        device.setDeviceName("设备未命名");
        device.setIsOnline("0");
        device.setActiveStatus("0");

        Device deviceExist = deviceMapper.selectByPrimaryKey(deviceId);

        if (deviceExist == null){
            deviceMapper.insertSelective(device);
            return "1";
        }else {
            deviceMapper.updateByPrimaryKeySelective(device);
//            System.out.println("设备已存在");
            return "0";
        }

    }

    @Override
    public List<Map<String, String>> findDeviceInformation(String companyId, String companyName, String deviceName, String deviceId,
                                      String isOnline, String activeStatus, String employeeCompanyId) {

        Device device = new Device();

        //设备信息筛选条件
        device.setCompanyId(companyId);
        device.setCompanyName(companyName);
        device.setDeviceName(deviceName);
        device.setDeviceId(deviceId);//设备编号就是deviceId
        device.setIsOnline(isOnline);
        device.setActiveStatus(activeStatus);

        List<Map<String, String>> mapList = deviceMapper.findByCondition(device);

//        System.out.println(JSON.toJSONString(mapList));
        if (mapList.size() > 0){
            for (Map<String, String> map : mapList) {
                //判断离线状态空
                if (map.get("is_online") == null || "".equals(map.get("is_online"))){

                    map.put("is_online", "无状态");
//                    System.out.println(map.get("device_id")+"设备离线状态为空");
                }else {
                    String isOnlineTemp = map.get("is_online");

                    if (isOnlineTemp.equals("1")){
                        map.put("is_online", "在线");
                    }else if (isOnlineTemp.equals("0")){
                        map.put("is_online", "离线");
                    }else{
                        map.put("is_online", "无状态");
                    }
                }

                //判断激活状态空
                if (map.get("active_status") == null || "".equals(map.get("active_status"))){

                    map.put("active_status", "无状态");
//                    System.out.println(map.get("device_id")+"设备激活状态为空");
                }else {
                    String activeStatusTemp = map.get("active_status");

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
                    }else {
                        map.put("active_status", "无状态");
                    }
                }

                //查找人员姓名
                String employeeId = map.get("operate_employee");
                try {
                    String employeeName = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, employeeCompanyId).getEmployeeName();
                    map.put("operate_employee", employeeName);
                }catch (Exception e){
//                    System.out.println("根据人员id没有查到操作人【"+employeeId+"】的姓名");
                    map.put("operate_employee", "暂无操作人记录");
                }

                //用空字符串替换undefined操作时间
                try {
                    String operateTime = map.get("operate_time");
                    if (StringUtils.isEmpty(operateTime)){
//                        System.out.println("当前设备【"+deviceId+"】没有查到操作时间记录");
                        map.put("operate_time", "暂无操作时间记录");
                    }
                }catch (Exception e){
//                    System.out.println("当前设备【"+deviceId+"】没有查到操作时间记录");
                    map.put("operate_time", "暂无操作时间记录");
                }

                //替换undefined设备名称
                if (StringUtils.isEmpty(map.get("device_name"))){
//                    System.out.println(JSON.toJSONString(map));
//                    System.out.println("设备未命名");
                    map.put("device_name", "设备未命名");
//                    System.out.println(JSON.toJSONString(map));
                }
            }
        }

//        System.out.println("------------"+JSON.toJSONString(mapList));

        return mapList;

    }

    public List<Map> queryAllDeviceInfo(String companyId) {
        return deviceMapper.selectAllDeviceInfo(companyId);
    }

    public List<Door> queryAllDoorInfoByCompanyId(String companyId){

        return doorMapper.selectAllDoorByCompanyId(companyId);
    }

    @Override
    public void bindDevice(String deviceId) {

        Device device = deviceMapper.selectByPrimaryKey(deviceId);

        Map<String, Object> bindInformation = new LinkedHashMap<String, Object>();
        bindInformation.put("companyId", device.getCompanyId());
        bindInformation.put("companyName", device.getCompanyName());

        //构造命令格式
        DoorCmd doorCmdBindDevice = new DoorCmd();
        doorCmdBindDevice.setServerId(serverId);
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
        doorCmdBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
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
        rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
    }

    @Override
    public void unBindDevice(String deviceId) {

        //构造命令格式
        DoorCmd doorCmdUnBindDevice = new DoorCmd();
        doorCmdUnBindDevice.setServerId(serverId);
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
        doorCmdUnBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
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
        rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
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
        doorCmdRecord.setServerId(serverId);
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
        doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
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
        rabbitMQSender.sendMessage(deviceId, doorRecordAll);
//        System.out.println("重启记录已回复");
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
            deviceRunningLog.setDeviceId(deviceId);

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
        doorCmdRecord.setServerId(serverId);
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
        doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
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
        rabbitMQSender.sendMessage(deviceId, doorRecordAll);
//        System.out.println("运行日志上传已回复");
    }

    /**
     * 定时下载升级更新设备系统
     * @param deviceIdList
     * @param downloadTime
     * @param updateTime
     */
    public String updateDeviceSystem(List<String> deviceIdList, String downloadTime, String updateTime){

        //返回命令id
        String superCmdId = "";

        DeviceSettingUpdate deviceSettingUpdate = new DeviceSettingUpdate();

        deviceSettingUpdate.setDownloadTimeSys(downloadTime);
        deviceSettingUpdate.setUpdateTimeSys(updateTime);

        for (String deviceId : deviceIdList) {

            //返回命令id
            superCmdId = FormatUtil.createUuid();

            deviceSettingUpdate.setDeviceId(deviceId);

            //检查是否存在该设备的升级设置信息，有则覆盖，无则新增
            DeviceSettingUpdate deviceSettingUpdateExist = deviceSettingUpdateMapper.selectByPrimaryKey(deviceId);
            if (deviceSettingUpdateExist == null){
                deviceSettingUpdateMapper.insertSelective(deviceSettingUpdate);
            }else {
                deviceSettingUpdateMapper.updateByPrimaryKeySelective(deviceSettingUpdate);
            }

            //查询最新的下载包路径信息
            List<DeviceUpdatePackSys> deviceUpdatePackSys = deviceUpdatePackSysMapper.selectAllByLatestTime();

            Map<String, String> mapHandOut = new HashMap<String, String>();
            if (deviceUpdatePackSys.size() > 0 && deviceUpdatePackSys.size() == 2 && deviceUpdatePackSys.size() < 3){
                mapHandOut.put("newSysVerion", deviceUpdatePackSys.get(0).getNewSysVerion());
                mapHandOut.put("isSameVesionUpdate", "");
                mapHandOut.put("downloadTime", downloadTime);
                mapHandOut.put("updateTime", updateTime);
                //判断升级包文件类型
                String pathTemp1 = deviceUpdatePackSys.get(0).getPath();
                String pathTemp2 = deviceUpdatePackSys.get(1).getPath();
                String path1 = "";
                String path2 = "";
                if ("prop".equals(pathTemp1.substring(pathTemp1.lastIndexOf(".")+1, pathTemp1.length())) && "zip".equals(pathTemp2.substring(pathTemp2.lastIndexOf(".")+1, pathTemp2.length()))){
                    path1 = pathTemp1;
                    path2 = pathTemp2;
                }else if ("zip".equals(pathTemp1.substring(pathTemp1.lastIndexOf(".")+1, pathTemp1.length())) && "prop".equals(pathTemp2.substring(pathTemp2.lastIndexOf(".")+1, pathTemp2.length()))){
                    path1 = pathTemp2;
                    path2 = pathTemp1;
                }
                mapHandOut.put("path1", path1);
                mapHandOut.put("path2", path2);
            }else {
                mapHandOut.put("newSysVerion", "");
                mapHandOut.put("isSameVesionUpdate", "");
                mapHandOut.put("downloadTime", downloadTime);
                mapHandOut.put("updateTime", updateTime);
                mapHandOut.put("path1", "");
                mapHandOut.put("path2", "");
            }

            //构造命令格式
            DoorCmd doorCmdUpdateSystem = new DoorCmd();
            doorCmdUpdateSystem.setServerId(serverId);
            doorCmdUpdateSystem.setDeviceId(deviceId);
            doorCmdUpdateSystem.setFileEdition("v1.3");
            doorCmdUpdateSystem.setCommandMode("C");
            doorCmdUpdateSystem.setCommandType("single");
            doorCmdUpdateSystem.setCommandTotal("1");
            doorCmdUpdateSystem.setCommandIndex("1");
            doorCmdUpdateSystem.setSubCmdId("");
            doorCmdUpdateSystem.setAction("UPDATE_DEVICE_SYSTEM");
            doorCmdUpdateSystem.setActionCode("1008");
            doorCmdUpdateSystem.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdUpdateSystem.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdUpdateSystem.setSuperCmdId(superCmdId);
            doorCmdUpdateSystem.setData(JSON.toJSONString(mapHandOut));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdUpdateSystem, "update", mapHandOut, "C");
            //命令状态设置为: 发送中
            doorCmdUpdateSystem.setStatus("1");
            //设置md5校验值
            doorCmdUpdateSystem.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
            //设置数据库的data字段
            doorCmdUpdateSystem.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
            //命令数据存入数据库
            entranceGuardService.insertCommand(doorCmdUpdateSystem);
            //立即下发数据到MQ
            rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
        }

        return superCmdId;
    }

    /**
     * 定时下载升级更新设备应用
     * @param deviceIdList
     * @param downloadTime
     * @param updateTime
     */
    public String updateDeviceApplication(List<String> deviceIdList, String downloadTime, String updateTime){

        //返回命令id
        String superCmdId = "";

        DeviceSettingUpdate deviceSettingUpdate = new DeviceSettingUpdate();

        deviceSettingUpdate.setDownloadTimeApp(downloadTime);
        deviceSettingUpdate.setUpdateTimeApp(updateTime);

        for (String deviceId : deviceIdList) {

            //返回命令id
            superCmdId = FormatUtil.createUuid();

            deviceSettingUpdate.setDeviceId(deviceId);

            //检查是否存在该设备的升级设置信息，有则覆盖，无则新增
            DeviceSettingUpdate deviceSettingUpdateExist = deviceSettingUpdateMapper.selectByPrimaryKey(deviceId);
            if (deviceSettingUpdateExist == null){
                deviceSettingUpdateMapper.insertSelective(deviceSettingUpdate);
            }else {
                deviceSettingUpdateMapper.updateByPrimaryKeySelective(deviceSettingUpdate);
            }

            Map<String, String> mapHandOut = new HashMap<String, String>();
            mapHandOut.put("isSameVesionUpdate", "");
            mapHandOut.put("downloadTime", downloadTime);
            mapHandOut.put("updateTime", updateTime);

            //构造命令格式
            DoorCmd doorCmdUpdateSystem = new DoorCmd();
            doorCmdUpdateSystem.setServerId(serverId);
            doorCmdUpdateSystem.setDeviceId(deviceId);
            doorCmdUpdateSystem.setFileEdition("v1.3");
            doorCmdUpdateSystem.setCommandMode("C");
            doorCmdUpdateSystem.setCommandType("single");
            doorCmdUpdateSystem.setCommandTotal("1");
            doorCmdUpdateSystem.setCommandIndex("1");
            doorCmdUpdateSystem.setSubCmdId("");
            doorCmdUpdateSystem.setAction("UPDATE_DEVICE_APP");
            doorCmdUpdateSystem.setActionCode("1009");
            doorCmdUpdateSystem.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdUpdateSystem.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdUpdateSystem.setSuperCmdId(superCmdId);
            doorCmdUpdateSystem.setData(JSON.toJSONString(mapHandOut));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdUpdateSystem, "update", mapHandOut, "C");
            //命令状态设置为: 发送中
            doorCmdUpdateSystem.setStatus("1");
            //设置md5校验值
            doorCmdUpdateSystem.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
            //设置数据库的data字段
            doorCmdUpdateSystem.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
            //命令数据存入数据库
            entranceGuardService.insertCommand(doorCmdUpdateSystem);
            //立即下发数据到MQ
            rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
        }

        return superCmdId;
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
//            System.out.println("CRC16校验成功，数据完好无损");
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
