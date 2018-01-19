package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.command.CmdUtil;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IDeviceService;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * 控制层：设备操作
 */

@Controller
@RequestMapping(value = "/device")
public class DeviceController {

    private static final Logger LOGGER = Logger.getLogger(DeviceController.class);

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Value("${serverId}")
    String serverId;

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
    private DoorEmployeePermissionMapper doorEmployeePermissionMapper;

    @Autowired
    private CmdUtil cmdUtil;

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private DeviceOnlineMapper deviceOnlineMapper;

    @Autowired
    private DeviceStatusCheckUtil deviceStatusCheckUtil;

    /**
     * 平台新增设备，未绑定公司的设备
     *
     * @param jsonString
     */
    @ResponseBody
    @RequestMapping(value = "/addDevice", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData addDevice(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "role":"superAdmin",
         "deviceId": "0f1a21d4e6fd3cb8"
         }
         */

//        LOGGER.info(jsonString);

        //解析JSON数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        String role = "";
        String deviceId = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            role = mapJson.get("role");
            deviceId = mapJson.get("deviceId");
        } catch (Exception e) {

            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (role == null || deviceId == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        //CRC16校验设备编码的合法性
        if (!deviceService.checkCrc16DeviceId(deviceId)){
            returnData.setMessage("设备编码校验失败，请检查设备编码是否输入错误");
            returnData.setReturnCode("4206");
            return returnData;
        }

        //角色权限控制的判断，仅此一处有，目前通过前端判断role角色来隐藏入口控制权限
        if ("superAdmin".equals(role)) {
//            LOGGER.info("已匹配到【超级管理员】角色");
            try {
                String returnCode = deviceService.addDevice(deviceId);

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
//            LOGGER.info("已匹配到【企业管理员】角色");
            returnData.setMessage("您没有操作权限");
            returnData.setReturnCode("3006");
            return returnData;
        } else if ("user".equals(role)) {
//            LOGGER.info("已匹配到【普通用户】角色");
            returnData.setMessage("您没有操作权限");
            returnData.setReturnCode("3006");
            return returnData;
        } else {
//            LOGGER.info("没有匹配的角色信息");
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
    @ResponseBody
    @RequestMapping(value = "/findDeviceInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData findDeviceInformation(@RequestBody String jsonString, HttpServletRequest request) {
        /**
         * 测试数据
         {
         "role": "superAdmin",
         "companyName": "华龙国际集团",
         "deviceName": "设备1",
         "deviceId": "0f1a21d4e6fd3cb8",
         "isOnline": "0",
         "activeStatus": "0",
         "page":"1",
         "rows":"3"
         }
         */
//        LOGGER.info(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);
        String role = "";
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
            role = mapJson.get("role");
        } catch (Exception e) {
            LOGGER.error("角色为null");
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

            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (role == null
                || companyName == null
                || deviceName == null
                || deviceId == null
                || isOnline == null
                || activeStatus == null
                || page == null
                || rows == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        String companyId = "";
        //角色权限控制的判断，仅此一处有，目前通过前端判断role角色来隐藏入口控制权限
        if ("superAdmin".equals(role)) {
//            LOGGER.info("已匹配到【超级管理员】角色");

        } else if ("admin".equals(role)) {
//            LOGGER.info("已匹配到【企业管理员】角色");
            companyId = request.getHeader("companyId");
        } else if ("user".equals(role)) {
//            LOGGER.info("已匹配到【普通用户】角色");
            returnData.setMessage("您没有操作权限");
            returnData.setReturnCode("3006");
            return returnData;
        } else {
//            LOGGER.info("没有匹配的角色信息");
            returnData.setMessage("没有匹配的角色信息");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
//            LOGGER.info("page = " + Integer.valueOf(page) + "\n" + "rows = " + Integer.valueOf(rows));
            Page pageHelperResult = PageHelper.startPage(Integer.valueOf(page), Integer.valueOf(rows));
            List<Map<String, String>> mapListResult = deviceService.findDeviceInformation(companyId,
                    companyName, deviceName, deviceId, isOnline, activeStatus, request.getHeader("companyId"));

            if (mapListResult.size() > 0) {
                Map map = PageUtils.doSplitPageOther(null, null, page, rows, pageHelperResult);
                returnData.setData(mapListResult);
                returnData.setMessage("数据请求成功");
                returnData.setReturnCode("3000");
                returnData.setPagecountNum((String) map.get("pagecountNum"));
                returnData.setTotalPages((String) map.get("totalPages"));
                return returnData;
            } else {
                returnData.setData(mapListResult);
                returnData.setMessage("无符合条件的设备");
                returnData.setReturnCode("4202");
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
     * 编辑当前设备的信息（超级管理员）
     *
     * @param jsonString
     * @return
     */
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/editorDeviceInformationSuper", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData editorDeviceInformationSuper(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8",
         "companyId": "FE0FB748F7A04BCAA3E4D736E3964B06",
         "companyName":"无敌的公司",
         "devicePlace": "无敌的乐山新村"
         }
         */

//        LOGGER.info(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String deviceId = "";
        String companyId = "";
        String companyName = "";
        String devicePlace = "";

        try {
            deviceId = mapJson.get("deviceId");
            companyId = mapJson.get("companyId");
            companyName = mapJson.get("companyName");
            devicePlace = mapJson.get("devicePlace");
        } catch (Exception e) {
            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null
                || companyId == null
                || companyName == null
                || devicePlace == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if ("".equals(deviceId)) {
            LOGGER.info("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            Device device = new Device();

            device.setDeviceId(deviceId);
            device.setCompanyId(companyId);
            if ("请选择".equals(companyName)){
                device.setCompanyName("");
            }else {
                device.setCompanyName(companyName);
            }
            device.setDevicePlace(devicePlace);
            //更新设备激活状态
            device.setActiveStatus("1");//待完善状态
            //更新设备绑定状态
            device.setIsUnbind("0");

            //更新设备信息
            deviceMapper.updateByPrimaryKeySelective(device);

            //下发绑定信息到设备
            deviceService.bindDevice(deviceId, companyId, companyName);

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
     * 编辑当前设备的信息（企业管理员）
     *
     * @param jsonString
     * @return
     */
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/editorDeviceInformationAdmin", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData editorDeviceInformationAdmin(@RequestBody String jsonString) {

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8",
         "deviceName": "无敌的设备",
         "deviceUsages": "无敌的考勤"
         }
         */

//        LOGGER.info(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String deviceId = "";
        String deviceName = "";
        String deviceUsages = "";

        try {
            deviceId = mapJson.get("deviceId");
            deviceName = mapJson.get("deviceName");
            deviceUsages = mapJson.get("deviceUsages");
        } catch (Exception e) {
            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null
                || deviceName == null
                || deviceUsages == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if ("".equals(deviceId)) {
            LOGGER.info("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            Device device = new Device();

            device.setDeviceId(deviceId);
            device.setDeviceName(deviceName);
            device.setDeviceUsages(deviceUsages);

            //更新设备信息
            deviceMapper.updateByPrimaryKeySelective(device);

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

//        LOGGER.info(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        String deviceId = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            deviceId = mapJson.get("deviceId");
        } catch (Exception e) {
            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if ("".equals(deviceId)) {
            LOGGER.info("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
//            //创建命令id
//            String superCmdId = FormatUtil.createUuid();

//            //构造命令格式
//            DoorCmd doorCmdRebootDevice = new DoorCmd();
//            doorCmdRebootDevice.setServerId(serverId);
//            doorCmdRebootDevice.setDeviceId(deviceId);
//            doorCmdRebootDevice.setFileEdition("v1.3");
//            doorCmdRebootDevice.setCommandMode("C");
//            doorCmdRebootDevice.setCommandType("single");
//            doorCmdRebootDevice.setCommandTotal("1");
//            doorCmdRebootDevice.setCommandIndex("1");
//            doorCmdRebootDevice.setSubCmdId("");
//            doorCmdRebootDevice.setAction("REBOOT_DEVICE");
//            doorCmdRebootDevice.setActionCode("1007");
//            doorCmdRebootDevice.setSendTime(CalendarUtil.getCurrentTime());
//            doorCmdRebootDevice.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//            doorCmdRebootDevice.setSuperCmdId(superCmdId);
//            doorCmdRebootDevice.setData("");
//
//            //获取完整的数据加协议封装格式
//            RabbitMQSender rabbitMQSender = new RabbitMQSender();
//            Map<String, Object> doorCmdPackageAll =  CmdUtil.messagePackaging(doorCmdRebootDevice, "", "", "NULLDATA");
//            //命令状态设置为: 发送中
//            doorCmdRebootDevice.setStatus("1");
//            //设置md5校验值
//            doorCmdRebootDevice.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
//            //设置数据库的data字段
//            doorCmdRebootDevice.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
//            //命令数据存入数据库
//            entranceGuardService.insertCommand(doorCmdRebootDevice);
//            //立即下发数据到MQ
//            rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);

            cmdUtil.handOutCmd(deviceId, "NULLDATA", "REBOOT_DEVICE", "1007", "", "",
                    "", "1", "", "", "", "");

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
     * 查询当前公司的所有设备信息（未和公司解绑，并且尚未绑定门的设备）
     */
    @ResponseBody
    @RequestMapping("/getAllDevice")
    public String getAllDeviceInfo(HttpServletRequest request) {
        //获取公司ID
        String companyId = request.getHeader("companyId");
        //返回给前端的数据
        Map resultMap = new HashMap();
        if(companyId!=null && !companyId.isEmpty()){
            List<Map> maps = deviceService.queryAllDeviceInfo(companyId);
            //添加返回码
            resultMap = ReturnCodeUtil.addReturnCode(maps);
            //LOGGER.info(JSONObject.toJSONString(result));
        }else{
            //未知的公司ID和人员ID
            resultMap = ReturnCodeUtil.addReturnCode(3);
        }
        return JSONObject.toJSONString(resultMap);
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

//        LOGGER.info(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);
        String companyId = mapJson.get("companyId");

        return deviceService.queryAllDoorInfoByCompanyId(companyId);
    }

    /**
     * 解绑设备
     */
    @ResponseBody
    @Transactional
    @RequestMapping("/unBindDevice")
    public ReturnData unBindDevice(@RequestBody String jsonString, HttpServletRequest request){

        /**
         * 测试数据
         {
         "deviceId": "0f1a21d4e6fd3cb8"
         }
         */

//        LOGGER.info(jsonString);

        //解析数据
        Map<String, String> mapJson = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonString);
        String deviceId = mapJson.get("deviceId");
//        String doorId = mapJson.get("doorId");

//        //查找设备id
//        String deviceId = doorMapper.findAllByDoorId(doorId).getDeviceId();

        return deviceService.unBindDevice(deviceId, "", "1", request);
    }

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

//        LOGGER.info("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
//        LOGGER.info(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("heartbeatData=", "");
//        LOGGER.info(jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")) {
//            LOGGER.info("收到空的心跳数据");
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
//            LOGGER.info("收到【"+deviceId+"】的心跳数据");

            //校验MD5
            //获取对方的md5
            String otherMd5 = (String) mapJson.get("MD5Check");
            mapJson.remove("MD5Check");
            String messageCheck = JSON.toJSONString(mapJson);
            //生成我的md5
            String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
//        LOGGER.info("MD5 = " + myMd5);
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)) {

//                LOGGER.info("MD5校验成功，数据完好无损");

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
//                        LOGGER.info("转换前的温度："+heartbeatMap.get("cpuTemper"));
                        //保留一位小数点的温度
                        Float floatCpuTemper = Float.parseFloat(heartbeatMap.get("cpuTemper"));
                        String cpuTemper = String.format("%.1f", floatCpuTemper / 1000);
//                        LOGGER.info("转换后的温度："+cpuTemper);
                        deviceHeartbeat.setCpuTemper(cpuTemper);
                        deviceHeartbeat.setCpuUnilization(heartbeatMap.get("cpuUnilization"));
                        deviceHeartbeat.setCpuUserUnilization(heartbeatMap.get("cpuUserUnilization"));
                        deviceHeartbeat.setInternalUnilization(heartbeatMap.get("internalUnilization"));
                        deviceHeartbeat.setAppUsed(JSON.toJSONString(((Map<String, Map<String, Map<String, Object>>>) mapJson.get("data"))
                                .get("heartbeat").get("appUsed")));
                        deviceHeartbeat.setTime(DateUtils.getDateTime());

                        deviceHeartbeatMapper.insertSelective(deviceHeartbeat);
//                        LOGGER.info("心跳数据已存储");

                    }

                    //回复设备
                    resultCode = "0";
                    resultMessage = "执行成功";
                    resultData.put("resultCode", resultCode);
                    resultData.put("resultMessage", resultMessage);
                }

            } else {
                LOGGER.info("MD5 = " + myMd5);
                LOGGER.info("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
            }

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
            doorCmdRecord.setAction("UPLOAD_DEVICE_HEARTBEAT");
            doorCmdRecord.setActionCode("1005");
            doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultData));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll = CmdUtil.messagePackaging(doorCmdRecord, "", resultData, "R");
            //命令状态设置为: 已回复
            doorCmdRecord.setStatus("5");
            doorCmdRecord.setResultCode(resultCode);
            doorCmdRecord.setResultMessage(resultMessage);
            //设置md5校验值
            doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
            //设置数据库的data字段
            doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
//            //命令数据存入数据库
//            entranceGuardService.insertCommand(doorCmdRecord);

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

//        LOGGER.info(jsonString);

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
            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (companyName == null
                || cpuUserUnilizationCondition == null
                || cpuTemperCondition == null
                || page == null
                || rows == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            //设备id去重集合
            Set<String> deviceIdList = new HashSet<String>();
            //返回结果集合
            Map<String, Object> deviceHeartBeatMapResult = new HashMap<String, Object>();
//            List<Map<String, Object>> deviceHeartbeatListResult = new ArrayList<Map<String, Object>>();

            List<Map> deviceList = deviceMapper.selectAllDevice("");
//            LOGGER.info("deviceList: "+JSON.toJSONString(deviceList));
            //遍历设备id
            if (deviceList.size() > 0) {
                for (Map<String, String> map : deviceList) {
                    //去重复的deviceId
                    deviceIdList.add(map.get("deviceId"));
                }

//            LOGGER.info(JSON.toJSONString(deviceIdList));

                //算出CPU两个展示参数各自的平均值
                double CpuUserUnilizationSum = 0;
                float CpuTemperSum = 0;
                int number = 0;

//                //遍历查找每一个设备最新的心跳数据信息
//                for (String deviceId : deviceIdList) {
//                    Map<String, Object> deviceHeartbeat = deviceHeartbeatMapper
//                            .selectLatestByDeviceId(deviceId, "", 0, 0, "", "");
//
//                    if (deviceHeartbeat != null) {
//                        String cpuUserUnilization = ((String) deviceHeartbeat.get("cpu_user_unilization")).replace("%", "");
//                        int cpuUserUnilizationNumber = Integer.valueOf(cpuUserUnilization);
//                        //累加cpu占用率
//                        CpuUserUnilizationSum = CpuUserUnilizationSum + cpuUserUnilizationNumber;
//                        String cpuTemper = (String) deviceHeartbeat.get("cpu_temper");
//                        float cpuTemperNumber = Float.valueOf(cpuTemper);
//                        //累加cpu温度
//                        CpuTemperSum = CpuTemperSum + cpuTemperNumber;
//                        //累加心跳数据有多少条
//                        number = number + 1;
//
////                        LOGGER.info(JSON.toJSONString(deviceHeartbeat));
//                    }
//                }

                //遍历查找每一个设备最新的心跳数据信息
                List<Map<String, Object>> deviceHeartbeatList = deviceHeartbeatMapper
                        .selectLatestByDeviceId("", 0, 0, "", "");
                for (Map<String, Object> deviceHeartbeatMap : deviceHeartbeatList) {

                    if (deviceHeartbeatList != null && deviceHeartbeatList.size() > 0 ) {
                        String cpuUserUnilization = ((String) deviceHeartbeatMap.get("cpu_user_unilization")).replace("%", "");
                        int cpuUserUnilizationNumber = Integer.valueOf(cpuUserUnilization);
                        //累加cpu占用率
                        CpuUserUnilizationSum = CpuUserUnilizationSum + cpuUserUnilizationNumber;
                        String cpuTemper = (String) deviceHeartbeatMap.get("cpu_temper");
                        float cpuTemperNumber = Float.valueOf(cpuTemper);
                        //累加cpu温度
                        CpuTemperSum = CpuTemperSum + cpuTemperNumber;
                        //累加心跳数据有多少条
                        number = number + 1;

//                        LOGGER.info(JSON.toJSONString(deviceHeartbeat));
                    }

                }


                //算出CPU两个展示参数各自的平均值
                String averageCpuUserUnilization = String.valueOf((int) Math.ceil(CpuUserUnilizationSum / number));
                String averageCpuTemper = String.format("%.1f", CpuTemperSum / number);

//                LOGGER.info("总的cpu占用率为：" + CpuUserUnilizationSum);
//                LOGGER.info("总的cpu温度为：" + CpuTemperSum);
//                LOGGER.info("总的心跳条数为：" + number);
//                LOGGER.info("平均cpu占用率为：" + averageCpuUserUnilization);
//                LOGGER.info("平均cpu温度为：" + averageCpuTemper);

//                //遍历查找每一个设备最新的心跳数据信息
//                for (String deviceId : deviceIdList) {
////                    LOGGER.info("page = "+Integer.valueOf(page)+"\n"+"rows = "+Integer.valueOf(rows));
////                    Page pageHelperResult = PageHelper.startPage(Integer.valueOf(page), Integer.valueOf(rows));
//                    Map<String, Object> deviceHeartbeat = deviceHeartbeatMapper
//                            .selectLatestByDeviceId(deviceId, companyName, Float.valueOf(averageCpuUserUnilization), Float.valueOf(averageCpuTemper), cpuUserUnilizationCondition, cpuTemperCondition);
//                    if (deviceHeartbeat != null) {
//                        deviceHeartbeatListResult.add(deviceHeartbeat);
////                        LOGGER.info("【" + deviceId + "】有符合条件的心跳信息");
//                    } else {
////                        LOGGER.info("【" + deviceId + "】没有符合条件的心跳信息");
//                    }
//                }

                //遍历查找每一个设备最新的心跳数据信息
                List<Map<String, Object>> deviceHeartbeatListResult = deviceHeartbeatMapper
                        .selectLatestByDeviceId(companyName, Float.valueOf(averageCpuUserUnilization), Float.valueOf(averageCpuTemper), cpuUserUnilizationCondition, cpuTemperCondition);


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
    @Transactional
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

//        LOGGER.info(jsonString);

        return deviceService.handOutDeviceSetting(jsonString);
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

//        LOGGER.info(jsonString);

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

            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (deviceId == null
                || activeStatus == null
                || companyName == null
                || page == null
                || rows == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
//            LOGGER.info("page = " + Integer.valueOf(page) + "\n" + "rows = " + Integer.valueOf(rows));
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

//                    LOGGER.info(JSON.toJSONString(deviceSettingUpdate));

                    //查询屏幕亮度数据
                    List<TimeRangeLcdBrightness> timeRangeLcdBrightnessList = timeRangeLcdBrightnessMapper.selectByPrimaryKey(deviceIdTemp);

                    //查询息屏时间
                    List<TimeRangeLcdOff> timeRangeLcdOffList = timeRangeLcdOffMapper.selectByPrimaryKey(deviceIdTemp);

                    if (timeRangeLcdBrightnessList.size() == 0){
                        TimeRangeLcdBrightness timeRangeLcdBrightness = new TimeRangeLcdBrightness();
                        timeRangeLcdBrightnessList.add(timeRangeLcdBrightness);
                        deviceSettingMap.put("lcdBrightnessList", timeRangeLcdBrightnessList);
                    }else {
                        deviceSettingMap.put("lcdBrightnessList", timeRangeLcdBrightnessList);
                    }

                    if (timeRangeLcdOffList.size() == 0){
                        TimeRangeLcdOff timeRangeLcdOff = new TimeRangeLcdOff();
                        timeRangeLcdOffList.add(timeRangeLcdOff);
                        deviceSettingMap.put("lcdOffTimeList", timeRangeLcdOffList);
                    }else {
                        deviceSettingMap.put("lcdOffTimeList", timeRangeLcdOffList);
                    }

//                    LOGGER.info("deviceId: " + deviceIdTemp + "\n" + "lcdBrightnessList: " + JSON.toJSONString(timeRangeLcdBrightnessList));

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

//            LOGGER.info("总的数据: " + JSON.toJSONString(deviceSettingList));
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

//        LOGGER.info("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
//        LOGGER.info(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("updateApp=", "");
//        LOGGER.info("转换后的数据：" + jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")) {
            LOGGER.info("收到空的app升级请求数据");
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
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)) {

//                LOGGER.info("MD5校验成功，数据完好无损");

                //CRC16校验deviceId
                if (deviceService.checkCrc16DeviceId(deviceId)) {

                    if (((Map<String, String>) mapJson.get("command")).get("ACTION").equals("GET_DEVICE_APP_UPDATE")) {
                        //获取app名称

                        String appName = ((Map<String, Map<String, String>>) mapJson.get("data")).get("update").get("appName");
//                        LOGGER.info("appName: " + appName);
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
                LOGGER.info("myMD5 = " + myMd5);
                LOGGER.info("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("returnObj", "");
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
            }

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
            doorCmdRecord.setAction("GET_DEVICE_APP_UPDATE");
            doorCmdRecord.setActionCode("1010");
            doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultData));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll = CmdUtil.messagePackaging(doorCmdRecord, "", resultData, "R");
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
//            LOGGER.info("app升级包请求返回的数据： " + JSON.toJSONString(doorRecordAll));

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

//        LOGGER.info("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
//        LOGGER.info(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("getIbeaconUuid=", "");
//        LOGGER.info(jsonUrlDecoderString);

        if (jsonUrlDecoderString.equals(null) || jsonUrlDecoderString.equals("")) {
            LOGGER.info("收到空的蓝牙参数请求数据");
            return new HashMap<>();
        } else {
            //解析JSON数据
            Map<String, Object> mapJson = (Map<String, Object>) net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
            Map<String, String> heartbeatMap = new HashMap<>();

            //回复设备
            Map<String, Object> resultData = new LinkedHashMap<String, Object>();
            String resultCode = "";
            String resultMessage = "";
            resultData.put("returnObj", "");
            resultData.put("resultCode", "999");
            resultData.put("resultMessage", "没有查到蓝牙参数信息");
            String deviceId = (String) mapJson.get("deviceId");

            //校验MD5
            //获取对方的md5
            String otherMd5 = (String) mapJson.get("MD5Check");
            mapJson.remove("MD5Check");
            String messageCheck = JSON.toJSONString(mapJson);
            //生成我的md5
            String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
            //双方的md5比较判断
            if (myMd5.equals(otherMd5)) {

//                LOGGER.info("MD5校验成功，数据完好无损");

//                //CRC16校验deviceId
//                if (deviceService.checkCrc16DeviceId(deviceId)){

                if (((Map<String, String>) mapJson.get("command")).get("ACTION").equals("GET_BLUETOOTH_PARAMETER_LIST")) {

                    if (StringUtils.isNotEmpty(deviceId)) {
//                            LOGGER.info("deviceId: "+deviceId);
                        List<Map<String, String>> deviceIdList = deviceMapper.selectAllDeviceIdOfCompanyByDeviceId(deviceId);

                        if (deviceIdList.size() > 0) {
//                            LOGGER.info("deviceIdList: " + JSON.toJSONString(deviceIdList));

                            //返回的蓝牙参数集合
                            List<Map<String, String>> bluetoothParameterList = new ArrayList<Map<String, String>>();

                            for (Map<String, String> deviceIdTemp : deviceIdList) {
                                List<Map<String, String>> versionInfoList = deviceMapper.selectAllVersionInfoByDeviceId(deviceIdTemp.get("device_id"));

                                //判断该设备有没有上传蓝牙参数
                                if (versionInfoList.size() > 0) {
//                                    LOGGER.info("versionInfoList: " + JSON.toJSONString(versionInfoList));
                                    Map<String, String> mapTemp = new HashMap<String, String>();
                                    for (Map<String, String> versionInfoMap : versionInfoList) {

//                                            LOGGER.info("versionInfoMap: "+JSON.toJSONString(versionInfoMap));

                                        String name = versionInfoMap.get("name");
                                        if ("major".equals(name)) {
                                            try {
                                                String majorValue = versionInfoMap.get("value");
                                                if (majorValue == null || "".equals(majorValue)) {
//                                                    LOGGER.info("major值null或者空字符串");
                                                } else {
                                                    mapTemp.put("major", versionInfoMap.get("value"));
                                                }

                                            } catch (Exception e) {
                                                LOGGER.error("取major报错");
                                            }
                                        } else if ("minor".equals(name)) {
                                            try {
                                                String minorValue = versionInfoMap.get("value");
                                                if (minorValue == null || "".equals(minorValue)) {
//                                                    LOGGER.info("minor值null或者空字符串");
                                                } else {
                                                    mapTemp.put("minor", versionInfoMap.get("value"));
                                                }

                                            } catch (Exception e) {
                                                LOGGER.error("取minor报错");
                                            }
                                        } else if ("ibeaconUuid".equals(name)) {
                                            try {
                                                String ibeaconUuidValue = versionInfoMap.get("value");
                                                if (ibeaconUuidValue == null || "".equals(ibeaconUuidValue)) {
//                                                    LOGGER.info("ibeaconUuid值null或者空字符串");
                                                } else {
                                                    mapTemp.put("ibeaconUuid", versionInfoMap.get("value"));
                                                }

                                            } catch (Exception e) {
                                                LOGGER.error("取ibeaconUuid报错");
                                            }
                                        }

                                    }

//                                    LOGGER.info("mapTemp = " + JSON.toJSONString(mapTemp));

                                    //判断mapTemp空
                                    if (mapTemp.isEmpty()) {
//                                        LOGGER.info("mapTemp空");
                                    } else {
//                                        LOGGER.info("mapTemp非空");
                                        bluetoothParameterList.add(mapTemp);
                                    }
                                } else {
//                                    LOGGER.info("没有查到设备【" + deviceIdTemp.get("device_id") + "】的蓝牙参数数据");
                                }
                            }
//                            LOGGER.info("bluetoothParameterList: " + JSON.toJSONString(bluetoothParameterList));

                            //回复设备
                            if (bluetoothParameterList.size() == 0) {
//                                LOGGER.info("没有查到蓝牙参数信息");
                                resultCode = "999";
                                resultMessage = "没有查到蓝牙参数信息";
                                resultData.put("returnObj", "");
                                resultData.put("resultCode", resultCode);
                                resultData.put("resultMessage", resultMessage);
                            } else {
//                                LOGGER.info("执行成功");
                                resultCode = "0";
                                resultMessage = "执行成功";
                                resultData.put("returnObj", bluetoothParameterList);
                                resultData.put("resultCode", resultCode);
                                resultData.put("resultMessage", resultMessage);
                            }
                        }else {
//                            LOGGER.info("设备未绑定公司，没有查到蓝牙参数信息");
                            resultCode = "999";
                            resultMessage = "设备未绑定公司，没有查到蓝牙参数信息";
                            resultData.put("returnObj", "");
                            resultData.put("resultCode", resultCode);
                            resultData.put("resultMessage", resultMessage);
                        }
                    }
                }
//                }

            } else {
                LOGGER.info("myMD5 = " + myMd5);
                LOGGER.info("MD5校验失败，数据已被修改");

                //回复设备
                resultCode = "6";
                resultMessage = "MD5校验失败";
                resultData.put("returnObj", "");
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
            }

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
            doorCmdRecord.setAction("GET_BLUETOOTH_PARAMETER_LIST");
            doorCmdRecord.setActionCode("1011");
            doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
            doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
            doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
            doorCmdRecord.setData(JSON.toJSONString(resultData));

            //获取完整的数据加协议封装格式
            RabbitMQSender rabbitMQSender = new RabbitMQSender();
            Map<String, Object> doorRecordAll = CmdUtil.messagePackaging(doorCmdRecord, "", resultData, "R");
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
//            LOGGER.info("设备请求的蓝牙参数返回信息：" + JSON.toJSONString(doorRecordAll));

            return doorRecordAll;
        }
    }

    /**
     * 给app返回蓝牙所需的参数
     *
     * @return
     */
    @Transactional
    @ResponseBody
    @RequestMapping(value = "/getBluetoothParameterListForApp", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData getBluetoothParameterListForApp(@RequestBody String jsonString, HttpServletRequest request) {

        /**
         * 测试数据
         {
         "employeeId":"40577C65D50D468E96D82648A525FBB8"
         }
         */

//        LOGGER.info("------------" + jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
//        LOGGER.info(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("employeeId=", "");
//        LOGGER.info("*************"+jsonUrlDecoderString);

        //提取数据
        Map<String, String> appJsonMap = (Map<String, String>)net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
        String employeeId = appJsonMap.get("employeeId");
//        String companyId = request.getHeader("companyId");
//        LOGGER.info("===="+employeeId);

        //返回参数给app
        ReturnData returnData = new ReturnData();

        try {
            if (StringUtils.isNotEmpty(employeeId)) {
                //获取公司id
                String companyId = request.getHeader("companyId");
                LOGGER.info("***********************************************************************公司的id是："+companyId);
                if (StringUtils.isNotEmpty(companyId)) {
                    //查出当前登录人在哪些设备有开门权限
                    List<Map> deviceList = doorEmployeeMapper.selectDeviceIdOfPermissionEffectiveByEmployeeId(employeeId, companyId);

                    //判断当前登录人是否有拥有开门权限的设备
                    if (deviceList.size() > 0) {
                        System.out.println("deviceList : "+JSON.toJSONString(deviceList));
                        //返回的蓝牙参数集合
                        List<Map<String, String>> bluetoothParameterList = new ArrayList<Map<String, String>>();

                        //设备和门没有绑定的数量
                        int deviceDoorBindCount = 0;

                        //存放蓝牙id
                        String bluetoothIdResult = "";

//                        LOGGER.info("该公司已绑定的所有设备: "+JSON.toJSONString(deviceList));

                        for (Map<String, String> deviceMap : deviceList) {
                            String deviceId = deviceMap.get("deviceId");

                            //查看设备有没有绑定门
                            Door doorExist = doorMapper.findAllByDeviceId(deviceId);
                            if (doorExist != null) {

                                //有绑定的设备和门，则计数加1
                                deviceDoorBindCount = deviceDoorBindCount + 1;

                                //判断当前登录人员在该门的开门权限有效期是否过期，过期则不显示该门
                                Map doorEmployeePermissionMap = doorEmployeePermissionMapper.selectEmployeePressionByLeftJoin(employeeId, doorExist.getDoorId());
                                String doorOpenStartTime = (String) doorEmployeePermissionMap.get("doorOpenStartTime");
                                String doorOpenEndTime = (String) doorEmployeePermissionMap.get("doorOpenEndTime");
                                if (!DateUtils.isBetweenTwoTime(doorOpenStartTime, doorOpenEndTime, DateUtils.getDateTime())){
                                    System.out.println("【"+employeeId+"】在【"+doorExist.getDoorId()+"】上权限无效");
                                    continue;
                                }

                                List<Map<String, String>> versionInfoList = deviceMapper.selectAllVersionInfoByDeviceId(deviceId);

//                                LOGGER.info("versionInfoList: " + JSON.toJSONString(versionInfoList));
                                Map<String, String> mapTemp = new HashMap<String, String>();
                                String major = "";
                                String minor = "";
                                String uuidIbeacon = "";
                                String characUuid = "";
                                String serviceUuid = "";
                                String moduleMac = "";
                                for (Map<String, String> versionInfoMap : versionInfoList) {

//                                    LOGGER.info("versionInfoMap: " + JSON.toJSONString(versionInfoMap));

                                    String name = versionInfoMap.get("name");
                                    if ("major".equals(name)) {
                                        major = versionInfoMap.get("value");
                                    } else if ("minor".equals(name)) {
                                        minor = versionInfoMap.get("value");
                                    } else if ("ibeaconUuid".equals(name)) {
                                        uuidIbeacon = versionInfoMap.get("value");
                                        mapTemp.put("uuidIbeacon", uuidIbeacon);
                                    } else if ("characUuid".equals(name)) {
                                        characUuid = versionInfoMap.get("value");
                                        mapTemp.put("characUuid", characUuid);
                                    } else if ("serviceUuid".equals(name)) {
                                        serviceUuid = versionInfoMap.get("value");
                                        mapTemp.put("serviceUuid", serviceUuid);
                                    } else if ("bleModeMacaddr".equals(name)) {
                                        moduleMac = versionInfoMap.get("value");
                                        mapTemp.put("moduleMac", moduleMac);

                                        //mac地址更新到设备信息表
                                        Device device = deviceMapper.selectByPrimaryKey(deviceId);
                                        if (device != null) {
                                            if (StringUtils.isEmpty(device.getMacAddress())) {
                                                Device deviceTemp = new Device();
                                                deviceTemp.setDeviceId(deviceId);
                                                deviceTemp.setMacAddress(moduleMac);
                                                deviceMapper.updateByPrimaryKeySelective(deviceTemp);
                                            }
                                        }
                                    }
                                }

                                //major,minor组拼
                                if (StringUtils.isNotEmpty(major) && StringUtils.isNotEmpty(minor)) {
                                    mapTemp.put("deviceSeries", major + "-" + minor);
                                } else {
                                    mapTemp.put("deviceSeries", "");
                                }

                                //判空
                                if (StringUtils.isEmpty(uuidIbeacon)) {
                                    mapTemp.put("uuidIbeacon", "");
                                }
                                if (StringUtils.isEmpty(characUuid)) {
                                    mapTemp.put("characUuid", "");
                                }
                                if (StringUtils.isEmpty(serviceUuid)) {
                                    mapTemp.put("serviceUuid", "");
                                }
                                if (StringUtils.isEmpty(moduleMac)) {
                                    mapTemp.put("moduleMac", "");
                                }

//                                LOGGER.info("deviceId = " + deviceId);
                                //获取门名称
                                Door door = doorMapper.findAllByDeviceId(deviceId);
                                mapTemp.put("deviceId", deviceId);
                                mapTemp.put("deviceName", door.getDoorName());

                                //判断该人员有没有蓝牙id
                                Employee employeeExist = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, companyId);
                                if (null == employeeExist){
                                    System.out.println("人员信息不同步，未查到您的信息");
                                    returnData.setMessage("人员信息不同步，未查到您的信息");
                                    returnData.setReturnCode("4007");
                                    return returnData;
                                }

                                if (StringUtils.isEmpty(employeeExist.getBluetoothNo())) {

                                    returnData.setMessage("您还没有分配蓝牙开门参数，请联系管理员下发您的开门权限");
                                    returnData.setReturnCode("4007");
                                    return returnData;
                                } else {
                                    //人员有蓝牙id，直接返给app端
                                    bluetoothIdResult = employeeExist.getBluetoothNo();
                                }

                                bluetoothParameterList.add(mapTemp);
                            }else {
//                                LOGGER.info("设备【"+deviceId+"】没有绑定门");
                            }
                        }

                        if (deviceDoorBindCount == 0){
                            returnData.setMessage("您的公司已有的所有设备都没有和门进行绑定");
                            returnData.setReturnCode("4007");
                            return returnData;
                        }

//                        LOGGER.info("bluetoothParameterList: " + JSON.toJSONString(bluetoothParameterList));

                        returnData.setData(bluetoothParameterList);
                        returnData.setMessage("数据请求成功");
                        returnData.setReturnCode("3000");
                        returnData.setBluetoothId(bluetoothIdResult);
                        return returnData;
                    } else {
                        returnData.setMessage("没有查到拥有进出权限的门，请联系管理员下发您的开门权限");
                        returnData.setReturnCode("4007");
                        return returnData;
                    }

                } else {
                    returnData.setMessage("无法获取当前登录人的公司信息");
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

    /**
     * 获取该公司的设备数量
     * @param jsonString
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getDeviceCount", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData getDeviceCount(@RequestBody String jsonString, HttpServletRequest request) {

        /**
         * 测试数据
         {
         "companyId": "A3789DSYAG7FA7"
         }
         */

//        LOGGER.info(jsonString);

        //提取数据
        Map<String, String> mapJson = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        String companyId = "";

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        try {
            companyId = request.getHeader("companyId");
        } catch (Exception e) {
            LOGGER.error("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (companyId == null) {
            LOGGER.info("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if ("".equals(companyId)) {
            LOGGER.info("必传参数字段为空字符串");
            returnData.setMessage("必传参数字段为空字符串");
            returnData.setReturnCode("3006");
            return returnData;
        }

        try {
            List<Map> deviceList = deviceMapper.selectAllDevice(companyId);

            returnData.setData(String.valueOf(deviceList.size()));
            returnData.setMessage("请求数据成功");
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
