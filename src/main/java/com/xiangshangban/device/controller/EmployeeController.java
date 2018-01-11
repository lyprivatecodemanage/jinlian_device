package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.command.CmdUtil;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.UrlUtil;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**
 * 控制层：用户操作
 */

@Controller
@RequestMapping(value = "/employee")
public class EmployeeController {

    public static final String file = "file";

    @Value("${employee.interface.address}")
    String employeeInterfaceAddress;

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Value("${serverId}")
    String serverId;

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private DoorEmployeePermissionMapper doorEmployeePermissionMapper;

    @Autowired
    private DoorSettingMapper doorSettingMapper;

    @Autowired
    private TimeRangeCommonEmployeeMapper timeRangeCommonEmployeeMapper;

    @Autowired
    private IEntranceGuardService entranceGuardService;

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private EmployeeBluetoothCountMapper employeeBluetoothCountMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private OSSController ossController;

    /**
     * 人员模块人员信息同步（之所以是这个名字，是因为之前打算用协议包成命令记录下来同步的操作日志）
     * @param userInformation
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/commandGenerate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void employeeCommandGenerate(@RequestBody String userInformation){

        System.out.println("[*] userInformation: " + userInformation);

        //JSON字符串解析
        List userInformationList = (List) JSON.parseArray(userInformation);
        List<Map<String, Object>> userInfoCollection = (List<Map<String, Object>>) userInformationList;

        iEmployeeService.employeeCommandGenerate(userInfoCollection);

    }

    /**
     * 下发人员信息及门禁权限
     * @param employeePermission
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/handOutEmployeePermission", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData handOutEmployeePermission(@RequestBody String employeePermission, HttpServletRequest request){

        /**测试数据
         {
         "immediatelyDownload": "1",
         "employeePermission": [
         {
         "doorId": "005",
         "doorName": "小鲤鱼跃龙门",
         "doorOpenStartTime": "2017-10-24",
         "doorOpenEndTime": "2017-12-29",
         "rangeDoorOpenType": "045",
         "employeeList": [
         {
         "employeeId": "9C305EC5587745FF9F0D8198512264D6",
         "employeeName": "赵武"
         }
         ],
         "oneWeekTimeList": [
         {
         "isAllDay": "1",
         "weekType": "1",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "0"
         },
         {
         "isAllDay": "1",
         "weekType": "2",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "1"
         },
         {
         "isAllDay": "1",
         "weekType": "3",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "1"
         },
         {
         "isAllDay": "1",
         "weekType": "4",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "1"
         },
         {
         "isAllDay": "1",
         "weekType": "5",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "1"
         },
         {
         "isAllDay": "1",
         "weekType": "6",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "1"
         },
         {
         "isAllDay": "1",
         "weekType": "7",
         "startTime": "00:00",
         "endTime": "23:59",
         "isDitto": "1"
         }
         ]
         }
         ]
         }
         */

        return iEmployeeService.handOutEmployeePermission(employeePermission, request);
    }

    /**
     * 批量下发存为草稿的人员信息和人员权限信息
     * @param jsonString
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/multipleHandOutEmployeePermission", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void multipleHandOutEmployeePermission(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "doorId":"001"
         }
         */

        Map<String, String> jsonMap = (Map<String, String>)JSONObject.fromObject(jsonString);
        String doorId = jsonMap.get("doorId");

        iEmployeeService.multipleHandOutEmployeePermission(doorId);

        return ;

    }

    /**
     * 删除设备上的人员基本信息（包括所有关联的权限所有的这个人的信息）（设备模块调用）
     * @param employeeIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/deleteEmployeeInformationDev", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData deleteEmployeeInformationDev(@RequestBody String employeeIdCollection, HttpServletRequest request){

        /**测试数据
         *
         {
         "doorId": "9",
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319"
         ]
         }
         */

        String operatorEmployeeId = request.getHeader("accessUserId");

        return iEmployeeService.deleteEmployeeInformationDev(employeeIdCollection, operatorEmployeeId);

    }

    /**
     * 删除设备上的人员基本信息（包括所有关联的权限所有的这个人的信息）（人员模块调用）
     * @param employeeIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/deleteEmployeeInformationEmp", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void deleteEmployeeInformationEmp(@RequestBody String employeeIdCollection, HttpServletRequest request){

        /**测试数据
         *
         {
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319"
         ],
         "companyId":"JDIAOUR9839DSAY98"
         }
         */

        String operatorEmployeeId = request.getHeader("accessUserId");

        iEmployeeService.deleteEmployeeInformationEmp(employeeIdCollection, operatorEmployeeId);

    }

//    /**
//     * 删除设备上的人员开门时间（废弃不用）
//     * @param employeeIdCollection
//     * @return
//     */
//    @ResponseBody
//    @Transactional
//    @RequestMapping(value = "/deleteEmployeePermission", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
//    public void deleteEmployeePermission(@RequestBody String employeeIdCollection){
//
//        /**测试数据
//         *
//         {
//         "deviceId": "1",
//         "employeeIdList": [
//         "897020EA96214392B28369F2B421E319"
//         ]
//         }
//         */
//
//        Map<String, Object> employeeIdMap = (Map<String, Object>)JSONObject.fromObject(employeeIdCollection);
//        List<String> employeeIdList = (List<String>)employeeIdMap.get("employeeIdList");
//        String deviceId = (String)employeeIdMap.get("deviceId");
//
//        //构造删除人员开门时间的命令格式
//        DoorCmd doorCmdDeleteEmployee = new DoorCmd();
//        doorCmdDeleteEmployee.setServerId(serverId);
//        doorCmdDeleteEmployee.setDeviceId(deviceId);
//        doorCmdDeleteEmployee.setFileEdition("v1.3");
//        doorCmdDeleteEmployee.setCommandMode("C");
//        doorCmdDeleteEmployee.setCommandType("single");
//        doorCmdDeleteEmployee.setCommandTotal("1");
//        doorCmdDeleteEmployee.setCommandIndex("1");
//        doorCmdDeleteEmployee.setSubCmdId("");
//        doorCmdDeleteEmployee.setAction("DELETE_USER_ACCESS_CONTROL");
//        doorCmdDeleteEmployee.setActionCode("3002");
//
//        doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
//        doorCmdDeleteEmployee.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//        doorCmdDeleteEmployee.setSuperCmdId(FormatUtil.createUuid());
//        doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdList));
//
//        //获取完整的数据加协议封装格式
//        Map<String, Object> userDeleteInformation =  CmdUtil.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList, "C");
//        //命令状态设置为: 发送中
//        doorCmdDeleteEmployee.setStatus("1");
//        //设置md5校验值
//        doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
//        //设置数据库的data字段
//        doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
//        //命令数据存入数据库
//        entranceGuardService.insertCommand(doorCmdDeleteEmployee);
//        //立即下发数据到MQ
//        RabbitMQSender rabbitMQSender = new RabbitMQSender();
//        rabbitMQSender.sendMessage(downloadQueueName, userDeleteInformation);
//
//    }

    /**
     * 人员人脸、指纹、卡号信息上传存储(HTTP POST)(人脸更新独立出来了，方法在下面,人脸删除和其它的保留了)
     * @param userInfo    人员信息
     * @param style   校验类型
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/saveEmployeeInputInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> saveEmployeeInputInfo(@RequestParam(name = "userInfo") String userInfo,
                                                     @RequestParam(name = "style") String style){

        /**
         * 测试数据
         {
         "MD5Check": "5886C3A93092AA583242FE54FF2F4F73",
         "commandIndex": "1",
         "commandMode": "C",
         "commandTotal": "1",
         "commandType": "Single",
         "deviceId": "0f1a21d4e6fd3cb8",
         "fileEdition": "v1.3",
         "outOfTime": "2017-11-06 17:36:07",
         "sendTime": "2017-11-09 17:36:07",
         "serverId": "001",
         "command": {
         "ACTION": "UPDATE_USER_LABEL",
         "ACTIONCode": "2003",
         "subCMDID": "",
         "superCMDID": "555555555555555555555555555555"
         },
         "data": {
         "userLabel": {
         "userId": "1_20081",
         "userFace": "RlBNHIfGErJpwnUUadRDhxSt28I2Fa",
         "userFinger1": "RlBNHIfGErJpwnUUadRDhxSt28I2Fa/cxeVVculERrdrUsOmNqhZwlU4qs6HlphzasX2OG/ax2XZsOHD93qqzM3527Do0FgZdQoNqHmzaE34urHjQaibY1IBeF1qykMom2LLwwjdYclHWb9xUkPXsalfBAdyrWDIRRV3eYTZ32PMgmbVo1IDuLBs5YTHsG5vAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==",
         "userFinger2": "RlBNHIfGErJpwnUUadRDhxSt28I2Fa/cxeVVculERrdrUsOmNqhZwlU4qs6HlphzasX2OG/ax2XZsOHD93qqzM3527Do0FgZdQoNqHmzaE34urHjQaibY1IBeF1qykMom2LLwwjdYclHWb9xUkPXsalfBAdyrWDIRRV3eYTZ32PMgmbVo1IDuLBs5YTHsG5vAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==",
         "userNFC": "123456789"
         }
         }
         }
         */

//        System.out.println("------------"+userInfo);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(userInfo);
//        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
//        jsonUrlDecoderString = jsonUrlDecoderString.replace("userInfo=", "");
        System.out.println(jsonUrlDecoderString);
        System.out.println("style: "+style);

        Map<String, Object> mapResult = (Map<String, Object>) JSONObject.fromObject(jsonUrlDecoderString);

        String deviceId = (String) mapResult.get("deviceId");

        //回复人员人脸、指纹、卡号信息上传
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        String resultCode = "";
        String resultMessage = "";

        //md5校验
        //获取对方的md5
        String otherMd5 = (String) mapResult.get("MD5Check");
        mapResult.remove("MD5Check");
        String messageCheck = JSON.toJSONString(mapResult);
//        System.out.println("messageCheck: "+messageCheck);
        //生成我的md5
        String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
        //双方的md5比较判断
        if (myMd5.equals(otherMd5)){
//            System.out.println("MD5校验成功，数据完好无损");

            //获取绑定该设备的公司的id
            String companyId = deviceMapper.selectByPrimaryKey(deviceId).getCompanyId();

            Map<String, Map<String, String>> dataMap = (Map<String, Map<String, String>>) mapResult.get("data");

            if ("0".equals(style)){
                //NFC录入

                String employeeNfc = dataMap.get("userLabel").get("userNFC");
                System.out.println("nfc卡号："+employeeNfc);
                if (StringUtils.isNotEmpty(employeeNfc)){
                    //判断该卡号是否有人员使用了
                    List<Employee> employeeExistList = employeeMapper.selectByEmployeeNfc(employeeNfc);
                    if (employeeExistList == null || employeeExistList.size() == 0){

                        return iEmployeeService.saveEmployeeInputInfo(jsonUrlDecoderString, deviceId, style, companyId);

                    }else if (employeeExistList.size() > 0){
                        resultCode = "999";
                        resultMessage = "该nfc卡号已被占用";
                        resultData.put("resultCode", resultCode);
                        resultData.put("resultMessage", resultMessage);
                        resultData.put("returnObj", "");
                        resultMap.put("result", resultData);

                        System.out.println("人员指纹、人脸信息上传已回复，该nfc卡号已被占用");
                    }

                }else {
                    //提取数据
                    Map<String, Object> allMap = JSONObject.fromObject(jsonUrlDecoderString);
                    Map<String, Object> dataMapTemp = (Map<String, Object>) allMap.get("data");
                    Map<String, Object> userLabelMap = (Map<String, Object>) dataMapTemp.get("userLabel");
                    String employeeId = (String) userLabelMap.get("userId");

                    //nfc空字符串，直接更新
                    Employee employee = new Employee();
                    employee.setEmployeeId(employeeId);
                    employee.setEmployeeCompanyId(companyId);
                    employee.setEmployeeNfc(employeeNfc);
                    employeeMapper.updateByEmployeeIdAndCompanyIdSelective(employee);

                    resultCode = "0";
                    resultMessage = "删除nfc成功";
                    resultData.put("resultCode", resultCode);
                    resultData.put("resultMessage", resultMessage);
                    resultData.put("returnObj", "");
                    resultMap.put("result", resultData);

                    //同步删除卡号操作
                    iEmployeeService.synchronizeEmployeePermissionForDevices(jsonUrlDecoderString, employeeId);
                }

            }else if ("1".equals(style)){
                //此处执行人脸特征值删除，而人脸特征值更新及上传人脸图片抽离到了下面的saveEmployeeFace方法里了

                return iEmployeeService.saveEmployeeInputInfo(jsonUrlDecoderString, deviceId, style, companyId);

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
            doorCmdRecord.setAction("UPDATE_USER_LABEL");
            doorCmdRecord.setActionCode("2003");
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

            return doorRecordAll;

        }else {
            System.out.println("myMd5 = " + myMd5);
            System.out.println("MD5校验失败，数据已被修改");

            //回复人员人脸、指纹、卡号信息上传
            resultCode = "6";
            resultMessage = "MD5校验失败";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", "");
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
            doorCmdRecord.setAction("UPDATE_USER_LABEL");
            doorCmdRecord.setActionCode("2003");
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

//            System.out.println("人员指纹、人脸信息上传已回复");
            return doorRecordAll;

        }

    }

    /**
     * 人员人脸信息上传存储(HTTP POST)
     * @param userInfo    人员信息
     * @param style   校验类型
     * @param file        文件
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/saveEmployeeFace", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> saveEmployeeFace(@RequestParam(name = "userInfo") String userInfo,
                                                     @RequestParam(name = "style") String style,
                                                     @RequestParam(name = "file") MultipartFile file){

        /**
         * 测试数据
         {
         "MD5Check": "5886C3A93092AA583242FE54FF2F4F73",
         "commandIndex": "1",
         "commandMode": "C",
         "commandTotal": "1",
         "commandType": "Single",
         "deviceId": "0f1a21d4e6fd3cb8",
         "fileEdition": "v1.3",
         "outOfTime": "2017-11-06 17:36:07",
         "sendTime": "2017-11-09 17:36:07",
         "serverId": "001",
         "command": {
         "ACTION": "UPDATE_USER_LABEL",
         "ACTIONCode": "2003",
         "subCMDID": "",
         "superCMDID": "555555555555555555555555555555"
         },
         "data": {
         "userLabel": {
         "userId": "1_20081",
         "userFace": "RlBNHIfGErJpwnUUadRDhxSt28I2Fa",
         "userFinger1": "RlBNHIfGErJpwnUUadRDhxSt28I2Fa/cxeVVculERrdrUsOmNqhZwlU4qs6HlphzasX2OG/ax2XZsOHD93qqzM3527Do0FgZdQoNqHmzaE34urHjQaibY1IBeF1qykMom2LLwwjdYclHWb9xUkPXsalfBAdyrWDIRRV3eYTZ32PMgmbVo1IDuLBs5YTHsG5vAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==",
         "userFinger2": "RlBNHIfGErJpwnUUadRDhxSt28I2Fa/cxeVVculERrdrUsOmNqhZwlU4qs6HlphzasX2OG/ax2XZsOHD93qqzM3527Do0FgZdQoNqHmzaE34urHjQaibY1IBeF1qykMom2LLwwjdYclHWb9xUkPXsalfBAdyrWDIRRV3eYTZ32PMgmbVo1IDuLBs5YTHsG5vAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA==",
         "userNFC": "123456789"
         }
         }
         }
         */

//        System.out.println("------------"+userInfo);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(userInfo);
//        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
//        jsonUrlDecoderString = jsonUrlDecoderString.replace("userInfo=", "");
        System.out.println(jsonUrlDecoderString);
        System.out.println("style: "+style);

        Map<String, Object> mapResult = (Map<String, Object>) JSONObject.fromObject(jsonUrlDecoderString);

        String deviceId = (String) mapResult.get("deviceId");

        //回复人员人脸、指纹、卡号信息上传
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        String resultCode = "";
        String resultMessage = "";

        //md5校验
        //获取对方的md5
        String otherMd5 = (String) mapResult.get("MD5Check");
        mapResult.remove("MD5Check");
        String messageCheck = JSON.toJSONString(mapResult);
//        System.out.println("messageCheck: "+messageCheck);
        //生成我的md5
        String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
        //双方的md5比较判断
        if (myMd5.equals(otherMd5)){
//            System.out.println("MD5校验成功，数据完好无损");

            //获取绑定该设备的公司的id
            String companyId = deviceMapper.selectByPrimaryKey(deviceId).getCompanyId();

            Map<String, Map<String, String>> dataMap = (Map<String, Map<String, String>>) mapResult.get("data");

            if ("1".equals(style)){
                //人脸录入

                //提取人员id
                Map<String, Object> allMapTemp = JSONObject.fromObject(jsonUrlDecoderString);
                Map<String, Object> dataMapTemp = (Map<String, Object>) allMapTemp.get("data");
                Map<String, Object> userLabelMapTemp = (Map<String, Object>) dataMapTemp.get("userLabel");
                String employeeId = (String) userLabelMapTemp.get("userId");

                //存储上传的人脸图片
                try {
                    if (null != file){
                        //versionCode随便填就行，但不能为空，人脸图片上传时没有实际作用
                        iEmployeeService.deviceUploadPackage("v1.9", file, "facePhoto", employeeId);
                    }
                } catch (IOException e) {
                    System.out.println("人脸图片上传IO异常");
                    e.printStackTrace();
                }

                return iEmployeeService.saveEmployeeInputInfo(jsonUrlDecoderString, deviceId, style, companyId);

            }

            resultCode = "999";
            resultMessage = "类型错误";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", "");
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
            doorCmdRecord.setAction("UPDATE_USER_LABEL");
            doorCmdRecord.setActionCode("2003");
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

            return doorRecordAll;

        }else {
            System.out.println("myMd5 = " + myMd5);
            System.out.println("MD5校验失败，数据已被修改");

            //回复人员人脸、指纹、卡号信息上传
            resultCode = "6";
            resultMessage = "MD5校验失败";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", "");
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
            doorCmdRecord.setAction("UPDATE_USER_LABEL");
            doorCmdRecord.setActionCode("2003");
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

//            System.out.println("人员指纹、人脸信息上传已回复");
            return doorRecordAll;

        }

    }

    /**
     * 测试接口
     */
    @ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void test(){

//        DoorCmd doorCmd = doorCmdMapper.selectByPrimaryKey("");
//
//        System.out.println("[*] 测试接口");
////        System.out.println(JSON.toJSONString(doorCmd.getData()));
//
//        System.out.println(JSONObject.fromObject(doorCmd.getData()));

        //临时所有人脸信息修复
//        List<Employee> employeeList = employeeMapper.temp();
//        System.out.println(JSON.toJSONString(employeeList));
//        System.out.println("size:"+employeeList.size());
//
//        for (Employee employee : employeeList) {
//            String employeeId = employee.getEmployeeId();
//            String employeeName = employee.getEmployeeName();
//            String employeeCompanyId = employee.getEmployeeCompanyId();
//            String employeeFace = employee.getEmployeeFace();
//
//            Map<String, Object> userFace = new HashMap<>();
//            userFace.put("userId", employeeId);
//            userFace.put("faceName", employeeName);
//            userFace.put("faceData", employeeFace);
//
//            System.out.println("userFace: "+JSON.toJSONString(userFace));
//
//            Employee employeeResult = new Employee();
//            employeeResult.setEmployeeId(employeeId);
//            employeeResult.setEmployeeName(employeeName);
//            employeeResult.setEmployeeCompanyId(employeeCompanyId);
//            employeeResult.setEmployeeFace(JSON.toJSONString(userFace));
//
//            employeeMapper.updateByEmployeeIdAndCompanyIdSelective(employeeResult);
//        }

//        //交享越物业管理临时人脸信息同步
//        List<Employee> employeeList = employeeMapper.selectAllByCompanyId("59EC11BD60834DA190F9218CD4C996C7");
//        System.out.println("人员总数："+employeeList.size());
//        int totalFace = 0;
//        for (Employee employee : employeeList) {
//            List<Employee> EmployeeTempList = employeeMapper.selectAllByEmployeeId(employee.getEmployeeId());
//            for (Employee employeeTemp : EmployeeTempList) {
//                String employeeFace = employeeTemp.getEmployeeFace();
//                String employeeFaceNull = employeeMapper.selectByEmployeeIdAndCompanyId(employeeTemp.getEmployeeId(), "59EC11BD60834DA190F9218CD4C996C7").getEmployeeFace();
//                if (StringUtils.isEmpty(employeeFaceNull) && StringUtils.isNotEmpty(employeeFace)){
//                    System.out.println("已查到【"+employeeTemp.getEmployeeName()+"】的人脸信息:"+employeeFace);
//                    totalFace ++;
//                    Employee employeeBean = new Employee();
//                    employeeBean.setEmployeeId(employeeTemp.getEmployeeId());
//                    employeeBean.setEmployeeCompanyId("59EC11BD60834DA190F9218CD4C996C7");
//                    employeeBean.setEmployeeFace(employeeFace);
//                    employeeMapper.updateByEmployeeIdAndCompanyIdSelective(employeeBean);
//                }
//            }
//        }
//        System.out.println("有人脸的人员总数："+totalFace);
    }
}
