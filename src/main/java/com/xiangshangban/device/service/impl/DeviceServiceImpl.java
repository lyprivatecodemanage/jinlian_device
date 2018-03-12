package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.command.CmdUtil;
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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 设备管理实现类
 */

@Service
public class DeviceServiceImpl implements IDeviceService {

    private static final Logger LOGGER = Logger.getLogger(DeviceServiceImpl.class);

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

    @Autowired
    private DeviceSettingMapper deviceSettingMapper;

    @Autowired
    private DoorCalendarMapper doorCalendarMapper;

    @Autowired
    private DoorSettingMapper doorSettingMapper;

    @Autowired
    private DoorTimingKeepOpenMapper doorTimingKeepOpenMapper;

    @Autowired
    private TimeRangeLcdBrightnessMapper timeRangeLcdBrightnessMapper;

    @Autowired
    private TimeRangeLcdOffMapper timeRangeLcdOffMapper;

    @Autowired
    private TimeRangePrivilegeEmployeeMapper timeRangePrivilegeEmployeeMapper;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private DoorEmployeePermissionMapper doorEmployeePermissionMapper;

    @Autowired
    private TimeRangeCommonEmployeeMapper timeRangeCommonEmployeeMapper;

    @Autowired
    private CmdUtil cmdUtil;

    @Transactional
    @Override
    public String addDevice(String deviceId) {

        Device device = new Device();

        //新增设备信息
        device.setDeviceId(deviceId);
        device.setDeviceName("设备未命名");
        device.setIsOnline("0");
        device.setActiveStatus("0");
        //更新设备绑定状态为未绑定
        device.setIsUnbind("1");

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

//        System.out.println("companyId: "+JSON.toJSONString(companyId));
//        System.out.println("mapList: "+JSON.toJSONString(mapList));
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

    public void bindDevice(String deviceId, String companyId, String companyName) {

        Map<String, Object> bindInformation = new LinkedHashMap<String, Object>();
        bindInformation.put("companyId", companyId);
        bindInformation.put("companyName", companyName);

//        //构造命令格式
//        DoorCmd doorCmdBindDevice = new DoorCmd();
//        doorCmdBindDevice.setServerId(serverId);
//        doorCmdBindDevice.setDeviceId(deviceId);
//        doorCmdBindDevice.setFileEdition("v1.3");
//        doorCmdBindDevice.setCommandMode("C");
//        doorCmdBindDevice.setCommandType("single");
//        doorCmdBindDevice.setCommandTotal("1");
//        doorCmdBindDevice.setCommandIndex("1");
//        doorCmdBindDevice.setSubCmdId("");
//        doorCmdBindDevice.setAction("BIND_DEVICE");
//        doorCmdBindDevice.setActionCode("1001");
//        doorCmdBindDevice.setSendTime(CalendarUtil.getCurrentTime());
//        doorCmdBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//        doorCmdBindDevice.setSuperCmdId(FormatUtil.createUuid());
//        doorCmdBindDevice.setData(JSON.toJSONString(bindInformation));
//
//        //获取完整的数据加协议封装格式
//        RabbitMQSender rabbitMQSender = new RabbitMQSender();
//        Map<String, Object> doorCmdPackageAll =  CmdUtil.messagePackaging(doorCmdBindDevice, "bindInformation", bindInformation, "C");
//        //命令状态设置为: 发送中
//        doorCmdBindDevice.setStatus("1");
//        //设置md5校验值
//        doorCmdBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//        //设置数据库的data字段
//        doorCmdBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//        //命令数据存入数据库
//        entranceGuardService.insertCommand(doorCmdBindDevice);
//        //立即下发数据到MQ
//        rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);

        String sendTime = DateUtils.getDateTime();

        cmdUtil.handOutCmd(deviceId, "C", "BIND_DEVICE", "1001", "", "bindInformation",
                bindInformation, "1", "", "", "", "", sendTime);
    }

    @Override
    public ReturnData unBindDevice(String oldDeviceId, String newDeviceId, String unBindType, HttpServletRequest request) {

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String operatorEmployeeId = request.getHeader("accessUserId");

        //查看设备的解绑状态
        String isUnbind = deviceMapper.selectByPrimaryKey(oldDeviceId).getIsUnbind();

        if ("0".equals(isUnbind)){
            //已绑定
            //跳出if执行后续的解绑操作
        }else if ("1".equals(isUnbind)){
            //未绑定
            returnData.setMessage("解绑成功！");
            returnData.setReturnCode("3000");
            return returnData;
        }else if ("2".equals(isUnbind)){
            //解绑中
            returnData.setMessage("正在解绑......请稍后");
            returnData.setReturnCode("4208");
            return returnData;
        }else if ("3".equals(isUnbind)){
            //解绑失败（超时无响应）
            returnData.setMessage("解绑失败，请重新解绑");
            returnData.setReturnCode("4209");

            //解绑失败，还原设备绑定状态
            Device device = new Device();
            device.setDeviceId(oldDeviceId);
            device.setIsUnbind("0");
            deviceMapper.updateByPrimaryKeySelective(device);

            return returnData;
        }else if ("4".equals(isUnbind)){
            //设备数据未全部上传
            returnData.setMessage("设备数据未全部上传，请稍后解绑");
            returnData.setReturnCode("4210");

            //解绑失败，还原设备绑定状态
            Device device = new Device();
            device.setDeviceId(oldDeviceId);
            device.setIsUnbind("0");
            deviceMapper.updateByPrimaryKeySelective(device);

            return returnData;
        }

//        //构造命令格式
//        DoorCmd doorCmdUnBindDevice = new DoorCmd();
//        doorCmdUnBindDevice.setServerId(serverId);
//        doorCmdUnBindDevice.setDeviceId(oldDeviceId);
//        doorCmdUnBindDevice.setFileEdition("v1.3");
//        doorCmdUnBindDevice.setCommandMode("C");
//        doorCmdUnBindDevice.setCommandType("single");
//        doorCmdUnBindDevice.setCommandTotal("1");
//        doorCmdUnBindDevice.setCommandIndex("1");
//        doorCmdUnBindDevice.setSubCmdId("");
//        doorCmdUnBindDevice.setAction("UNBIND_DEVICE");
//        doorCmdUnBindDevice.setActionCode("1002");
//        doorCmdUnBindDevice.setSendTime(CalendarUtil.getCurrentTime());
//        doorCmdUnBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//        doorCmdUnBindDevice.setSuperCmdId(FormatUtil.createUuid());

        String doorId = "";
        try {
            doorId = doorMapper.findDoorIdByDeviceId(oldDeviceId).getDoorId();
        }catch (Exception e){
            doorId = "";
        }

        Map<String, String> unbindTypeMap = new HashMap<>();
        if ("1".equals(unBindType)){
            //场景1：解绑设备和公司
            unbindTypeMap.put("unbindType", "1");
            unbindTypeMap.put("newDeviceId", newDeviceId);
            unbindTypeMap.put("doorId", doorId);
            unbindTypeMap.put("operatorEmployeeId", operatorEmployeeId);
//            doorCmdUnBindDevice.setData(JSON.toJSONString(unbindTypeMap));
        }else if ("0".equals(unBindType)){
            //场景2：解绑设备和门（和1的区别是：删除的数据里，解绑设备和门，设备端要保留公司信息）
            unbindTypeMap.put("unbindType", "0");
            unbindTypeMap.put("newDeviceId", newDeviceId);
            unbindTypeMap.put("doorId", doorId);
            unbindTypeMap.put("operatorEmployeeId", operatorEmployeeId);
//            doorCmdUnBindDevice.setData(JSON.toJSONString(unbindTypeMap));
        }else if ("2".equals(unBindType)){
            //场景3：删除门（场景3设备端要删除的数据和场景2的相同，不同的是场景3不需要再次同步门的信息到新设备上）
            unbindTypeMap.put("unbindType", "2");
            unbindTypeMap.put("newDeviceId", newDeviceId);
            unbindTypeMap.put("doorId", doorId);
            unbindTypeMap.put("operatorEmployeeId", operatorEmployeeId);
//            doorCmdUnBindDevice.setData(JSON.toJSONString(unbindTypeMap));
        }

//        //获取完整的数据加协议封装格式
//        RabbitMQSender rabbitMQSender = new RabbitMQSender();
//        Map<String, Object> doorCmdPackageAll =  CmdUtil.messagePackaging(doorCmdUnBindDevice, "", "", "NULLDATA");
//        //命令状态设置为: 发送中
//        doorCmdUnBindDevice.setStatus("1");
//        //设置md5校验值
//        doorCmdUnBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//        //设置数据库的data字段
//        doorCmdUnBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//        //命令数据存入数据库
//        entranceGuardService.insertCommand(doorCmdUnBindDevice);
//        //立即下发数据到MQ
//        rabbitMQSender.sendMessage(oldDeviceId, doorCmdPackageAll);

        String sendTime = DateUtils.getDateTime();
        cmdUtil.handOutCmd(oldDeviceId, "C", "UNBIND_DEVICE", "1002", operatorEmployeeId, "unbindTypeMap",
                unbindTypeMap, "1", "", "", "", "120", sendTime);

        //设备状态设置为解绑中
        Device device = new Device();
        device.setDeviceId(oldDeviceId);
        device.setIsUnbind("2");
        deviceMapper.updateByPrimaryKeySelective(device);

        returnData.setMessage("正在解绑......请稍后");
        returnData.setReturnCode("4208");
        return returnData;
    }

    @Override
    public void unBindDeviceDeleteOperation(String deviceId) {
        //1.device表的公司id、公司名称置空，设备激活状态重置为待激活，设备所处地理位置置空，设备用途置空总服务时长置空，已使用时长置空剩余服务时长置空
        Device device = new Device();
        device.setDeviceId(deviceId);
        device.setCompanyId("");
        device.setCompanyName("");
        device.setActiveStatus("0");
        device.setDevicePlace("");
        device.setDeviceUsages("");
        device.setTotalServerTime("");
        device.setHaveUsedTime("");
        device.setRemainServerTime("");
        deviceMapper.updateByPrimaryKeySelective(device);

        //2.door_表设备id置null，绑定时间插入当前解绑时间，使门上的记录都变为不可操作的历史
        String doorId = "";
        try {
            doorId = doorMapper.findDoorIdByDeviceId(deviceId).getDoorId();
            doorMapper.updateDeviceIdNull(doorId);
            Door door = new Door();
            door.setDoorId(doorId);
            door.setBindDate(DateUtils.getDateTime());
            doorMapper.updateByPrimaryKeySelective(door);
        }catch (Exception e){
            System.out.println("该设备还未绑定过门");
        }
    }

//    @Override
//    public void unBindDevice(String deviceId) {
//
//        //构造命令格式
//        DoorCmd doorCmdUnBindDevice = new DoorCmd();
//        doorCmdUnBindDevice.setServerId(serverId);
//        doorCmdUnBindDevice.setDeviceId(deviceId);
//        doorCmdUnBindDevice.setFileEdition("v1.3");
//        doorCmdUnBindDevice.setCommandMode("C");
//        doorCmdUnBindDevice.setCommandType("single");
//        doorCmdUnBindDevice.setCommandTotal("1");
//        doorCmdUnBindDevice.setCommandIndex("1");
//        doorCmdUnBindDevice.setSubCmdId("");
//        doorCmdUnBindDevice.setAction("UNBIND_DEVICE");
//        doorCmdUnBindDevice.setActionCode("1002");
//        doorCmdUnBindDevice.setSendTime(CalendarUtil.getCurrentTime());
//        doorCmdUnBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//        doorCmdUnBindDevice.setSuperCmdId(FormatUtil.createUuid());
//        doorCmdUnBindDevice.setData("");
//
//        //获取完整的数据加协议封装格式
//        RabbitMQSender rabbitMQSender = new RabbitMQSender();
//        Map<String, Object> doorCmdPackageAll =  cmdUtil.messagePackaging(doorCmdUnBindDevice, "", "", "NULLDATA");
//        //命令状态设置为: 发送中
//        doorCmdUnBindDevice.setStatus("1");
//        //设置md5校验值
//        doorCmdUnBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//        //设置数据库的data字段
//        doorCmdUnBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//        //命令数据存入数据库
//        entranceGuardService.insertCommand(doorCmdUnBindDevice);
//        //立即下发数据到MQ
//        rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
//    }

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

            //重启记录时间大于当前服务器时间的数据不保存
            if (!DateUtils.isTime1LtTime2((String) singleRecordMap.get("rebootTime"), DateUtils.getDateTime())){
                continue;
            }

            rebootId = (String) singleRecordMap.get("rebootId");
            rebootIdList.add(rebootId);

            DeviceRebootRecord deviceRebootRecord = new DeviceRebootRecord();
            deviceRebootRecord.setRebootId(rebootId);
            deviceRebootRecord.setDeviceId((String) singleRecordMap.get("deviceId"));
            deviceRebootRecord.setRebootNumber((String) singleRecordMap.get("rebootNumber"));
            deviceRebootRecord.setRebootTime((String) singleRecordMap.get("rebootTime"));

            DeviceRebootRecord deviceRebootRecordExist = deviceRebootRecordMapper.selectByRebootIdAndDeviceId(rebootId, deviceId);
            if (deviceRebootRecordExist == null){
                deviceRebootRecordMapper.insertSelective(deviceRebootRecord);
            }else {
//                deviceRebootRecordMapper.updateByPrimaryKeySelective(deviceRebootRecord);
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
        Map<String, Object> doorRecordAll =  CmdUtil.messagePackaging(doorCmdRecord, "", resultData, "R");
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

            DeviceRunningLog deviceRunningLogExist = deviceRunningLogMapper.selectByLogIdAndDeviceId(logId, deviceId);
            if (deviceRunningLogExist == null){
                deviceRunningLogMapper.insertSelective(deviceRunningLog);
            }else {
//                deviceRunningLogMapper.updateByPrimaryKeySelective(deviceRunningLog);
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
        Map<String, Object> doorRecordAll =  CmdUtil.messagePackaging(doorCmdRecord, "", resultData, "R");
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

        DeviceSettingUpdate deviceSettingUpdate = new DeviceSettingUpdate();

        deviceSettingUpdate.setDownloadTimeSys(downloadTime);
        deviceSettingUpdate.setUpdateTimeSys(updateTime);

        for (String deviceId : deviceIdList) {

            deviceSettingUpdate.setDeviceId(deviceId);

            //检查是否存在该设备的升级设置信息，有则覆盖，无则新增
            DeviceSettingUpdate deviceSettingUpdateExist = deviceSettingUpdateMapper.selectByPrimaryKey(deviceId);
            if (deviceSettingUpdateExist == null){
                deviceSettingUpdateMapper.insertSelective(deviceSettingUpdate);
            }else {
                deviceSettingUpdateMapper.updateByPrimaryKeySelective(deviceSettingUpdate);
            }

            //判断系统升级时间是否空，空则不下发数据
            if (StringUtils.isEmpty(downloadTime) && StringUtils.isEmpty(updateTime)){
                return "";
            }

            //查询最新的下载包路径信息
            List<DeviceUpdatePackSys> deviceUpdatePackSys = deviceUpdatePackSysMapper.selectAllByLatestTime();

            Map<String, String> mapHandOut = new HashMap<String, String>();
            if (deviceUpdatePackSys.size() == 2){
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

                //确保下载地址两个都有
                if (StringUtils.isEmpty(path1) || StringUtils.isEmpty(path2)){
                    return "0";
                }
            }else {
                return "0";
            }

            //            //构造命令格式
//            DoorCmd doorCmdUpdateSystem = new DoorCmd();
//            doorCmdUpdateSystem.setServerId(serverId);
//            doorCmdUpdateSystem.setDeviceId(deviceId);
//            doorCmdUpdateSystem.setFileEdition("v1.3");
//            doorCmdUpdateSystem.setCommandMode("C");
//            doorCmdUpdateSystem.setCommandType("single");
//            doorCmdUpdateSystem.setCommandTotal("1");
//            doorCmdUpdateSystem.setCommandIndex("1");
//            doorCmdUpdateSystem.setSubCmdId("");
//            doorCmdUpdateSystem.setAction("UPDATE_DEVICE_SYSTEM");
//            doorCmdUpdateSystem.setActionCode("1008");
//            doorCmdUpdateSystem.setSendTime(CalendarUtil.getCurrentTime());
//            doorCmdUpdateSystem.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//            doorCmdUpdateSystem.setSuperCmdId(superCmdId);
//            doorCmdUpdateSystem.setData(JSON.toJSONString(mapHandOut));
//
//            //获取完整的数据加协议封装格式
//            RabbitMQSender rabbitMQSender = new RabbitMQSender();
//            Map<String, Object> doorCmdPackageAll =  CmdUtil.messagePackaging(doorCmdUpdateSystem, "update", mapHandOut, "C");
//            //命令状态设置为: 发送中
//            doorCmdUpdateSystem.setStatus("1");
//            //设置md5校验值
//            doorCmdUpdateSystem.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//            //设置数据库的data字段
//            doorCmdUpdateSystem.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//            //命令数据存入数据库
//            entranceGuardService.insertCommand(doorCmdUpdateSystem);
//            //立即下发数据到MQ
//            rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);

            String sendTime = DateUtils.getDateTime();
            cmdUtil.handOutCmd(deviceId, "C", "UPDATE_DEVICE_SYSTEM", "1008", "", "update",
                    mapHandOut, "1", "", "", "", "", sendTime);
        }
        return "";
    }

    /**
     * 定时下载升级更新设备应用
     * @param deviceIdList
     * @param downloadTime
     * @param updateTime
     */
    public String updateDeviceApplication(List<String> deviceIdList, String downloadTime, String updateTime){

        DeviceSettingUpdate deviceSettingUpdate = new DeviceSettingUpdate();

        deviceSettingUpdate.setDownloadTimeApp(downloadTime);
        deviceSettingUpdate.setUpdateTimeApp(updateTime);

        for (String deviceId : deviceIdList) {

            deviceSettingUpdate.setDeviceId(deviceId);

            //检查是否存在该设备的升级设置信息，有则覆盖，无则新增
            DeviceSettingUpdate deviceSettingUpdateExist = deviceSettingUpdateMapper.selectByPrimaryKey(deviceId);
            if (deviceSettingUpdateExist == null){
                deviceSettingUpdateMapper.insertSelective(deviceSettingUpdate);
            }else {
                deviceSettingUpdateMapper.updateByPrimaryKeySelective(deviceSettingUpdate);
            }

            //判断应用升级时间是否空，空则不下发数据
            if (StringUtils.isEmpty(downloadTime)){
                return "";
            }

            Map<String, String> mapHandOut = new HashMap<String, String>();
            mapHandOut.put("isSameVesionUpdate", "");
            mapHandOut.put("downloadTime", downloadTime);
            mapHandOut.put("updateTime", updateTime);

//            //构造命令格式
//            DoorCmd doorCmdUpdateSystem = new DoorCmd();
//            doorCmdUpdateSystem.setServerId(serverId);
//            doorCmdUpdateSystem.setDeviceId(deviceId);
//            doorCmdUpdateSystem.setFileEdition("v1.3");
//            doorCmdUpdateSystem.setCommandMode("C");
//            doorCmdUpdateSystem.setCommandType("single");
//            doorCmdUpdateSystem.setCommandTotal("1");
//            doorCmdUpdateSystem.setCommandIndex("1");
//            doorCmdUpdateSystem.setSubCmdId("");
//            doorCmdUpdateSystem.setAction("UPDATE_DEVICE_APP");
//            doorCmdUpdateSystem.setActionCode("1009");
//            doorCmdUpdateSystem.setSendTime(CalendarUtil.getCurrentTime());
//            doorCmdUpdateSystem.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//            doorCmdUpdateSystem.setSuperCmdId(superCmdId);
//            doorCmdUpdateSystem.setData(JSON.toJSONString(mapHandOut));
//
//            //获取完整的数据加协议封装格式
//            RabbitMQSender rabbitMQSender = new RabbitMQSender();
//            Map<String, Object> doorCmdPackageAll =  CmdUtil.messagePackaging(doorCmdUpdateSystem, "update", mapHandOut, "C");
//            //命令状态设置为: 发送中
//            doorCmdUpdateSystem.setStatus("1");
//            //设置md5校验值
//            doorCmdUpdateSystem.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//            //设置数据库的data字段
//            doorCmdUpdateSystem.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//            //命令数据存入数据库
//            entranceGuardService.insertCommand(doorCmdUpdateSystem);
//            //立即下发数据到MQ
//            rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);

            String sendTime = DateUtils.getDateTime();
            cmdUtil.handOutCmd(deviceId, "C", "UPDATE_DEVICE_APP", "1009", "", "update",
                    mapHandOut, "1", "", "", "", "", sendTime);
        }
        return "";
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

    @Override
    public void synchronizationDoorInfoToNewDevice(String doorId, String newDeviceId, String operatorEmployeeId) {
        //组合需要重新下发的数据
        String oldDeviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();

        //1.重新绑定公司到新设备（新设备一定是先绑定到公司，才能换绑到门上）
        String companyId = deviceMapper.selectByPrimaryKey(newDeviceId).getCompanyId();
        String companyName = deviceMapper.selectByPrimaryKey(newDeviceId).getCompanyName();
        bindDevice(newDeviceId, companyId, companyName);

        //2.同步设备默认设置到新绑定的设备（从设备那边沟通获取到的默认值）
        Map<String, Object> deviceSettingMap = new HashMap<>();
        deviceSettingMap.put("deviceIdList", new ArrayList<>().add(newDeviceId));
        deviceSettingMap.put("heartbeatPeriod", "60");
        deviceSettingMap.put("faceThreshold", "70.0");
        deviceSettingMap.put("fingerThreshold", "");
        deviceSettingMap.put("faceDetecTime", "");
        deviceSettingMap.put("lcdBrightnessList", new ArrayList<>());
        deviceSettingMap.put("fanOnTemp", "72");
        deviceSettingMap.put("fanOffTemp", "60");
        deviceSettingMap.put("enableSelfHelpBioRegister", "on");
        deviceSettingMap.put("enableSelfHelpCardRegister", "on");
        deviceSettingMap.put("enableTimedReboot", "off");
        deviceSettingMap.put("timedRebootPeriod", "");
        deviceSettingMap.put("timedRebootTime", "");
        deviceSettingMap.put("downloadTimeSys", "");
        deviceSettingMap.put("updateTimeSys", "");
        deviceSettingMap.put("downloadTimeApp", "");
        deviceSettingMap.put("updateTimeApp", "");
        deviceService.handOutDeviceSetting(JSON.toJSONString(deviceSettingMap));

        //3.同步门禁高级设置到新绑定的设备
        //3.1门禁设置同步
        DoorSetting doorSetting = doorSettingMapper.selectByPrimaryKey(doorId);
        Map<String, Object> doorFeaturesSetupMap = new HashMap<>();
        if (null != doorSetting){
            doorFeaturesSetupMap.put("doorName", doorMapper.selectByPrimaryKey(doorId).getDoorName());
            doorFeaturesSetupMap.put("timeLimitDoorOpen", "");
            doorFeaturesSetupMap.put("timeLimitLockOpen", "");
            doorFeaturesSetupMap.put("enableDoorOpenRecord", doorSetting.getEnableDoorEventRecord());
            doorFeaturesSetupMap.put("countLimitAuthenticationFailed", doorSetting.getFaultCountAuthentication());
            doorFeaturesSetupMap.put("alarmTimeLength", doorSetting.getAlarmTimeLengthTrespass());
            doorFeaturesSetupMap.put("publicPassword1", doorSetting.getFirstPublishPassword());
            doorFeaturesSetupMap.put("publicPassword2", doorSetting.getSecondPublishPassword());
            doorFeaturesSetupMap.put("threatenPassword", doorSetting.getThreatenPublishPasswrod());
            doorFeaturesSetupMap.put("deviceManagePassword", doorSetting.getManagerPassword());
            doorFeaturesSetupMap.put("enableDoorKeepOpen", doorSetting.getEnableDoorKeepOpen());
            doorFeaturesSetupMap.put("enableFirstCardKeepOpen", doorSetting.getEnableFirstCardKeepOpen());
            doorFeaturesSetupMap.put("enableDoorCalendar", doorSetting.getEnableDoorCalendar());
            doorFeaturesSetupMap.put("oneWeekTimeList", new ArrayList<>());//定时常开已废弃不用，在设备上可设置此功能
            entranceGuardService.doorCommonSetupAdditionalCmd(newDeviceId, operatorEmployeeId, doorFeaturesSetupMap);
        }
        //3.2首卡权限同步
        List<TimeRangePrivilegeEmployee> timeRangePrivilegeEmployeeList = timeRangePrivilegeEmployeeMapper.selectByDoorId(doorId);
        if (timeRangePrivilegeEmployeeList.size() > 0){
            //首卡权限人员id去重
            Set<String> employeeIdSet = new HashSet<>();
            for (TimeRangePrivilegeEmployee trpe : timeRangePrivilegeEmployeeList) {
                employeeIdSet.add(trpe.getEmployeeId());
            }
            //循环遍历获取每个人的首卡权限时间段
            for (String employeeId : employeeIdSet) {
                List<TimeRangePrivilegeEmployee> trpeList = timeRangePrivilegeEmployeeMapper.selectByPrimaryKey(employeeId);
                List<String> employeeIdList = new ArrayList<>();
                employeeIdList.add(employeeId);

                List<Map<String, String>> oneWeekTimeFirstCardList = new ArrayList<>();
                for (TimeRangePrivilegeEmployee trpe : trpeList) {
                    Map<String, String> oneWeekTimeFirstCardMap = new HashMap<>();
                    oneWeekTimeFirstCardMap.put("startTime", trpe.getRangeStartTime());
                    oneWeekTimeFirstCardMap.put("endTime", trpe.getRangeEndTime());
                    oneWeekTimeFirstCardMap.put("startWeekNumber", trpe.getStartWeekNumber());
                    oneWeekTimeFirstCardMap.put("endWeekNumber", trpe.getEndWeekNumber());
                    oneWeekTimeFirstCardList.add(oneWeekTimeFirstCardMap);
                }

                //重新组装数据（发给设备的格式）
                List oneWeekTimeFirstCardListTemp = new ArrayList<>();
                for (int i=0; i<oneWeekTimeFirstCardList.size(); i++) {
                    String startTime = oneWeekTimeFirstCardList.get(i).get("startTime");
                    String endTime = oneWeekTimeFirstCardList.get(i).get("endTime");
                    String startWeekString = oneWeekTimeFirstCardList.get(i).get("startWeekNumber");
                    String endWeekString = oneWeekTimeFirstCardList.get(i).get("endWeekNumber");
                    int startWeekNumber = Integer.parseInt(startWeekString);
                    int endWeekNumber = Integer.parseInt(endWeekString);

                    for (int j=0; j<(endWeekNumber-startWeekNumber+1); j++){
                        Map<String, String> oneWeekTimeFirstCardMapTemp = new HashMap<String, String>();

                        oneWeekTimeFirstCardMapTemp.put("weekType", String.valueOf((startWeekNumber + j)));
                        oneWeekTimeFirstCardMapTemp.put("startTime", startTime);
                        oneWeekTimeFirstCardMapTemp.put("endTime", endTime);
                        oneWeekTimeFirstCardMapTemp.put("doorOpenType", "");

                        oneWeekTimeFirstCardListTemp.add(oneWeekTimeFirstCardMapTemp);
                    }
                }

                //下发每一个人的首卡常开命令
                entranceGuardService.handOutFirstCardCmd(newDeviceId, operatorEmployeeId, employeeIdList, oneWeekTimeFirstCardListTemp);
            }
        }
        //3.3门禁日历同步
        List<DoorCalendar> doorCalendarList = doorCalendarMapper.selectByPrimaryKey(doorId);
        if (doorCalendarList.size() > 0){
            List<Map<String, String>> accessCalendar = new ArrayList<>();
            for (DoorCalendar doorCalendar : doorCalendarList) {
                Map<String, String> accessCalendarMap = new HashMap<>();
                accessCalendarMap.put("deviceCalendarDate", doorCalendar.getCalendarDate());
                accessCalendarMap.put("enableDoorOpenGlobal", doorCalendar.getWeatherOpenDoor());
                accessCalendar.add(accessCalendarMap);
            }
            //下发门禁日历命令
            entranceGuardService.handOutDoorCalendarCmd(newDeviceId, operatorEmployeeId, accessCalendar);
        }

        //4.人员开门权限同步
        List<DoorEmployee> doorEmployeeList = doorEmployeeMapper.selectAllByDoorId(doorId);
        //普通开门权限集合人员id去重
        Set<String> employeeIdSet = new HashSet<>();
        for (DoorEmployee doorEmployee : doorEmployeeList) {
            employeeIdSet.add(doorEmployee.getEmployeeId());
        }
        //遍历下发所有人员的开门权限时间区间
        for (String employeeId : employeeIdSet) {

            String sendTime = DateUtils.getDateTime();

            //4.1人员基本信息同步
            Employee employeeTemp = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, companyId);
            //组装人员数据DATA
            Map<String, Object> userInformation = new LinkedHashMap<String, Object>();
            userInformation.put("userId", employeeId);
            userInformation.put("userCode", employeeTemp.getEmployeeNumber());
            userInformation.put("userName", employeeTemp.getEmployeeName());
            userInformation.put("userDeptId", employeeTemp.getEmployeeDepartmentId());
            userInformation.put("userDeptName", employeeTemp.getEmployeeDepartmentName());
            userInformation.put("birthday", employeeTemp.getEmployeeBirthday());
            userInformation.put("entryTime", employeeTemp.getEmployeeEntryTime());
            userInformation.put("probationaryExpired", employeeTemp.getEmployeeProbationaryExpired());
            userInformation.put("contractExpired", employeeTemp.getEmployeeContractExpired());
            userInformation.put("adminFlag", employeeTemp.getAdminFlag());
            userInformation.put("userImg", employeeTemp.getEmployeeImg());
            userInformation.put("userPhoto", employeeTemp.getEmployeePhoto());
            userInformation.put("userFinger1", employeeTemp.getEmployeeFinger1());
            userInformation.put("userFinger2", employeeTemp.getEmployeeFinger2());
            userInformation.put("userFace", employeeTemp.getEmployeeFace());
            userInformation.put("userPhone", employeeTemp.getEmployeePhone());
            userInformation.put("userNFC", employeeTemp.getEmployeeNfc());
            userInformation.put("bluetoothId", employeeTemp.getBluetoothNo());

            //下发人员基本信息
            cmdUtil.handOutCmd(newDeviceId, "C", "UPDATE_USER_INFO", "2001", operatorEmployeeId,
                    "userInfo", userInformation, "1", "", "", employeeId, "", sendTime);

            //4.2人员时间权限同步
            String rangeFlagId = doorEmployeeMapper.selectByEmployeeIdAndDoorId(employeeId, doorId).getRangeFlagId();
            DoorEmployeePermission doorEmployeePermission = doorEmployeePermissionMapper.selectByRangeFlagId(rangeFlagId);

            String doorOpenStartTime = doorEmployeePermission.getDoorOpenStartTime();
            String doorOpenEndTime = doorEmployeePermission.getDoorOpenEndTime();

            List<TimeRangeCommonEmployee> trceList = timeRangeCommonEmployeeMapper.selectByPrimaryKey(rangeFlagId);
            List<Map<String, String>> oneWeekTimeList = new ArrayList<>();
            for (TimeRangeCommonEmployee trce : trceList) {
                Map<String, String> oneDayTimeMap = new HashMap<>();
                oneDayTimeMap.put("weekType", trce.getDayOfWeek());
                oneDayTimeMap.put("startTime", trce.getRangeStartTime());
                oneDayTimeMap.put("endTime", trce.getRangeEndTime());
                oneDayTimeMap.put("doorOpenType", trce.getRangeDoorOpenType());
                oneWeekTimeList.add(oneDayTimeMap);
            }

            //组装更新人员门禁权限业务数据DATA
            Map<String, Object> userPermission = new LinkedHashMap<String, Object>();
            userPermission.put("employeeId", employeeId);
            userPermission.put("permissionValidityBeginTime", doorOpenStartTime);
            userPermission.put("permissionValidityEndTime", doorOpenEndTime);
            try {
                userPermission.put("employeeDoorPassword", doorSetting.getFirstPublishPassword());
            }catch (Exception e){
                userPermission.put("employeeDoorPassword", "");
                System.out.println("【"+employeeTemp.getEmployeeName()+"】当前下发到的门的公共开门密码未设置");
            }
            userPermission.put("oneWeekTimeList", oneWeekTimeList);
            //下发人员开门权限时间区间
            cmdUtil.handOutCmd(newDeviceId, "C", "UPDATE_USER_ACCESS_CONTROL", "3001", operatorEmployeeId,
                    "userPermission", userPermission, "1", "", "", employeeId, "", sendTime);
        }
    }

    @Override
    public ReturnData handOutDeviceSetting(String jsonString) {

        //解析数据
        Map<String, Object> mapJson = (Map<String, Object>) net.sf.json.JSONObject.fromObject(jsonString);

        List<String> deviceIdList = new ArrayList<String>();
//        String heartbeatPeriod = "";
        String faceThreshold = "";
        String fingerThreshold = "";
        String faceDetecTime = "";
        String fanOnTemp = "";
        String fanOffTemp = "";
        String enableSelfHelpBioRegister = "";
        String enableSelfHelpCardRegister = "";
        String enableTimedReboot = "";
        String timedRebootPeriod = "";
        String timedRebootTime = "";
        String downloadTimeSys = "";
        String updateTimeSys = "";
        String downloadTimeApp = "";
        String updateTimeApp = "";
        List<Map<String, String>> lcdBrightnessList = new ArrayList<Map<String, String>>();
        List<Map<String, String>> lcdOffTimeList = new ArrayList<Map<String, String>>();

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        //存储设备设置
        DeviceSetting deviceSetting = new DeviceSetting();

        try {
            deviceIdList = (List<String>) mapJson.get("deviceIdList");
//            heartbeatPeriod = (String) mapJson.get("heartbeatPeriod");
            faceThreshold = (String) mapJson.get("faceThreshold");
            fingerThreshold = (String) mapJson.get("fingerThreshold");
            faceDetecTime = (String) mapJson.get("faceDetecTime");
            fanOnTemp = (String) mapJson.get("fanOnTemp");
            fanOffTemp = (String) mapJson.get("fanOffTemp");
            if ("off".equals((String) mapJson.get("enableSelfHelpBioRegister"))){
                enableSelfHelpBioRegister = "0";
            }else if ("on".equals((String) mapJson.get("enableSelfHelpBioRegister"))){
                enableSelfHelpBioRegister = "1";
            }
            if ("off".equals((String) mapJson.get("enableSelfHelpCardRegister"))){
                enableSelfHelpCardRegister = "0";
            }else if ("on".equals((String) mapJson.get("enableSelfHelpCardRegister"))){
                enableSelfHelpCardRegister = "1";
            }
            if ("off".equals((String) mapJson.get("enableTimedReboot"))){
                enableTimedReboot = "0";
            }else if ("on".equals((String) mapJson.get("enableTimedReboot"))){
                enableTimedReboot = "1";
            }
            timedRebootPeriod = (String) mapJson.get("timedRebootPeriod");
            timedRebootTime = (String) mapJson.get("timedRebootTime");
            downloadTimeSys = (String) mapJson.get("downloadTimeSys");
            updateTimeSys = (String) mapJson.get("updateTimeSys");
            downloadTimeApp = (String) mapJson.get("downloadTimeApp");
            updateTimeApp = (String) mapJson.get("updateTimeApp");
            lcdBrightnessList = (List<Map<String, String>>) mapJson.get("lcdBrightnessList");
            lcdOffTimeList = (List<Map<String, String>>) mapJson.get("lcdOffTimeList");
        } catch (Exception e) {
            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

//        heartbeatPeriod == null
        if (faceThreshold == null
                || fingerThreshold == null
                || faceDetecTime == null
                || fanOnTemp == null
                || fanOffTemp == null
                || enableSelfHelpBioRegister == null
                || enableSelfHelpCardRegister == null
                || enableTimedReboot == null
                || timedRebootPeriod == null
                || timedRebootTime == null
                || downloadTimeSys == null
                || updateTimeSys == null
                || downloadTimeApp == null
                || updateTimeApp == null
                || deviceIdList == null
                || lcdBrightnessList == null
                || lcdOffTimeList == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceIdList.size() == 0) {
            LOGGER.info("没有选择设备");
            returnData.setMessage("没有选择设备");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
//            deviceSetting.setHeartbeatPeriod(heartbeatPeriod);
            deviceSetting.setFaceThreshold(faceThreshold);
            deviceSetting.setFingerThreshold(fingerThreshold);
            deviceSetting.setFaceDetecTime(faceDetecTime);
            deviceSetting.setFanOnTemp(fanOnTemp);
            deviceSetting.setFanOffTemp(fanOffTemp);
            deviceSetting.setEnableSelfHelpBioRegister(enableSelfHelpBioRegister);
            deviceSetting.setEnableSelfHelpCardRegister(enableSelfHelpCardRegister);
            deviceSetting.setEnableTimedReboot(enableTimedReboot);
            deviceSetting.setTimedRebootPeriod(timedRebootPeriod);
            deviceSetting.setTimedRebootTime(timedRebootTime);

            //遍历设备集合，批量下发设置
            for (String deviceId : deviceIdList) {

                deviceSetting.setDeviceId(deviceId);

                DeviceSetting deviceSettingExit = deviceSettingMapper.selectByPrimaryKey(deviceId);
                if (deviceSettingExit == null) {
                    deviceSettingMapper.insertSelective(deviceSetting);
                } else {
                    deviceSettingMapper.updateByPrimaryKeySelective(deviceSetting);
                }

                //删除该设备相关的时间区间设置
                List<TimeRangeLcdBrightness> timeRangeLcdBrightnessExit = timeRangeLcdBrightnessMapper.selectByPrimaryKey(deviceId);
                if (timeRangeLcdBrightnessExit.size() > 0) {
                    timeRangeLcdBrightnessMapper.deleteByPrimaryKey(deviceId);
                }

                for (Map<String, String> lcdBrightnessMap : lcdBrightnessList) {

                    TimeRangeLcdBrightness timeRangeLcdBrightness = new TimeRangeLcdBrightness();
                    timeRangeLcdBrightness.setDeviceId(deviceId);
                    timeRangeLcdBrightness.setStartTime(lcdBrightnessMap.get("startTime"));
                    timeRangeLcdBrightness.setEndTime(lcdBrightnessMap.get("endTime"));
                    timeRangeLcdBrightness.setValue(lcdBrightnessMap.get("value"));
                    //重新插入新的时间区间设置
                    timeRangeLcdBrightnessMapper.insertSelective(timeRangeLcdBrightness);

                }

                //删除该设备相关的时间区间设置
                List<TimeRangeLcdOff> timeRangeLcdOffExit = timeRangeLcdOffMapper.selectByPrimaryKey(deviceId);
                if (timeRangeLcdOffExit.size() > 0) {
                    timeRangeLcdOffMapper.deleteByPrimaryKey(deviceId);
                }

                for (Map<String, String> lcdOffTimeMap : lcdOffTimeList) {

                    String startTime = lcdOffTimeMap.get("startTime");
                    String endTime = lcdOffTimeMap.get("endTime");

                    if (StringUtils.isNotEmpty(endTime)){
                        TimeRangeLcdOff timeRangeLcdOff = new TimeRangeLcdOff();
                        timeRangeLcdOff.setDeviceId(deviceId);
                        timeRangeLcdOff.setStartTime(startTime);
                        timeRangeLcdOff.setEndTime(endTime);
                        //重新插入新的时间区间设置
                        timeRangeLcdOffMapper.insertSelective(timeRangeLcdOff);
                    }
                }

                //mapJson移除deviceIdList
                mapJson.remove("deviceIdList");

//                //构造命令格式
//                DoorCmd doorCmdBindDevice = new DoorCmd();
//                doorCmdBindDevice.setServerId(serverId);
//                doorCmdBindDevice.setDeviceId(deviceId);
//                doorCmdBindDevice.setFileEdition("v1.3");
//                doorCmdBindDevice.setCommandMode("C");
//                doorCmdBindDevice.setCommandType("single");
//                doorCmdBindDevice.setCommandTotal("1");
//                doorCmdBindDevice.setCommandIndex("1");
//                doorCmdBindDevice.setSubCmdId("");
//                doorCmdBindDevice.setAction("UPDATE_DEVICE_SYSTEM_SETTING");
//                doorCmdBindDevice.setActionCode("1003");
//                doorCmdBindDevice.setSendTime(CalendarUtil.getCurrentTime());
//                doorCmdBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//                doorCmdBindDevice.setSuperCmdId(FormatUtil.createUuid());
//                doorCmdBindDevice.setData(JSON.toJSONString(mapJson));
//
//                //获取完整的数据加协议封装格式
//                RabbitMQSender rabbitMQSender = new RabbitMQSender();
//                Map<String, Object> doorCmdPackageAll = CmdUtil.messagePackaging(doorCmdBindDevice, "setting", mapJson, "C");
//                //命令状态设置为: 发送中
//                doorCmdBindDevice.setStatus("1");
//                //设置md5校验值
//                doorCmdBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//                //设置数据库的data字段
//                doorCmdBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//                //命令数据存入数据库
//                entranceGuardService.insertCommand(doorCmdBindDevice);
//                //立即下发数据到MQ
//                rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);

                String sendTime = DateUtils.getDateTime();

                cmdUtil.handOutCmd(deviceId, "C", "UPDATE_DEVICE_SYSTEM_SETTING", "1003",
                        "", "setting", mapJson, "1", "", "", "", "", sendTime);
            }

            //保存并下发系统更新设置
            String codeSys = deviceService.updateDeviceSystem(deviceIdList, downloadTimeSys, updateTimeSys);

            if ("0".equals(codeSys)){
                returnData.setMessage("没有查询到可以升级的系统升级包");
                returnData.setReturnCode("4202");
                return returnData;
            }

            //保存下发应用更新设置
            deviceService.updateDeviceApplication(deviceIdList, downloadTimeApp, updateTimeApp);

            returnData.setMessage("已执行下发设备系统设置操作");
            returnData.setReturnCode("3000");
            return returnData;
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
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

	@Override
	public List<String> getDeviceNameOffLine(String companyId) {
		return deviceMapper.getDeviceNameOffLine(companyId);
	}

}
