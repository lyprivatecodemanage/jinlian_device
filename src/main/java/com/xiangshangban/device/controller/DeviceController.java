package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.UrlUtil;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IDeviceService;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

/**
 * 控制层：设备操作
 */

@Controller
@RequestMapping(value = "/device")
public class DeviceController {

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private DeviceHeartbeatMapper deviceHeartbeatMapper;

    @Autowired
    private IEntranceGuardService entranceGuardService;

    @Autowired
    private DeviceSettingMapper deviceSettingMapper;

    @Autowired
    private TimeRangeLcdBrightnessMapper timeRangeLcdBrightnessMapper;

    @Autowired
    private TimeRangeLcdOffMapper timeRangeLcdOffMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private DeviceSettingUpdateMapper deviceSettingUpdateMapper;

    @Autowired
    private DeviceUpdatePackSysMapper deviceUpdatePackSysMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    /**
     * 平台新增设备，未绑定公司的设备
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/addDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData addDevice(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8",
         "macAddress": "001062654135123"
         }
         */

        System.out.println(jsonString);

        //解析JSON数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);

        String deviceId = "";
        String macAddress = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            deviceId = mapJson.get("deviceId");
            macAddress = mapJson.get("macAddress");
        }catch (Exception e){

            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null || macAddress == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            String returnCode = deviceService.addDevice(deviceId, macAddress);

            if ("0".equals(returnCode)){
                returnData.setMessage("设备已存在");
                returnData.setReturnCode("4202");
                return returnData;
            }else if ("1".equals(returnCode)){
                returnData.setMessage("数据请求成功");
                returnData.setReturnCode("3000");
                return returnData;
            }else {
                returnData.setMessage("服务器错误");
                returnData.setReturnCode("3001");
                return returnData;
            }

        }catch (Exception e){
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 查找当前公司的设备信息(包括筛选功能，无参传入查询全部设备)
     * @param jsonString
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/findDeviceInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData findDeviceInformation(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "companyId": "A3789DSYAG7FA7",
         "companyName": "华龙国际集团",
         "deviceName": "设备1",
         "deviceId": "0f1a21d4e6fd3cb8",
         "isOnline": "0",
         "activeStatus": "0"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
        String companyId = "";
        String companyName = "";
        String deviceName = "";
        String deviceId = "";
        String isOnline = "";
        String activeStatus = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            companyId = mapJson.get("companyId");
            companyName = mapJson.get("companyName");
            deviceName = mapJson.get("deviceName");
            deviceId = mapJson.get("deviceId");
            isOnline = mapJson.get("isOnline");
            activeStatus = mapJson.get("activeStatus");
        }catch (Exception e){

            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (companyId == null
                || companyName == null
                || deviceName == null
                || deviceId == null
                || isOnline == null
                || activeStatus == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            List<Map<String, String>> mapListResult = deviceService.findDeviceInformation(companyId,
                    companyName, deviceName, deviceId, isOnline, activeStatus);

            if (mapListResult.size() > 0){
                returnData.setData(mapListResult);
                returnData.setMessage("数据请求成功");
                returnData.setReturnCode("3000");
                return returnData;
            }else {
                returnData.setData(mapListResult);
                returnData.setMessage("没有匹配的数据");
                returnData.setReturnCode("4203");
                return returnData;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 平台管理员编辑当前设备的信息
     * @param jsonString
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/editorDeviceInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData editorDeviceInformation(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8",
         "deviceName": "无敌的设备",
         "companyId": "A4F5A833EE674AE6B85F5582CCB3550D",
         "companyName":"无敌的公司",
         "doorName": "无敌的大门",
         "devicePlace": "无敌的乐山新村",
         "deviceUsages": "无敌的考勤"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String deviceId = "";
        String companyId = "";
        String companyName = "";
        String deviceName = "";
        String doorName = "";
        String devicePlace = "";
        String deviceUsages = "";

        try {
            deviceId = mapJson.get("deviceId");
            companyId = mapJson.get("companyId");
            companyName = mapJson.get("companyName");
            deviceName = mapJson.get("deviceName");
            doorName = mapJson.get("doorName");
            devicePlace = mapJson.get("devicePlace");
            deviceUsages = mapJson.get("deviceUsages");
        }catch (Exception e){
            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null
                || companyId == null
                || companyName == null
                || deviceName == null
                || doorName == null
                || devicePlace == null
                || deviceUsages == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if ("".equals(deviceId)){
            System.out.println("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            deviceService.editorDeviceInformation(deviceId, companyId, companyName, deviceName,
                    doorName, devicePlace, deviceUsages);

            returnData.setMessage("数据请求成功");
            returnData.setReturnCode("3000");
            return returnData;
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 重启指定的设备(下发重启设备的命令)
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/rebootDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData rebootDevice(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "deviceId": "9yqf97ayy397fq72"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);

        String deviceId = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            deviceId = mapJson.get("deviceId");
        }catch (Exception e){
            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        } //

        if ("".equals(deviceId)){
            System.out.println("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            String superCmdId = deviceService.rebootDevice(deviceId);

            returnData.setMessage("数据请求成功");
            returnData.setReturnCode("3000");
            return returnData;
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 查询所有的设备信息（一个设备信息列表）
     */
    @ResponseBody
    @RequestMapping("/getAllDevice")
    public List<Device> getAllDeviceInfo(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "companyId":"A3789DSYAG7FA7"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
        String companyId = mapJson.get("companyId");

        return deviceService.queryAllDeviceInfo(companyId);
    }

    /**
     * 绑定设备
     */
    @ResponseBody
    @RequestMapping("/bindDevice")
    public void bindDevice(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "companyId": "A3789DSYAG7FA7",
         "companyName": "无敌的公司",
         "deviceId": "0f1a21d4e6fd3cb8"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
        String companyId = mapJson.get("companyId");
        String companyName = mapJson.get("companyName");
        String deviceId = mapJson.get("deviceId");

        deviceService.bindDevice(companyId, companyName, deviceId);

    }

    /**
     * 解绑设备
     */
    @ResponseBody
    @RequestMapping("/unBindDevice")
    public void unBindDevice(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "doorId": "0f1a21d4e6fd3cb8"
         }
         */

        System.out.println(jsonString);

        //解析数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
        String doorId = mapJson.get("doorId");

        //查找设备id
        String deviceId = doorMapper.findAllByDoorId(doorId).getDeviceId();

        deviceService.unBindDevice(deviceId);

    }

    /**
     * 保存设备上传的心跳信息（心跳信息包括设备的各种状态信息，如cpu、内存，HTTP POST上传）
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping("/saveDeviceHeartBeat")
    public Map<String, Object> saveDeviceHeartBeat(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "serverId": "001",
         "deviceId": "0f1a21d4e6fd3cb8",
         "fileEdition": "v1.3",
         "commandMode": "C",
         "commandType": "single",
         "commandTotal": "1",
         "commandIndex": "1",
         "sendTime": "2017-10-31 19:12:25",
         "outOfTime": "2017-11-03 19:12",
         "MD5Check": "D8137716E799D4830A1D1BC3321860E7",
         "command": {
         "superCMDID": "49641B5A57474BE2B5E4BE126AC63C49",
         "subCMDID": "",
         "ACTION": "UPLOAD_DEVICE_HEARTBEAT",
         "ACTIONCode": "1005"
         },
         "data": {
         "heartbeat": {
         "deviceId": "111",
         "lockState": "0",
         "wifiOpne": "0",
         "ip": "192.168.0.20",
         "mask": "255.255.255.0",
         "gate": "192.168.0.1",
         "timeLimitDoorOpen": "0",
         "timeLimitLockOpen": "0",
         "doorAlarm": "0",
         "fireAlarm": "0",
         "userNumber": "0",
         "KeySwitch": "0",
         "companyId": "0",
         "companyName": "0",
         "dataUploadstate": "0",
         "romAvailableSize": "70.8",
         "cpuFreq": "996000",
         "cpuTemper": "56.8",
         "cpuUnilization": "70.8",
         "cpuUserUnilization": "70.8",
         "internalUnilization ": "70.8",
         "appUsed": [
         {
         "appName": " CPUserialport",
         "cpu": "70.8",
         "ram": "70.8"
         }
         ]
         }
         }
         }
         */

        System.out.println("------------"+jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("heartbeatData=", "");
        System.out.println(jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")){
            System.out.println("收到空的心跳数据");
            return new HashMap<>();
        }else {
            //解析JSON数据
            Map<String, Object> mapJson = (Map<String, Object>)net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
            Map<String, String> heartbeatMap = new HashMap<>();

            //回复设备
            Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
            Map<String, Object> resultData = new LinkedHashMap<String, Object>();
            String resultCode = "";
            String resultMessage = "";
            String deviceId = "";

            //校验MD5
            //获取对方的md5
            String otherMd5 = (String) mapJson.get("MD5Check");
            mapJson.remove("MD5Check");
            String messageCheck = JSON.toJSONString(mapJson);
            //生成我的md5
            String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
//        System.out.println("MD5 = " + myMd5);
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)){

                System.out.println("MD5校验成功，数据完好无损");
                deviceId = (String) mapJson.get("deviceId");

                //CRC16校验deviceId
                if (deviceService.checkCrc16DeviceId(deviceId)){

                    if (((Map<String, String>) mapJson.get("command")).get("ACTION").equals("UPLOAD_DEVICE_HEARTBEAT")){
                        //获取心跳map
                        heartbeatMap = ((Map<String, Map<String, String>>)mapJson.get("data")).get("heartbeat");

                        DeviceHeartbeat deviceHeartbeat = new DeviceHeartbeat();

                        deviceHeartbeat.setDeviceId(deviceId);
                        deviceHeartbeat.setLockState(heartbeatMap.get("lockState"));
                        deviceHeartbeat.setWifiOpne(heartbeatMap.get("wifiOpne"));
                        deviceHeartbeat.setIp(heartbeatMap.get("ip"));
                        deviceHeartbeat.setMask(heartbeatMap.get("mask"));
                        deviceHeartbeat.setGate(heartbeatMap.get("gate"));
                        deviceHeartbeat.setTimeLimitDoorOpen(heartbeatMap.get("timeLimitDoorOpen"));
                        deviceHeartbeat.setTimeLimitLockOpen(heartbeatMap.get("timeLimitLockOpen"));
                        deviceHeartbeat.setDoorAlarm(heartbeatMap.get("doorAlarm"));
                        deviceHeartbeat.setFireAlarm(heartbeatMap.get("fireAlarm"));
                        deviceHeartbeat.setUserNumber(heartbeatMap.get("userNumber"));
                        deviceHeartbeat.setKeySwitch(heartbeatMap.get("keySwitch"));
                        deviceHeartbeat.setCompanyId(heartbeatMap.get("companyId"));
                        deviceHeartbeat.setCompanyName(heartbeatMap.get("companyName"));
                        deviceHeartbeat.setDataUploadstate(heartbeatMap.get("dataUploadstate"));
                        deviceHeartbeat.setRomAvailableSize(heartbeatMap.get("romAvailableSize"));
                        deviceHeartbeat.setCpuFreq(heartbeatMap.get("cpuFreq"));
//                        System.out.println("转换前的温度："+heartbeatMap.get("cpuTemper"));
                        //保留一位小数点的温度
                        Float floatCpuTemper = Float.parseFloat(heartbeatMap.get("cpuTemper"));
                        String cpuTemper = String.format("%.1f", floatCpuTemper/1000);
//                        System.out.println("转换后的温度："+cpuTemper);
                        deviceHeartbeat.setCpuTemper(cpuTemper);
                        deviceHeartbeat.setCpuUnilization(heartbeatMap.get("cpuUnilization"));
                        deviceHeartbeat.setCpuUserUnilization(heartbeatMap.get("cpuUserUnilization"));
                        deviceHeartbeat.setInternalUnilization(heartbeatMap.get("internalUnilization"));
                        deviceHeartbeat.setAppUsed(JSON.toJSONString(((Map<String, Map<String, Map<String, Object>>>) mapJson.get("data"))
                                .get("heartbeat").get("appUsed")));
                        deviceHeartbeat.setTime(DateUtils.getDateTime());

                        deviceHeartbeatMapper.insertSelective(deviceHeartbeat);
                        System.out.println("心跳数据已存储");

                    }

                    //回复设备
                    resultCode = "0";
                    resultMessage = "执行成功";
                    resultData.put("resultCode", resultCode);
                    resultData.put("resultMessage", resultMessage);
                    resultMap.put("result", resultData);
                }

            }else {
                System.out.println("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
                resultMap.put("result", resultData);
            }

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
            doorCmdRecord.setAction("UPLOAD_DEVICE_HEARTBEAT");
            doorCmdRecord.setActionCode("1005");
            doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdRecord.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultMap));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll =  rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
            //命令状态设置为: 已回复
            doorCmdRecord.setStatus("5");
            doorCmdRecord.setResultCode(resultCode);
            doorCmdRecord.setResultMessage(resultMessage);
            //设置md5校验值
            doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
            //设置数据库的data字段
            doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
            //命令数据存入数据库
            entranceGuardService.insertCommand(doorCmdRecord);

            return doorRecordAll;
        }
    }

    /**
     * 获取心跳数据展示到前端
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping("/getDeviceHeartBeat")
    public ReturnData getDeviceHeartBeat(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "companyName":"华龙国际集团",
         "cpuUserUnilizationCondition":"1",
         "cpuTemperCondition":"1"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = net.sf.json.JSONObject.fromObject(jsonString);

        String companyName = "";
        String cpuUserUnilizationCondition = "";
        String cpuTemperCondition = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            companyName = mapJson.get("companyName");
            cpuUserUnilizationCondition = mapJson.get("cpuUserUnilizationCondition");
            cpuTemperCondition = mapJson.get("cpuTemperCondition");
        }catch (Exception e){
            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (companyName == null || cpuUserUnilizationCondition == null || cpuTemperCondition == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            //设备id去重集合
            Set<String> deviceIdList = new HashSet<String>();
            //返回结果集合
            Map<String, Object> deviceHeartBeatMapResult = new HashMap<String, Object>();
            List<Map<String, Object>> deviceHeartbeatListResult = new ArrayList<Map<String, Object>>();

            List<Device> deviceList = deviceMapper.selectAllDeviceInfo("");

            //遍历设备id
            for (Device device : deviceList) {
                //去重复的deviceId
                deviceIdList.add(device.getDeviceId());
            }

//        System.out.println(JSON.toJSONString(deviceIdList));

            //算出CPU两个展示参数各自的平均值
            double CpuUserUnilizationSum = 0;
            float CpuTemperSum = 0;
            int number = 0;

            //遍历查找每一个设备最新的心跳数据信息
            for (String deviceId : deviceIdList) {
                Map<String, Object> deviceHeartbeat = deviceHeartbeatMapper
                        .selectLatestByDeviceId(deviceId, "", 0, 0, "", "");

                if (deviceHeartbeat != null){
                    String cpuUserUnilization = ((String) deviceHeartbeat.get("cpu_user_unilization")).replace("%", "");
                    int cpuUserUnilizationNumber = Integer.valueOf(cpuUserUnilization);
                    //累加cpu占用率
                    CpuUserUnilizationSum = CpuUserUnilizationSum + cpuUserUnilizationNumber;
                    String cpuTemper = (String) deviceHeartbeat.get("cpu_temper");
                    float cpuTemperNumber = Float.valueOf(cpuTemper);
                    //累加cpu温度
                    CpuTemperSum = CpuTemperSum + cpuTemperNumber;
                    //累加心跳数据有多少条
                    number = number + 1;

                    System.out.println(JSON.toJSONString(deviceHeartbeat));
                }
            }

            //算出CPU两个展示参数各自的平均值
            String averageCpuUserUnilization = String.valueOf((int) Math.ceil(CpuUserUnilizationSum / number));
            String averageCpuTemper = String.format("%.1f", CpuTemperSum / number);

            System.out.println("总的cpu占用率为："+CpuUserUnilizationSum);
            System.out.println("总的cpu温度为："+CpuTemperSum);
            System.out.println("总的心跳条数为："+number);
            System.out.println("平均cpu占用率为："+averageCpuUserUnilization);
            System.out.println("平均cpu温度为："+averageCpuTemper);

            //遍历查找每一个设备最新的心跳数据信息
            for (String deviceId : deviceIdList) {
                Map<String, Object> deviceHeartbeat = deviceHeartbeatMapper
                        .selectLatestByDeviceId(deviceId, companyName, Float.valueOf(averageCpuUserUnilization), Float.valueOf(averageCpuTemper), cpuUserUnilizationCondition, cpuTemperCondition);
                if (deviceHeartbeat != null){
                    deviceHeartbeatListResult.add(deviceHeartbeat);
                    System.out.println("【"+deviceId+"】有符合条件的心跳信息");
                }else {
                    System.out.println("【"+deviceId+"】没有符合条件的心跳信息");
                }
            }

            //返回数据
            deviceHeartBeatMapResult.put("averageCpuUserUnilization", averageCpuUserUnilization);
            deviceHeartBeatMapResult.put("averageCpuTemper", averageCpuTemper);
            deviceHeartBeatMapResult.put("deviceHeartbeatListResult", deviceHeartbeatListResult);

            returnData.setData(deviceHeartBeatMapResult);
            returnData.setMessage("数据请求成功");
            returnData.setReturnCode("3000");
            return returnData;
        }catch (Exception e){
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 下发设备系统设置
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping("/handOutDeviceSetting")
    public ReturnData handOutDeviceSetting(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "deviceIdList": [
         "0f1a21d4e6fd3cb8",
         "0f1a21d4e6fd3cb8"
         ],
         "heartbeatPeriod": "60",
         "faceThreshold": "79.1",
         "fingerThreshold": "56.8",
         "faceDetecTime": "15",
         "lcdBrightnessList": [
         {
         "startTime": "10:06",
         "endTime": "11:06",
         "value": "80"
         },
         {
         "startTime": "14:00",
         "endTime": "18:00",
         "value": "50"
         }
         ],
         "lcdOffTimeList": [
         {
         "startTime": "10:06",
         "endTime": "11:06"
         },
         {
         "startTime": "14:00",
         "endTime": "18:00"
         }
         ],
         "fanOnTemp": "70.8",
         "fanOffTemp": "65.3",
         "enableSelfHelpBioRegister": "0",
         "enableSelfHelpCardRegister": "0",
         "enableTimedReboot": "0",
         "timedRebootPeriod": "2",
         "timedRebootTime": "10:06",
         "downloadTimeSys":"2017-11-13 17:03",
         "updateTimeSys":"2017-11-13 03:00",
         "downloadTimeApp":"2017-11-13 17:03",
         "updateTimeApp":"2017-11-13 03:00"
         }
         */

        System.out.println(jsonString);

        //解析数据
        Map<String, Object> mapJson = (Map<String, Object>)net.sf.json.JSONObject.fromObject(jsonString);

        List<String> deviceIdList = new ArrayList<String>();
        String heartbeatPeriod = "";
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
            heartbeatPeriod = (String) mapJson.get("heartbeatPeriod");
            faceThreshold = (String) mapJson.get("faceThreshold");
            fingerThreshold = (String) mapJson.get("fingerThreshold");
            faceDetecTime = (String) mapJson.get("faceDetecTime");
            fanOnTemp = (String) mapJson.get("fanOnTemp");
            fanOffTemp = (String) mapJson.get("fanOffTemp");
            enableSelfHelpBioRegister = (String) mapJson.get("enableSelfHelpBioRegister");
            enableSelfHelpCardRegister = (String) mapJson.get("enableSelfHelpCardRegister");
            enableTimedReboot = (String) mapJson.get("enableTimedReboot");
            timedRebootPeriod = (String) mapJson.get("timedRebootPeriod");
            timedRebootTime = (String) mapJson.get("timedRebootTime");
            downloadTimeSys = (String) mapJson.get("downloadTimeSys");
            updateTimeSys = (String) mapJson.get("updateTimeSys");
            downloadTimeApp = (String) mapJson.get("downloadTimeApp");
            updateTimeApp = (String) mapJson.get("updateTimeApp");
            lcdBrightnessList = (List<Map<String, String>>) mapJson.get("lcdBrightnessList");
            lcdOffTimeList = (List<Map<String, String>>) mapJson.get("lcdOffTimeList");
        }catch (Exception e){
            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (heartbeatPeriod == null
                || faceThreshold == null
                || fingerThreshold == null
                || faceDetecTime ==null
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
                || lcdOffTimeList == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceIdList.size() == 0){
            System.out.println("没有选择设备");
            returnData.setMessage("没有选择设备");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            deviceSetting.setHeartbeatPeriod(heartbeatPeriod);
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

                    TimeRangeLcdOff timeRangeLcdOff = new TimeRangeLcdOff();
                    timeRangeLcdOff.setDeviceId(deviceId);
                    timeRangeLcdOff.setStartTime(lcdOffTimeMap.get("startTime"));
                    timeRangeLcdOff.setEndTime(lcdOffTimeMap.get("endTime"));
                    //重新插入新的时间区间设置
                    timeRangeLcdOffMapper.insertSelective(timeRangeLcdOff);
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
                doorCmdBindDevice.setAction("UPDATE_DEVICE_SYSTEM_SETTING");
                doorCmdBindDevice.setActionCode("1003");
                doorCmdBindDevice.setSendTime(CalendarUtil.getCurrentTime());
                doorCmdBindDevice.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(), 3));
                doorCmdBindDevice.setSuperCmdId(FormatUtil.createUuid());
                doorCmdBindDevice.setData(JSON.toJSONString(mapJson));

                //获取完整的数据加协议封装格式
                RabbitMQSender rabbitMQSender = new RabbitMQSender();
                Map<String, Object> doorCmdPackageAll = rabbitMQSender.messagePackaging(doorCmdBindDevice, "setting", mapJson, "C");
                //命令状态设置为: 发送中
                doorCmdBindDevice.setStatus("1");
                //设置md5校验值
                doorCmdBindDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
                //设置数据库的data字段
                doorCmdBindDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
                //命令数据存入数据库
                entranceGuardService.insertCommand(doorCmdBindDevice);
//            //立即下发数据到MQ
//            rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);
            }

            //保存并下发系统更新设置
            deviceService.updateDeviceSystem(deviceIdList, downloadTimeSys, updateTimeSys);

            //保存下发应用更新设置
            deviceService.updateDeviceApplication(deviceIdList, downloadTimeApp, updateTimeApp);

            returnData.setMessage("数据请求成功");
            returnData.setReturnCode("3000");
            return returnData;
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }
}
