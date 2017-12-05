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
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 控制层：用户操作
 */

@Controller
@RequestMapping(value = "/employee")
public class EmployeeController {

    public static final String file = "file";
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

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private EmployeeBluetoothCountMapper employeeBluetoothCountMapper;

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
    public ReturnData handOutEmployeePermission(@RequestBody String employeePermission){

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

        System.out.println(employeePermission);

        //解析json字符串
        Map<String, Object> employeePermissionCollection = (Map<String, Object>) JSONObject.fromObject(employeePermission);

//        try {
//
//        }catch (Exception e){
//
//        }

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        //提取数据
        List<Map<String, Object>> employeePermissionList = (List<Map<String, Object>>)employeePermissionCollection.get("employeePermission");
        //获取保存草稿还是立即下发
        String immediatelyDownload = (String) employeePermissionCollection.get("immediatelyDownload");

        for (Map<String, Object> employeePermissionMap : employeePermissionList){

            //提取门信息
            String doorId = (String) employeePermissionMap.get("doorId");
            if (null == doorId){
                System.out.println("必传参数字段不存在");
                returnData.setMessage("必传参数字段不存在");
                returnData.setReturnCode("3006");
                return returnData;
            }
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

                //从本地查人员信息
                Employee employeeLocal = employeeMapper.selectByPrimaryKey(employeeMap.get("employeeId"));

                if (employeeLocal == null){
                    System.out.println("人员信息不同步，未查到【"+employeeMap.get("employeeName")+"】的信息");
                    returnData.setMessage("人员信息不同步，未查到【"+employeeMap.get("employeeName")+"】的信息");
                    returnData.setReturnCode("4007");
                    return returnData;
                }else {
                    //获取人员id
                    String employeeId = employeeLocal.getEmployeeId();

                    //存放蓝牙id
                    String bluetoothIdResult = "";

                    //下发基本信息时，给该人员分配自增长的蓝牙id
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
                            //第一次初始化时生成的蓝牙id
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
                            //新生成的蓝牙id
                            bluetoothIdResult = String.valueOf(bluetoothCount);
                        }
                    }else {
                        //人员有蓝牙id时，直接获取
                        bluetoothIdResult = employeeExist.getBluetoothNo();
                    }

                    String employeeName = employeeLocal.getEmployeeName();
                    String employeeNo = employeeLocal.getEmployeeNumber();
                    String departmentId = employeeLocal.getEmployeeDepartmentId();
                    String departmentName = employeeLocal.getEmployeeDepartmentName();
                    String entryTime = employeeLocal.getEmployeeEntryTime();
                    String probationaryExpired = employeeLocal.getEmployeeProbationaryExpired();
                    String employeePhone = employeeLocal.getEmployeePhone();
                    String blueboothId = employeeLocal.getBluetoothNo();

                    String birthday = employeeLocal.getEmployeeBirthday();
                    String contractExpired = employeeLocal.getEmployeeContractExpired();
                    String adminFlag = employeeLocal.getAdminFlag();
                    String userImg = employeeLocal.getEmployeeImg();
                    String userPhoto = employeeLocal.getEmployeePhoto();

                    //组装人员数据DATA
                    Map<String, Object> userInformation = new LinkedHashMap<String, Object>();
                    userInformation.put("userId", employeeId);
                    userInformation.put("userCode", employeeNo);
                    userInformation.put("userName", employeeName);
                    userInformation.put("userDeptId", departmentId);
                    userInformation.put("userDeptName", departmentName);
                    userInformation.put("birthday", birthday);
                    userInformation.put("entryTime", entryTime);
                    userInformation.put("probationaryExpired", probationaryExpired);
                    userInformation.put("contractExpired", contractExpired);
                    userInformation.put("adminFlag", adminFlag);
                    userInformation.put("userImg", userImg);
                    userInformation.put("userPhoto", userPhoto);
                    userInformation.put("userFinger1", "");
                    userInformation.put("userFinger2", "");
                    userInformation.put("userFace", "");
                    userInformation.put("userPhone", employeePhone);
                    userInformation.put("userNFC", "");
                    userInformation.put("bluetoothId", bluetoothIdResult);

                    /**
                     * 下发人员开门的门禁权限
                     */
                    //创建人员有效日期和一周时间区间的关联标识id
                    String rangeFlagId = FormatUtil.createUuid();

                    //查询当前门是不是下发过这个人员
                    DoorEmployee doorEmployeeExist = doorEmployeeMapper.selectByEmployeeIdAndDoorId(employeeId, doorId);

                    //关联人员和门禁
                    iEmployeeService.relateEmployeeAndDoor(doorId, doorName, employeeId, employeeName, rangeFlagId);

                    List<Map<String, Object>> oneWeekTimeList = new ArrayList<Map<String, Object>>();
                    oneWeekTimeList = (List<Map<String, Object>>) employeePermissionMap.get("oneWeekTimeList");

                    //获取统一的开门方式
                    String rangeDoorOpenType = (String) employeePermissionMap.get("rangeDoorOpenType");

                    if (doorEmployeeExist != null){

                        //删除上一次下发的这个门的人员开门时间区间信息
                        String rangeFlagIdTemp = doorEmployeeExist.getRangeFlagId();
                        timeRangeCommonEmployeeMapper.deleteByPrimaryKey(rangeFlagIdTemp);

                    }

                    //遍历一周的时间区间,最多4*7=28条数据
                    for (Map<String, Object> timeRangeMap : oneWeekTimeList) {

                        //提取时间区间权限信息
                        String weekType = (String) timeRangeMap.get("weekType");
                        String isAllDay = (String) timeRangeMap.get("isAllDay");
                        String startTime = (String) timeRangeMap.get("startTime");
                        String endTime = (String) timeRangeMap.get("endTime");
                        String isDitto = (String) timeRangeMap.get("isDitto");

                        //添加每个时间区间的开门类型
                        timeRangeMap.put("doorOpenType", rangeDoorOpenType);

//                        //判断是否是全天时间
//                        if (isAllDay.equals("1")){
//                            startTime = "00:00";
//                            endTime = "23:59";
//                            timeRangeMap.put("startTime", "00:00");
//                            timeRangeMap.put("endTime", "23:59");
//                        }

                        //关联人员门禁权限之开门时间区间和开门方式
                        iEmployeeService.relateEmployeeAndPermission(rangeFlagId, employeeId, weekType, isAllDay, startTime,
                                endTime, rangeDoorOpenType, isDitto);

                    }

                    //查询普通人员的公共密码
                    DoorSetting doorSetting = doorSettingMapper.selectByPrimaryKey(doorId);

                    //组装更新人员门禁权限业务数据DATA
                    Map<String, Object> userPermission = new LinkedHashMap<String, Object>();
                    userPermission.put("employeeId", employeeId);

                    //提取开门权限有效日期，年月日
                    String doorOpenStartTime = (String) employeePermissionMap.get("doorOpenStartTime");
                    String doorOpenEndTime = (String) employeePermissionMap.get("doorOpenEndTime");

                    //存储开门权限有效日期
                    DoorEmployeePermission doorEmployeePermission = new DoorEmployeePermission();
                    doorEmployeePermission.setEmployeeId(employeeId);
                    doorEmployeePermission.setDoorOpenStartTime(doorOpenStartTime);
                    doorEmployeePermission.setDoorOpenEndTime(doorOpenEndTime);
                    doorEmployeePermission.setRangeFlagId(rangeFlagId);

                    if (doorEmployeeExist != null){

                        String rangeFlagIdTemp = doorEmployeeExist.getRangeFlagId();
                        //删除上一次下发的这个门的人员权限信息
                        doorEmployeePermissionMapper.deleteByRangeFlagId(rangeFlagIdTemp);

                    }

                    doorEmployeePermissionMapper.insertSelective(doorEmployeePermission);

                    userPermission.put("permissionValidityBeginTime", doorOpenStartTime);
                    userPermission.put("permissionValidityEndTime", doorOpenEndTime);
                    try {
                        userPermission.put("employeeDoorPassword", doorSetting.getFirstPublishPassword());
                    }catch (Exception e){
                        userPermission.put("employeeDoorPassword", "");
                        System.out.println("【"+employeeName+"】的门设置未设置");
                    }

                    userPermission.put("oneWeekTimeList", oneWeekTimeList);

                    //部分需要循环修改的命令格式
                    //人员基本信息
                    doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
                    doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
                    doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
                    doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformation));
//                doorCmdEmployeeInformation.setData(JSON.toJSONString(employeeLocal));
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
//                    Map<String, Object> userInformationAll =  RabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "userInfo", employeeLocal, "C");
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
//                    Map<String, Object> userInformationAll =  rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "userInfo", employeeLocal, "C");
                        //命令状态设置为: 发送中
                        doorCmdEmployeeInformation.setStatus("1");
                        //设置md5校验值
                        doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
                        //设置数据库的data字段
                        doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
                        //命令数据存入数据库
                        entranceGuardService.insertCommand(doorCmdEmployeeInformation);
                        //立即下发数据到MQ
                        rabbitMQSender.sendMessage(deviceId, userInformationAll);

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
                        //立即下发数据到MQ
                        rabbitMQSender.sendMessage(deviceId, userPermissionAll);
                    }
                }
            }
        }

        if ("0".equals(immediatelyDownload)){
            returnData.setMessage("本次下发的信息，已存为草稿");
            returnData.setReturnCode("3000");
            return returnData;
        }else if ("1".equals(immediatelyDownload)){
            returnData.setMessage("已执行下发人员信息操作，请前往门列表查看当前选中的门，了解具体的下发状态");
            returnData.setReturnCode("3000");
            return returnData;
        }else {
            returnData.setMessage("非法参数");
            returnData.setReturnCode("3006");
            return returnData;
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
     * 删除设备上的人员基本信息（包括所有关联的权限所有的这个人的信息）（设备模块调用）
     * @param employeeIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/deleteEmployeeInformationDev", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData deleteEmployeeInformationDev(@RequestBody String employeeIdCollection){

        /**测试数据
         *
         {
         "doorId": "9",
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319"
         ]
         }
         */

        return iEmployeeService.deleteEmployeeInformationDev(employeeIdCollection);

    }

    /**
     * 删除设备上的人员基本信息（包括所有关联的权限所有的这个人的信息）（人员模块调用）
     * @param employeeIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/deleteEmployeeInformationEmp", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void deleteEmployeeInformationEmp(@RequestBody String employeeIdCollection){

        /**测试数据
         *
         {
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319"
         ],
         "companyId":"JDIAOUR9839DSAY98"
         }
         */

        iEmployeeService.deleteEmployeeInformationEmp(employeeIdCollection);

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
     * @param userInfo    人员信息
     * @param style   校验类型
     * @param file        文件
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/saveEmployeeInputInfo", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> saveEmployeeInputInfo(@RequestParam(name = "userInfo") String userInfo,
                                                     @RequestParam(name = "style") String style,
                                                     @RequestParam(name = "file") String file){

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

        System.out.println("------------"+userInfo);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(userInfo);
//        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
//        jsonUrlDecoderString = jsonUrlDecoderString.replace("userInfo=", "");
        System.out.println(jsonUrlDecoderString);

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
        System.out.println("messageCheck: "+messageCheck);
        //生成我的md5
        String myMd5 = MD5Util.encryptPassword(messageCheck, "XC9EO5GKOIVRMBQ2YE8X");
        System.out.println("myMd5 = " + myMd5);
        //双方的md5比较判断
        if (myMd5.equals(otherMd5)){
            System.out.println("MD5校验成功，数据完好无损");

            Map<String, Map<String, String>> dataMap = (Map<String, Map<String, String>>) mapResult.get("data");

            if ("0".equals(style)){
                //NFC录入

                String employeeNfc = dataMap.get("userLabel").get("userNFC");

                if (StringUtils.isNotEmpty(employeeNfc)){
                    //判断该卡号是否有人员使用了
                    List<Employee> employeeExistList = employeeMapper.selectByEmployeeNfc(employeeNfc);
                    if (employeeExistList == null || employeeExistList.size() == 0){

                        return iEmployeeService.saveEmployeeInputInfo(jsonUrlDecoderString, deviceId, style);

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
                    employee.setEmployeeNfc(employeeNfc);
                    employeeMapper.updateByPrimaryKeySelective(employee);

                    resultCode = "0";
                    resultMessage = "删除nfc成功";
                    resultData.put("resultCode", resultCode);
                    resultData.put("resultMessage", resultMessage);
                    resultData.put("returnObj", "");
                    resultMap.put("result", resultData);
                }

            }else if ("1".equals(style)){
                //人脸录入

                return iEmployeeService.saveEmployeeInputInfo(jsonUrlDecoderString, deviceId, style);

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

            return doorRecordAll;

        }else {
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
