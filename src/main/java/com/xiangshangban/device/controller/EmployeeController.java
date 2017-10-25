package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEmployeeService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private DoorEmployeePermissionMapper doorEmployeePermissionMapper;

    @Autowired
    private DoorSettingMapper doorSettingMapper;

    @Autowired
    private TimeRangeCommonEmployeeMapper timeRangeCommonEmployeeMapper;

    /**
     * 人员操作命令生成器（暂未使用）
     * @param userInformation
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/commandGenerate", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void employeeCommandGenerate(@RequestBody String userInformation){

        System.out.println("[*] userInformation: " + userInformation);

        //JSON字符串解析
        Map<String, Object> userInformationMap = (Map<String, Object>)JSONObject.fromObject(userInformation);
        String action = (String) userInformationMap.get("action");
        List<String> userIdCollection = (List<String>) userInformationMap.get("employeeIdCollection");

        iEmployeeService.employeeCommandGenerate(action, userIdCollection);

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
            doorCmdEmployeeInformation.setSuperCmdId("");
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
            doorCmdEmployeePermission.setSuperCmdId("");
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
                String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8085/EmployeeController/selectByEmployee", httpData);
                System.out.println("[*] HTTP send: 已发出请求");
                System.out.println("[*] employeeInfo: " + employeeInfo);

                //取出需要的人员信息
                Map<String, String> employeeInfoMap = (Map<String, String>)JSONObject.fromObject(employeeInfo).get("emp");

                //增加人员信息到本地人员表
                //以后补

                //组装人员数据DATA
                Map<String, Object> userInformation = new LinkedHashMap<String, Object>();
                userInformation.put("userId", employeeInfoMap.get("employeeId"));
                userInformation.put("userCode", employeeInfoMap.get("employeeNo"));
                userInformation.put("userName", employeeInfoMap.get("employeeName"));
                userInformation.put("userDeptId", employeeInfoMap.get("departmentId"));
                userInformation.put("userDeptName", employeeInfoMap.get("departmentName"));
                userInformation.put("birthday", "");
                userInformation.put("entryTime", employeeInfoMap.get("entryTime"));
                userInformation.put("probationaryExpired", employeeInfoMap.get("probationaryExpired"));
                userInformation.put("contractExpired", "");
                userInformation.put("adminFlag", "");
                userInformation.put("userImg", "");
                userInformation.put("userPhoto", "");
                userInformation.put("userFinger1", "");
                userInformation.put("userFinger2", "");
                userInformation.put("userFace", "");
                userInformation.put("userPhone", employeeInfoMap.get("employeePhone"));
                userInformation.put("userNFC", "");


                /**
                 * 下发人员开门的门禁权限
                 */
                //关联人员和门禁
                String employeeId = employeeMap.get("employeeId");
                String employeeName = employeeMap.get("employeeName");
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
                userPermission.put("permissionValidityBeginTime", doorEmployeePermission.getDoorOpenStartTime());
                userPermission.put("permissionValidityEndTime", doorEmployeePermission.getDoorOpenEndTime());
                userPermission.put("employeeDoorPassword", doorSetting.getFirstPublishPassword());
                userPermission.put("oneWeekTimeList", oneWeekTimeList);

                //部分需要循环修改的命令格式
                //人员基本信息
                doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
                doorCmdEmployeeInformation.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
                doorCmdEmployeeInformation.setSubCmdId(FormatUtil.createUuid());
                doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformation));
                //人员基本开门门禁权限信息
                doorCmdEmployeePermission.setSendTime(CalendarUtil.getCurrentTime());
                doorCmdEmployeePermission.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
                doorCmdEmployeePermission.setSubCmdId(FormatUtil.createUuid());
                doorCmdEmployeePermission.setData(JSON.toJSONString(userPermission));

                //判断是否立即下发数据到设备
                if (immediatelyDownload.equals("0")){

                    /**
                     * 人员基本信息
                     */
                    //获取完整的数据加协议封装格式
                    Map<String, Object> userInformationAll =  RabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "userInfo", userInformation);
                    //命令状态设置为: 待发送
                    doorCmdEmployeeInformation.setStatus("0");
                    //设置md5校验值
                    doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
                    //命令数据存入数据库
                    iEmployeeService.insertEmployeeCommand(doorCmdEmployeeInformation);

                    /**
                     * 人员开门权限
                     */
                    //获取完整的数据加协议封装格式
                    Map<String, Object> userPermissionAll =  RabbitMQSender.messagePackaging(doorCmdEmployeePermission, "userPermission", userPermission);
                    //命令状态设置为: 待发送
                    doorCmdEmployeePermission.setStatus("0");
                    //设置md5校验值
                    doorCmdEmployeePermission.setMd5Check((String) userPermissionAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeePermission.setData(JSON.toJSONString(userPermissionAll.get("data")));
                    //命令数据存入数据库
                    iEmployeeService.insertEmployeeCommand(doorCmdEmployeePermission);

                }else if (immediatelyDownload.equals("1")){

                    /**
                     * 人员基本信息
                     */
                    //获取完整的数据加协议封装格式
                    RabbitMQSender rabbitMQSender = new RabbitMQSender();
                    Map<String, Object> userInformationAll =  rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "userInfo", userInformation);
                    //命令状态设置为: 发送中
                    doorCmdEmployeeInformation.setStatus("1");
                    //设置md5校验值
                    doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
                    //命令数据存入数据库
                    iEmployeeService.insertEmployeeCommand(doorCmdEmployeeInformation);
                    //立即下发数据到MQ
                    rabbitMQSender.sendMessage("hello", userInformationAll);

                    /**
                     * 人员开门权限
                     */
                    //获取完整的数据加协议封装格式
                    Map<String, Object> userPermissionAll =  rabbitMQSender.messagePackaging(doorCmdEmployeePermission, "userPermission", userPermission);
                    //命令状态设置为: 发送中
                    doorCmdEmployeePermission.setStatus("1");
                    //设置md5校验值
                    doorCmdEmployeePermission.setMd5Check((String) userPermissionAll.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdEmployeePermission.setData(JSON.toJSONString(userPermissionAll.get("data")));
                    //命令数据存入数据库
                    iEmployeeService.insertEmployeeCommand(doorCmdEmployeePermission);
                    //立即下发数据到MQ
                    rabbitMQSender.sendMessage("hello", userPermissionAll);

                }
            }
        }
    }

    /**
     * 删除设备上的人员基本信息
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

        Map<String, Object> employeeIdMap = (Map<String, Object>)JSONObject.fromObject(employeeIdCollection);
        List<String> employeeIdList = (List<String>)employeeIdMap.get("employeeIdList");
        String deviceId = (String)employeeIdMap.get("deviceId");

        //更新本地指定的人员状态为删除状态
        //以后补全


        //构造删除人员的命令格式
        DoorCmd doorCmdDeleteEmployee = new DoorCmd();
        doorCmdDeleteEmployee.setServerId("001");
        doorCmdDeleteEmployee.setDeviceId(deviceId);
        doorCmdDeleteEmployee.setFileEdition("v1.3");
        doorCmdDeleteEmployee.setCommandMode("C");
        doorCmdDeleteEmployee.setCommandType("single");
        doorCmdDeleteEmployee.setCommandTotal("1");
        doorCmdDeleteEmployee.setCommandIndex("1");
        doorCmdDeleteEmployee.setSuperCmdId("");
        doorCmdDeleteEmployee.setAction("DELETE_USER_INFO");
        doorCmdDeleteEmployee.setActionCode("2002");

        doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdDeleteEmployee.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdDeleteEmployee.setSubCmdId(FormatUtil.createUuid());
        doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdList));

        //获取完整的数据加协议封装格式
        Map<String, Object> userDeleteInformation =  RabbitMQSender.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList);
        //命令状态设置为: 发送中
        doorCmdDeleteEmployee.setStatus("1");
        //设置md5校验值
        doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
        //设置数据库的data字段
        doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
        //命令数据存入数据库
        iEmployeeService.insertEmployeeCommand(doorCmdDeleteEmployee);
        //立即下发数据到MQ
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        rabbitMQSender.sendMessage("hello", userDeleteInformation);

    }

    /**
     * 删除设备上的人员开门时间
     * @param employeeIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/deleteEmployeePermission", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public void deleteEmployeePermission(@RequestBody String employeeIdCollection){

        /**测试数据
         *
         {
         "deviceId": "1",
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319"
         ]
         }
         */

        Map<String, Object> employeeIdMap = (Map<String, Object>)JSONObject.fromObject(employeeIdCollection);
        List<String> employeeIdList = (List<String>)employeeIdMap.get("employeeIdList");
        String deviceId = (String)employeeIdMap.get("deviceId");

        //构造删除人员开门时间的命令格式
        DoorCmd doorCmdDeleteEmployee = new DoorCmd();
        doorCmdDeleteEmployee.setServerId("001");
        doorCmdDeleteEmployee.setDeviceId(deviceId);
        doorCmdDeleteEmployee.setFileEdition("v1.3");
        doorCmdDeleteEmployee.setCommandMode("C");
        doorCmdDeleteEmployee.setCommandType("single");
        doorCmdDeleteEmployee.setCommandTotal("1");
        doorCmdDeleteEmployee.setCommandIndex("1");
        doorCmdDeleteEmployee.setSuperCmdId("");
        doorCmdDeleteEmployee.setAction("DELETE_USER_ACCESS_CONTROL");
        doorCmdDeleteEmployee.setActionCode("3002");

        doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdDeleteEmployee.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdDeleteEmployee.setSubCmdId(FormatUtil.createUuid());
        doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdList));

        //获取完整的数据加协议封装格式
        Map<String, Object> userDeleteInformation =  RabbitMQSender.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList);
        //命令状态设置为: 发送中
        doorCmdDeleteEmployee.setStatus("1");
        //设置md5校验值
        doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
        //设置数据库的data字段
        doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
        //命令数据存入数据库
        iEmployeeService.insertEmployeeCommand(doorCmdDeleteEmployee);
        //立即下发数据到MQ
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        rabbitMQSender.sendMessage("hello", userDeleteInformation);

    }

    /**
     * 根据公司id查询门列表
     * @param companyIdCollection
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/findDoorIdByCompanyId", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public List<Door> findDoorIdByCompanyId(@RequestBody String companyIdCollection){

        Map<String, String> companyIdMap = (Map<String, String>)JSONObject.fromObject(companyIdCollection);
        String companyId = companyIdMap.get("companyId");

        return iEmployeeService.findDoorIdByCompanyId(companyId);

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
