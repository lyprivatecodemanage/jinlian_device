package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.PageUtils;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

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

    @Autowired
    private DeviceMapper deviceMapper;

    /**
     * 添加命令到命令表
     * @param doorCmd
     */
    @Override
    public void insertCommand(DoorCmd doorCmd) {

//        DoorCmd doorCmdExist = doorCmdMapper.selectBySubCmdId(doorCmd);

//        if (doorCmdExist == null){
        try {
            doorCmdMapper.insert(doorCmd);
        }catch (Exception e){
            System.out.println("该命令superCmdId字段重复！");
        }
//            System.out.println("命令["+doorCmd.getSubCmdId()+"]的信息不存在");
//        }else {
//            doorCmdMapper.updateBySubCmdId(doorCmd);
//            System.out.println("命令["+doorCmd.getSubCmdId()+"]的信息已存在");
//        }

    }

    //TODO ############《基础信息》###############
    @Override
    public boolean addDoorInfo(Door door) {
        int insert = doorMapper.insert(door);
        if(insert>0){
            return true;
        }else{
            return false;
        }
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
     * 查询door_表主键的最大值
     * @return
     */
    @Override
    public int queryPrimaryKeyFromDoor() {
        return doorMapper.selectPrimaryKeyFromDoor();
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
     * @param
     * @return
     */
    @Override
    public List<Map> queryAllDoorInfo(Map map) {
        List<Map> doorInfo = doorMapper.getDoorInfo(map);
        return doorInfo ;
    }
    //TODO ############《日志管理》###############
    /**
     * 按条件查询日志信息
     * @return
     * {
     *     "companyName":"无敌的公司",----->企业名称
     *     "deviceName":"无敌的设备",----------------------------------->设备名称
     *     "operateType":"1",----------------->操作类型ID<预留的搜索条件：暂时不需要>
     *     "time":"2017-11-20 18:00",--------->时间
     *     "page":"1",------------->当前页码
     *     "rows":"10"------------->每一页显示的行数
     * }
     */
    @Override
    public Map queryLogCommand(String requestParam) {
        //定义操作内容
        String[] operateStr ={"待发送","下发中","下发成功","下发失败","删除人员权限","已回复"};

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Map map = new HashMap();
        map.put("companyName",jsonObject.get("companyName")!=null?"%"+jsonObject.get("companyName").toString()+"%":null);
        map.put("deviceName",jsonObject.get("deviceName")!=null?"%"+jsonObject.get("deviceName").toString()+"%":null);
       /* map.put("operateType",jsonObject.get("operateType")!=null?jsonObject.get("operateType").toString():null);*/
        map.put("time",jsonObject.get("time")!=null?"%"+jsonObject.get("time").toString()+"%":null);

        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        Page pageObj = null;
        if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
            pageObj = PageHelper.startPage(Integer.parseInt(page.toString()),Integer.parseInt(rows.toString()));
        }

        List<Map> logs = doorCmdMapper.selectLogCommand(map);

        //将没有的字段设置为“”
        for(int i=0;i<logs.size();i++){
            logs.get(i).put("super_cmd_id",logs.get(i).get("super_cmd_id")==null?"":logs.get(i).get("super_cmd_id"));
            logs.get(i).put("send_time",logs.get(i).get("send_time")==null?"":logs.get(i).get("send_time"));
            logs.get(i).put("status",logs.get(i).get("status")==null?"":logs.get(i).get("status"));
            logs.get(i).put("company_name",logs.get(i).get("company_name")==null?"":logs.get(i).get("company_name"));
            logs.get(i).put("device_name",logs.get(i).get("device_name")==null?"":logs.get(i).get("device_name"));
            logs.get(i).put("employee_name",logs.get(i).get("employee_name")==null?"":logs.get(i).get("employee_name"));
        }

        //完善数据结构
        for(int i=0;i<logs.size();i++){
            String status = logs.get(i).get("status").toString();
            for(int j=0;j<operateStr.length;j++){
                if(status.equals(String.valueOf(j))){
                    logs.get(i).put("status",operateStr[j].toString());
                }
            }
        }

        List outterLogList = new ArrayList();
        //更改返回的数据的字段名称，方便识别
        for(int i=0;i<logs.size();i++){
            Map innerMap = new HashMap();
            innerMap.put("logId",logs.get(i).get("super_cmd_id"));
            innerMap.put("companyName",logs.get(i).get("company_name"));
            innerMap.put("deviceName",logs.get(i).get("device_name"));
            innerMap.put("operateType",logs.get(i).get("数据"));//暂时固定：后期要更改
            innerMap.put("operateEmployee",logs.get(i).get("employee_name"));
            innerMap.put("time",logs.get(i).get("send_time"));
            innerMap.put("operateContent",logs.get(i).get("status"));

            outterLogList.add(innerMap);
        }
        return PageUtils.doSplitPage(null,outterLogList,page,rows,pageObj,1);
    }

    /**
     * 批量删除日志信息
     * @param requestParam
     * @return
        {
        "logIdList":[
        {"log_id":"26C8746239514090926B68CE0A07AA60"},
        {"log_id":"725E7BD1B1F44DFAB4C64CA09D790E4A"}
        ]
        }
     */
    @Override
    public boolean clearLogCommand(String requestParam) {
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        JSONArray logArray = JSONArray.parseArray(com.alibaba.fastjson.JSONObject.toJSONString(jsonObject.get("logIdList")));
        List list = new ArrayList();
        com.alibaba.fastjson.JSONObject logId = null;
        for(int i=0;i<logArray.size();i++){
            logId = com.alibaba.fastjson.JSONObject.parseObject(logArray.get(i).toString());
            list.add(logId.get("log_id"));
        }
        int delRsult = doorCmdMapper.removeLogCommand(list);
        return delRsult>0?true:false;
    }

    //TODO  ##################《授权中心》################
    @Override
    public List<Map> authoQueryAllDoor(Map map) {
        if(map.get("doorName")!=null&&!map.get("doorName").toString().isEmpty()){
           map.put("doorName","%"+map.get("dorName")+"%");
        }
        List<Map> doorEmployeeList = doorEmployeeMapper.queryDoorEmployeeInfo(map);
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
    public List<Map> queryRelateEmpPermissionInfo(Map relateEmpPermissionCondition) {
        if(relateEmpPermissionCondition.get("empName")!=null&&!relateEmpPermissionCondition.get("empName").toString().isEmpty()){
            relateEmpPermissionCondition.put("empName","%"+relateEmpPermissionCondition.get("empName")+"%");
        }
        if(relateEmpPermissionCondition.get("deptName")!=null&&!relateEmpPermissionCondition.get("deptName").toString().isEmpty()){
            relateEmpPermissionCondition.put("deptName","%"+relateEmpPermissionCondition.get("deptName")+"%");
        }
        if(relateEmpPermissionCondition.get("openTime")!=null&&!relateEmpPermissionCondition.get("openTime").toString().isEmpty()){
            relateEmpPermissionCondition.put("openTime","%"+relateEmpPermissionCondition.get("openTime")+"%");
        }
        List<Map> maps = doorEmployeeMapper.selectRelateEmpPermissionInfo(relateEmpPermissionCondition);
        return maps;
    }

    /**
     *查询具有门禁权限的人员一周的开门时间段
     * @param empId
     * @return
     */
    @Override
    public List<Map> queryAWeekOpenTime(String empId) {
        List<Map> maps = doorEmployeeMapper.selectAWeekOpenTime(empId);
        return maps;
    }

    /**
     * 根据门的id查询门的定时常开信息
     * @param doorId
     * @return
     */
    @Override
    public List<DoorTimingKeepOpen> queryKeepOpenInfo(String doorId) {
        List<DoorTimingKeepOpen> doorTimingKeepOpens = doorTimingKeepOpenMapper.selectKeepOpenInfo(doorId);
        return doorTimingKeepOpens;
    }

    /**
     *根据门的id查询门的首卡常开信息
     * @param doorId
     * @return
     */
    @Override
    public List<Map> queryFirstCardKeepOpenInfo(String doorId) {
        List<Map> maps = timeRangePrivilegeEmployeeMapper.selectFirstCardKeepOpenInfo(doorId);
        return maps;
    }

    /**
     * 根据门的id查询门禁信息
     * @return
     */
    @Override
    public List <DoorCalendar> queryDoorCalendarInfo(String doorId) {
        List<DoorCalendar> doorCalendars = doorCalendarMapper.selectDoorCalendarInfo(doorId);
        return doorCalendars;
    }

    /**
     * 根据门的id查询门的设置信息
     * @param doorId
     * @return
     */
    @Override
    public List<Map> queryDoorSettingInfo(String doorId) {
        List<Map> doorSetting = doorSettingMapper.selectDoorSettingInfo(doorId);
        return doorSetting;
    }


    //TODO ################《门禁记录》################

    //查询打卡记录和门禁异常
    @Override
    public List<Map> queryPunchCardRecord(DoorRecordCondition doorRecordCondition,int flag) {
        //验证数据的合法性
        if(doorRecordCondition!=null){
            if(doorRecordCondition.getName()!=null&&!doorRecordCondition.getName().isEmpty()){
                doorRecordCondition.setName("%"+doorRecordCondition.getName()+"%");
            }
            if(doorRecordCondition.getDepartment()!=null&&!doorRecordCondition.getDepartment().isEmpty()){
                doorRecordCondition.setDepartment("%"+doorRecordCondition.getDepartment()+"%");
            }
            if(doorRecordCondition.getPunchCardTime()!=null&&!doorRecordCondition.getPunchCardTime().isEmpty()){
                doorRecordCondition.setPunchCardTime("%"+doorRecordCondition.getPunchCardTime()+"%");
            }
            List<Map> doorRecords = null;
            if(flag==0){//打卡记录
                doorRecords = doorRecordMapper.selectPunchCardRecord(doorRecordCondition);
            }
            if(flag==1){//门禁异常
                doorRecords = doorExceptionMapper.selectDoorExceptionRecord(doorRecordCondition);
            }
            return doorRecords;
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
        doorCmdEmployeeInformation.setSubCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_ACCESS_CONTROL_SETTING");
        doorCmdEmployeeInformation.setActionCode("3003");

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
        doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
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
//        //立即下发数据到MQ
//        rabbitMQSender.sendMessage(downloadQueueName, userInformationAll);
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
        doorCmdEmployeeInformation.setSubCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_FIRST_CARD_NORMAL_OPENED");
        doorCmdEmployeeInformation.setActionCode("3004");

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
        doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
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
//        //立即下发数据到MQ
//        rabbitMQSender.sendMessage(downloadQueueName, userInformationAll);
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
        if (doorSettingExist == null) {
            //存储门禁配置到门禁配置表
            doorSettingMapper.insertSelective(doorSetting);
        } else {
            //更新门禁配置到门禁配置表
            doorSettingMapper.updateByPrimaryKeySelective(doorSetting);
        }

        //判断某个门的门禁日历信息是否存在
        List<DoorCalendar> doorCalendarList = doorCalendarMapper.selectByPrimaryKey(doorId);
        if (doorCalendarList != null) {
            //有信息存在则删除该门的所有门禁日历信息然后后面的时候重新添加
            doorCalendarMapper.deleteByPrimaryKey(doorId);
        }

        //首卡常开时间判断及存入数据库
        if (accessCalendar.size() > 0) {

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
        doorCmdEmployeeInformation.setSubCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_ACCESS_CALENDER");
        doorCmdEmployeeInformation.setActionCode("3005");

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
        doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
        doorCmdEmployeeInformation.setData(JSON.toJSONString(accessCalendar));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> userInformationAll = rabbitMQSender.messagePackaging(doorCmdEmployeeInformation, "accessCalendar", accessCalendar, "C");
        //命令状态设置为: 发送中
        doorCmdEmployeeInformation.setStatus("1");
        //设置md5校验值
        doorCmdEmployeeInformation.setMd5Check((String) userInformationAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdEmployeeInformation.setData(JSON.toJSONString(userInformationAll.get("data")));
        //命令数据存入数据库
        insertCommand(doorCmdEmployeeInformation);
//        //立即下发数据到MQ
//        rabbitMQSender.sendMessage(downloadQueueName, userInformationAll);
    }

    //门禁记录上传存储（mq过来的门禁记录信息）
    @Override
    public void doorRecordSave(String doorRecordMap) {

        //提取数据
        Map<String, Object> doorRecordMapTemp = (Map<String, Object>) JSONObject.fromObject(doorRecordMap);
        List<Map<String, String>> doorRecordList = (List<Map<String, String>>)doorRecordMapTemp.get("data");
        String resultCode;
        String resultMessage;

        //遍历门禁记录
        for (Map<String, String> recordMap : doorRecordList) {

            DoorRecord doorRecord = new DoorRecord();

            doorRecord.setDoorPermissionRecordId(recordMap.get("id"));
            doorRecord.setEmployeeId(recordMap.get("userId"));
//            doorRecord.setUpperState(recordMap.get("uploadFlag"));//暂不使用
            doorRecord.setEmployeeGroupName(recordMap.get("userName"));
            doorRecord.setEventResult(recordMap.get("outcome"));
            doorRecord.setEventResultReason(recordMap.get("cause"));
            doorRecord.setRecordType(recordMap.get("attType"));
            doorRecord.setDoorId(doorMapper.findDoorIdByDeviceId(recordMap.get("deviceId")).getDoorId());
            doorRecord.setDeviceGroupName(recordMap.get("deviceName"));
            doorRecord.setRecordDate(recordMap.get("attTime"));
            doorRecord.setRealWeek(recordMap.get("week"));
            doorRecord.setEventPhotoGroupId(recordMap.get("eventPhotoCombinationId"));

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
        resultCode = "0";
        resultMessage = "执行成功";
        resultData.put("resultCode", resultCode);
        resultData.put("resultMessage", resultMessage);
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
        doorCmdRecord.setCommandType("S");
        doorCmdRecord.setCommandTotal("1");
        doorCmdRecord.setCommandIndex("1");
        doorCmdRecord.setSubCmdId("");
        doorCmdRecord.setAction("UPLOAD_ACCESS_RECORD");
        doorCmdRecord.setActionCode("3006");
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
        insertCommand(doorCmdRecord);
//        //立即下发回复数据到MQ
//        rabbitMQSender.sendMessage(downloadQueueName, doorRecordAll);
        System.out.println("门禁记录上传已回复MQ");
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

    //查询一个人在一段时间之内的最早最晚打卡时间
    @Override
    public List<String> queryPunchCardTime (String empId, String companyId, String startTime, String endTime){
        Map map = new HashMap();
        map.put("empId", empId);
        map.put("companyId", companyId);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        List<String> strings = doorRecordMapper.selectPunchCardTime(map);
        return strings;
    }

    /**
     * TODO APP接口，查询员工的打卡记录
     */
    @Override
    public List<Map> queryEmpPunchCardRecord(String requestParam) {
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Object empId = jsonObject.get("empId");
        Object searchTime = jsonObject.get("searchTime");

        List<Map> punchCardRecord = null;
        if(empId!=null){
            Map map = new HashMap();
            map.put("empId",empId.toString());
            map.put("recordDate",(searchTime==null || searchTime.toString().isEmpty())?"%"+DateUtils.getDate()+"%":"%"+searchTime+"%");
            punchCardRecord = doorRecordMapper.selectEmpPunchRecord(map);
        }
        return punchCardRecord;
    }
}
