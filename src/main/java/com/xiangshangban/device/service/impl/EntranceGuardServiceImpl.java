package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.controller.EntranceGuardController;
import com.xiangshangban.device.dao.*;
import com.xiangshangban.device.service.IEntranceGuardService;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * author : Administrator
 * date: 2017/10/19 10:38
 * describe: TODO 门禁管理实现类
 */
@Service
public class EntranceGuardServiceImpl implements IEntranceGuardService {

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Value("${serverId}")
    String serverId;

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

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private EntranceGuardController entranceGuardController;

    /**
     * 添加命令到命令表
     * @param doorCmd
     */
    @Transactional
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
    public String queryPrimaryKeyFromDoor() {
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

            currDoor.setOperateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime()));

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
     *     "deviceName":"无敌的设备",----------------------------------->设备名称
     *     "operateCommand":"1",----------------->操作指令
     *     "page":"1",------------->当前页码
     *     "rows":"10"------------->每一页显示的行数
     * }
     */
    @Override
    public Map queryLogCommand(String requestParam,String companyId) {
        //定义操作内容
        String[] operateStr ={"待发送","下发中","下发成功","下发失败"};

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Map map = new HashMap();
        map.put("deviceName",jsonObject.get("deviceName")!=null?"%"+jsonObject.get("deviceName").toString()+"%":null);
        map.put("operateCommand",jsonObject.get("operateCommand")!=null?jsonObject.get("operateCommand").toString():null);
        map.put("companyId",(companyId!=null && !companyId.isEmpty())?companyId:null);


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
            logs.get(i).put("action_code",logs.get(i).get("action_code")==null?"":logs.get(i).get("action_code"));
            logs.get(i).put("send_time",logs.get(i).get("send_time")==null?"":logs.get(i).get("send_time"));
            logs.get(i).put("status",logs.get(i).get("status")==null?"":logs.get(i).get("status"));
            /*logs.get(i).put("company_name",logs.get(i).get("company_name")==null?"":logs.get(i).get("company_name"));*/
            logs.get(i).put("device_name",logs.get(i).get("device_name")==null?"":logs.get(i).get("device_name"));
            logs.get(i).put("employee_name",logs.get(i).get("employee_name")==null?"":logs.get(i).get("employee_name"));
        }

        //完善数据结构(更改下发的状态为文字)
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
            //获取指令类型
            Object cmdType = logs.get(i).get("action_code");
            innerMap.put("logId",logs.get(i).get("super_cmd_id"));
            /* innerMap.put("companyName",logs.get(i).get("company_name"));*/
            innerMap.put("deviceName",logs.get(i).get("device_name"));
            //设置指令类型
            if(cmdType!=null){
                if(cmdType.toString().trim().equals("3003")){
                    innerMap.put("operateCommand","门禁系统设置");
                }
                if(cmdType.toString().trim().equals("3004")){
                    innerMap.put("operateCommand","首卡常开");
                }
                if(cmdType.toString().trim().equals("3005")){
                    innerMap.put("operateCommand","门禁日历");
                }
            }else{
                innerMap.put("operateCommand","");
            }
            innerMap.put("operateEmployee",logs.get(i).get("employee_name"));
            innerMap.put("time",logs.get(i).get("send_time"));
            innerMap.put("status",logs.get(i).get("status"));

            outterLogList.add(innerMap);
        }
        return PageUtils.doSplitPage(null,outterLogList,page,rows,pageObj,2);
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
           map.put("doorName","%"+map.get("doorName")+"%");
        }
        List<Map> doorEmployeeList = doorEmployeeMapper.queryDoorEmployeeInfo(map);
        return doorEmployeeList;
    }

    /**
     * 查询门（设备上）最后一次下发人员相关指令的时间
     * @param doorId
     * @return
     */
    @Override
    public String querySendTime(String doorId) {
        //获取设备的ID和有关人员的指令的最后下发时间
        String sendTime = doorEmployeeMapper.selectSendTime(doorId);
        return sendTime;
    }

    /**
     * 查询门关联的用户的权限信息（开门方式，开门时间，指令下发状态等）
     * @param relateEmpPermissionCondition 查询条件
     * @return
     */
    @Override
    public List<Map> queryRelateEmpPermissionInfo(Map relateEmpPermissionCondition,String companyId) {
        //根据门的ID查询门关联的设备的ID
        Door doorObj = doorMapper.findAllByDoorId(relateEmpPermissionCondition.get("doorId").toString());
        //TODO 维护door_employee表进行door_employee表数据的删除和添加
        maintainDoorEmployee(doorObj,companyId);

        if(relateEmpPermissionCondition.get("empName")!=null&&!relateEmpPermissionCondition.get("empName").toString().isEmpty()){
            relateEmpPermissionCondition.put("empName","%"+relateEmpPermissionCondition.get("empName")+"%");
        }
        if(relateEmpPermissionCondition.get("deptName")!=null&&!relateEmpPermissionCondition.get("deptName").toString().isEmpty()){
            relateEmpPermissionCondition.put("deptName","%"+relateEmpPermissionCondition.get("deptName")+"%");
        }
        Object openTime = relateEmpPermissionCondition.get("openTime");
        if(openTime==null || openTime.toString().equals("")){
            relateEmpPermissionCondition.put("rangeStartTime","");
            relateEmpPermissionCondition.put("rangeEndTime","");
        }else{
            relateEmpPermissionCondition.put("rangeStartTime",openTime.toString().split("-")[0].trim());
            relateEmpPermissionCondition.put("rangeEndTime",openTime.toString().split("-")[1].trim());
        }
        //移除openTime
        relateEmpPermissionCondition.remove("openTime");

        if(relateEmpPermissionCondition.get("openType")!=null && !relateEmpPermissionCondition.get("openType").toString().isEmpty()){
            relateEmpPermissionCondition.put("openType","%"+relateEmpPermissionCondition.get("openType")+"%");
        }

        Page pageObj = null;
      if(relateEmpPermissionCondition.get("pageIndex")!=null && !relateEmpPermissionCondition.get("pageIndex").toString().isEmpty()){
            if(relateEmpPermissionCondition.get("rowNumber")!=null && !relateEmpPermissionCondition.get("rowNumber").toString().isEmpty()){
                int pageIndex = Integer.parseInt(relateEmpPermissionCondition.get("pageIndex").toString());
                int rowNumber = Integer.parseInt(relateEmpPermissionCondition.get("rowNumber").toString());
                pageObj = PageHelper.startPage(pageIndex, rowNumber);
            }
        }
        //查询门相关的人员的基本信息和周一的最早的打卡时间段
        List<Map> maps = doorEmployeeMapper.selectMondayPunchCardTimeAndEmpInfo(relateEmpPermissionCondition);

        //遍历所有人员信息，查询该人员相关的指令的最后下发时间和状态
        if(maps!=null && maps.size()>0){
            for(int i=0;i<maps.size();i++){
                Object employee_id = maps.get(i).get("employee_id");
                if(employee_id!=null){
                    Map requestParam = new HashMap();
                    requestParam.put("employeeId",employee_id.toString());
                    requestParam.put("deviceId",doorObj.getDeviceId());
                    //TODO 获取下发时间(---此处要更改（可能该设备上关联的人员还没有进行下发操作，查询指令信息时查询不到的，所以说就没有“isDelCommand标志”）----)
                    List<Map> commandList = doorEmployeeMapper.selectRelateEmpCommand(requestParam);
                    if(commandList!=null && commandList.size()>0){
                        //设置下发时间
                        maps.get(i).put("lasttime",commandList.get(0).get("send_time").toString());
                        //判断指令的条数
                        if(commandList.size()==1){
                            //获取action_code
                            String actionCode = commandList.get(0).get("action_code").toString();
                            if(actionCode.equals("2002")){//删除人员权限
                                maps.get(i).put("status",commandList.get(0).get("status").toString());
                                //放置删除指令特有的标志位
                                maps.get(i).put("isDelCommand","1");
                            }
                        }
                        if(commandList.size()==2){
                            //获取actionCode
                            String firstActionCode = commandList.get(0).get("action_code").toString();
                            String secondActionCode = commandList.get(1).get("action_code").toString();

                            if((firstActionCode.equals("2001") && secondActionCode.equals("3001"))
                                    ||firstActionCode.equals("3001") && secondActionCode.equals("2001")){ //下发、更新人员权限
                                String firstStatus = commandList.get(0).get("status").toString();
                                String secondStatus = commandList.get(1).get("status").toString();

                                if(firstStatus.equals("2") && secondStatus.equals("2")){
                                    maps.get(i).put("status","2");
                                }else if(firstStatus.equals("0") || secondStatus.equals("0")){
                                    maps.get(i).put("status","0");
                                }else if(firstStatus.equals("1") || secondStatus.equals("1")){
                                    maps.get(i).put("status","1");
                                }else if(firstStatus.equals("3") || secondStatus.equals("3")){
                                    maps.get(i).put("status","3");
                                }

                                maps.get(i).put("isDelCommand","0");
                            }
                        }
                    }
                }
            }
        }
        //添加Page对象，返回给Controller层
        Map pageMap = new HashMap();
        pageMap.put("pageObj",pageObj);
        maps.add(pageMap);

        return maps;
    }


    /**
     * TODO 维护doorEmployee表
     * 判断当前门关联的人员的最新的一条指令是哪个（删除、下发人员）
     */
    public void maintainDoorEmployee(Door doorObj,String companyId){
        //查询该门（设备）上所有人员的最新的指令是哪种指令
        Map allEmpMap = new HashMap();
        allEmpMap.put("employeeId",null);
        allEmpMap.put("deviceId",doorObj==null?null:doorObj.getDeviceId());
        List<Map> commandList = doorEmployeeMapper.selectRelateEmpCommand(allEmpMap);

        if(commandList!=null && commandList.size()>0){
            //根据人员的ID对指令数据进行分组
            Map<String,List<Map>> empCommandMap = new HashMap<>();
            for(int i=0;i<commandList.size();i++){
                String key = commandList.get(i).get("employee_id").toString();
                if(empCommandMap.containsKey(key)){
                    Map innerMap = new HashMap();
                    innerMap.put("actionCode",commandList.get(i).get("action_code").toString());
                    innerMap.put("sendTime",commandList.get(i).get("send_time"));
                    innerMap.put("status",commandList.get(i).get("status"));

                    empCommandMap.get(key).add(innerMap);
                }else{
                    List<Map> list = new ArrayList();
                    Map innerMap = new HashMap();
                    innerMap.put("actionCode",commandList.get(i).get("action_code").toString());
                    innerMap.put("sendTime",commandList.get(i).get("send_time"));
                    innerMap.put("status",commandList.get(i).get("status"));
                    list.add(innerMap);
                    empCommandMap.put(key,list);
                }
            }

            //判断最新指令的类型和状态（删除人员/下发人员    <待发送、下发中、下发成功、下发失败>）
            for(String key:empCommandMap.keySet()){
                int commandNum = empCommandMap.get(key).size();
                //根据人员的ID和公司的ID查询人员的名称
                Map requestParam = new HashMap();
                requestParam.put("empId",key);
                requestParam.put("companyId",companyId);
                if(commandNum==1){
                    String actionCode = empCommandMap.get(key).get(0).get("actionCode").toString();
                    String status = empCommandMap.get(key).get(0).get("status").toString();
                    if(actionCode.equals("2002")){ //删除人员权限
                        //判断删除指令的状态
                        if(status.equals("4")){
                            //删除该人员在door_employee表中的信息
                            Map delMap = new HashMap();
                            delMap.put("doorId",doorObj.getDoorId());
                            delMap.put("employeeId",key);
                            doorEmployeeMapper.deleteByDoorIdAndEmployeeId(delMap);
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据门的ID查询门的名称
     * @param doorId
     * @return
     */
    @Override
    public String queryDoorNameByDoorId(String doorId) {
        Door door = doorMapper.selectByPrimaryKey(doorId);
        if(door!=null){
            return door.getDoorName();
        }else{
            return null;
        }
    }



    /**
     *查询具有门禁权限的人员一周的开门时间段
     * @param empId
     * @return
     */
    @Override
    public List<Map> queryAWeekOpenTime(String empId,String doorId) {
        Map param = new HashMap();
        param.put("empId",empId);
        param.put("doorId",doorId);
        List<Map> maps = doorEmployeeMapper.selectAWeekOpenTime(param);
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
    public List<Map> queryFirstCardKeepOpenInfo(String doorId,String companyId) {
        Map param  = new HashMap();
        param.put("doorId",doorId);
        param.put("companyId",companyId);
        List<Map> maps = timeRangePrivilegeEmployeeMapper.selectFirstCardKeepOpenInfo(param);
        return maps;
    }

    /**
     * 根据门的id查询门禁日历信息
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
            if(doorRecordCondition.getDeviceName()!=null&&!doorRecordCondition.getDeviceName().isEmpty()){
                doorRecordCondition.setDeviceName("%"+doorRecordCondition.getDeviceName()+"%");
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

    //门禁配置---功能配置（身份验证失败次数、非法入侵、报警时长、密码、开门事件记录，定时常开）
    @Override
    public void doorCommonSetupAdditional(String doorId, String countLimitAuthenticationFailed, String enableAlarm,
                                          String alarmTimeLength, String publicPassword1, String publicPassword2, String threatenPassword,
                                          String deviceManagePassword, String enableDoorOpenRecord, List oneWeekTimeDoorKeepList,
                                          String enableDoorKeepOpen, String enableFirstCardKeepOpen, String enableDoorCalendar,
                                          String operatorEmployeeId) {

        //获取设备id
        Door doorInfo = doorMapper.findAllByDoorId(doorId);

        String deviceId = doorInfo.getDeviceId();

        //本地存储的门禁配置数据结构
        DoorSetting doorSetting = new DoorSetting();
        doorSetting.setDoorId(doorId);
        doorSetting.setFaultCountAuthentication(countLimitAuthenticationFailed);
        if (enableAlarm.equals("0")){
            doorSetting.setAlarmTimeLengthTrespass(alarmTimeLength);
        }else if (enableAlarm.equals("1")){
            doorSetting.setAlarmTimeLengthTrespass("0");
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
        if (doorTimingKeepOpenList.size() > 0){
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
                String isAllDay = oneWeekTimeMap.get("isAllDay");
                String isDitto = oneWeekTimeMap.get("isDitto");

                DoorTimingKeepOpen doorTimingKeepOpen = new DoorTimingKeepOpen();
                doorTimingKeepOpen.setDoorId(doorId);
                doorTimingKeepOpen.setDayOfWeek(weekType);
                doorTimingKeepOpen.setTimingOpenStartTime(startTime);
                doorTimingKeepOpen.setTimingOpenEndTime(endTime);
                doorTimingKeepOpen.setIsAllDay(isAllDay);
                doorTimingKeepOpen.setIsDitto(isDitto);

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
        doorCmdEmployeeInformation.setServerId(serverId);
        doorCmdEmployeeInformation.setDeviceId(deviceId);
        doorCmdEmployeeInformation.setFileEdition("v1.3");
        doorCmdEmployeeInformation.setCommandMode("C");
        doorCmdEmployeeInformation.setCommandType("single");
        doorCmdEmployeeInformation.setCommandTotal("1");
        doorCmdEmployeeInformation.setCommandIndex("1");
        doorCmdEmployeeInformation.setSubCmdId("");
        doorCmdEmployeeInformation.setAction("UPDATE_ACCESS_CONTROL_SETTING");
        doorCmdEmployeeInformation.setActionCode("3003");
        doorCmdEmployeeInformation.setOperateEmployeeId(operatorEmployeeId);

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
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(deviceId, userInformationAll);
    }

    //门禁配置---功能配置（首卡常开权限）
    @Override
    public void handOutFirstCard(String doorId, String enableFirstCardKeepOpen, List<String> employeeIdList, List oneWeekTimeFirstCardList, String operatorEmployeeId) {

        System.out.println("employeeIdList = "+JSON.toJSONString(employeeIdList));
        //获取设备id
        Door doorInfo = doorMapper.findAllByDoorId(doorId);

        String deviceId = doorInfo.getDeviceId();

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

        //遍历人员
        for (String employeeId : employeeIdList) {

            //判断某个人的首卡常开时间区间信息是否存在
            List<TimeRangePrivilegeEmployee> timeRangePrivilegeEmployeeList = timeRangePrivilegeEmployeeMapper.selectExitByEmployeeId(employeeId);
            if (timeRangePrivilegeEmployeeList != null){
                //有信息存在则删除该人的所有首卡常开时间区间信息然后后面的时候重新添加
                timeRangePrivilegeEmployeeMapper.deleteByEmployeeId(employeeId);
            }

            //首卡常开时间判断及存入数据库
            if (oneWeekTimeFirstCardList.size() > 0){

                List<Map<String, String>> oneWeekTimeCollection = (List<Map<String, String>>) oneWeekTimeFirstCardList;
                for (Map<String, String> oneWeekTimeMap : oneWeekTimeCollection) {
                    String startTime = oneWeekTimeMap.get("startTime");
                    String endTime = oneWeekTimeMap.get("endTime");
                    String startWeekNumber = oneWeekTimeMap.get("startWeekNumber");
                    String endWeekNumber = oneWeekTimeMap.get("endWeekNumber");

                    TimeRangePrivilegeEmployee timeRangePrivilegeEmployee = new TimeRangePrivilegeEmployee();
                    timeRangePrivilegeEmployee.setEmployeeId(employeeId);
                    timeRangePrivilegeEmployee.setDoorId(doorId);
                    timeRangePrivilegeEmployee.setRangeStartTime(startTime);
                    timeRangePrivilegeEmployee.setRangeEndTime(endTime);
                    timeRangePrivilegeEmployee.setStartWeekNumber(startWeekNumber);
                    timeRangePrivilegeEmployee.setEndWeekNumber(endWeekNumber);
                    timeRangePrivilegeEmployee.setRangeFlagId(FormatUtil.createUuid());

                    //更新首卡常开时间段
                    timeRangePrivilegeEmployeeMapper.insertSelective(timeRangePrivilegeEmployee);
                }
            }
        }

        //重新组装数据（发给设备的格式）
        List oneWeekTimeFirstCardListTemp = new ArrayList<>();
        for (int i=0; i<oneWeekTimeFirstCardList.size(); i++) {
            String startTime = ((List<Map<String, String>>) oneWeekTimeFirstCardList).get(i).get("startTime");
            String endTime = ((List<Map<String, String>>) oneWeekTimeFirstCardList).get(i).get("endTime");
            String startWeekString = ((List<Map<String, String>>) oneWeekTimeFirstCardList).get(i).get("startWeekNumber");
            String endWeekString = ((List<Map<String, String>>) oneWeekTimeFirstCardList).get(i).get("endWeekNumber");
            int startWeekNumber = Integer.parseInt(startWeekString);
            int endWeekNumber = Integer.parseInt(endWeekString);

            for (int j=0; j<(endWeekNumber-startWeekNumber+1); j++){
                Map<String, String> oneWeekTimeFirstCardMap = new HashMap<String, String>();

                oneWeekTimeFirstCardMap.put("weekType", String.valueOf((startWeekNumber + j)));
                oneWeekTimeFirstCardMap.put("startTime", startTime);
                oneWeekTimeFirstCardMap.put("endTime", endTime);
                oneWeekTimeFirstCardMap.put("doorOpenType", "");

                oneWeekTimeFirstCardListTemp.add(oneWeekTimeFirstCardMap);
            }
        }

//        System.out.println(JSON.toJSONString(oneWeekTimeFirstCardListTemp));

        //下发的门禁配置DATA数据结构
        Map<String, Object> firstCardSetupMap = new LinkedHashMap<String, Object>();
        firstCardSetupMap.put("employeeIdList", employeeIdList);
        firstCardSetupMap.put("oneWeekTimeList", oneWeekTimeFirstCardListTemp);

        //下发门禁常规设置
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
        doorCmdEmployeeInformation.setAction("UPDATE_FIRST_CARD_NORMAL_OPENED");
        doorCmdEmployeeInformation.setActionCode("3004");
        doorCmdEmployeeInformation.setOperateEmployeeId(operatorEmployeeId);

        doorCmdEmployeeInformation.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdEmployeeInformation.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
        doorCmdEmployeeInformation.setSuperCmdId(FormatUtil.createUuid());
        doorCmdEmployeeInformation.setData(JSON.toJSONString(firstCardSetupMap));

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
        rabbitMQSender.sendMessage(deviceId, userInformationAll);
        System.out.println("userInformationAll"+JSON.toJSONString(userInformationAll));
    }

    //门禁配置---功能配置（门禁日历）
    @Override
    public void handOutDoorCalendar(String doorId, String enableDoorCalendar, List accessCalendar, String operatorEmployeeId) {

        //获取设备id
        Door doorInfo = doorMapper.findAllByDoorId(doorId);

        String deviceId = doorInfo.getDeviceId();

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

        //门禁日历时间判断及存入数据库
        if (accessCalendar.size() > 0) {

            List<Map<String, String>> accessCalendarCollection = (List<Map<String, String>>) accessCalendar;
            for (Map<String, String> accessCalendarMap : accessCalendarCollection) {
                String deviceCalendarDate = accessCalendarMap.get("deviceCalendarDate");
                String enableDoorOpenGlobal = accessCalendarMap.get("enableDoorOpenGlobal");

                DoorCalendar doorCalendar = new DoorCalendar();
                doorCalendar.setDoorId(doorId);
                doorCalendar.setCalendarDate(deviceCalendarDate);
                doorCalendar.setWeatherOpenDoor(enableDoorOpenGlobal);

                DoorCalendar doorCalendarExist = doorCalendarMapper.selectDoorCalendarExist(doorCalendar);

                if (doorCalendarExist == null){
                    //更新门禁定时常开时间段
                    doorCalendarMapper.insertSelective(doorCalendar);
                }else {
                    doorCalendarMapper.updateByPrimaryKeySelective(doorCalendar);
                }
            }
        }

        //下发门禁常规设置
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
        doorCmdEmployeeInformation.setAction("UPDATE_ACCESS_CALENDER");
        doorCmdEmployeeInformation.setActionCode("3005");
        doorCmdEmployeeInformation.setOperateEmployeeId(operatorEmployeeId);

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
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(deviceId, userInformationAll);
    }

    //门禁记录上传存储（mq过来的门禁记录信息）
    @Override
    public void doorRecordSave(String doorRecordMap) {

        //提取数据
        Map<String, Object> doorRecordMapTemp = (Map<String, Object>) JSONObject.fromObject(doorRecordMap);
        List<Map<String, String>> doorRecordList = (List<Map<String, String>>)doorRecordMapTemp.get("data");
        String resultCode;
        String resultMessage;

        String deviceId = (String) doorRecordMapTemp.get("deviceId");

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

            String doorId = "";
            try {
                doorId = doorMapper.findDoorIdByDeviceId((String) doorRecordMapTemp.get("deviceId")).getDoorId();
                doorRecord.setDoorId(doorId);
            }catch (Exception e){
                System.out.println("门禁记录上传：该设备还未关联门，将不记录门id");
                doorRecord.setDoorId("");
            }

            doorRecord.setDeviceGroupName(recordMap.get("deviceName"));
            doorRecord.setRecordDate(recordMap.get("attTime"));
            doorRecord.setRealWeek(recordMap.get("week"));
            doorRecord.setEventPhotoGroupId(recordMap.get("eventPhotoCombinationId"));
            doorRecord.setDeviceId(deviceId);

            //查找记录是否重复上传
            DoorRecord doorRecordExit = doorRecordMapper.selectByRecordIdDoorIdAndDeviceId(recordMap.get("id"), doorId, deviceId);

            if (doorRecordExit == null){
                //保存门禁记录到数据库
                doorRecordMapper.insertSelective(doorRecord);
            }else {
                //设备恢复出厂设置了，记录id又会从1开始，这时候需要根据时间区分记录是否保存
                Boolean judge = DateUtils.isTime1LtTime2(doorRecordExit.getRecordDate(), recordMap.get("week"));
                if (judge){
                    doorRecordMapper.insertSelective(doorRecord);
                }

//                //更新门禁记录到数据库
//                doorRecordMapper.updateByPrimaryKeySelective(doorRecord);
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
        doorCmdRecord.setServerId(serverId);
        doorCmdRecord.setDeviceId(deviceId);
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
        //立即下发回复数据到MQ
        rabbitMQSender.sendMessage(deviceId, doorRecordAll);
//        System.out.println("门禁记录上传已回复MQ");
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

    //TODO 查询一个人在一段时间之内的最早最晚打卡时间
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
     * 查询指定公司人员的签到签退情况
     * @return
     *
     *   {
     *       "empName":"人员名称"，
     *       "deptName":"部门名称"，
     *       "recordTime":"2017-12-10~2017-12-20"----->记录时间段
     *       "page":"当前页码",
     *       "rows":"每一页要显示的行数"
     *   }
     */
    @Override
    public Map querySignInAndOutRecord(String requestParam,String companyId) {
        //解析数据
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Object empName = jsonObject.get("empName");
        Object deptName = jsonObject.get("deptName");
        Object recordTime = jsonObject.get("recordTime");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");

        //获取签到签退记录
        Map param = new HashMap();
        param.put("companyId",companyId);
        param.put("empName",(empName!=null && !empName.toString().isEmpty())?"%"+empName.toString()+"%":null);
        param.put("deptName",(deptName!=null && !deptName.toString().isEmpty())?"%"+deptName.toString()+"%":null);
        if(recordTime!=null && !recordTime.toString().trim().isEmpty()){
            param.put("recordStartTime",recordTime.toString().split("~")[0].trim());
            param.put("recordEndTime",recordTime.toString().split("~")[1].trim());
        }else{
            param.put("recordStartTime",null);
            param.put("recordEndTime",null);
        }
        //进行分页操作
        Page pageObj = null;
        if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
            pageObj = PageHelper.startPage(Integer.parseInt(page.toString()), Integer.parseInt(rows.toString()));
        }
        List<SignInAndOut> signInAndOuts = doorRecordMapper.selectSignInAndOutRecord(param);
        //返回结果
        Map resultMap = new HashMap();
        //重新遍历数据，更改数据结构
        List<Map> realData = new ArrayList<>();
        if(signInAndOuts!=null && signInAndOuts.size()>0){
            for(SignInAndOut sign : signInAndOuts){
                Map innerMap = new HashMap();
                innerMap.put("empId",sign.getEmpId());
                innerMap.put("empName",sign.getEmpName());
                innerMap.put("empDept",sign.getEmpDept());
                innerMap.put("punchCardTime",sign.getSignIn().split(" ")[0].trim());
                innerMap.put("signIn",sign.getSignIn().split(" ")[1].trim());
                innerMap.put("signOut",sign.getSignOut().split(" ")[1].trim());

                realData.add(innerMap);
            }
        }
        resultMap = PageUtils.doSplitPage(null, realData, page, rows, pageObj,2);
        return resultMap;
    }

    /**
     * 导出考勤记录（出入记录、门禁异常）到Excel
     * @param excelName 导出的Excel表的名称
     * @param out 输出流
     * @param companyId 公司ID
     */
    @Override
    public void exportRecordToExcel(String requestParam,String excelName, OutputStream out, String companyId) {
        //解析请求参数
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Object flag = jsonObject.get("flag");

        if(flag!=null && !flag.toString().trim().isEmpty()){
            int value =  Integer.parseInt(flag.toString().trim());
            switch (value){
                case 0:  //条件查询出入记录
                    Map doorRecord = entranceGuardController.getDoorRecordAndException(requestParam, companyId, 0);
                    Object doorData = doorRecord.get("data");
                    if(doorData!=null && !doorData.toString().isEmpty()){
                        //要导出的出入记录数据
                        List<Map> doorResource = (List<Map>)doorData;
                        String[] inOutHeaders = new String[]{"姓名", "所属部门", "设备名称", "打卡方式","打卡时间"};
                        //导出出入记录
                        ExportRecordUtil.exportAnyRecordToExcel(doorResource,excelName,inOutHeaders,out,value);
                    }
                    break;
                case 1:  //条件查询门禁异常
                    Map exceptionRecord = entranceGuardController.getDoorRecordAndException(requestParam, companyId, 1);
                    Object exceptionData = exceptionRecord.get("data");
                    if(exceptionData!=null && !exceptionData.toString().isEmpty()){
                        //要导出的门禁异常记录数据
                        List<Map> doorExceptionResource = (List<Map>)exceptionData;
                        String[] exceptionHeaders = new String[]{"姓名", "所属部门", "设备名称", "日期","报警记录"};
                        //导出门禁异常记录
                        ExportRecordUtil.exportAnyRecordToExcel(doorExceptionResource,excelName,exceptionHeaders,out,value);
                    }
                    break;
                case 2:  //条件查询签到签退记录
                    Object empName = jsonObject.get("empName");
                    Object deptName = jsonObject.get("deptName");
                    Object recordTime = jsonObject.get("recordTime");
                    //获取签到签退记录
                    Map param = new HashMap();
                    param.put("companyId",companyId);
                    param.put("empName",(empName!=null && !empName.toString().isEmpty())?"%"+empName.toString()+"%":null);
                    param.put("deptName",(deptName!=null && !deptName.toString().isEmpty())?"%"+deptName.toString()+"%":null);
                    if(recordTime!=null && !recordTime.toString().trim().isEmpty()){
                        param.put("recordStartTime",recordTime.toString().split("~")[0]);
                        param.put("recordEndTime",recordTime.toString().split("~")[1]);
                    }else{
                        param.put("recordStartTime",null);
                        param.put("recordEndTime",null);
                    }
                    //查询签到记录
                    List<SignInAndOut> signInAndOutRecord = doorRecordMapper.selectSignInAndOutRecord(param);

                    String[] signInAndOutHeaders = new String[]{"ID","姓名", "所属部门", "打卡日期", "签到/签退"};
                    //导出签到签退记录
                    ExportRecordUtil.exportAnyRecordToExcel(signInAndOutRecord,excelName,signInAndOutHeaders,out,value);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * TODO APP接口，查询员工的打卡记录
     */
    @Override
    public List<Map> queryEmpPunchCardRecord(String requestParam,String companyId) {
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Object empId = jsonObject.get("empId");
        Object searchTime = jsonObject.get("searchTime");
       /* Object companyId = jsonObject.get("companyId");*/

        List<Map> punchCardRecord = null;
        if(empId!=null){
            Map map = new HashMap();
            map.put("empId",empId.toString());
            map.put("recordDate",(searchTime==null || searchTime.toString().isEmpty())?"%"+DateUtils.getDate()+"%":"%"+searchTime+"%");
            map.put("companyId",companyId!=null&& !companyId.toString().trim().isEmpty()?companyId.toString():null);
            punchCardRecord = doorRecordMapper.selectEmpPunchRecord(map);
        }else{
            System.out.println("APP---------->传递的员工参数为null !!!");
        }
        return punchCardRecord;
    }
}
