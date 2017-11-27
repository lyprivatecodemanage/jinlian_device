package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IDeviceService;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
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

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

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
    private DeviceUpdatePackAppMapper deviceUpdatePackAppMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EmployeeBluetoothCountMapper employeeBluetoothCountMapper;

    /**
     * 平台新增设备，未绑定公司的设备
     *
     * @param jsonString
     */
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/addDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData addDevice(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "role":"superAdmin",
         "deviceId": "0f1a21d4e6fd3cb8",
         "macAddress": "001062654135123"
         }
         */

        System.out.println(jsonString);

        //解析JSON数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        String role = "";
        String deviceId = "";
        String macAddress = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            role = mapJson.get("role");
            deviceId = mapJson.get("deviceId");
            macAddress = mapJson.get("macAddress");
        } catch (Exception e) {

            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (role == null || deviceId == null || macAddress == null) {
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        //角色权限控制的判断，仅此一处有，目前通过前端判断role角色来隐藏入口控制权限
        if ("superAdmin".equals(role)) {
            System.out.println("已匹配到【超级管理员】角色");
            try {
                String returnCode = deviceService.addDevice(deviceId, macAddress);

                if ("0".equals(returnCode)) {
                    returnData.setMessage("设备已存在");
                    returnData.setReturnCode("4202");
                    return returnData;
                } else if ("1".equals(returnCode)) {
                    returnData.setMessage("添加设备成功");
                    returnData.setReturnCode("3000");
                    return returnData;
                } else {
                    returnData.setMessage("服务器错误");
                    returnData.setReturnCode("3001");
                    return returnData;
                }

            } catch (Exception e) {
                e.printStackTrace();
                returnData.setMessage("服务器错误");
                returnData.setReturnCode("3001");
                return returnData;
            }

        } else if ("admin".equals(role)) {
            System.out.println("已匹配到【企业管理员】角色");
            returnData.setMessage("您没有操作权限");
            returnData.setReturnCode("3006");
            return returnData;
        } else if ("user".equals(role)) {
            System.out.println("已匹配到【普通用户】角色");
            returnData.setMessage("您没有操作权限");
            returnData.setReturnCode("3006");
            return returnData;
        } else {
            System.out.println("没有匹配的角色信息");
            returnData.setMessage("没有匹配的角色信息");
            returnData.setReturnCode("3006");
            return returnData;
        }
    }

    /**
     * 查找当前公司的设备信息(包括筛选功能，无参传入查询全部设备)
     *
     * @param jsonString
     * @return
     */
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/findDeviceInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData findDeviceInformation(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "companyId": "A3789DSYAG7FA7",
         "companyName": "华龙国际集团",
         "deviceName": "设备1",
         "deviceId": "0f1a21d4e6fd3cb8",
         "isOnline": "0",
         "activeStatus": "0",
         "page":"1",
         "rows":"3"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);
        String companyId = "";
        String companyName = "";
        String deviceName = "";
        String deviceId = "";
        String isOnline = "";
        String activeStatus = "";
        String page = "";
        String rows = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            companyId = mapJson.get("companyId");
        } catch (Exception e) {
            System.out.println("公司id为null");
            returnData.setMessage("请登录账号后再操作");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            companyName = mapJson.get("companyName");
            deviceName = mapJson.get("deviceName");
            deviceId = mapJson.get("deviceId");
            isOnline = mapJson.get("isOnline");
            activeStatus = mapJson.get("activeStatus");
            page = mapJson.get("page");
            rows = mapJson.get("rows");
        } catch (Exception e) {

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
                || activeStatus == null
                || page == null
                || rows == null) {
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            System.out.println("page = " + Integer.valueOf(page) + "\n" + "rows = " + Integer.valueOf(rows));
            Page pageHelperResult = PageHelper.startPage(Integer.valueOf(page), Integer.valueOf(rows));
            List<Map<String, String>> mapListResult = deviceService.findDeviceInformation(companyId,
                    companyName, deviceName, deviceId, isOnline, activeStatus);

            if (mapListResult.size() > 0) {
                Map map = PageUtils.doSplitPageOther(null, null, page, rows, pageHelperResult);
                returnData.setData(mapListResult);
                returnData.setMessage("数据请求成功");
                returnData.setReturnCode("3000");
                returnData.setPagecountNum((String) map.get("pagecountNum"));
                returnData.setTotalPages((String) map.get("totalPages"));
                return returnData;
            } else {
                if (!"".equals(companyId)){
                    returnData.setData(mapListResult);
                    returnData.setMessage("您的公司暂无已绑定的设备");
                    returnData.setReturnCode("4203");
                    return returnData;
                }else {
                    returnData.setData(mapListResult);
                    returnData.setMessage("数据请求成功");
                    returnData.setReturnCode("3000");
                    return returnData;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 平台管理员编辑当前设备的信息
     *
     * @param jsonString
     * @return
     */
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/editorDeviceInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData editorDeviceInformation(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8",
         "deviceName": "无敌的设备",
         "companyId": "FE0FB748F7A04BCAA3E4D736E3964B06",
         "companyName":"无敌的公司",
         "devicePlace": "无敌的乐山新村",
         "deviceUsages": "无敌的考勤"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String deviceId = "";
        String companyId = "";
        String companyName = "";
        String deviceName = "";
        String devicePlace = "";
        String deviceUsages = "";

//        try {
//            companyId = mapJson.get("companyId");
//        } catch (Exception e) {
//            System.out.println("公司id为null");
//            returnData.setMessage("请登录账号后再操作");
//            returnData.setReturnCode("3006");
//            return returnData;
//        }

//        try {
//            deviceId = mapJson.get("deviceId");
//            companyName = mapJson.get("companyName");
//            deviceName = mapJson.get("deviceName");
//            devicePlace = mapJson.get("devicePlace");
//            deviceUsages = mapJson.get("deviceUsages");
//        } catch (Exception e) {
//            System.out.println("必传参数字段为null");
//            returnData.setMessage("必传参数字段为null");
//            returnData.setReturnCode("3006");
//            return returnData;
//        }

//        if (deviceId == null
//                || companyId == null
//                || companyName == null
//                || deviceName == null
//                || devicePlace == null
//                || deviceUsages == null) {
//            System.out.println("必传参数字段不存在");
//            returnData.setMessage("必传参数字段不存在");
//            returnData.setReturnCode("3006");
//            return returnData;
//        }

//        if ("".equals(deviceId)) {
//            System.out.println("必传参数字段为空字符串");
//            returnData.setMessage("必传参数字段为空字符串");
//            returnData.setReturnCode("3006");
//            return returnData;
//        }

        try {
            companyId = mapJson.get("companyId");
            deviceId = mapJson.get("deviceId");
            companyName = mapJson.get("companyName");
            deviceName = mapJson.get("deviceName");
            devicePlace = mapJson.get("devicePlace");
            deviceUsages = mapJson.get("deviceUsages");
        } catch (Exception e) {

        }

        try {
            deviceService.editorDeviceInformation(deviceId, companyId, companyName, deviceName,
                    devicePlace, deviceUsages);

            returnData.setMessage("编辑信息成功");
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
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/rebootDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData rebootDevice(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "deviceId": "9yqf97ayy397fq72"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        String deviceId = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            deviceId = mapJson.get("deviceId");
        } catch (Exception e) {
            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null) {
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if ("".equals(deviceId)) {
            System.out.println("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            String superCmdId = deviceService.rebootDevice(deviceId);

            returnData.setMessage("重启操作已发出请求，请前往设备查看，网络波动时，会有一定的延时");
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
     * 查询当前公司的所有设备信息（一个设备信息列表）
     */
    @ResponseBody
    @RequestMapping("/getAllDevice")
    public String getAllDeviceInfo(@RequestBody String jsonString) {

        //提取数据
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        String companyId = jsonObject.get("companyId") != null ? jsonObject.get("companyId").toString() : null;
        List<Map> maps = deviceService.queryAllDeviceInfo(companyId);
        //添加返回码
        Map result = ReturnCodeUtil.addReturnCode(maps);
        System.out.println(JSONObject.toJSONString(result));
        return JSONObject.toJSONString(result);
    }

    /**
     * 查询当前公司的所有门的信息（一个门信息列表）
     */
    @ResponseBody
    @RequestMapping("/getAllDoorInfoByCompanyId")
    public List<Door> getAllDoorInfoByCompanyId(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "companyId":"A3789DSYAG7FA7"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);
        String companyId = mapJson.get("companyId");

        return deviceService.queryAllDoorInfoByCompanyId(companyId);
    }

//    /**
//     * 绑定设备
//     */
//    @ResponseBody
//    @RequestMapping("/bindDevice")
//    public void bindDevice(@RequestBody String jsonString){
//
//        /**
//         * 测试数据
//         {
//         "companyId": "A3789DSYAG7FA7",
//         "companyName": "无敌的公司",
//         "deviceId": "0f1a21d4e6fd3cb8"
//         }
//         */
//
//        System.out.println(jsonString);
//
//        //提取数据
//        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
//        String companyId = mapJson.get("companyId");
//        String companyName = mapJson.get("companyName");
//        String deviceId = mapJson.get("deviceId");
//
//        deviceService.bindDevice(companyId, companyName, deviceId);
//
//    }

//    /**
//     * 解绑设备
//     */
//    @ResponseBody
//    @RequestMapping("/unBindDevice")
//    public void unBindDevice(@RequestBody String jsonString){
//
//        /**
//         * 测试数据
//         {
//         "doorId": "0f1a21d4e6fd3cb8"
//         }
//         */
//
//        System.out.println(jsonString);
//
//        //解析数据
//        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
//        String doorId = mapJson.get("doorId");
//
//        //查找设备id
//        String deviceId = doorMapper.findAllByDoorId(doorId).getDeviceId();
//
//        deviceService.unBindDevice(deviceId);
//
//    }

    /**
     * 保存设备上传的心跳信息（心跳信息包括设备的各种状态信息，如cpu、内存，HTTP POST上传）
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping("/saveDeviceHeartBeat")
    public Map<String, Object> saveDeviceHeartBeat(@RequestBody String jsonString) {

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

        System.out.println("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("heartbeatData=", "");
        System.out.println(jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")) {
            System.out.println("收到空的心跳数据");
            return new HashMap<>();
        } else {
            //解析JSON数据
            Map<String, Object> mapJson = (Map<String, Object>) net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
            Map<String, String> heartbeatMap = new HashMap<>();

            //回复设备
            Map<String, Object> resultData = new LinkedHashMap<String, Object>();
            String resultCode = "";
            String resultMessage = "";
            String deviceId = (String) mapJson.get("deviceId");

            //校验MD5
            //获取对方的md5
            String otherMd5 = (String) mapJson.get("MD5Check");
            mapJson.remove("MD5Check");
            String messageCheck = JSON.toJSONString(mapJson);
            //生成我的md5
            String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
//        System.out.println("MD5 = " + myMd5);
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)) {

                System.out.println("MD5校验成功，数据完好无损");

                //CRC16校验deviceId
                if (deviceService.checkCrc16DeviceId(deviceId)) {

                    if (((Map<String, String>) mapJson.get("command")).get("ACTION").equals("UPLOAD_DEVICE_HEARTBEAT")) {
                        //获取心跳map
                        heartbeatMap = ((Map<String, Map<String, String>>) mapJson.get("data")).get("heartbeat");

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
                        String cpuTemper = String.format("%.1f", floatCpuTemper / 1000);
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
                }

            } else {
                System.out.println("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
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
            doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultData));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll = rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
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
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping("/getDeviceHeartBeat")
    public ReturnData getDeviceHeartBeat(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "companyName":"华龙国际集团",
         "cpuUserUnilizationCondition":"1",
         "cpuTemperCondition":"1",
         "page":"1",
         "rows":"3"
         }
         */

        System.out.println(jsonString);

        //提取数据
        Map<String, String> mapJson = net.sf.json.JSONObject.fromObject(jsonString);

        String companyName = "";
        String cpuUserUnilizationCondition = "";
        String cpuTemperCondition = "";
        String page = "";
        String rows = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            companyName = mapJson.get("companyName");
            cpuUserUnilizationCondition = mapJson.get("cpuUserUnilizationCondition");
            cpuTemperCondition = mapJson.get("cpuTemperCondition");
            page = mapJson.get("page");
            rows = mapJson.get("rows");
        } catch (Exception e) {
            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (companyName == null
                || cpuUserUnilizationCondition == null
                || cpuTemperCondition == null
                || page == null
                || rows == null) {
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

            List<Map> deviceList = deviceMapper.selectAllDeviceInfo("");
//            System.out.println("deviceList: "+JSON.toJSONString(deviceList));
            //遍历设备id
            if (deviceList.size() > 0) {
                for (Map<String, String> map : deviceList) {
                    //去重复的deviceId
                    deviceIdList.add(map.get("deviceId"));
                }

//            System.out.println(JSON.toJSONString(deviceIdList));

                //算出CPU两个展示参数各自的平均值
                double CpuUserUnilizationSum = 0;
                float CpuTemperSum = 0;
                int number = 0;

                //遍历查找每一个设备最新的心跳数据信息
                for (String deviceId : deviceIdList) {
                    Map<String, Object> deviceHeartbeat = deviceHeartbeatMapper
                            .selectLatestByDeviceId(deviceId, "", 0, 0, "", "");

                    if (deviceHeartbeat != null) {
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

                System.out.println("总的cpu占用率为：" + CpuUserUnilizationSum);
                System.out.println("总的cpu温度为：" + CpuTemperSum);
                System.out.println("总的心跳条数为：" + number);
                System.out.println("平均cpu占用率为：" + averageCpuUserUnilization);
                System.out.println("平均cpu温度为：" + averageCpuTemper);

                //遍历查找每一个设备最新的心跳数据信息
                for (String deviceId : deviceIdList) {
//                    System.out.println("page = "+Integer.valueOf(page)+"\n"+"rows = "+Integer.valueOf(rows));
//                    Page pageHelperResult = PageHelper.startPage(Integer.valueOf(page), Integer.valueOf(rows));
                    Map<String, Object> deviceHeartbeat = deviceHeartbeatMapper
                            .selectLatestByDeviceId(deviceId, companyName, Float.valueOf(averageCpuUserUnilization), Float.valueOf(averageCpuTemper), cpuUserUnilizationCondition, cpuTemperCondition);
                    if (deviceHeartbeat != null) {
                        deviceHeartbeatListResult.add(deviceHeartbeat);
                        System.out.println("【" + deviceId + "】有符合条件的心跳信息");
                    } else {
                        System.out.println("【" + deviceId + "】没有符合条件的心跳信息");
                    }
                }

                //添加手动分页
                List newInfo = new ArrayList();
                if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
                    int pageIndex = Integer.parseInt(page.toString());
                    int pageSize = Integer.parseInt(rows.toString());

                    for (int i = ((pageIndex - 1) * pageSize); i < (pageSize * pageIndex); i++) {
                        if (i == deviceHeartbeatListResult.size()) {
                            break;
                        }
                        newInfo.add(deviceHeartbeatListResult.get(i));
                    }
                }

                //返回数据
                deviceHeartBeatMapResult.put("averageCpuUserUnilization", averageCpuUserUnilization);
                deviceHeartBeatMapResult.put("averageCpuTemper", averageCpuTemper);
                deviceHeartBeatMapResult.put("deviceHeartbeatListResult", newInfo);

                Map map = PageUtils.doSplitPageOther(deviceHeartbeatListResult, null, page, rows, null);
                returnData.setData(deviceHeartBeatMapResult);
                returnData.setMessage("数据请求成功");
                returnData.setReturnCode("3000");
                returnData.setTotalPages((String) map.get("totalPages"));
                returnData.setPagecountNum((String) map.get("pagecountNum"));
                return returnData;
            } else {
                returnData.setMessage("当前没有已添加的设备");
                returnData.setReturnCode("4007");
                return returnData;
            }
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 下发设备系统设置
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping("/handOutDeviceSetting")
    public ReturnData handOutDeviceSetting(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "deviceIdList": [
         "0f1a21d4e6fd3cb8",
         "0f1a21d4e6fd3cb8"
         ],
//         "heartbeatPeriod": "60",
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
         "enableSelfHelpBioRegister": "off",
         "enableSelfHelpCardRegister": "off",
         "enableTimedReboot": "off",
         "timedRebootPeriod": "2",
         "timedRebootTime": "10:06",
         "downloadTimeSys":"2017-11-13 17:03",
         "updateTimeSys":"2017-11-13 03:00",
         "downloadTimeApp":"2017-11-13 17:03",
         "updateTimeApp":""
         }
         */

        System.out.println(jsonString);

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
            System.out.println("必传参数字段为null");
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
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceIdList.size() == 0) {
            System.out.println("没有选择设备");
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
                doorCmdBindDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
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
                //立即下发数据到MQ
                rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
            }

            //保存并下发系统更新设置
            deviceService.updateDeviceSystem(deviceIdList, downloadTimeSys, updateTimeSys);

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

    /**
     * 查询设备系统设置
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/getDeviceSetting", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData getDeviceSetting(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8",
         "activeStatus": "0",
         "companyName": "无敌的公司",
         "page":"1",
         "rows":"3"
         }
         */

        System.out.println(jsonString);

        //解析JSON数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        String deviceId = "";
        String activeStatus = "";
        String companyName = "";
        String page = "";
        String rows = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            deviceId = mapJson.get("deviceId");
            activeStatus = mapJson.get("activeStatus");
            companyName = mapJson.get("companyName");
            page = mapJson.get("page");
            rows = mapJson.get("rows");
        } catch (Exception e) {

            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null
                || activeStatus == null
                || companyName == null
                || page == null
                || rows == null) {
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            System.out.println("page = " + Integer.valueOf(page) + "\n" + "rows = " + Integer.valueOf(rows));
            Page pageHelperResult = PageHelper.startPage(Integer.valueOf(page), Integer.valueOf(rows));
            List<Map<String, Object>> deviceSettingList = deviceSettingMapper.selectDeviceSettingByCondition(deviceId, activeStatus, companyName);

            if (deviceSettingList.size() > 0) {
                for (Map<String, Object> deviceSettingMap : deviceSettingList) {
                    String deviceIdTemp = (String) deviceSettingMap.get("deviceId");

                    //查询系统升级下载时间
                    DeviceSettingUpdate deviceSettingUpdate = deviceSettingUpdateMapper.selectByPrimaryKey(deviceIdTemp);

                    deviceSettingMap.put("downloadTimeSys", deviceSettingUpdate.getDownloadTimeSys());
                    deviceSettingMap.put("updateTimeSys", deviceSettingUpdate.getUpdateTimeSys());
                    deviceSettingMap.put("downloadTimeApp", deviceSettingUpdate.getDownloadTimeApp());
                    deviceSettingMap.put("updateTimeApp", deviceSettingUpdate.getUpdateTimeApp());

                    System.out.println(JSON.toJSONString(deviceSettingUpdate));

                    //查询屏幕亮度数据
                    List<TimeRangeLcdBrightness> timeRangeLcdBrightnessList = timeRangeLcdBrightnessMapper.selectByPrimaryKey(deviceIdTemp);

                    //查询息屏时间
                    List<TimeRangeLcdOff> timeRangeLcdOffList = timeRangeLcdOffMapper.selectByPrimaryKey(deviceIdTemp);

                    deviceSettingMap.put("lcdBrightnessList", timeRangeLcdBrightnessList);
                    deviceSettingMap.put("lcdOffTimeList", timeRangeLcdOffList);
                    System.out.println("deviceId: " + deviceIdTemp + "\n" + "lcdBrightnessList: " + JSON.toJSONString(timeRangeLcdBrightnessList));

                    //数据展示处理
                    if ("0".equals(deviceSettingMap.get("enableSelfHelpBioRegister"))) {
                        deviceSettingMap.put("enableSelfHelpBioRegister", "不允许");
                    } else if ("1".equals(deviceSettingMap.get("enableSelfHelpBioRegister"))) {
                        deviceSettingMap.put("enableSelfHelpBioRegister", "允许");
                    }

                    if ("0".equals(deviceSettingMap.get("enableSelfHelpCardRegister"))) {
                        deviceSettingMap.put("enableSelfHelpCardRegister", "不允许");
                    } else if ("1".equals(deviceSettingMap.get("enableSelfHelpCardRegister"))) {
                        deviceSettingMap.put("enableSelfHelpCardRegister", "允许");
                    }

                    if ("0".equals(deviceSettingMap.get("enableTimedReboot"))) {
                        deviceSettingMap.put("enableTimedReboot", "不允许");
                    } else if ("1".equals(deviceSettingMap.get("enableTimedReboot"))) {
                        deviceSettingMap.put("enableTimedReboot", "允许");
                    }
                }
            }

            System.out.println("总的数据: " + JSON.toJSONString(deviceSettingList));
            Map map = PageUtils.doSplitPageOther(null, null, page, rows, pageHelperResult);
            returnData.setData(deviceSettingList);
            returnData.setMessage("数据请求成功");
            returnData.setReturnCode("3000");
            returnData.setPagecountNum((String) map.get("pagecountNum"));
            returnData.setTotalPages((String) map.get("totalPages"));
            return returnData;
        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 设备主动请求获取app升级包的路径版本等信息
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/getDeviceAppUpdate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> getDeviceAppUpdate(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "serverId": "001",
         "deviceId": "0f1a21d4e6fd3cb8-1-2-6277",
         "fileEdition": "v1.3",
         "commandMode": "C",
         "commandType": "single",
         "commandTotal": "1",
         "commandIndex": "1",
         "sendTime": "2017-10-31 19:12:25",
         "outOfTime": "2017-11-03 19:12",
         "MD5Check": "24CF5FCDF3CC6C63D55335E8C8396ED3",
         "command": {
         "superCMDID": "49641B5A57474BE2B5E4BE126AC63C49",
         "subCMDID": "",
         "ACTION": "GET_DEVICE_APP_UPDATE",
         "ACTIONCode": "1010"
         },
         "data": {
         "update": {
         "appName": "123.txt"
         }
         }
         }
         */

        System.out.println("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("updateApp=", "");
        System.out.println("转换后的数据：" + jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")) {
            System.out.println("收到空的app升级请求数据");
            return new HashMap<>();
        } else {
            //解析JSON数据
            Map<String, Object> mapJson = (Map<String, Object>) net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
            Map<String, String> heartbeatMap = new HashMap<>();

            //回复设备
            Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
            Map<String, Object> resultData = new LinkedHashMap<String, Object>();
            String resultCode = "";
            String resultMessage = "";
            String deviceId = (String) mapJson.get("deviceId");
            DeviceUpdatePackApp deviceUpdatePackAppResult = new DeviceUpdatePackApp();

            //校验MD5
            //获取对方的md5
            String otherMd5 = (String) mapJson.get("MD5Check");
            mapJson.remove("MD5Check");
            String messageCheck = JSON.toJSONString(mapJson);
            //生成我的md5
            String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
            System.out.println("myMD5 = " + myMd5);
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)) {

                System.out.println("MD5校验成功，数据完好无损");

                //CRC16校验deviceId
                if (deviceService.checkCrc16DeviceId(deviceId)) {

                    if (((Map<String, String>) mapJson.get("command")).get("ACTION").equals("GET_DEVICE_APP_UPDATE")) {
                        //获取app名称

                        String appName = ((Map<String, Map<String, String>>) mapJson.get("data")).get("update").get("appName");
                        System.out.println("appName: " + appName);
                        if (StringUtils.isNotEmpty(appName)) {
                            deviceUpdatePackAppResult = deviceUpdatePackAppMapper.selectByPrimaryKey(appName);
                        }
                    }

                    //回复设备
                    if (deviceUpdatePackAppResult == null) {
                        resultCode = "999";
                        resultMessage = "没有找到对应的app名称的升级包信息";
                        resultData.put("returnObj", "");
                        resultData.put("resultCode", resultCode);
                        resultData.put("resultMessage", resultMessage);
                    } else {
                        resultCode = "0";
                        resultMessage = "执行成功";
                        resultData.put("returnObj", deviceUpdatePackAppResult);
                        resultData.put("resultCode", resultCode);
                        resultData.put("resultMessage", resultMessage);
                    }
                }

            } else {
                System.out.println("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("returnObj", "");
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
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
            doorCmdRecord.setAction("GET_DEVICE_APP_UPDATE");
            doorCmdRecord.setActionCode("1010");
            doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultData));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll = rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
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
            System.out.println("app升级包请求返回的数据： " + JSON.toJSONString(doorRecordAll));

            return doorRecordAll;
        }
    }

    /**
     * 设备主动请求获取蓝牙信息参数
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/getBluetoothParameterList", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> getBluetoothParameterList(@RequestBody String jsonString) {

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
         "MD5Check": "67D5EAF1E2A1835911DC004FA05BDF3A",
         "command": {
         "superCMDID": "49641B5A57474BE2B5E4BE126AC63C49",
         "subCMDID": "",
         "ACTION": "GET_BLUETOOTH_PARAMETER_LIST",
         "ACTIONCode": "1011"
         },
         "data": {
         }
         }
         */

        System.out.println("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("getIbeaconUuid=", "");
        System.out.println(jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")) {
            System.out.println("收到空的蓝牙参数请求数据");
            return new HashMap<>();
        } else {
            //解析JSON数据
            Map<String, Object> mapJson = (Map<String, Object>) net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
            Map<String, String> heartbeatMap = new HashMap<>();

            //回复设备
            Map<String, Object> resultData = new LinkedHashMap<String, Object>();
            String resultCode = "";
            String resultMessage = "";
            String deviceId = (String) mapJson.get("deviceId");

            //校验MD5
            //获取对方的md5
            String otherMd5 = (String) mapJson.get("MD5Check");
            mapJson.remove("MD5Check");
            String messageCheck = JSON.toJSONString(mapJson);
            //生成我的md5
            String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
            System.out.println("myMD5 = " + myMd5);
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)) {

                System.out.println("MD5校验成功，数据完好无损");

//                //CRC16校验deviceId
//                if (deviceService.checkCrc16DeviceId(deviceId)){

                if (((Map<String, String>) mapJson.get("command")).get("ACTION").equals("GET_BLUETOOTH_PARAMETER_LIST")) {

                    if (StringUtils.isNotEmpty(deviceId)) {
//                            System.out.println("deviceId: "+deviceId);
                        List<Map<String, String>> deviceIdList = deviceMapper.selectAllDeviceIdOfCompanyByDeviceId(deviceId);

                        if (deviceIdList.size() > 0) {
                            System.out.println("deviceIdList: " + JSON.toJSONString(deviceIdList));

                            //返回的蓝牙参数集合
                            List<Map<String, String>> bluetoothParameterList = new ArrayList<Map<String, String>>();

                            for (Map<String, String> deviceIdTemp : deviceIdList) {
                                List<Map<String, String>> versionInfoList = deviceMapper.selectAllVersionInfoByDeviceId(deviceIdTemp.get("device_id"));

                                //判断该设备有没有上传蓝牙参数
                                if (versionInfoList.size() > 0) {
                                    System.out.println("versionInfoList: " + JSON.toJSONString(versionInfoList));
                                    Map<String, String> mapTemp = new HashMap<String, String>();
                                    for (Map<String, String> versionInfoMap : versionInfoList) {

//                                            System.out.println("versionInfoMap: "+JSON.toJSONString(versionInfoMap));

                                        String name = versionInfoMap.get("name");
                                        if ("major".equals(name)) {
                                            try {
                                                String majorValue = versionInfoMap.get("value");
                                                if (majorValue == null || "".equals(majorValue)) {
                                                    System.out.println("major值null或者空字符串");
                                                } else {
                                                    mapTemp.put("major", versionInfoMap.get("value"));
                                                }

                                            } catch (Exception e) {
                                                System.out.println("取major报错");
                                            }
                                        } else if ("minor".equals(name)) {
                                            try {
                                                String minorValue = versionInfoMap.get("value");
                                                if (minorValue == null || "".equals(minorValue)) {
                                                    System.out.println("minor值null或者空字符串");
                                                } else {
                                                    mapTemp.put("minor", versionInfoMap.get("value"));
                                                }

                                            } catch (Exception e) {
                                                System.out.println("取minor报错");
                                            }
                                        } else if ("ibeaconUuid".equals(name)) {
                                            try {
                                                String ibeaconUuidValue = versionInfoMap.get("value");
                                                if (ibeaconUuidValue == null || "".equals(ibeaconUuidValue)) {
                                                    System.out.println("ibeaconUuid值null或者空字符串");
                                                } else {
                                                    mapTemp.put("ibeaconUuid", versionInfoMap.get("value"));
                                                }

                                            } catch (Exception e) {
                                                System.out.println("取ibeaconUuid报错");
                                            }
                                        }

                                    }

                                    System.out.println("mapTemp = " + JSON.toJSONString(mapTemp));

                                    //判断mapTemp空
                                    if (mapTemp.isEmpty()) {
                                        System.out.println("mapTemp空");
                                    } else {
                                        System.out.println("mapTemp非空");
                                        bluetoothParameterList.add(mapTemp);
                                    }
                                } else {
                                    System.out.println("没有查到设备【" + deviceIdTemp.get("device_id") + "】的蓝牙参数数据");
                                }
                            }
                            System.out.println("bluetoothParameterList: " + JSON.toJSONString(bluetoothParameterList));

                            //回复设备
                            if (bluetoothParameterList.size() == 0) {
                                System.out.println("没有查到蓝牙参数信息");
                                resultCode = "999";
                                resultMessage = "没有查到蓝牙参数信息";
                                resultData.put("returnObj", "");
                                resultData.put("resultCode", resultCode);
                                resultData.put("resultMessage", resultMessage);
                            } else {
                                System.out.println("执行成功");
                                resultCode = "0";
                                resultMessage = "执行成功";
                                resultData.put("returnObj", bluetoothParameterList);
                                resultData.put("resultCode", resultCode);
                                resultData.put("resultMessage", resultMessage);
                            }
                        }
                    }
                }
//                }

            } else {
                System.out.println("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("returnObj", "");
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
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
            doorCmdRecord.setAction("GET_BLUETOOTH_PARAMETER_LIST");
            doorCmdRecord.setActionCode("1011");
            doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultData));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll = rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
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
            System.out.println("设备请求的蓝牙参数返回信息：" + JSON.toJSONString(doorRecordAll));

            return doorRecordAll;
        }
    }

    /**
     * 给app返回蓝牙所需的参数
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getBluetoothParameterListForApp", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData getBluetoothParameterListForApp(@Param("employeeId") String employeeId) {

        //返回参数给app
        ReturnData returnData = new ReturnData();

        try {
            if (StringUtils.isNotEmpty(employeeId)) {
                //根据人员id查询公司id
                Employee employee = employeeMapper.selectByPrimaryKey(employeeId);
                if (employee != null) {
                    //查出该公司的所有设备
                    String companyId = employee.getEmployeeCompanyId();
                    List<Map> deviceList = deviceMapper.selectAllDeviceInfo(companyId);

                    //判断该公司是否有设备
                    if (deviceList.size() > 0) {
                        //返回的蓝牙参数集合
                        List<Map<String, String>> bluetoothParameterList = new ArrayList<Map<String, String>>();

                        //设备和门没有绑定的数量
                        int deviceDoorBindCount = 0;

                        //存放蓝牙id
                        String bluetoothIdResult = "";

                        System.out.println("该公司已绑定的所有设备: "+JSON.toJSONString(deviceList));

                        for (Map<String, String> deviceMap : deviceList) {
                            String deviceId = deviceMap.get("deviceId");

                            //查看设备有没有绑定门
                            Door doorExist = doorMapper.findAllByDeviceId(deviceId);
                            if (doorExist != null) {
                                //有绑定的设备和门，则计数加1
                                deviceDoorBindCount = deviceDoorBindCount + 1;
                                List<Map<String, String>> versionInfoList = deviceMapper.selectAllVersionInfoByDeviceId(deviceId);

                                //判断该设备有没有上传蓝牙参数
                                if (versionInfoList.size() > 0) {
                                    System.out.println("versionInfoList: " + JSON.toJSONString(versionInfoList));
                                    Map<String, String> mapTemp = new HashMap<String, String>();
                                    String major = "";
                                    String minor = "";
                                    for (Map<String, String> versionInfoMap : versionInfoList) {

                                        System.out.println("versionInfoMap: " + JSON.toJSONString(versionInfoMap));

                                        String name = versionInfoMap.get("name");
                                        if ("major".equals(name)) {
                                            major = versionInfoMap.get("value");
                                        } else if ("minor".equals(name)) {
                                            minor = versionInfoMap.get("value");
                                        } else if ("ibeaconUuid".equals(name)) {
                                            mapTemp.put("uuidIbeacon", versionInfoMap.get("value"));
                                        } else if ("characUuid".equals(name)) {
                                            mapTemp.put("characUuid", versionInfoMap.get("value"));
                                        } else if ("serviceUuid".equals(name)) {
                                            mapTemp.put("serviceUuid", versionInfoMap.get("value"));
                                        }
                                    }
                                    if (StringUtils.isNotEmpty(major) && StringUtils.isNotEmpty(minor)) {
                                        mapTemp.put("deviceSeries", major + "-" + minor);
                                    } else {
                                        mapTemp.put("deviceSeries", "");
                                    }

                                    System.out.println("deviceId = " + deviceId);
                                    //获取门名称
                                    Door door = doorMapper.findAllByDeviceId(deviceId);
                                    mapTemp.put("deviceId", deviceId);
                                    mapTemp.put("deviceName", door.getDoorName());
                                    mapTemp.put("moduleMac", deviceMap.get("macAddress"));

                                    //判断该人员有没有蓝牙id
                                    Employee employeeExist = employeeMapper.selectByPrimaryKey(employeeId);
                                    if (StringUtils.isEmpty(employeeExist.getBluetoothNo())){
                                        //人员没有蓝牙id，分配唯一的蓝牙id
                                        EmployeeBluetoothCount employeeBluetoothCountExist = employeeBluetoothCountMapper.selectByPrimaryKey("1");
                                        if (employeeBluetoothCountExist == null){
                                            //第一次空表的时候初始化蓝牙id总和
                                            EmployeeBluetoothCount employeeBluetoothCountTemp = new EmployeeBluetoothCount();
                                            employeeBluetoothCountTemp.setId("1");
                                            employeeBluetoothCountTemp.setBluetoothCount("1");
                                            employeeBluetoothCountMapper.insertSelective(employeeBluetoothCountTemp);
                                            //存入人员表里的蓝牙id
                                            Employee employeeTemp = new Employee();
                                            employeeTemp.setEmployeeId(employeeId);
                                            employeeTemp.setBluetoothNo("1");
                                            employeeMapper.updateByPrimaryKeySelective(employeeTemp);
                                            //把第一次生成的蓝牙id返给app端
                                            bluetoothIdResult = "1";
                                        }else {
                                            //蓝牙id总和表有数据时更新
                                            int bluetoothCount = Integer.parseInt(employeeBluetoothCountExist.getBluetoothCount());
                                            bluetoothCount = bluetoothCount + 1;
                                            //存入人员表里的蓝牙id
                                            Employee employeeTemp = new Employee();
                                            employeeTemp.setEmployeeId(employeeId);
                                            employeeTemp.setBluetoothNo(String.valueOf(bluetoothCount));
                                            employeeMapper.updateByPrimaryKeySelective(employeeTemp);
                                            //更新蓝牙id计数总和
                                            EmployeeBluetoothCount employeeBluetoothCount = new EmployeeBluetoothCount();
                                            employeeBluetoothCount.setId("1");
                                            employeeBluetoothCount.setBluetoothCount(String.valueOf(bluetoothCount));
                                            employeeBluetoothCountMapper.updateByPrimaryKey(employeeBluetoothCount);
                                            //把生成的新的蓝牙id，返回给app端
                                            bluetoothIdResult = String.valueOf(bluetoothCount);
                                        }
                                    }else {
                                        //人员有蓝牙id，直接返给app端
                                        bluetoothIdResult = employeeExist.getBluetoothNo();
                                    }

                                    bluetoothParameterList.add(mapTemp);
                                } else {
                                    System.out.println("没有查到设备【" + deviceId + "】的蓝牙参数数据");
                                }
                            }else {
                                System.out.println("设备【"+deviceId+"】没有绑定门");
                            }
                        }

                        if (deviceDoorBindCount == 0){
                            returnData.setMessage("您的公司已有的所有设备都没有和门进行绑定");
                            returnData.setReturnCode("4007");
                            return returnData;
                        }

                        System.out.println("bluetoothParameterList: " + JSON.toJSONString(bluetoothParameterList));

                        returnData.setData(bluetoothParameterList);
                        returnData.setMessage("数据请求成功");
                        returnData.setReturnCode("3000");
                        returnData.setBluetoothId(bluetoothIdResult);
                        return returnData;
                    } else {
                        returnData.setMessage("您的公司暂无已绑定的设备");
                        returnData.setReturnCode("4007");
                        return returnData;
                    }

                } else {
                    returnData.setMessage("没有查到当前登录人员的信息");
                    returnData.setReturnCode("4007");
                    return returnData;
                }
            } else {
                returnData.setMessage("必传字段不存在或为空字符串，请登录后再操作");
                returnData.setReturnCode("4007");
                return returnData;
            }

        } catch (Exception e) {
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }
}
