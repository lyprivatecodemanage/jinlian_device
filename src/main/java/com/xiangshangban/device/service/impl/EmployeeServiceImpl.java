package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.HttpRequestFactory;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**d
 * date: 2017/10/19 10:38
 * describe: TODO 用户管理实现类
 */

@Service
public class EmployeeServiceImpl implements IEmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private TimeRangeCommonEmployeeMapper timeRangeCommonEmployeeMapper;

    @Autowired
    private DoorRecordMapper doorRecordMapper;

    @Autowired
    private IEntranceGuardService entranceGuardService;

//    //人员模块命令生成器（暂未使用）
//    @Override
//    public void employeeCommandGenerate(String action, List<String> employeeIdCollection) {
//
//        if (action.equals("UPDATE_USER_INFO")){
//
//            for (String employeeId : employeeIdCollection) {
//
//                //根据人员id请求单个人员信息
//                String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", employeeId);
//                System.out.println("[*] send: 已发出请求");
//                System.out.println("[*] employeeInfo: " + employeeInfo);
//
//                //取出需要的人员信息
//                Employee employee = new Employee();
//                Map<String, String> employeeInfoMap = (Map<String, String>)JSONObject.fromObject(employeeInfo);
//                employee.setEmployeeId(employeeInfoMap.get("employeeId"));
//                employee.setEmployeeNumber(employeeInfoMap.get("employeeNo"));
//                employee.setEmployeeName(employeeInfoMap.get("employeeName"));
//                employee.setEmployeeDepartmentId(employeeInfoMap.get("departmentId"));
//                employee.setEmployeeDepartmentName(employeeInfoMap.get("departmentName"));
//                employee.setEmployeeBirthday("");
//                employee.setEmployeeEntryTime(employeeInfoMap.get("entryTime"));
//                employee.setEmployeeProbationaryExpired(employeeInfoMap.get("probationaryExpired"));
//                employee.setEmployeeContractExpired("");
//                employee.setAdminFlag("");
//                employee.setEmployeePhone(employeeInfoMap.get("employeePhone"));
//
//                //人员信息存储到本地
//                //查询人员信息是否存在
//                Employee employeeExit = employeeMapper.selectByPrimaryKey(employeeId);
//                if (employeeExit == null){
//                    employeeMapper.insertSelective(employee);
//                }else {
//                    employeeMapper.updateByPrimaryKeySelective(employee);
//                }
//
//                //DATA JSON字符串
//                String dataJsonString = JSON.toJSONString(employee);
//
//                //获取人员和设备关联的信息
//                String doorId = doorEmployeeMapper.selectByPrimaryKey(employeeId).getDoorId();
//                String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();
//
//                //生成一条人员修改命令
//                DoorCmd doorCmd = new DoorCmd();
//                //协议格式
//                doorCmd.setServerId("");
//                doorCmd.setDeviceId(deviceId);
//                doorCmd.setFileEdition("v1.3");
//                doorCmd.setCommandMode("C");
//                doorCmd.setCommandType("single");
//                doorCmd.setCommandTotal("1");
//                doorCmd.setCommandIndex("1");
//                doorCmd.setSendTime(CalendarUtil.getCurrentTime());
//                doorCmd.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
//                doorCmd.setSuperCmdId("");
//                doorCmd.setSubCmdId("");
//                doorCmd.setAction(action);
//                doorCmd.setActionCode("2001");
//                doorCmd.setData(dataJsonString);
//                doorCmd.setStatus("0");
//
//                System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));
//
////                //发送命令到MQ
////                RabbitMQSender rabbitMQSender = new RabbitMQSender();
////                rabbitMQSender.sendMessage("hello", JSON.toJSONString(doorCmd));
//
//                //储存人员修改命令到命令表里
//                doorCmdMapper.insert(doorCmd);
//
//            }
//
//        }else if (action.equals("DELETE_USER_INFO")){
//
//            //DATA JSON字符串
//            String dataJsonString = JSON.toJSONString(employeeIdCollection);
//
//            //获取人员和设备关联的信息
//            String doorId = doorEmployeeMapper.selectByPrimaryKey(employeeIdCollection.get(0)).getDoorId();
//            String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();
//
//            //生成一条人员删除命令
//            DoorCmd doorCmd = new DoorCmd();
//            //协议格式
//            doorCmd.setServerId("");
//            doorCmd.setDeviceId(deviceId);
//            doorCmd.setFileEdition("v1.3");
//            doorCmd.setCommandMode("C");
//            doorCmd.setCommandType("single");
//            doorCmd.setCommandTotal("1");
//            doorCmd.setCommandIndex("1");
//            doorCmd.setSendTime(CalendarUtil.getCurrentTime());
//            doorCmd.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(), 3));
//            doorCmd.setSuperCmdId("");
//            doorCmd.setSubCmdId("");
//            doorCmd.setAction(action);
//            doorCmd.setActionCode("2002");
//            doorCmd.setData(dataJsonString);
//            doorCmd.setStatus("0");
//
//            System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));
//
//            //储存人员修改命令到命令表里
//            doorCmdMapper.insert(doorCmd);
//
//        }
//    }

    /**
     * 关联门和人员（同一个人传入的数据不一样时执行更新操作）
     * @param doorId
     * @param employeeId
     */
    @Override
    public void relateEmployeeAndDoor(String doorId, String doorName, String employeeId, String employeeName) {

        DoorEmployee doorEmployee = new DoorEmployee();
        doorEmployee.setDoorId(doorId);
        doorEmployee.setDoorName(doorName);
        doorEmployee.setEmployeeId(employeeId);
        doorEmployee.setEmployeeName(employeeName);

        //查询这个人有没有数据
        DoorEmployee doorEmployeeExist = doorEmployeeMapper.selectByPrimaryKey(employeeId);

        if (doorEmployeeExist == null){
            doorEmployeeMapper.insert(doorEmployee);
            System.out.println("人员["+employeeName+"]和门禁["+doorName+"]的关联不存在");
        }else {
            doorEmployeeMapper.updateByPrimaryKey(doorEmployee);
            System.out.println("人员["+employeeName+"]和门禁["+doorName+"]的关联已存在");
        }

    }

    /**
     * 根据公司id查询门列表
     * @param companyId
     */
    @Override
    public List<Door> findDoorIdByCompanyId(String companyId) {

        List<Door> doorIdList = new ArrayList<Door>();

        List<String> deviceIdList = deviceMapper.findDeviceIdByCompanyId(companyId);
        for (String deviceId : deviceIdList) {
            Door door = doorMapper.findDoorIdByDeviceId(deviceId);
            doorIdList.add(door);
        }

        return doorIdList;

    }

    /**
     * 关联人员门禁权限
     * @param employeeId
     * @param dayOfWeek
     * @param isAllDay
     * @param rangeStartTime
     * @param rangeEndTime
     * @param rangeDoorOpenType
     */
    @Override
    public void relateEmployeeAndPermission(String employeeId, String dayOfWeek, String isAllDay, String rangeStartTime, String rangeEndTime, String rangeDoorOpenType) {

        TimeRangeCommonEmployee timeRangeCommonEmployee = new TimeRangeCommonEmployee();

        timeRangeCommonEmployee.setRangeFlagId(FormatUtil.createUuid());
        timeRangeCommonEmployee.setEmployeeId(employeeId);
        timeRangeCommonEmployee.setDayOfWeek(dayOfWeek);
        timeRangeCommonEmployee.setIsAllDay(isAllDay);
        timeRangeCommonEmployee.setRangeStartTime(rangeStartTime);
        timeRangeCommonEmployee.setRangeEndTime(rangeEndTime);
        timeRangeCommonEmployee.setRangeDoorOpenType(rangeDoorOpenType);

//        //查询这个人有没有数据
//        TimeRangeCommonEmployee timeRangeCommonEmployeeExist = timeRangeCommonEmployeeMapper.selectByEmployeeId(timeRangeCommonEmployee);

//        if (timeRangeCommonEmployeeExist == null){
            timeRangeCommonEmployeeMapper.insert(timeRangeCommonEmployee);
//            System.out.println("人员["+employeeId+"]的门禁权限时间区间不存在");
//        }else {
//            timeRangeCommonEmployeeMapper.updateByEmployeeId(timeRangeCommonEmployee);
//            System.out.println("人员["+employeeId+"]和门禁权限时间区间已存在");
//        }

    }

    //人员人脸、指纹、卡号信息上传存储
    @Override
    public void saveEmployeeInputInfo(String employeeInputInfo) {

        //提取数据
        Map<String, String> dataMap = JSONObject.fromObject(employeeInputInfo);
        dataMap.get("userLabel");
        Employee employee = new Employee();
        employee.setEmployeeId(dataMap.get("userId"));
        employee.setEmployeeFace(dataMap.get("userFace"));
        employee.setEmployeeFinger1(dataMap.get("userFinger1"));
        employee.setEmployeeFinger2(dataMap.get("userFinger2"));
        employee.setEmployeeNfc(dataMap.get("userNFC"));

        //保存人员的指纹、人脸、卡号数据
        employeeMapper.updateByPrimaryKeySelective(employee);

    }

    //门禁记录上传存储
    @Override
    public void doorRecordSave(String doorRecordMap) {

        //提取数据
        Map<String, Object> doorRecordMapTemp = (Map<String, Object>)JSONObject.fromObject(doorRecordMap);
        List<Map<String, String>> doorRecordList = (List<Map<String, String>>)doorRecordMapTemp.get("data");

        //遍历门禁记录
        for (Map<String, String> recordMap : doorRecordList) {

            DoorRecord doorRecord = new DoorRecord();

            doorRecord.setDoorPermissionRecordId(recordMap.get("id"));
            doorRecord.setEmployeeId(recordMap.get("userId"));
            doorRecord.setDoorId(doorMapper.findDoorIdByDeviceId(recordMap.get("deviceId")).getDoorId());
            doorRecord.setRecordType(recordMap.get("attType"));
            doorRecord.setUpperState(recordMap.get("uploadFlag"));
            doorRecord.setEmployeeGroupName(recordMap.get("userName"));
            doorRecord.setDeviceGroupName(recordMap.get("deviceName"));
            doorRecord.setRealWeek(recordMap.get("week"));
            doorRecord.setEventResult(recordMap.get("outcome"));
            doorRecord.setEventResultReason(recordMap.get("cause"));
            doorRecord.setRecordDate(recordMap.get("attTime"));

            //查找记录是否重复上传
            DoorRecord doorRecordExit = doorRecordMapper.selectByPrimaryKey(recordMap.get("id"));

            if (doorRecordExit == null){
                //保存门禁记录到数据库
                doorRecordMapper.insertSelective(doorRecord);
            }else {
                //更新门禁记录到数据库
                doorRecordMapper.updateByPrimaryKeySelective(doorRecord);
            }
        }

        //回复门禁记录上传
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        List<Object> objList = new ArrayList<Object>();
        resultData.put("resultCode", "0");
        resultData.put("resultMessage", "执行成功");
        for (Map<String, String> recordMap : doorRecordList) {
            Map<String, Object> objMap = new LinkedHashMap<String, Object>();
            objMap.put("id", recordMap.get("id"));
            objList.add(objMap);
        }
        resultData.put("returnObj", objList);
        resultMap.put("result", resultData);


        //构造命令格式
        DoorCmd doorCmdRecord = new DoorCmd();
        doorCmdRecord.setServerId("001");
        doorCmdRecord.setDeviceId(doorRecordList.get(0).get("deviceId"));
        doorCmdRecord.setFileEdition("v1.3");
        doorCmdRecord.setCommandMode("R");
        doorCmdRecord.setCommandType("single");
        doorCmdRecord.setCommandTotal("1");
        doorCmdRecord.setCommandIndex("1");
        doorCmdRecord.setSuperCmdId("");
        doorCmdRecord.setAction("UPLOAD_ACCESS_RECORD");
        doorCmdRecord.setActionCode("3006");
        doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdRecord.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdRecord.setSubCmdId(FormatUtil.createUuid());
        doorCmdRecord.setData(JSON.toJSONString(resultMap));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorRecordAll =  rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
        //命令状态设置为: 发送中
        doorCmdRecord.setStatus("1");
        //设置md5校验值
        doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdRecord);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage("hello", doorRecordAll);
    }

    public static void main(String[] args) {
        String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", "13DFF865799A42C785F33AAFDC2FDD2D");
        System.out.println("[*] send: 已发出请求");
        System.out.println(employeeInfo);
    }
}
