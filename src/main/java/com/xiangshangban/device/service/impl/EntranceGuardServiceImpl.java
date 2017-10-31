package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEntranceGuardService;
import com.xiangshangban.device.bean.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 门禁管理实现类
 */
@Service
public class EntranceGuardServiceImpl implements IEntranceGuardService {


    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private DoorEmployeeMapper doorEmployeeMapper;

    @Autowired
    private DoorRecordMapper doorRecordMapper;

    @Autowired
    private DoorExceptionMapper doorExceptionMapper;

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Autowired
    private DoorSettingMapper doorSettingMapper;

    @Autowired
    private DoorTimingKeepOpenMapper doorTimingKeepOpenMapper;

    @Autowired
    private TimeRangePrivilegeEmployeeMapper timeRangePrivilegeEmployeeMapper;

    @Autowired
    private DoorCalendarMapper doorCalendarMapper;

    /**
     * 添加or更新命令表
     * @param doorCmd
     */
    @Override
    public void insertCommand(DoorCmd doorCmd) {

//        DoorCmd doorCmdExist = doorCmdMapper.selectBySubCmdId(doorCmd);

//        if (doorCmdExist == null){
        doorCmdMapper.insert(doorCmd);
//            System.out.println("命令["+doorCmd.getSubCmdId()+"]的信息不存在");
//        }else {
//            doorCmdMapper.updateBySubCmdId(doorCmd);
//            System.out.println("命令["+doorCmd.getSubCmdId()+"]的信息已存在");
//        }

    }

    //TODO ############《基础信息》###############
    @Override
    public boolean addDoorInfo(Door door) {
        return false;
    }

    @Override
    public boolean deleteDoorInfo(Door door) {
        return false;
    }

    /**
     * 批量删除门信息
     * @param doorList
     * @return
     */
    @Override
    public boolean delDoorInfoByBatch(List<String> doorList) {
        int i = doorMapper.delDoorBatch(doorList);
        if(i>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 更新门信息
     * @param door
     * @return
     */
    @Override
    public boolean updateDoorInfo(Door door) {
        if(door!=null&&!door.getDoorId().isEmpty()){
            //首先查询该id对应的门的信息
            Door currDoor = doorMapper.selectByPrimaryKey(door.getDoorId());
            //设置数据
            if(!door.getDeviceId().isEmpty()){
                currDoor.setDeviceId(door.getDeviceId());
            }
            if(!door.getDoorName().isEmpty()){
                currDoor.setDoorName(door.getDoorName());
            }
            if(!door.getOperateEmployee().isEmpty()){
                currDoor.setOperateEmployee(door.getOperateEmployee());
            }

            currDoor.setOperateTime(new SimpleDateFormat("yyyy-MM-dd HH-mm-ss").format(new Date().getTime()));

            int i = doorMapper.updateByPrimaryKey(currDoor);
            if(i>0){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    /**
     * 根据门名称，查询门信息
     * @param door
     * @return
     */
    @Override
    public List<Map> queryAllDoorInfo(Door door) {
        if(door.getDoorName()!=null&&!door.getDoorName().isEmpty()){
            door.setDoorName("%"+door.getDoorName()+"%");
        }
        List<Map> doorInfo = doorMapper.getDoorInfo(door);
        return doorInfo ;
    }

    /**
     *通过门ID，单独进行门信息查询
     * @param doorId
     * @return
     */
    @Override
    public Door queryDoorInfo(String doorId) {
        return doorMapper.selectByPrimaryKey(doorId);
    }


    //TODO  ##################《授权中心》################
    @Override
    public List<Map> authoQueryAllDoor(DoorEmployee doorEmployee) {
        if(doorEmployee.getDoorName()!=null&&!doorEmployee.getDoorName().isEmpty()){
            doorEmployee.setDoorName("%"+doorEmployee.getDoorName()+"%");
        }
        List<Map> doorEmployeeList = doorEmployeeMapper.queryDoorEmployeeInfo(doorEmployee);
        return doorEmployeeList;
    }

    /**
     * 查询门（设备上）最后一次接收到指令的时间
     * @param doorName
     * @return
     */
    @Override
    public List<Map> querySendTime(String doorName) {
        String verifyStr = "";
        if(doorName!=null&&!doorName.isEmpty()){
            verifyStr = "%"+doorName+"%";
        }
        List<Map> maps = doorEmployeeMapper.selectSendTime(verifyStr);
        List<Map> newMaps = new ArrayList<Map>();
        String door_id;
        for(int i=0;i<maps.size();i++){
            door_id = doorEmployeeMapper.selectDoorIdByDeviceId(maps.get(i).get("device_id").toString());
            Map innerMap= new HashMap();
            innerMap.put("doorId",door_id);
            innerMap.put("sendTime",maps.get(i).get("sendtime"));
            newMaps.add(innerMap);
        }
        return newMaps;
    }

    /**
     * 查询门关联的用户的权限信息（开门方式，开门时间，指令下发状态等）
     * @param relateEmpPermissionCondition 查询条件
     * @return
     */
    @Override
    public List<Map> queryRelateEmpPermissionInfo(RelateEmpPermissionCondition relateEmpPermissionCondition) {
        if(relateEmpPermissionCondition.getEmpName()!=null&&!relateEmpPermissionCondition.getEmpName().isEmpty()){
            relateEmpPermissionCondition.setEmpName("%"+relateEmpPermissionCondition.getEmpName()+"%");
        }
        if(relateEmpPermissionCondition.getDeptName()!=null&&!relateEmpPermissionCondition.getDeptName().isEmpty()){
            relateEmpPermissionCondition.setDeptName("%"+relateEmpPermissionCondition.getDeptName()+"%");
        }

        List<Map> maps = doorEmployeeMapper.selectRelateEmpPermissionInfo(relateEmpPermissionCondition);
        return maps;
    }

    /**
     * 查询设备命令信息（下方时间、下发状态、下发数据）
     * @param relateEmpPermissionCondition
     * @return
     */
    @Override
    public List<Map> queryCMDInfo(RelateEmpPermissionCondition relateEmpPermissionCondition) {
        List<Map> maps = doorEmployeeMapper.selectCMDInfo(relateEmpPermissionCondition);
        return maps;
    }


    //TODO ################《门禁记录》################

    //查询打卡记录
    @Override
    public List<DoorRecord> queryPunchCardRecord(DoorRecordCondition doorRecordCondition) {
        //验证数据的合法性
        if(doorRecordCondition!=null){
            if(doorRecordCondition.getName()!=null&&!doorRecordCondition.getName().isEmpty()){
                doorRecordCondition.setName("%"+doorRecordCondition.getName()+"%");
            }
            if(doorRecordCondition.getDepartment()!=null&&!doorRecordCondition.getDepartment().isEmpty()){
                doorRecordCondition.setDepartment("%"+doorRecordCondition.getDepartment()+"%");
            }
            if(doorRecordCondition.getPunchCardType()!=null&&!doorRecordCondition.getPunchCardType().isEmpty()){
                doorRecordCondition.setPunchCardType("%"+doorRecordCondition.getPunchCardType()+"%");
            }
            List<DoorRecord> doorRecords = doorRecordMapper.selectPunchCardRecord(doorRecordCondition);
            return doorRecords;
        }else{
            return null;
        }
    }

    //查询门禁异常记录
    @Override
    public List<DoorException> queryDoorExceptionRecord(DoorExceptionCondition doorExceptionCondition) {
        //验证数据的合法性
        if(doorExceptionCondition!=null){
            if(doorExceptionCondition.getName()!=null&&!doorExceptionCondition.getName().isEmpty()){
                doorExceptionCondition.setName("%"+doorExceptionCondition.getName()+"%");
            }
            if(doorExceptionCondition.getDepartment()!=null&&!doorExceptionCondition.getDepartment().isEmpty()){
                doorExceptionCondition.setDepartment("%"+doorExceptionCondition.getDepartment()+"%");
            }
            if(doorExceptionCondition.getAlarmType()!=null&&!doorExceptionCondition.getAlarmType().isEmpty()){
                doorExceptionCondition.setAlarmType("%"+doorExceptionCondition.getAlarmType()+"%");
            }
            List<DoorException> doorExceptionRecords = doorExceptionMapper.selectDoorExceptionRecord(doorExceptionCondition);
            return doorExceptionRecords;
        }else{
            return null;
        }
    }

    //门禁配置---功能配置（身份验证失败次数、非法入侵、报警时长、密码、开门事件记录）
    @Override
    public void doorCommonSetupAdditional(String doorId, String countLimitAuthenticationFailed, String enableAlarm,
                                          String alarmTimeLength, String publicPassword1, String publicPassword2, String threatenPassword,
                                          String deviceManagePassword, String enableDoorOpenRecord, List oneWeekTimeDoorKeepList,
                                          String enableDoorKeepOpen, String enableFirstCardKeepOpen, String enableDoorCalendar) {

        //获取设备id
        Door doorInfo = doorMapper.findAllByDoorId(doorId);

        //本地存储的门禁配置数据结构
        DoorSetting doorSetting = new DoorSetting();
        doorSetting.setDoorId(doorId);
        doorSetting.setFaultCountAuthentication(countLimitAuthenticationFailed);
        if (enableAlarm.equals("0")){
            doorSetting.setAlarmTimeLengthTrespass(alarmTimeLength);
        }
        doorSetting.setFirstPublishPassword(publicPassword1);
        doorSetting.setSecondPublishPassword(publicPassword2);
        doorSetting.setThreatenPublishPasswrod(threatenPassword);
        doorSetting.setManagerPassword(deviceManagePassword);
        doorSetting.setEnableDoorEventRecord(enableDoorOpenRecord);
        doorSetting.setEnableDoorKeepOpen(enableDoorKeepOpen);

        //判断门禁配置表里有没有这个门的配置信息，有则更新，无则增加
        DoorSetting doorSettingExist = doorSettingMapper.selectByPrimaryKey(doorId);
        if (doorSettingExist == null){
            //存储门禁配置到门禁配置表
            doorSettingMapper.insertSelective(doorSetting);
        }else {
            //更新门禁配置到门禁配置表
            doorSettingMapper.updateByPrimaryKeySelective(doorSetting);
        }

        //判断某个门的定时常开时间区间信息是否存在
        List<DoorTimingKeepOpen> doorTimingKeepOpenList = doorTimingKeepOpenMapper.selectExistByDoorId(doorId);
        if (doorTimingKeepOpenList != null){
            //有信息存在则删除该门的所有定时常开时间区间信息然后后面的时候重新添加
            doorTimingKeepOpenMapper.deleteByPrimaryKey(doorId);
        }

        //门禁定时常开时间判断及存入数据库
        if (oneWeekTimeDoorKeepList.size() > 0){

            List<Map<String, String>> oneWeekTimeCollection = (List<Map<String, String>>) oneWeekTimeDoorKeepList;
            for (Map<String, String> oneWeekTimeMap : oneWeekTimeCollection) {
                String weekType = oneWeekTimeMap.get("weekType");
                String startTime = oneWeekTimeMap.get("startTime");
                String endTime = oneWeekTimeMap.get("endTime");

                DoorTimingKeepOpen doorTimingKeepOpen = new DoorTimingKeepOpen();
                doorTimingKeepOpen.setDoorId(doorId);
                doorTimingKeepOpen.setDayOfWeek(weekType);
                doorTimingKeepOpen.setTimingOpenStartTime(startTime);
                doorTimingKeepOpen.setTimingOpenEndTime(endTime);

                //更新门禁定时常开时间段
                doorTimingKeepOpenMapper.insertSelective(doorTimingKeepOpen);

            }
        }


        //下发的门禁配置DATA数据结构
        Map<String, Object> doorSetupMap = new LinkedHashMap<String, Object>();
        doorSetupMap.put("doorName", doorInfo.getDoorName());
        doorSetupMap.put("timeLimitDoorOpen", "");
        doorSetupMap.put("timeLimitLockOpen", "");
        doorSetupMap.put("enableDoorOpenRecord", enableDoorOpenRecord);
        doorSetupMap.put("countLimitAuthenticationFailed", countLimitAuthenticationFailed);
        if (enableAlarm.equals("0")){
            doorSetupMap.put("alarmTimeLength", alarmTimeLength);
        }else{
            doorSetupMap.put("alarmTimeLength", "0");
        }
        doorSetupMap.put("publicPassword1", publicPassword1);
        doorSetupMap.put("publicPassword2", publicPassword2);
        doorSetupMap.put("threatenPassword", threatenPassword);
        doorSetupMap.put("deviceManagePassword", deviceManagePassword);
        doorSetupMap.put("enableDoorKeepOpen", enableDoorKeepOpen);
        doorSetupMap.put("enableFirstCardKeepOpen", enableFirstCardKeepOpen);
        doorSetupMap.put("enableDoorCalendar", enableDoorCalendar);
        doorSetupMap.put("oneWeekTimeList", oneWeekTimeDoorKeepList);

        //下发门禁常规设置
        //构造命令格式
        DoorCmd doorCmdEmployeeInformation = new DoorCmd();
        doorCmdEmployeeInformation.setServerId("001");
        doorCmdEmployeeInformation.setDeviceId(doorInfo.getDeviceId());
        doorCmdEmployeeInformation.setFileEdition("v1.3");
        doorCmdEmployeeInformation.setCommandMode("C");
        doorCmdEmployeeInformation.setCommandType("single");
        doorCmdEmployeeInformation.setCommandTotal("1");
        doorCmdEmployeeInformation.setCommandIndex("1");
        doorCmdEmployeeInformation.setSuperCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_ACCESS_CONTROL_SETTING");
        doorCmdEmployeeInformation.setActionCode("3003");

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdEmployeeInformation.setSubCmdId(FormatUtil.createUuid());
        doorCmdEmployeeInformation.setData(JSON.toJSONString(doorSetting));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> userInformationAll =  rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "attSetting", doorSetupMap, "C");
        //命令状态设置为: 发送中
        doorCmdEmployeeInformation.setStatus("1");
        //设置md5校验值
        doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
        //命令数据存入数据库
        insertCommand(doorCmdEmployeeInformation);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage("hello", userInformationAll);
    }

    //门禁配置---功能配置（首卡常开权限）
    @Override
    public void handOutFirstCard(String doorId, String enableFirstCardKeepOpen, List<String> employeeIdList, List oneWeekTimeFirstCardList) {

        //获取设备id
        Door doorInfo = doorMapper.findAllByDoorId(doorId);

        //本地存储的门禁配置数据结构
        DoorSetting doorSetting = new DoorSetting();
        doorSetting.setDoorId(doorId);
        doorSetting.setEnableFirstCardKeepOpen(enableFirstCardKeepOpen);

        //判断门禁配置表里有没有这个门的配置信息，有则更新，无则增加
        DoorSetting doorSettingExist = doorSettingMapper.selectByPrimaryKey(doorId);
        if (doorSettingExist == null){
            //存储门禁配置到门禁配置表
            doorSettingMapper.insertSelective(doorSetting);
        }else {
            //更新门禁配置到门禁配置表
            doorSettingMapper.updateByPrimaryKeySelective(doorSetting);
        }

        ////遍历人员
        for (String employeeId : employeeIdList) {

            //判断某个人的首卡常开时间区间信息是否存在
            List<TimeRangePrivilegeEmployee> timeRangePrivilegeEmployeeList = timeRangePrivilegeEmployeeMapper.selectExitByEmployeeId(doorId);
            if (timeRangePrivilegeEmployeeList != null){
                //有信息存在则删除该人的所有首卡常开时间区间信息然后后面的时候重新添加
                timeRangePrivilegeEmployeeMapper.deleteByEmployeeId(employeeId);
            }

            //首卡常开时间判断及存入数据库
            if (oneWeekTimeFirstCardList.size() > 0){

                List<Map<String, String>> oneWeekTimeCollection = (List<Map<String, String>>) oneWeekTimeFirstCardList;
                for (Map<String, String> oneWeekTimeMap : oneWeekTimeCollection) {
                    String weekType = oneWeekTimeMap.get("weekType");
                    String startTime = oneWeekTimeMap.get("startTime");
                    String endTime = oneWeekTimeMap.get("endTime");
                    String doorOpenType = oneWeekTimeMap.get("doorOpenType");

                    TimeRangePrivilegeEmployee timeRangePrivilegeEmployee = new TimeRangePrivilegeEmployee();
                    timeRangePrivilegeEmployee.setEmployeeId(employeeId);
                    timeRangePrivilegeEmployee.setDoorId(doorId);
                    timeRangePrivilegeEmployee.setDayOfWeek(weekType);
                    timeRangePrivilegeEmployee.setRangeStartTime(startTime);
                    timeRangePrivilegeEmployee.setRangeEndTime(endTime);
                    timeRangePrivilegeEmployee.setRangeDoorOpenType(doorOpenType);
                    timeRangePrivilegeEmployee.setRangeFlagId(FormatUtil.createUuid());

                    //更新首卡常开时间段
                    timeRangePrivilegeEmployeeMapper.insertSelective(timeRangePrivilegeEmployee);

                }
            }

        }
        
        //下发的门禁配置DATA数据结构
        Map<String, Object> firstCardSetupMap = new LinkedHashMap<String, Object>();
        firstCardSetupMap.put("employeeIdList", employeeIdList);
        firstCardSetupMap.put("oneWeekTimeList", oneWeekTimeFirstCardList);

        //下发门禁常规设置
        //构造命令格式
        DoorCmd doorCmdEmployeeInformation = new DoorCmd();
        doorCmdEmployeeInformation.setServerId("001");
        doorCmdEmployeeInformation.setDeviceId(doorInfo.getDeviceId());
        doorCmdEmployeeInformation.setFileEdition("v1.3");
        doorCmdEmployeeInformation.setCommandMode("C");
        doorCmdEmployeeInformation.setCommandType("single");
        doorCmdEmployeeInformation.setCommandTotal("1");
        doorCmdEmployeeInformation.setCommandIndex("1");
        doorCmdEmployeeInformation.setSuperCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_FIRST_CARD_NORMAL_OPENED");
        doorCmdEmployeeInformation.setActionCode("3004");

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdEmployeeInformation.setSubCmdId(FormatUtil.createUuid());
        doorCmdEmployeeInformation.setData(JSON.toJSONString(doorSetting));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> userInformationAll =  rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "firstCardKeepDoorOpen", firstCardSetupMap, "C");
        //命令状态设置为: 发送中
        doorCmdEmployeeInformation.setStatus("1");
        //设置md5校验值
        doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
        //命令数据存入数据库
        insertCommand(doorCmdEmployeeInformation);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage("hello", userInformationAll);
    }

    //门禁配置---功能配置（门禁日历）
    @Override
    public void handOutDoorCalendar(String doorId, String enableDoorCalendar, List accessCalendar) {

        //获取设备id
        Door doorInfo = doorMapper.findAllByDoorId(doorId);

        //本地存储的门禁配置数据结构
        DoorSetting doorSetting = new DoorSetting();
        doorSetting.setDoorId(doorId);
        doorSetting.setEnableDoorCalendar(enableDoorCalendar);

        //判断门禁配置表里有没有这个门的配置信息，有则更新，无则增加
        DoorSetting doorSettingExist = doorSettingMapper.selectByPrimaryKey(doorId);
        if (doorSettingExist == null){
            //存储门禁配置到门禁配置表
            doorSettingMapper.insertSelective(doorSetting);
        }else {
            //更新门禁配置到门禁配置表
            doorSettingMapper.updateByPrimaryKeySelective(doorSetting);
        }

        //判断某个门的门禁日历信息是否存在
        List<DoorCalendar> doorCalendarList = doorCalendarMapper.selectByPrimaryKey(doorId);
        if (doorCalendarList != null){
            //有信息存在则删除该门的所有门禁日历信息然后后面的时候重新添加
            doorCalendarMapper.deleteByPrimaryKey(doorId);
        }

        //首卡常开时间判断及存入数据库
        if (accessCalendar.size() > 0){

            List<Map<String, String>> accessCalendarCollection = (List<Map<String, String>>) accessCalendar;
            for (Map<String, String> accessCalendarMap : accessCalendarCollection) {
                String deviceCalendarDate = accessCalendarMap.get("deviceCalendarDate");
                String enableDoorOpenGlobal = accessCalendarMap.get("enableDoorOpenGlobal");

                DoorCalendar doorCalendar = new DoorCalendar();
                doorCalendar.setDoorId(doorId);
                doorCalendar.setCalendarDate(deviceCalendarDate);
                doorCalendar.setWeatherOpenDoor(enableDoorOpenGlobal);

                //更新门禁定时常开时间段
                doorCalendarMapper.insertSelective(doorCalendar);

            }
        }

        //下发门禁常规设置
        //构造命令格式
        DoorCmd doorCmdEmployeeInformation = new DoorCmd();
        doorCmdEmployeeInformation.setServerId("001");
        doorCmdEmployeeInformation.setDeviceId(doorInfo.getDeviceId());
        doorCmdEmployeeInformation.setFileEdition("v1.3");
        doorCmdEmployeeInformation.setCommandMode("C");
        doorCmdEmployeeInformation.setCommandType("single");
        doorCmdEmployeeInformation.setCommandTotal("1");
        doorCmdEmployeeInformation.setCommandIndex("1");
        doorCmdEmployeeInformation.setSuperCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_ACCESS_CALENDER");
        doorCmdEmployeeInformation.setActionCode("3005");

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdEmployeeInformation.setSubCmdId(FormatUtil.createUuid());
        doorCmdEmployeeInformation.setData(JSON.toJSONString(accessCalendar));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> userInformationAll =  rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "accessCalendar", accessCalendar, "C");
        //命令状态设置为: 发送中
        doorCmdEmployeeInformation.setStatus("1");
        //设置md5校验值
        doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
        //命令数据存入数据库
        insertCommand(doorCmdEmployeeInformation);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage("hello", userInformationAll);

    }
}
