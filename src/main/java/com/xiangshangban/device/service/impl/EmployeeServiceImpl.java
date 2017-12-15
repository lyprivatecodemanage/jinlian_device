package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.IEntranceGuardService;
import com.xiangshangban.device.service.OSSFileService;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

/**d
 * date: 2017/10/19 10:38
 * describe: TODO 用户管理实现类
 */

@Service
public class EmployeeServiceImpl implements IEmployeeService {

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    @Value("${employee.interface.address}")
    String employeeInterfaceAddress;

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

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

    @Autowired
    private DoorEmployeePermissionMapper doorEmployeePermissionMapper;

    @Autowired
    OSSFileService oSSFileService;

    @Autowired
    DeviceUpdatePackSysMapper deviceUpdatePackSysMapper;

    //人员模块人员信息同步
    @Override
    public void employeeCommandGenerate(List<Map<String, Object>> employeeInfoCollection) {

//        if (action.equals("UPDATE_USER_INFO")){

            for (Map<String, Object> employeeInfoMap : employeeInfoCollection) {

                String employeeId = (String) employeeInfoMap.get("employeeId");
                String employeeNo = (String) employeeInfoMap.get("employeeNo");
                String employeeName = (String) employeeInfoMap.get("employeeName");
                String departmentId = (String) employeeInfoMap.get("departmentId");
                String departmentName = (String) employeeInfoMap.get("departmentName");
                String entryTime = (String) employeeInfoMap.get("entryTime");
                String probationaryExpired = (String) employeeInfoMap.get("probationaryExpired");
                String employeePhone = (String) employeeInfoMap.get("loginName");
                String employeeStatus = (String) employeeInfoMap.get("employeeStatus");
                String companyId = (String) employeeInfoMap.get("companyId");
                String companyName = (String) employeeInfoMap.get("companyName");
                String companyNo = (String) employeeInfoMap.get("companyNo");

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
                employee.setCompanyNo(companyNo);

                //查询人员信息是否存在
                Employee employeeExit = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, companyId);
//                Employee employeeExit = employeeMapper.selectByPrimaryKey(employeeId);
                if (employeeExit == null){
                    employeeMapper.insertSelective(employee);
                }else {
                    employeeMapper.updateByPrimaryKeySelective(employee);
                }

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
//                doorCmd.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//                doorCmd.setSuperCmdId("");
//                doorCmd.setSubCmdId("");
//                doorCmd.setAction(action);
//                doorCmd.setActionCode("2001");
//                doorCmd.setData(dataJsonString);
//                doorCmd.setStatus("0");
//                doorCmd.setEmployeeId(employeeId);
//
//                System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));

//                //发送命令到MQ
//                RabbitMQSender rabbitMQSender = new RabbitMQSender();
//                rabbitMQSender.sendMessage(downloadQueueName, JSON.toJSONString(doorCmd));

                //储存人员修改命令到命令表里
//                doorCmdMapper.insert(doorCmd);

            }

//        }else if (action.equals("DELETE_USER_INFO")){

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
//            doorCmd.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//            doorCmd.setSuperCmdId("");
//            doorCmd.setSubCmdId("");
//            doorCmd.setAction(action);
//            doorCmd.setActionCode("2002");
//            doorCmd.setData(dataJsonString);
//            doorCmd.setStatus("0");
//            doorCmd.setEmployeeId(employeeId);
//
//            System.out.println("[*] CMD: " + JSON.toJSONString(doorCmd));
//
//            //储存人员修改命令到命令表里
//            doorCmdMapper.insert(doorCmd);

//        }
    }

    /**
     * 关联门和人员
     * @param doorId
     * @param employeeId
     */
    @Override
    public void relateEmployeeAndDoor(String doorId, String doorName, String employeeId, String employeeName, String rangeFlagId) {

        DoorEmployee doorEmployee = new DoorEmployee();
        doorEmployee.setDoorId(doorId);
        doorEmployee.setDoorName(doorName);
        doorEmployee.setEmployeeId(employeeId);
        doorEmployee.setEmployeeName(employeeName);
        doorEmployee.setRangeFlagId(rangeFlagId);

        //查询这个人有没有数据
        DoorEmployee doorEmployeeExist = doorEmployeeMapper.selectByEmployeeIdAndDoorId(employeeId, doorId);

        //一个人可以在多个门上有开门权限
        if (doorEmployeeExist != null) {
            doorEmployeeMapper.deleteByRangeFlagId(doorEmployeeExist.getRangeFlagId());
        }

        doorEmployeeMapper.insert(doorEmployee);

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
    public void relateEmployeeAndPermission(String rangeFlagId, String employeeId, String dayOfWeek,
                                            String isAllDay, String rangeStartTime, String rangeEndTime,
                                            String rangeDoorOpenType, String isDitto) {

        TimeRangeCommonEmployee timeRangeCommonEmployee = new TimeRangeCommonEmployee();

        timeRangeCommonEmployee.setRangeFlagId(rangeFlagId);
        timeRangeCommonEmployee.setEmployeeId(employeeId);
        timeRangeCommonEmployee.setDayOfWeek(dayOfWeek);
        timeRangeCommonEmployee.setIsAllDay(isAllDay);
        timeRangeCommonEmployee.setRangeStartTime(rangeStartTime);
        timeRangeCommonEmployee.setRangeEndTime(rangeEndTime);
        timeRangeCommonEmployee.setRangeDoorOpenType(rangeDoorOpenType);
        timeRangeCommonEmployee.setIsDitto(isDitto);

        timeRangeCommonEmployeeMapper.insert(timeRangeCommonEmployee);

    }

    //人员人脸、指纹、卡号信息上传存储
    @Override
    public Map<String, Object> saveEmployeeInputInfo(String jsonString, String deviceId, String style, String companyId) {

        //提取数据
        Map<String, Object> allMap = JSONObject.fromObject(jsonString);
        Map<String, Object> dataMap = (Map<String, Object>) allMap.get("data");
        Map<String, Object> userLabelMap = (Map<String, Object>) dataMap.get("userLabel");
        String employeeId = (String) userLabelMap.get("userId");
        String resultCode;
        String resultMessage;

        Employee employee = new Employee();
        employee.setEmployeeId(employeeId);
        employee.setEmployeeCompanyId(companyId);
        if ("1".equals(style)){
            try {
                employee.setEmployeeFace(((Map<String, String>) userLabelMap.get("userFace")).get("faceData"));
            }catch (Exception e){
                employee.setEmployeeFace("");
            }
        }
        if (StringUtils.isNotEmpty((String) userLabelMap.get("userFinger1"))){
            employee.setEmployeeFinger1((String) userLabelMap.get("userFinger1"));
        }
        if (StringUtils.isNotEmpty((String) userLabelMap.get("userFinger1"))){
            employee.setEmployeeFinger2((String) userLabelMap.get("userFinger2"));
        }
        if (StringUtils.isNotEmpty((String) userLabelMap.get("userNFC"))){
            employee.setEmployeeNfc((String) userLabelMap.get("userNFC"));
        }

        //回复人员人脸、指纹、卡号信息上传
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();

        //查看人员是否存在
        Employee employeeExist = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, companyId);

        if (employeeExist != null){
            //更新人员的指纹、人脸、卡号数据
            employeeMapper.updateByEmployeeIdAndCompanyIdSelective(employee);

            resultCode = "0";
            resultMessage = "执行成功";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", employeeId);
            resultMap.put("result", resultData);
            System.out.println("人员指纹、人脸信息上传成功");
        }else {
            resultCode = "999";
            resultMessage = "该人员信息不存在";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", "");
            resultMap.put("result", resultData);

            System.out.println("服务器没有设备端存在的该人员的信息");
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

//        //上传的是人脸信息时，同步该人员的人脸、指纹信息到其它有权限的设备上
//        if ("1".equals(style)){
//            //1.同步该人员的人脸、指纹信息到人员表（一个人对多个公司）
//            Employee employeeTemp = new Employee();
//            employeeTemp.setEmployeeId(employeeId);
//            employeeTemp.setEmployeeFace(((Map<String, String>) userLabelMap.get("userFace")).get("faceData"));
//            if (StringUtils.isNotEmpty((String) userLabelMap.get("userFinger1"))){
//                employeeTemp.setEmployeeFinger1((String) userLabelMap.get("userFinger1"));
//            }
//            if (StringUtils.isNotEmpty((String) userLabelMap.get("userFinger1"))){
//                employeeTemp.setEmployeeFinger2((String) userLabelMap.get("userFinger2"));
//            }
//            employeeMapper.updateByPrimaryKeySelective(employeeTemp);
//            //2.同步该人员的人脸、指纹信息到其它有权限的设备上
//            List<Map> deviceList = deviceMapper.selectAllDevice(companyId);
//            for (Map<String, String> map : deviceList) {
//                map.get("");
//            }
//
//        }
//
        System.out.println("人员指纹、人脸信息上传已回复");
        return doorRecordAll;

    }

    /**
     * 删除设备上的人员的所有关联信息（设备模块调用）
     * @param employeeIdCollection
     * @return
     */
    @Override
    public ReturnData deleteEmployeeInformationDev(String employeeIdCollection, String operatorEmployeeId) {

        Map<String, Object> employeeIdMap = (Map<String, Object>)JSONObject.fromObject(employeeIdCollection);
        List<String> employeeIdList = (List<String>)employeeIdMap.get("employeeIdList");
        String doorId = (String)employeeIdMap.get("doorId");

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String deviceId = "";
        Door door = doorMapper.selectByPrimaryKey(doorId);
        if (door != null){
            try {
                deviceId = door.getDeviceId();

                //构造删除人员的命令格式
                DoorCmd doorCmdDeleteEmployee = new DoorCmd();
                doorCmdDeleteEmployee.setServerId("001");
                doorCmdDeleteEmployee.setDeviceId(deviceId);
                doorCmdDeleteEmployee.setFileEdition("v1.3");
                doorCmdDeleteEmployee.setCommandMode("C");
                doorCmdDeleteEmployee.setCommandType("single");
                doorCmdDeleteEmployee.setCommandTotal("1");
                doorCmdDeleteEmployee.setCommandIndex("1");
                doorCmdDeleteEmployee.setSubCmdId("");
                doorCmdDeleteEmployee.setAction("DELETE_USER_INFO");
                doorCmdDeleteEmployee.setActionCode("2002");
                doorCmdDeleteEmployee.setOperateEmployeeId(operatorEmployeeId);

                //删除每个人都要产生一条命令
                for (String employeeId : employeeIdList){

                    List<String> employeeIdListTemp = new ArrayList<String>();
                    employeeIdListTemp.add(employeeId);

                    doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
                    doorCmdDeleteEmployee.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
                    doorCmdDeleteEmployee.setSuperCmdId(FormatUtil.createUuid());
                    doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdListTemp));
                    doorCmdDeleteEmployee.setEmployeeId(employeeId);

                    //获取完整的数据加协议封装格式
                    RabbitMQSender rabbitMQSender = new RabbitMQSender();
                    Map<String, Object> userDeleteInformation =  RabbitMQSender.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList, "C");
                    //命令状态设置为: 发送中
                    doorCmdDeleteEmployee.setStatus("1");
                    //设置md5校验值
                    doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
                    //设置数据库的data字段
                    doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
                    //命令数据存入数据库
                    entranceGuardService.insertCommand(doorCmdDeleteEmployee);
                    //立即下发数据到MQ
                    rabbitMQSender.sendMessage(deviceId, userDeleteInformation);
                }

                returnData.setMessage("已执行删除设备上人员权限的操作");
                returnData.setReturnCode("4202");
                return returnData;

            }catch (Exception e){
                e.printStackTrace();
                returnData.setMessage("服务器错误");
                returnData.setReturnCode("3001");
                return returnData;
            }
        }else {
            returnData.setMessage("没有找到和此门关联的设备");
            returnData.setReturnCode("4007");
            return returnData;
        }
    }

    /**
     * 删除设备上的人员的所有关联信息（人员模块调用）
     * @param employeeIdCollection
     * @return
     */
    @Override
    public ReturnData deleteEmployeeInformationEmp(String employeeIdCollection, String operatorEmployeeId) {

        //提取数据
        Map<String, Object> employeeIdMap = (Map<String, Object>)JSONObject.fromObject(employeeIdCollection);
        List<String> employeeIdList = (List<String>)employeeIdMap.get("employeeIdList");
        String companyId = (String) employeeIdMap.get("companyId");

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        //查找该公司下的所有设备
        List<String> deviceIdList = deviceMapper.findDeviceIdByCompanyId(companyId);

        if (deviceIdList.size() > 0){
            for (String deviceId : deviceIdList) {
                try {
                    //构造删除人员的命令格式
                    DoorCmd doorCmdDeleteEmployee = new DoorCmd();
                    doorCmdDeleteEmployee.setServerId("001");
                    doorCmdDeleteEmployee.setDeviceId(deviceId);
                    doorCmdDeleteEmployee.setFileEdition("v1.3");
                    doorCmdDeleteEmployee.setCommandMode("C");
                    doorCmdDeleteEmployee.setCommandType("single");
                    doorCmdDeleteEmployee.setCommandTotal("1");
                    doorCmdDeleteEmployee.setCommandIndex("1");
                    doorCmdDeleteEmployee.setSubCmdId("");
                    doorCmdDeleteEmployee.setAction("DELETE_USER_INFO");
                    doorCmdDeleteEmployee.setActionCode("2002");
                    doorCmdDeleteEmployee.setOperateEmployeeId(operatorEmployeeId);

                    //删除每个人都要产生一条命令
                    for (String employeeId : employeeIdList) {

                        List<String> employeeIdListTemp = new ArrayList<String>();
                        employeeIdListTemp.add(employeeId);

                        doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
                        doorCmdDeleteEmployee.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
                        doorCmdDeleteEmployee.setSuperCmdId(FormatUtil.createUuid());
                        doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdList));

                        //获取完整的数据加协议封装格式
                        RabbitMQSender rabbitMQSender = new RabbitMQSender();
                        Map<String, Object> userDeleteInformation = RabbitMQSender.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList, "C");
                        //命令状态设置为: 发送中
                        doorCmdDeleteEmployee.setStatus("1");
                        //设置md5校验值
                        doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
                        //设置数据库的data字段
                        doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
                        //命令数据存入数据库
                        entranceGuardService.insertCommand(doorCmdDeleteEmployee);
                        //立即下发数据到MQ
                        rabbitMQSender.sendMessage(deviceId, userDeleteInformation);

                    }
                }catch (Exception e){
                    e.printStackTrace();
                    returnData.setMessage("服务器错误");
                    returnData.setReturnCode("3001");
                    return returnData;
                }
            }

            returnData.setMessage("已执行删除这些人员，在本公司的所有设备上的信息及开门权限");
            returnData.setReturnCode("3000");
            return returnData;

        }else {
            returnData.setMessage("您的公司没有绑定任何设备，删除人员的操作将不会下发到设备");
            returnData.setReturnCode("4007");
            return returnData;
        }
    }

    @Override
    public void multipleHandOutEmployeePermission(String doorId) {

        String deviceId;
        Set<String> hashSet = new HashSet<String>();
        List<String> employeeIdList;
        List<DoorCmd> doorCmdLatestList = new ArrayList<DoorCmd>();

        Door door = doorMapper.selectByPrimaryKey(doorId);
        if (door != null) {
            deviceId = door.getDeviceId();
            //查找当前设备存为草稿的所有人员信息和人员开门权限的下发命令
            List<DoorCmd> doorCmdList = doorCmdMapper.selectEmployeeDraftByDeviceId(deviceId);
            for (DoorCmd doorCmd : doorCmdList) {
                String employeeId = doorCmd.getEmployeeId();
                //人员id去重复
                hashSet.add(employeeId);
            }
//            System.out.println(JSON.toJSONString(doorCmdList));
//            System.out.println(JSON.toJSONString(hashSet));

            employeeIdList = new ArrayList<String>(hashSet);
            for (String employeeId : employeeIdList) {
                //取出每个人最新的两条不同类型的草稿命令存在List里
//                System.out.println(employeeId);
                List<DoorCmd> doorCmdListUserInfo = doorCmdMapper.selectCmdByEmployeeIdSendTimeDesc(employeeId, "UPDATE_USER_INFO");
                List<DoorCmd> doorCmdListUserAccessControlInfo = doorCmdMapper.selectCmdByEmployeeIdSendTimeDesc(employeeId, "UPDATE_USER_ACCESS_CONTROL");
                if (doorCmdListUserInfo.size() > 0) {
                    DoorCmd doorCmdLatestUser = doorCmdListUserInfo.get(0);
                    doorCmdLatestList.add(doorCmdLatestUser);
                }

                if (doorCmdListUserAccessControlInfo.size() > 0) {
                    DoorCmd doorCmdLatestAccessControl = doorCmdListUserAccessControlInfo.get(0);
                    doorCmdLatestList.add(doorCmdLatestAccessControl);
                }
            }

//            System.out.println(JSON.toJSONString(doorCmdLatestList));

            if (doorCmdLatestList.size() > 0) {
                //遍历所有最新的草稿命令批量下发
                for (DoorCmd doorCmd : doorCmdLatestList) {
                    //获取完整的数据加协议封装格式
                    RabbitMQSender rabbitMQSender = new RabbitMQSender();
                    //命令状态设置为: 发送中
                    doorCmd.setStatus("1");
                    //更新草稿命令由待发送状态变成发送中状态
                    doorCmdMapper.updateByPrimaryKey(doorCmd);
                    //立即下发数据到MQ
                    rabbitMQSender.sendMessage(deviceId, JSON.toJSONString(doorCmd));
//                    System.out.println(JSON.toJSONString(doorCmd));
                }
            } else {
                System.out.println("没有可以批量下发的人员");
            }
        }
    }

    //根据人员Id查找人的信息
    @Override
    public Employee findEmployeeById(String empId) {
        Employee employee = employeeMapper.selectByPrimaryKey(empId);
        return employee;

    }

    //同步不同设备上，有开门权限的人员的权限信息
    @Override
    public void synchronizeEmployeePermissionForDevices(String employeeId) {

        //查询该人员的有效时间是否过期
        DoorEmployeePermission doorEmployeePermission = doorEmployeePermissionMapper.selectByPrimaryKey(employeeId);

        if (doorEmployeePermission != null){

        }

        //从本地查人员信息
        Employee employeeLocal = employeeMapper.selectByPrimaryKey(employeeId);

        if (employeeLocal == null){
            System.out.println("人员信息不同步，未查到【"+employeeLocal.getEmployeeName()+"】的信息");
        }else {
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

            String userFinger1 = employeeLocal.getEmployeeFinger1();
            String userFinger2 = employeeLocal.getEmployeeFinger2();
            String userFace = employeeLocal.getEmployeeFace();
            String userNFC = employeeLocal.getEmployeeNfc();

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
            userInformation.put("userFinger1", userFinger1);
            userInformation.put("userFinger2", userFinger2);
            userInformation.put("userFace", userFace);
            userInformation.put("userPhone", employeePhone);
            userInformation.put("userNFC", userNFC);
            userInformation.put("bluetoothId", blueboothId);

            //查询该人员在哪些设备上有开门权限


            //下发人员基本信息
            //构造命令格式
            DoorCmd doorCmdEmployeeInformation = new DoorCmd();
            doorCmdEmployeeInformation.setServerId("001");
//            doorCmdEmployeeInformation.setDeviceId(deviceId);
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
//            doorCmdEmployeePermission.setDeviceId(deviceId);
            doorCmdEmployeePermission.setFileEdition("v1.3");
            doorCmdEmployeePermission.setCommandMode("C");
            doorCmdEmployeePermission.setCommandType("single");
            doorCmdEmployeePermission.setCommandTotal("1");
            doorCmdEmployeePermission.setCommandIndex("1");
            doorCmdEmployeePermission.setSubCmdId("");
            doorCmdEmployeePermission.setAction("UPDATE_USER_ACCESS_CONTROL");
            doorCmdEmployeePermission.setActionCode("3001");
        }

    }

    @Override
    public String deviceUploadPackage(String versionCode, MultipartFile uploadResource,
                                      String fileType, String employeeId) throws IOException {
        /**
         * fileType：facePhoto时为人脸图片上传，为任意其它字符串时为系统升级包上传
         */
        //验证参数的完整性
        Map result = new HashMap();
        if((versionCode==null || versionCode.isEmpty()) ||(uploadResource==null || uploadResource.isEmpty())){
            //参数异常
            result = ReturnCodeUtil.addReturnCode(1);
        }else{

            String funcDirectory = "";
            //判断文件类型
            if ("facePhoto".equals(fileType)){
                funcDirectory = "FacePhotoLibrary/"+employeeId;
            }else {
                //设置上传文件保存的路径
                funcDirectory = "device/update/system/"+versionCode;
            }

            //上传
            String filePath = oSSFileService.devicePackageUpload(funcDirectory,uploadResource,fileType);
            if(filePath.trim().equals("false")){
                //上传失败
                result = ReturnCodeUtil.addReturnCode(Boolean.valueOf(filePath.trim()),"上传升级包（应用包）失败");
            }else{
                if (!"facePhoto".equals(fileType)){
                    //上传成功，保存信息到本地
                    DeviceUpdatePackSys deviceUpdatePackSys = new DeviceUpdatePackSys();
                    deviceUpdatePackSys.setNewSysVerion(versionCode);
                    deviceUpdatePackSys.setPath(filePath.trim());
                    deviceUpdatePackSys.setCreateTime(DateUtils.getDateTime());

                    //根据路径查询当前本地数据库中是否已经存在该资源
                    String status = deviceUpdatePackSysMapper.verifyWhetherExistsResource(filePath.trim());
                    if(status==null || "".equals(status)){
                        //添加新的数据
                        int insertResult = deviceUpdatePackSysMapper.insert(deviceUpdatePackSys);

                        if(insertResult>0){
                            result = ReturnCodeUtil.addReturnCode(true,"上传文件成功，并保存信息至本地数据库");
                        }else{
                            result = ReturnCodeUtil.addReturnCode(false,"上传文件成功，保存至本地数据库失败");
                        }
                    }else{
                        //根据路径更新操作时间
                        Map map = new HashMap();
                        map.put("createTime",DateUtils.getDateTime());
                        map.put("path",filePath.trim());
                        deviceUpdatePackSysMapper.updateOperateTime(map);
                        result = ReturnCodeUtil.addReturnCode(true,"上传文件成功，并保存信息至本地数据库");
                    }
                }
            }
        }
        return com.alibaba.fastjson.JSONObject.toJSONString(result);
    }

    public static void main(String[] args) {
        String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", "13DFF865799A42C785F33AAFDC2FDD2D");
        System.out.println("[*] send: 已发出请求");
        System.out.println(employeeInfo);
    }
}
