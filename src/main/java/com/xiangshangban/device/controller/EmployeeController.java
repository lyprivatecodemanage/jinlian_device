package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
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
 * 控制层：用户操作
 */

@Controller
@RequestMapping(value = "/employee")
public class EmployeeController {

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    @Value("${employee.interface.address}")
    String employeeInterfaceAddress;

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

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

    /**
     * 人员模块人员信息同步
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
    public void handOutEmployeePermission(@RequestBody String employeePermission){

        /**测试数据
         {
         "immediatelyDownload": "1",
         "employeePermission": [
         {
         "doorId": "001",
         "doorName": "金念大门",
         "employeeList": [
         {
         "employeeId": "897020EA96214392B28369F2B421E319",
         "employeeName": "吴费"
         },
         {
         "employeeId": "9C305EC5587745FF9F0D8198512264D6",
         "employeeName": "赵武"
         }
         ],
         "rangeDoorOpenType": "0",
         "oneWeekTimeList": [
         {
         "isAllDay": "0",
         "weekType": "3",
         "startTime": "08:00",
         "endTime": "12:00"
         },
         {
         "isAllDay": "0",
         "weekType": "3",
         "startTime": "14:00",
         "endTime": "18:00"
         }
         ]
         }
         ]
         }
         */

        //解析json字符串
        Map<String, Object> employeePermissionCollection = (Map<String, Object>) JSONObject.fromObject(employeePermission);
        List<Map<String, Object>> employeePermissionList = (List<Map<String, Object>>)employeePermissionCollection.get("employeePermission");

        //获取保存草稿还是立即下发
        String immediatelyDownload = (String) employeePermissionCollection.get("immediatelyDownload");

        for (Map<String, Object> employeePermissionMap : employeePermissionList){

            //提取门信息
            String doorId = (String) employeePermissionMap.get("doorId");
            String doorName = (String) employeePermissionMap.get("doorName");
            List<Map<String, String>> employeeList = (List<Map<String,String>>) employeePermissionMap.get("employeeList");

            //获取门对应的设备的id
            String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();

            //下发人员基本信息
            //构造命令格式
            DoorCmd doorCmdEmployeeInformation = new DoorCmd();
            doorCmdEmployeeInformation.setServerId("001");
            doorCmdEmployeeInformation.setDeviceId(deviceId);
            doorCmdEmployeeInformation.setFileEdition("v1.3");
            doorCmdEmployeeInformation.setCommandMode("C");
            doorCmdEmployeeInformation.setCommandType("single");
            doorCmdEmployeeInformation.setCommandTotal("1");
            doorCmdEmployeeInformation.setCommandIndex("1");
            doorCmdEmployeeInformation.setSubCmdId("");
            doorCmdEmployeeInformation.setAction("UPDATE_USER_INFO");
            doorCmdEmployeeInformation.setActionCode("2001");

            //下发人员门禁权限
            //构造命令格式
            DoorCmd doorCmdEmployeePermission = new DoorCmd();
            doorCmdEmployeePermission.setServerId("001");
            doorCmdEmployeePermission.setDeviceId(deviceId);
            doorCmdEmployeePermission.setFileEdition("v1.3");
            doorCmdEmployeePermission.setCommandMode("C");
            doorCmdEmployeePermission.setCommandType("single");
            doorCmdEmployeePermission.setCommandTotal("1");
            doorCmdEmployeePermission.setCommandIndex("1");
            doorCmdEmployeePermission.setSubCmdId("");
            doorCmdEmployeePermission.setAction("UPDATE_USER_ACCESS_CONTROL");
            doorCmdEmployeePermission.setActionCode("3001");

            //遍历人员
            for (Map<String, String> employeeMap : employeeList) {

                System.out.println("employeeId = " + employeeMap.get("employeeId"));

                Map<String, Object> httpData = new HashMap<String, Object>();
                httpData.put("employeeId", employeeMap.get("employeeId"));

                /**
                 * 下发选定的人员的基本信息
                 */
                //根据人员id请求单个人员信息
                String employeeInfo = HttpRequestFactory.sendRequet(employeeInterfaceAddress, httpData);
                System.out.println("[*] HTTP send: 已发出请求");
                System.out.println("[*] employeeInfo: " + employeeInfo);

                Map<String, String> employeeInfoMap = new HashMap<String, String>();

                //取出需要的人员信息
                try {
                    employeeInfoMap = (Map<String, String>)JSONObject.fromObject(employeeInfo).get("data");
                }catch (Exception e){
                    System.out.println("人员模块不在线!");
                }
                String employeeId = employeeInfoMap.get("employeeId");
                String employeeNo = employeeInfoMap.get("employeeNo");
                String employeeName = employeeInfoMap.get("employeeName");
                String departmentId = employeeInfoMap.get("departmentId");
                String departmentName = employeeInfoMap.get("departmentName");
                String entryTime = employeeInfoMap.get("entryTime");
                String probationaryExpired = employeeInfoMap.get("probationaryExpired");
                String employeePhone = employeeInfoMap.get("employeePhone");
                String employeeStatus = employeeInfoMap.get("employeeStatus");
                String companyId = employeeInfoMap.get("companyId");
                String companyName = employeeInfoMap.get("companyName");

                //增加人员信息到本地人员表
                Employee employee = new Employee();
                employee.setEmployeeId(employeeId);
                employee.setEmployeeNumber(employeeNo);
                employee.setEmployeeName(employeeName);
                employee.setEmployeeDepartmentId(departmentId);
                employee.setEmployeeDepartmentName(departmentName);
                employee.setEmployeeEntryTime(entryTime);
                employee.setEmployeeProbationaryExpired(probationaryExpired);
                employee.setEmployeePhone(employeePhone);
                employee.setEmployeeStatus(employeeStatus);
                employee.setUpdateTime(DateUtils.getDateTime());
                employee.setEmployeeCompanyId(companyId);
                employee.setEmployeeCompanyName(companyName);

                //查询人员信息是否存在
                Employee employeeExit = employeeMapper.selectByPrimaryKey(employeeId);
                if (employeeExit == null){
                    employeeMapper.insertSelective(employee);
                }else {
                    employeeMapper.updateByPrimaryKeySelective(employee);
                }

                //组装人员数据DATA
                Map<String, Object> userInformation = new LinkedHashMap<String, Object>();
                userInformation.put("userId", employeeId);
                userInformation.put("userCode", employeeNo);
                userInformation.put("userName", employeeName);
                userInformation.put("userDeptId", departmentId);
                userInformation.put("userDeptName", departmentName);
                userInformation.put("birthday", "");
                userInformation.put("entryTime", entryTime);
                userInformation.put("probationaryExpired", probationaryExpired);
                userInformation.put("contractExpired", "");
                userInformation.put("adminFlag", "");
                userInformation.put("userImg", "");
                userInformation.put("userPhoto", "");
                userInformation.put("userFinger1", "");
                userInformation.put("userFinger2", "");
                userInformation.put("userFace", "");
                userInformation.put("userPhone", employeePhone);
                userInformation.put("userNFC", "");


                /**
                 * 下发人员开门的门禁权限
                 */
                //关联人员和门禁
//                String employeeId = employeeMap.get("employeeId");
//                String employeeName = employeeMap.get("employeeName");
                iEmployeeService.relateEmployeeAndDoor(doorId, doorName, employeeId, employeeName);

                List<Map<String, Object>> oneWeekTimeList = new ArrayList<Map<String, Object>>();
                oneWeekTimeList= (List<Map<String, Object>>) employeePermissionMap.get("oneWeekTimeList");

                //获取统一的开门方式
                String rangeDoorOpenType = (String) employeePermissionMap.get("rangeDoorOpenType");

                //判断某个人员的时间区间信息是否存在
                List<TimeRangeCommonEmployee> timeRangeCommonEmployeeList = timeRangeCommonEmployeeMapper.selectExistByEmployeeId(employeeId);
                if (timeRangeCommonEmployeeList != null){
                    //有信息存在则删除该人员的所有时间区间信息然后后面的时候重新添加
                    timeRangeCommonEmployeeMapper.deleteByEmployeeId(employeeId);
                }

                //遍历一周的时间区间,最多4*7=28条数据
                for (Map<String, Object> timeRangeMap : oneWeekTimeList) {

                    //提取时间区间权限信息
                    String weekType = (String) timeRangeMap.get("weekType");
                    String isAllDay = (String) timeRangeMap.get("isAllDay");
                    String startTime = (String) timeRangeMap.get("startTime");
                    String endTime = (String) timeRangeMap.get("endTime");

                    //添加每个时间区间的开门类型
                    timeRangeMap.put("doorOpenType", rangeDoorOpenType);

                    //判断是否是全天时间
                    if (isAllDay.equals("1")){
                        startTime = "00:00";
                        endTime = "23:59";
                    }

                    //关联人员门禁权限之开门时间区间和开门方式
                    iEmployeeService.relateEmployeeAndPermission(employeeId, weekType, isAllDay, startTime,
                            endTime, rangeDoorOpenType);

                }

                //查询某个人员开门权限有效时间
                DoorEmployeePermission doorEmployeePermission = doorEmployeePermissionMapper.selectByPrimaryKey(employeeId);

                //查询普通人员的公共密码
                DoorSetting doorSetting = doorSettingMapper.selectByPrimaryKey(doorId);

                //组装更新人员门禁权限业务数据DATA
                Map<String, Object> userPermission = new LinkedHashMap<String, Object>();
                userPermission.put("employeeId", employeeId);
                try {
                    userPermission.put("permissionValidityBeginTime", doorEmployeePermission.getDoorOpenStartTime());
                }catch (NullPointerException e){
                    System.out.println("【"+employeeName+"】的开门权限有效时间未设置");
                }
                userPermission.put("permissionValidityEndTime", doorEmployeePermission.getDoorOpenEndTime());
                userPermission.put("employeeDoorPassword", doorSetting.getFirstPublishPassword());
                userPermission.put("oneWeekTimeList", oneWeekTimeList);

                //部分需要循环修改的命令格式
                //人员基本信息
                doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
                doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
                doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
                doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformation));
                doorCmdEmployeeInformation.setEmployeeId(employeeId);
                //人员基本开门门禁权限信息
                doorCmdEmployeePermission.setSendTime(CalendarUtil.getCurrentTime());
                doorCmdEmployeePermission.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
                doorCmdEmployeePermission.setSuperCmdId(FormatUtil.createUuid());
                doorCmdEmployeePermission.setData(JSON.toJSONString(userPermission));
                doorCmdEmployeePermission.setEmployeeId(employeeId);

                //判断是否立即下发数据到设备
                if (immediatelyDownload.equals("0")){

                    /**
                     * 人员基本信息
                     */
                    //获取完整的数据加协议封装格式
                    Map<String, Object> userInformationAll =  RabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "userInfo", userInformation, "C");
                    //命令状态设置为: 待发送
                    doorCmdEmployeeInformation.setStatus("0");
                    //设置md5校验值
                    doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
                    //命令数据存入数据库
                    entranceGuardService.insertCommand(doorCmdEmployeeInformation);

                    /**
                     * 人员开门权限
                     */
                    //获取完整的数据加协议封装格式
                    Map<String, Object> userPermissionAll =  RabbitMQSender.messagePackaging(doorCmdEmployeePermission, "userPermission", userPermission, "C");
                    //命令状态设置为: 待发送
                    doorCmdEmployeePermission.setStatus("0");
                    //设置md5校验值
                    doorCmdEmployeePermission.setMd5Check((String) userPermissionAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeePermission.setData(JSON.toJSONString(userPermissionAll.get("data")));
                    //命令数据存入数据库
                    entranceGuardService.insertCommand(doorCmdEmployeePermission);

                }else if (immediatelyDownload.equals("1")){

                    /**
                     * 人员基本信息
                     */
                    //获取完整的数据加协议封装格式
                    RabbitMQSender rabbitMQSender = new RabbitMQSender();
                    Map<String, Object> userInformationAll =  rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "userInfo", userInformation, "C");
                    //命令状态设置为: 发送中
                    doorCmdEmployeeInformation.setStatus("1");
                    //设置md5校验值
                    doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
                    //命令数据存入数据库
                    entranceGuardService.insertCommand(doorCmdEmployeeInformation);
//                    //立即下发数据到MQ
//                    rabbitMQSender.sendMessage(downloadQueueName, userInformationAll);

                    /**
                     * 人员开门权限
                     */
                    //获取完整的数据加协议封装格式
                    Map<String, Object> userPermissionAll =  rabbitMQSender.messagePackaging(doorCmdEmployeePermission, "userPermission", userPermission, "C");
                    //命令状态设置为: 发送中
                    doorCmdEmployeePermission.setStatus("1");
                    //设置md5校验值
                    doorCmdEmployeePermission.setMd5Check((String) userPermissionAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeePermission.setData(JSON.toJSONString(userPermissionAll.get("data")));
                    //命令数据存入数据库
                    entranceGuardService.insertCommand(doorCmdEmployeePermission);
//                    //立即下发数据到MQ
//                    rabbitMQSender.sendMessage(downloadQueueName, userPermissionAll);

                }
            }
        }
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
     * 删除设备上的人员基本信息（包括所有关联的权限所有的这个人的信息）
     * @param employeeIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/deleteEmployeeInformation", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void deleteEmployeeInformation(@RequestBody String employeeIdCollection){

        /**测试数据
         *
         {
         "deviceId": "1",
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319"
         ]
         }
         */

        iEmployeeService.deleteEmployeeInformation(employeeIdCollection);

    }

//    /**
//     * 删除设备上的人员开门时间（废弃不用）
//     * @param employeeIdCollection
//     * @return
//     */
//    @Value("${rabbitmq.download.queue.name}")
//    String downloadQueueName;
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
//        doorCmdDeleteEmployee.setServerId("001");
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
//        Map<String, Object> userDeleteInformation =  RabbitMQSender.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList, "C");
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
     * 人员人脸、指纹、卡号信息上传存储(HTTP POST)
     * @param jsonString
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/saveEmployeeInputInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> saveEmployeeInputInfo(@RequestBody String jsonString){

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

        System.out.println("------------"+jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("userInfo=", "");
        System.out.println(jsonUrlDecoderString);

        Map<String, Object> mapResult = (Map<String, Object>) JSONObject.fromObject(jsonUrlDecoderString);

        String deviceId = (String) mapResult.get("deviceId");

        //md5校验
        //获取对方的md5
        String otherMd5 = (String) mapResult.get("MD5Check");
        mapResult.remove("MD5Check");
        String messageCheck = JSON.toJSONString(mapResult);
        //生成我的md5
        String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
        //双方的md5比较判断
        if (myMd5.equals(otherMd5)){
            System.out.println("MD5校验成功，数据完好无损");

            Map<String, Map<String, String>> dataMap = (Map<String, Map<String, String>>) mapResult.get("data");
            String employeeNfc = dataMap.get("userLabel").get("userNFC");
            //判断该卡号是否有人员使用了
            Employee employeeExist = employeeMapper.selectByEmployeeNfc(employeeNfc);
            if (employeeExist == null){

                return iEmployeeService.saveEmployeeInputInfo(jsonUrlDecoderString, deviceId);

            }else {
                //回复人员人脸、指纹、卡号信息上传
                Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
                Map<String, Object> resultData = new LinkedHashMap<String, Object>();
                String resultCode = "999";
                String resultMessage = "该nfc卡号已被占用";
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
                resultData.put("returnObj", "");
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
                doorCmdRecord.setAction("UPDATE_USER_LABEL");
                doorCmdRecord.setActionCode("2003");
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

                System.out.println("人员指纹、人脸信息上传已回复，该nfc卡号已被占用");
                return doorRecordAll;
            }

        }else {
            System.out.println("MD5校验失败，数据已被修改");

            //回复人员人脸、指纹、卡号信息上传
            Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
            Map<String, Object> resultData = new LinkedHashMap<String, Object>();
            String resultCode = "6";
            String resultMessage = "MD5校验失败";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", "");
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
            doorCmdRecord.setAction("UPDATE_USER_LABEL");
            doorCmdRecord.setActionCode("2003");
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

            System.out.println("人员指纹、人脸信息上传已回复");
            return doorRecordAll;

        }

    }

    /**
     * 测试接口
     */
    @ResponseBody
    @RequestMapping(value = "/test", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void test(){

        DoorCmd doorCmd = doorCmdMapper.selectByPrimaryKey("");

        System.out.println("[*] 测试接口");
//        System.out.println(JSON.toJSONString(doorCmd.getData()));

        System.out.println(JSONObject.fromObject(doorCmd.getData()));

    }

}
