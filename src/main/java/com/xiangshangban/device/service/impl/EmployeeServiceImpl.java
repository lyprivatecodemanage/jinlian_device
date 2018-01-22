package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.command.CmdUtil;
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

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.*;

/**d
 * date: 2017/10/19 10:38
 * describe: TODO 用户管理实现类
 */

@Service
public class EmployeeServiceImpl implements IEmployeeService {

    @Value("${employee.interface.address}")
    String employeeInterfaceAddress;

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Value("${serverId}")
    String serverId;

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
    private OSSFileService oSSFileService;

    @Autowired
    private DeviceUpdatePackSysMapper deviceUpdatePackSysMapper;

    @Autowired
    private EmployeeBluetoothCountMapper employeeBluetoothCountMapper;

    @Autowired
    private IEmployeeService iEmployeeService;

    @Autowired
    private DoorSettingMapper doorSettingMapper;

    @Autowired
    private CmdUtil cmdUtil;

    //人员模块人员信息同步
    @Override
    public void employeeCommandGenerate(List<Map<String, Object>> employeeInfoCollection) {

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
            if (employeeExit == null) {
                employeeMapper.insertSelective(employee);
            } else {
                employeeMapper.updateByEmployeeIdAndCompanyIdSelective(employee);
            }
        }
    }

    /**
     * 关联门和人员
     * @param doorId
     * @param employeeId
     */
    @Override
    public void relateEmployeeAndDoor(String doorId, String doorName, String employeeId, String employeeName, String rangeFlagId, String deviceId) {

        DoorEmployee doorEmployee = new DoorEmployee();
        doorEmployee.setDoorId(doorId);
        doorEmployee.setDoorName(doorName);
        doorEmployee.setEmployeeId(employeeId);
        doorEmployee.setEmployeeName(employeeName);
        doorEmployee.setRangeFlagId(rangeFlagId);
        doorEmployee.setDeviceId(deviceId);
        doorEmployee.setCreateDate(DateUtils.getDateTime());

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
                                            String rangeDoorOpenType, String isDitto, String deviceId) {

        TimeRangeCommonEmployee timeRangeCommonEmployee = new TimeRangeCommonEmployee();

        timeRangeCommonEmployee.setRangeFlagId(rangeFlagId);
        timeRangeCommonEmployee.setEmployeeId(employeeId);
        timeRangeCommonEmployee.setDayOfWeek(dayOfWeek);
        timeRangeCommonEmployee.setIsAllDay(isAllDay);
        timeRangeCommonEmployee.setRangeStartTime(rangeStartTime);
        timeRangeCommonEmployee.setRangeEndTime(rangeEndTime);
        timeRangeCommonEmployee.setRangeDoorOpenType(rangeDoorOpenType);
        timeRangeCommonEmployee.setIsDitto(isDitto);
        timeRangeCommonEmployee.setDeviceId(deviceId);

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
            System.out.println("人脸信息: " + JSON.toJSONString(userLabelMap.get("userFace")));

            try {
                if (StringUtils.isEmpty((String) userLabelMap.get("userFace"))) {
                    System.out.println("人脸信息为空1");
                    employee.setEmployeeFace((String) userLabelMap.get("userFace"));
                }
            }catch (Exception e){
//                System.out.println("人脸信息不为空，值="+JSON.toJSONString(userLabelMap.get("userFace")));
                employee.setEmployeeFace(JSON.toJSONString(userLabelMap.get("userFace")));
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
//        System.out.println("employeeId: "+employeeId+"     companyId: "+companyId);

        //查看人员是否存在
        Employee employeeExist = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, companyId);

        if (employeeExist != null){
//            System.out.println("employee: "+JSON.toJSONString(employee));
            //更新人员的指纹、人脸、卡号数据
            employeeMapper.updateByEmployeeIdAndCompanyIdSelective(employee);

            resultCode = "0";
            resultMessage = "执行成功";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", employeeId);
            resultMap.put("result", resultData);
//            System.out.println("人员指纹、人脸信息上传成功");

            //同步人脸信息到其它设备
//        if ("1".equals(style)){
            synchronizeEmployeePermissionForDevices(jsonString, employeeId);
//        }

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

//        System.out.println("doorRecordAll = "+JSON.toJSONString(doorRecordAll));
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

//                //构造删除人员的命令格式
//                DoorCmd doorCmdDeleteEmployee = new DoorCmd();
//                doorCmdDeleteEmployee.setServerId(serverId);
//                doorCmdDeleteEmployee.setDeviceId(deviceId);
//                doorCmdDeleteEmployee.setFileEdition("v1.3");
//                doorCmdDeleteEmployee.setCommandMode("C");
//                doorCmdDeleteEmployee.setCommandType("single");
//                doorCmdDeleteEmployee.setCommandTotal("1");
//                doorCmdDeleteEmployee.setCommandIndex("1");
//                doorCmdDeleteEmployee.setSubCmdId("");
//                doorCmdDeleteEmployee.setAction("DELETE_USER_INFO");
//                doorCmdDeleteEmployee.setActionCode("2002");
//                doorCmdDeleteEmployee.setOperateEmployeeId(operatorEmployeeId);

                //删除每个人都要产生一条命令
                for (String employeeId : employeeIdList){
                    //集合里只存一个人
                    List<String> employeeIdListTemp = new ArrayList<String>();
                    employeeIdListTemp.add(employeeId);

//                    doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
//                    doorCmdDeleteEmployee.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//                    doorCmdDeleteEmployee.setSuperCmdId(FormatUtil.createUuid());
//                    doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdListTemp));
//                    doorCmdDeleteEmployee.setEmployeeId(employeeId);
//
//                    //获取完整的数据加协议封装格式
//                    RabbitMQSender rabbitMQSender = new RabbitMQSender();
//                    Map<String, Object> userDeleteInformation =  CmdUtil.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList, "C");
//                    //命令状态设置为: 发送中
//                    doorCmdDeleteEmployee.setStatus("1");
//                    //设置md5校验值
//                    doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
//                    //设置数据库的data字段
//                    doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
//                    //命令数据存入数据库
//                    entranceGuardService.insertCommand(doorCmdDeleteEmployee);
//                    //立即下发数据到MQ
//                    rabbitMQSender.sendMessage(deviceId, userDeleteInformation);

                    //下发人员删除命令，包括删除权限
                    String sendTime = DateUtils.getDateTime();
                    cmdUtil.handOutCmd(deviceId, "C", "DELETE_USER_INFO", "2002", operatorEmployeeId,
                            "employeeIdList", employeeIdListTemp, "1", "", "", employeeId, "", sendTime);
                }

                returnData.setMessage("已执行删除设备上人员权限的操作");
                returnData.setReturnCode("3000");
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
        Map<String, Object> employeeIdMap = (Map<String, Object>) ((List)JSON.parseArray(employeeIdCollection)).get(0);
        List<String> employeeIdList = (List<String>)employeeIdMap.get("employeeIdList");
        String companyId = (String) employeeIdMap.get("companyId");

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        //查找该公司下的所有设备
        List<String> deviceIdList = deviceMapper.findDeviceIdByCompanyId(companyId);

        if (deviceIdList.size() > 0){
            for (String deviceId : deviceIdList) {
                try {
//                    //构造删除人员的命令格式
//                    DoorCmd doorCmdDeleteEmployee = new DoorCmd();
//                    doorCmdDeleteEmployee.setServerId(serverId);
//                    doorCmdDeleteEmployee.setDeviceId(deviceId);
//                    doorCmdDeleteEmployee.setFileEdition("v1.3");
//                    doorCmdDeleteEmployee.setCommandMode("C");
//                    doorCmdDeleteEmployee.setCommandType("single");
//                    doorCmdDeleteEmployee.setCommandTotal("1");
//                    doorCmdDeleteEmployee.setCommandIndex("1");
//                    doorCmdDeleteEmployee.setSubCmdId("");
//                    doorCmdDeleteEmployee.setAction("DELETE_USER_INFO");
//                    doorCmdDeleteEmployee.setActionCode("2002");
//                    doorCmdDeleteEmployee.setOperateEmployeeId(operatorEmployeeId);

                    //删除每个人都要产生一条命令
                    for (String employeeId : employeeIdList) {
                        //集合里只存一个人
                        List<String> employeeIdListTemp = new ArrayList<String>();
                        employeeIdListTemp.add(employeeId);

//                        doorCmdDeleteEmployee.setSendTime(CalendarUtil.getCurrentTime());
//                        doorCmdDeleteEmployee.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
//                        doorCmdDeleteEmployee.setSuperCmdId(FormatUtil.createUuid());
//                        doorCmdDeleteEmployee.setData(JSON.toJSONString(employeeIdList));
//
//                        //获取完整的数据加协议封装格式
//                        RabbitMQSender rabbitMQSender = new RabbitMQSender();
//                        Map<String, Object> userDeleteInformation = CmdUtil.messagePackaging(doorCmdDeleteEmployee, "employeeIdList", employeeIdList, "C");
//                        //命令状态设置为: 发送中
//                        doorCmdDeleteEmployee.setStatus("1");
//                        //设置md5校验值
//                        doorCmdDeleteEmployee.setMd5Check((String) userDeleteInformation.get("MD5Check"));
//                        //设置数据库的data字段
//                        doorCmdDeleteEmployee.setData(JSON.toJSONString(userDeleteInformation.get("data")));
//                        //命令数据存入数据库
//                        entranceGuardService.insertCommand(doorCmdDeleteEmployee);
//                        //立即下发数据到MQ
//                        rabbitMQSender.sendMessage(deviceId, userDeleteInformation);

                        //下发人员删除命令，包括删除权限
                        String sendTime = DateUtils.getDateTime();
                        cmdUtil.handOutCmd(deviceId, "C", "DELETE_USER_INFO", "2002", operatorEmployeeId,
                                "employeeIdList", employeeIdListTemp, "1", "", "", employeeId, "", sendTime);
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

    //同步不同设备上，有开门权限的人员的基本信息
    @Override
    public void synchronizeEmployeePermissionForDevices(String jsonString, String employeeId) {

        System.out.println("已进入人脸同步方法..............................................");
        System.out.println("jsonString = "+jsonString);
        System.out.println("employeeId = "+employeeId);

        //提取数据
        Map<String, Object> allMap = JSONObject.fromObject(jsonString);
        Map<String, Object> dataMap = (Map<String, Object>) allMap.get("data");
        Map<String, Object> userLabelMap = (Map<String, Object>) dataMap.get("userLabel");

        //1.同步该人员的人脸、指纹信息到人员表（一个人对多个公司）
        Employee employeeTemp = new Employee();
        employeeTemp.setEmployeeId(employeeId);

        try {
            if (StringUtils.isEmpty((String) userLabelMap.get("userFace"))) {
                System.out.println("人脸信息为空1");
                employeeTemp.setEmployeeFace((String) userLabelMap.get("userFace"));
            }
        }catch (Exception e){
            System.out.println("人脸信息不为空，值="+JSON.toJSONString(userLabelMap.get("userFace")));
            employeeTemp.setEmployeeFace(JSON.toJSONString(userLabelMap.get("userFace")));
        }

        if (StringUtils.isNotEmpty((String) userLabelMap.get("userFinger1"))) {
            employeeTemp.setEmployeeFinger1((String) userLabelMap.get("userFinger1"));
        }
        if (StringUtils.isNotEmpty((String) userLabelMap.get("userFinger1"))) {
            employeeTemp.setEmployeeFinger2((String) userLabelMap.get("userFinger2"));
        }
        employeeMapper.updateByPrimaryKeySelective(employeeTemp);

        //查询当前录人脸的门
        String localDoorId = doorMapper.findDoorIdByDeviceId((String) allMap.get("deviceId")).getDoorId();
        if (null == localDoorId){
            System.out.println("当前设备【"+(String) allMap.get("deviceId")+"】未绑定门");
            return;
        }

        //2.同步该人员的人脸、指纹信息到其它有权限的设备上
        List<DoorEmployee> doorEmployeeList = doorEmployeeMapper.selectByPrimaryKey(employeeId);
        System.out.println("doorEmployeeList = "+ JSON.toJSONString(doorEmployeeList));
        for (DoorEmployee doorEmployee : doorEmployeeList) {

            //录人脸的这个门不再同步
            if (localDoorId.equals(doorEmployee.getDoorId())){
                System.out.println("正在录人脸的门【"+doorEmployee.getDoorName()+"】将不进行同步");
                continue;
            }

            Door door = doorMapper.selectByPrimaryKey(doorEmployee.getDoorId());

            //如果某个门删除了，跳过这个门
            String deviceId = "";
            try {
                deviceId = door.getDeviceId();
            }catch (Exception e){
                System.out.println("【"+doorEmployee.getDoorName()+"】门已删除，同步将跳过这个门");
                continue;
            }

            //删除门时会删除对应关系，需要判断
            if (StringUtils.isEmpty(deviceId)){
                System.out.println("同步人脸信息时，门【"+door.getDoorId()+"】和设备的关联关系不存在");
                continue;
            }
            String companyId = deviceMapper.selectByPrimaryKey(deviceId).getCompanyId();
            if (StringUtils.isEmpty(companyId)){
                System.out.println("同步人脸信息时，设备【"+deviceId+"】和公司的关联关系不存在");
                continue;
            }

            //向每一个有权限的设备下发该设备对应公司的该人员的信息
            //查询该人员的有效时间是否过期
            Map<String, String> doorEmployeePermission = doorEmployeePermissionMapper.selectEmployeePressionByLeftJoin(employeeId, door.getDoorId());

            System.out.println("doorEmployeePermission = "+JSON.toJSONString(doorEmployeePermission));
            //这个人有开门权限设置
            if (doorEmployeePermission != null){
                String startTime = doorEmployeePermission.get("doorOpenStartTime");
                String endTime = doorEmployeePermission.get("doorOpenEndTime");
                String nowTime = DateUtils.getDateTime();

                //这个人的开门权限时间有效
                Boolean timeEffective = DateUtils.isBetweenTwoTime(startTime, endTime, nowTime);
                System.out.println("startTime = "+startTime);
                System.out.println("endTime = "+endTime);
                System.out.println("nowTime = "+nowTime);
                System.out.println("timeEffective = "+timeEffective);

                //这个人的最后一条下发的命令信息
                List<DoorCmd> doorCmdList = doorCmdMapper.selectDoorCmdLatestByEmployeeId(employeeId);
                //找不到最后一条命令就不同步
                if (doorCmdList.size() == 0){
                    System.out.println("找不到【"+employeeId+"】的最后一条下发命令");
                    continue;
                }
                //最新的下发命令不是下发该人员的信息就不同步
                for (DoorCmd doorCmd : doorCmdList) {
                    String actionCode = doorCmd.getActionCode();
                    if (!"2001".equals(actionCode) && !"3001".equals(actionCode)){
                        System.out.println("【"+employeeId+"】最后一条命令不是下发命令");
                        continue;
                    }
                }

                //权限有效并且往设备下发过该人员
                if (timeEffective){
                    //获取要下发的这个人的基本信息
                    Employee employeeLocal = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, companyId);

                    if (employeeLocal == null){
                        System.out.println("人员信息不同步，未查到【"+employeeId+"】的信息");
                        continue;
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

                        System.out.println("userFace-------------: "+userFace);
                        System.out.println("userFace+++++++++++++: "+userInformation.get("userFace"));
                        System.out.println("userNfc==============: "+userNFC);

                        //下发人员基本信息
                        //构造命令格式
                        DoorCmd doorCmdEmployeeInformation = new DoorCmd();
                        doorCmdEmployeeInformation.setServerId(serverId);
                        doorCmdEmployeeInformation.setDeviceId(deviceId);
                        doorCmdEmployeeInformation.setFileEdition("v1.3");
                        doorCmdEmployeeInformation.setCommandMode("C");
                        doorCmdEmployeeInformation.setCommandType("single");
                        doorCmdEmployeeInformation.setCommandTotal("1");
                        doorCmdEmployeeInformation.setCommandIndex("1");
                        doorCmdEmployeeInformation.setSubCmdId("");
                        doorCmdEmployeeInformation.setAction("UPDATE_USER_INFO");
                        doorCmdEmployeeInformation.setActionCode("2001");

                        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
                        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
                        doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
                        doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformation));
                        doorCmdEmployeeInformation.setEmployeeId("synchronization:"+employeeId);

                        //获取完整的数据加协议封装格式
                        RabbitMQSender rabbitMQSender = new RabbitMQSender();
                        Map<String, Object> userInformationAll =  CmdUtil.messagePackaging(doorCmdEmployeeInformation, "userInfo", userInformation, "C");
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

                        System.out.println("【"+employeeId+"】的人脸信息已向【"+deviceId+"】设备发出同步操作");
                    }
                }
            }
        }
        System.out.println("已退出人脸同步方法..............................................");
    }

    @Override
    public String deviceUploadPackage(String versionCode, MultipartFile uploadResource,
                                      String fileType, String employeeId) throws IOException {
        /**
         * fileType：facePhoto时为人脸图片上传，为任意其它字符串时为系统升级包上传（系统升级不再在此处处理）
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

    @Override
    public ReturnData handOutEmployeePermission(String employeePermission, HttpServletRequest request) {

        String operatorEmployeeId = request.getHeader("accessUserId");

//        System.out.println(employeePermission);

        //解析json字符串
        Map<String, Object> employeePermissionCollection = (Map<String, Object>) JSONObject.fromObject(employeePermission);

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

            //遍历人员
            for (Map<String, String> employeeMap : employeeList) {

                //判断公司获取是否成功
                if (StringUtils.isEmpty(request.getHeader("companyId"))){
                    System.out.println("没有获取到当前登录人的公司信息");
                    returnData.setMessage("没有获取到当前登录人的公司信息");
                    returnData.setReturnCode("4007");
                    return returnData;
                }

                //从本地查人员信息
                Employee employeeLocal = employeeMapper.selectByEmployeeIdAndCompanyId(employeeMap.get("employeeId"), request.getHeader("companyId"));

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
                    Employee employeeExist = employeeMapper.selectByEmployeeIdAndCompanyId(employeeId, request.getHeader("companyId"));
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

                    String birthday = employeeLocal.getEmployeeBirthday();
                    String contractExpired = employeeLocal.getEmployeeContractExpired();
                    String adminFlag = employeeLocal.getAdminFlag();
                    String userImg = employeeLocal.getEmployeeImg();
                    String userPhoto = employeeLocal.getEmployeePhoto();

                    String userFace = employeeLocal.getEmployeeFace();
                    String userFinger1 = employeeLocal.getEmployeeFinger1();
                    String userFinger2 = employeeLocal.getEmployeeFinger2();
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
                    userInformation.put("bluetoothId", bluetoothIdResult);

                    /**
                     * 下发人员开门的门禁权限
                     */
                    //创建人员有效日期和一周时间区间的关联标识id
                    String rangeFlagId = FormatUtil.createUuid();

                    //查询当前门是不是下发过这个人员
                    DoorEmployee doorEmployeeExist = doorEmployeeMapper.selectByEmployeeIdAndDoorId(employeeId, doorId);

                    //关联人员和门禁
                    iEmployeeService.relateEmployeeAndDoor(doorId, doorName, employeeId, employeeName, rangeFlagId, deviceId);

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

                        //关联人员门禁权限之开门时间区间和开门方式
                        iEmployeeService.relateEmployeeAndPermission(rangeFlagId, employeeId, weekType, isAllDay, startTime,
                                endTime, rangeDoorOpenType, isDitto, deviceId);

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
                    doorEmployeePermission.setDeviceId(deviceId);

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
                        System.out.println("【"+employeeName+"】当前下发到的门的公共开门密码未设置");
                    }

                    userPermission.put("oneWeekTimeList", oneWeekTimeList);

                    //人员下发时发送时间需要控制到两条命令完全一样，曾出现过1秒的差值导致下发人员列表部分人没有下发状态
                    String sendTime = DateUtils.getDateTime();

                    //判断是否立即下发数据到设备
                    if (immediatelyDownload.equals("0")){

                        /**
                         * 人员基本信息存为草稿
                         */
                        cmdUtil.handOutCmd(deviceId, "C", "UPDATE_USER_INFO", "2001", operatorEmployeeId,
                                "userInfo", userInformation, "0", "", "", employeeId, "", sendTime);

                        /**
                         * 人员开门权限存为草稿
                         */
                        cmdUtil.handOutCmd(deviceId, "C", "UPDATE_USER_ACCESS_CONTROL", "3001", operatorEmployeeId,
                                "userPermission", userPermission, "0", "", "", employeeId, "", sendTime);

                    }else if (immediatelyDownload.equals("1")){

                        //修改设备的激活状态
                        Device device = new Device();
                        device.setDeviceId(deviceId);
                        device.setActiveStatus("2");//使用中
                        deviceMapper.updateByPrimaryKeySelective(device);

                        /**
                         * 人员基本信息立即下发
                         */
                        Map userInformationAll = cmdUtil.handOutCmd(deviceId, "C", "UPDATE_USER_INFO", "2001", operatorEmployeeId,
                                "userInfo", userInformation, "1", "", "", employeeId, "", sendTime);

                        System.out.println("基本信息："+JSON.toJSONString(userInformationAll));

                        /**
                         * 人员开门权限立即下发
                         */
                        Map userPermissionAll = cmdUtil.handOutCmd(deviceId, "C", "UPDATE_USER_ACCESS_CONTROL", "3001", operatorEmployeeId,
                                "userPermission", userPermission, "1", "", "", employeeId, "", sendTime);

                        System.out.println("开门权限信息："+JSON.toJSONString(userPermissionAll));
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

    public static void main(String[] args) {
        String employeeInfo = HttpRequestFactory.sendRequet("http://192.168.0.108:8072/EmployeeController/selectByEmployee", "13DFF865799A42C785F33AAFDC2FDD2D");
        System.out.println("[*] send: 已发出请求");
        System.out.println(employeeInfo);
    }
}
