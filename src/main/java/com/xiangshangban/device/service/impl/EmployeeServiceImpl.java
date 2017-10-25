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
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private TimeRangeCommonEmployeeMapper timeRangeCommonEmployeeMapper;

    //人员模块命令生成器（暂未使用）
    @Override
    public void employeeCommandGenerate(String action, List<String> employeeIdCollection) {

        if (action.equals("UPDATE_USER_INFO")){

            for (String employeeId : employeeIdCollection) {

                //根据人员id请求单个人员信息
                String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", employeeId);
                System.out.println("[*] send: 已发出请求");
                System.out.println("[*] employeeInfo: " + employeeInfo);

                //取出需要的人员信息
                Employee employee = new Employee();
                Map<String, String> employeeInfoMap = (Map<String, String>)JSONObject.fromObject(employeeInfo);
                employee.setEmployeeId(employeeInfoMap.get("employeeId"));
                employee.setEmployeeNumber(employeeInfoMap.get("employeeNo"));
                employee.setEmployeeName(employeeInfoMap.get("employeeName"));
                employee.setEmployeeDepartmentId(employeeInfoMap.get("departmentId"));
                employee.setEmployeeDepartmentName(employeeInfoMap.get("departmentName"));
                employee.setEmployeeBirthday("");
                employee.setEmployeeEntryTime(employeeInfoMap.get("entryTime"));
                employee.setEmployeeProbationaryExpired(employeeInfoMap.get("probationaryExpired"));
                employee.setEmployeeContractExpired("");
                employee.setAdminFlag("");
                employee.setEmployeePhone(employeeInfoMap.get("employeePhone"));

                //DATA JSON字符串
                String dataJsonString = JSON.toJSONString(employee);

                //获取人员和设备关联的信息
                String doorId = doorEmployeeMapper.selectByPrimaryKey(employeeId).getDoorId();
                String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();

                //生成一条人员修改命令
                DoorCmd doorCmd = new DoorCmd();
                //协议格式
                doorCmd.setServerId("");
                doorCmd.setDeviceId(deviceId);
                doorCmd.setFileEdition("v1.3");
                doorCmd.setCommandMode("C");
                doorCmd.setCommandType("single");
                doorCmd.setCommandTotal("1");
                doorCmd.setCommandIndex("1");
                doorCmd.setSendTime(CalendarUtil.getCurrentTime());
                doorCmd.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
                doorCmd.setSuperCmdId("");
                doorCmd.setSubCmdId("");
                doorCmd.setAction(action);
                doorCmd.setActionCode("2001");
                doorCmd.setData(dataJsonString);
                doorCmd.setStatus("0");

                System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));

//                //发送命令到MQ
//                RabbitMQSender rabbitMQSender = new RabbitMQSender();
//                rabbitMQSender.sendMessage("hello", JSON.toJSONString(doorCmd));

                //储存人员修改命令到命令表里
                doorCmdMapper.insert(doorCmd);

            }

        }else if (action.equals("DELETE_USER_INFO")){

            //DATA JSON字符串
            String dataJsonString = JSON.toJSONString(employeeIdCollection);

            //获取人员和设备关联的信息
            String doorId = doorEmployeeMapper.selectByPrimaryKey(employeeIdCollection.get(0)).getDoorId();
            String deviceId = doorMapper.selectByPrimaryKey(doorId).getDeviceId();

            //生成一条人员删除命令
            DoorCmd doorCmd = new DoorCmd();
            //协议格式
            doorCmd.setServerId("");
            doorCmd.setDeviceId(deviceId);
            doorCmd.setFileEdition("v1.3");
            doorCmd.setCommandMode("C");
            doorCmd.setCommandType("single");
            doorCmd.setCommandTotal("1");
            doorCmd.setCommandIndex("1");
            doorCmd.setSendTime(CalendarUtil.getCurrentTime());
            doorCmd.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(), 3));
            doorCmd.setSuperCmdId("");
            doorCmd.setSubCmdId("");
            doorCmd.setAction(action);
            doorCmd.setActionCode("2002");
            doorCmd.setData(dataJsonString);
            doorCmd.setStatus("0");

            System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));

            //储存人员修改命令到命令表里
            doorCmdMapper.insert(doorCmd);

        }
    }

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

    /**
     * 添加or更新命令表
     * @param doorCmd
     */
    @Override
    public void insertEmployeeCommand(DoorCmd doorCmd) {

        DoorCmd doorCmdExist = doorCmdMapper.selectBySubCmdId(doorCmd);

//        if (doorCmdExist == null){
            doorCmdMapper.insert(doorCmd);
//            System.out.println("命令["+doorCmd.getSubCmdId()+"]的信息不存在");
//        }else {
//            doorCmdMapper.updateBySubCmdId(doorCmd);
//            System.out.println("命令["+doorCmd.getSubCmdId()+"]的信息已存在");
//        }

    }

    public static void main(String[] args) {
        String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", "13DFF865799A42C785F33AAFDC2FDD2D");
        System.out.println("[*] send: 已发出请求");
        System.out.println(employeeInfo);
    }
}
