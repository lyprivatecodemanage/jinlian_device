package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.PageUtils;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 控制层：门禁操作
 */
@RestController
@RequestMapping("/door")
public class EntranceGuardController {

    @Autowired
    private IEntranceGuardService iegs;

    @Autowired
    private DoorMapper doorMapper;

    //TODO 门禁管理------“基础信息”

    /**
     * 删除门信息
     *
     * @return
     */
    @PostMapping("/basic/delDoor")
    public String delDoor(@RequestParam("idList") List<String> list) {
        boolean b = iegs.delDoorInfoByBatch(list);
        return JSONArray.toJSONString(b);
    }

    /**
     * 添加门信息
     */
    @PostMapping(value = "/basic/addDoor")
    public String addDoor(Door door) {
        boolean b = iegs.addDoorInfo(door);
        return JSONArray.toJSONString(b);
    }

    /**
     * 修改门信息（更改门关联的设备）
     */
    @PostMapping(value = "/basic/updateDoor")
    public String updateDoor(Door door) {
        boolean b = iegs.updateDoorInfo(door);
        return JSONArray.toJSONString(b);
    }

    /**
     * 查询门信息（根据门名称）
     */
    @GetMapping(value = "/basci/selectDoor")
    public String selectDoor(String page,String rows,String doorName) {
        Door door = new Door();
        door.setDoorName(doorName);

        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            PageHelper.startPage(Integer.parseInt(page),Integer.parseInt(rows));
        }

        List<Map> maps = iegs.queryAllDoorInfo(door);

        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            //进行分页操作
            maps = PageUtils.doSplitPage(maps);
        }

        return JSONArray.toJSONString(maps);
    }

    //TODO 门禁管理------“授权中心”
    /**

     * 获取门信息（包括关联的人员信息）
     * @param doorName
     * @return
     */
    @GetMapping("/autho/queryDoor")
    public  String queryDoor(String page,String rows,String doorName){
        DoorEmployee doorEmployee = new DoorEmployee();
        doorEmployee.setDoorName(doorName);

        //查询门关联的数据
        List<Map> maps = iegs.authoQueryAllDoor(doorEmployee);

        Map dataItem;
        List<Map> innerList;
        Map innerMap;
        Map<String,List<Map>> resultMap = new HashMap<String,List<Map>>();
        Map<String,Map<String,Integer>> realMap = new HashMap<String,Map<String,Integer>>();
        List<String> keyList = new ArrayList<String>();

        //根据门名称对数据进行分组
        for(int i=0;i<maps.size();i++){
            //maps集合中的每一个map
            dataItem = maps.get(i);
            //如果拥有对应的key的话，去取出该key对应的List<Map>
            if(resultMap.containsKey(dataItem.get("door_name"))){
                //获取该List<Map>然后向其中添加一个map
                resultMap.get(dataItem.get("door_name")).add(dataItem);
            }else{
                List<Map> list = new ArrayList<Map>();
                list.add(dataItem);
                resultMap.put(dataItem.get("door_name").toString(),list);
            }
            //保存key值
            keyList.add(dataItem.get("door_name").toString());
        }
        //去除重复的key
        List newList = new ArrayList(new TreeSet(keyList));
        /**
         * 计算关联数据的数量
         * 一号门=[{employee_nfc=32rrwef, door_name=一号门, door_id=1, employee_face=fsdfsdsfs, employee_phone=12121}]
         */
       for(int j=0;j<newList.size();j++){

           //关联信息的数量
           int relatePhone = 0;
           int relateFace= 0;
           int relateNFC = 0;
           String doorId = "";
           innerList = resultMap.get(newList.get(j));

           for(int k=0;k<innerList.size();k++){
               /**
                * innerMap数据样式：{employee_nfc=32rrwef, door_name=一号门, door_id=1, employee_face=fsdfsdsfs, employee_phone=12121}
                */
               innerMap = innerList.get(k);
               //关联手机号数量
               if(innerMap.get("employee_phone")!=null&&!innerMap.get("employee_phone").toString().isEmpty()){
                   relatePhone++;
               }
               //关联人脸数量
               if(innerMap.get("employee_face")!=null&&!innerMap.get("employee_face").toString().isEmpty()){
                   relateFace++;
               }
               //关联NFC数量
               if(innerMap.get("employee_nfc")!=null&&!innerMap.get("employee_nfc").toString().isEmpty()){
                   relateNFC++;
               }
               if(innerMap.get("door_id")!=null&&!innerMap.get("door_id").toString().isEmpty()){
                   doorId = innerMap.get("door_id").toString();
               }
           }

           //包装数据
           if(realMap.containsKey(newList.get(j).toString())){
               if(realMap.get(newList.get(j)).get("relatePhone")!=null){
                   realMap.get(newList.get(j)).put("relatePhone",relatePhone);
               }
               if(realMap.get(newList.get(j)).get("relateFace")!=null){
                   realMap.get(newList.get(j)).put("relateFace",relateFace);
               }
               if(realMap.get(newList.get(j)).get("relateNFC")!=null){
                   realMap.get(newList.get(j)).put("relateNFC",relateNFC);
               }
           }else{
               Map<String,Integer> map = new HashMap<String,Integer>();
               map.put("doorId",Integer.parseInt(doorId));
               map.put("relatePhone",relatePhone);
               map.put("relateFace",relateFace);
               map.put("relateNFC",relateNFC);
               realMap.put(newList.get(j).toString(),map);
           }
       }

        /**
         * 格式化数据的样式
         * {"一号门":{"relateNFC":1,"relateFace":1,"relatePhone":1,door_id=1},"二号门":{"relateNFC":2,"relateFace":1,"relatePhone":2,door_id=2}}
         */
        List<Map> outterList = new ArrayList<Map>();

        //拼接最后下发时间
        Integer timeDoorId;
        Integer outterDoorId;
        List<Map> sentTimes = iegs.querySendTime(doorName);

        for(int h=0;h<newList.size();h++){
            Map outterMap = new HashedMap();

            outterMap.put("doorName",newList.get(h));
            outterMap.put("relateInfo",realMap.get(newList.get(h)));

            outterDoorId =realMap.get(newList.get(h)).get("doorId");
            for(int j=0;j<sentTimes.size();j++){
                timeDoorId = Integer.parseInt(sentTimes.get(j).get("doorId").toString());
                if(timeDoorId==outterDoorId){
                    outterMap.put("sendTime",sentTimes.get(j).get("sendTime"));
                }
            }
            outterList.add(outterMap);
        }

        List<Map> newInfo = new ArrayList<Map>();
        //进行分页操作
        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            int pageIndex = Integer.parseInt(page);
            int pageSize = Integer.parseInt(rows);

            for(int i=((pageIndex-1)*pageSize);i<(pageSize*pageIndex);i++){
                newInfo.add(outterList.get(i));
            }

            //追加数据总行数
            PageUtils.doSplitPage(newInfo);
            return JSONArray.toJSONString(newInfo);
        }else{
            return JSONArray.toJSONString(outterList);
        }
    }

    /**
     * 查询门关联的用户的权限信息（员工姓名、部门、开门方式，开门时间、设备指令下发时间、状态、数据）
     * @return
     */
    @RequestMapping("/autho/getRelateEmpPermissionInfo")
    public String getRelateEmpPermissionInfo(String doorId,String empName,String deptName,String openType,String issueState,String page,String rows){

        RelateEmpPermissionCondition relateEmpPermissionCondition = new RelateEmpPermissionCondition();
        relateEmpPermissionCondition.setDoorId(doorId);
        relateEmpPermissionCondition.setEmpName(empName);
        relateEmpPermissionCondition.setDeptName(deptName);
        relateEmpPermissionCondition.setOpenType(openType);
        relateEmpPermissionCondition.setIssueState(issueState);

        //分页
        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            PageHelper.startPage(Integer.parseInt(page),Integer.parseInt(rows));
        }

        //门关联的《员工姓名、部门、开门方式，开门时间》
        List<Map> maps = iegs.queryRelateEmpPermissionInfo(relateEmpPermissionCondition);

        List<String> deviceIds = new ArrayList<String>();
        for (int i=0;i<maps.size()-1;i++){
            //获取设备id
            deviceIds.add(maps.get(i).get("device_id").toString());
        }

        //去除重复的deviceId
        List<String> singleDevices = new ArrayList<String>(new TreeSet<String>(deviceIds));

        List<Map> cmdInfo;
        String outterEmpId ="0";
        String  innerEmpId = "0";
        Map<String,List<Map>> allEmpCMD = new HashMap<String,List<Map>>();
        List<String> allEmpCMDKey = new ArrayList<String>();

        //根据设备的id查询设备中的指令
        for(int j=0;j<singleDevices.size();j++){

            relateEmpPermissionCondition.setDeviceId(singleDevices.get(j));
            //一个设备对应用多个用户
            cmdInfo = iegs.queryCMDInfo(relateEmpPermissionCondition);

            //（遍历员工打卡时间、打卡方式信息）匹配员工和对应的指令
            for(int i=0;i<maps.size();i++){

                //保存该员工对应指令信息
                List<Map> empCMDInfo = new ArrayList<Map>();

                outterEmpId = maps.get(i).get("employee_id").toString();

                //遍历该设备中所有的命令
                for(int s=0;s<cmdInfo.size();s++){
                    JSONObject data = JSONObject.parseObject(cmdInfo.get(s).get("data").toString());
                    //获取到命令中的员工id
                   /* innerEmpId = JSONObject.parseObject(data.get("userPermission").toString()).get("employeeId").toString();*/
                   try{
                       //获取命令中包含的empId
                       innerEmpId = JSONObject.parseObject(data.get("userPermission").toString()).get("employeeId").toString();
                   }catch(Exception e){
                       try {
                           innerEmpId = JSONObject.parseObject(data.get("userInfo").toString()).get("userId").toString();
                       }catch(Exception e1){
                       }
                   }
                    //匹配到该员工对应的指令
                    if(innerEmpId.equals(outterEmpId)){
                        //保存该员工对应的指令信息
                        empCMDInfo.add(cmdInfo.get(s));
                    }
                }
                allEmpCMD.put(outterEmpId,empCMDInfo);
                allEmpCMDKey.add(outterEmpId);
            }
        }

        //去除重复的key
        List<String> newKeyList = new ArrayList<String>(new TreeSet<>(allEmpCMDKey));

        //遍历所有员工对应指令，然后挑选出每个员工最后下发的那条指令（包括：下发时间、状态、下发数据）
        List<Map<String,Map>> listMaps = new ArrayList<Map<String,Map>>();
        String firstSendTime = "";
        String secondSendTime = "";

        for(int x = 0;x<allEmpCMD.size();x++){
            //默认第一个元素就是最后的下发时间
            Map lastTime = allEmpCMD.get(newKeyList.get(x)).get(0);

            firstSendTime = lastTime.get("send_time").toString();
            for(int y = 1;y<allEmpCMD.get(newKeyList.get(x)).size();y++){
                secondSendTime = allEmpCMD.get(newKeyList.get(x)).get(y).get("send_time").toString();
                if(firstSendTime.compareTo(secondSendTime)<0){
                    lastTime = allEmpCMD.get(newKeyList.get(x)).get(y);
                }
            }
            Map<String,Map> map = new HashMap<String,Map>();
            map.put(newKeyList.get(x),lastTime);
            //移除data数据
            map.get(newKeyList.get(x)).remove("data");
            listMaps.add(map);
        }

        //组拼数据（将用户的：姓名、部门、开门时间、开门方式、和最后下发时间、下发状态进行组拼）
        String aEmpId = "0";
        for(int a=0;a<maps.size();a++){//12
            aEmpId = maps.get(a).get("employee_id").toString();
            for(int b=0;b<listMaps.size();b++){//5
                if(listMaps.get(b).containsKey(aEmpId)){
                    //将该key对应的value拼接到maps中
                    maps.get(a).put("cmdInfo", listMaps.get(b).get(aEmpId));
                }
            }
        }

        //输出测试数据
        for(int i=0;i<maps.size();i++){
            System.out.println(maps.get(i).get("cmdInfo"));
        }
        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            //追加数据总行数信息
            maps = PageUtils.doSplitPage(maps);
        }
        return JSONArray.toJSONString(maps);
    }

    /**
     * 高级设置（功能模块默认信息查询）
     */
    @GetMapping("/autho/getHighSettingForFunction")
    public String getHighSettingForFunction(@RequestParam(required = true) String doorId){
        //获取该门的设置信息
        List<Map> doorSetting = iegs.queryDoorSettingInfo(doorId);
        //定时常开信息
        List<DoorTimingKeepOpen> doorTimingKeepOpens = iegs.queryKeepOpenInfo(doorId);
        //获取首卡常开信息
        List<Map> firstCardKeepOpen = iegs.queryFirstCardKeepOpenInfo(doorId);

        Map<String,List> firstOpenEmp = new HashMap<String,List>();
        List<String> inner = new ArrayList<String>();

        //挑选出具有首卡常开的人员
        for(int i=0;i<firstCardKeepOpen.size();i++){
            String employee_id = firstCardKeepOpen.get(i).get("employee_id").toString();
            inner.add(employee_id);
        }

        //去除重复的人员ID
        List<String> newInner = new ArrayList<String>(new TreeSet<String>(inner));

        //具有首卡常开权限的人员信息
        firstOpenEmp.put("firstOpenEmp",newInner);

        //去除内部的employee_id和employee_name
        for(int i=0;i<firstCardKeepOpen.size();i++){
            firstCardKeepOpen.get(i).remove("employee_id");
            firstCardKeepOpen.get(i).remove("employee_name");
        }

        //添加具有首卡常开权限的人员信息，到数据集合中
        firstCardKeepOpen.add(firstOpenEmp);

        //门禁日历信息
        List<DoorCalendar> doorCalendars = iegs.queryDoorCalendarInfo(doorId);

        Map map = new HashMap();

        map.put("doorSetting",doorSetting.get(0));
        map.put("timingKeepOpen",doorTimingKeepOpens);
        map.put("firstCardKeepOpen",firstCardKeepOpen);
        map.put("doorCanlendar",doorCalendars);

        return JSONArray.toJSONString(map);
    }

    /**
     * 高级设置（常规模块默认信息查询）
     * @param doorId
     * @return
     */
    @GetMapping("/autho/getHighSettingForConvention")
    public String getHighSettingForConvention(String doorId){
        List<Map> maps = iegs.queryDoorSettingInfo(doorId);
        return JSONArray.toJSONString(maps.get(0));
    }

    //TODO 门禁管理------------门禁记录

    //1)出入记录

    @RequestMapping("/record/getInOutRecord")
    public String getInOutRecord(String empName,String dept,String recordType,String recordTime,String page,String rows){
        DoorRecordCondition doorRecordCondition = new DoorRecordCondition();

        doorRecordCondition.setName(empName);
        doorRecordCondition.setDepartment(dept);
        doorRecordCondition.setPunchCardType(recordType);
        doorRecordCondition.setPunchCardTime(recordTime);


        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            PageHelper.startPage(Integer.parseInt(page),Integer.parseInt(rows));
        }

        List<Map> doorRecords = iegs.queryPunchCardRecord(doorRecordCondition);

        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            //进行分页操作
            doorRecords = PageUtils.doSplitPage(doorRecords);
        }

        return JSONArray.toJSONString(doorRecords);
    }

    // 2)门禁异常

    @RequestMapping("/record/getDoorException")
    public String getDoorException(String empName,String dept,String alarmType,String alarmTime,String page,String rows){
        DoorExceptionCondition doorExceptionCondition = new DoorExceptionCondition();

        doorExceptionCondition.setName(empName);
        doorExceptionCondition.setDepartment(dept);
        doorExceptionCondition.setAlarmType(alarmType);
        doorExceptionCondition.setAlarmTime(alarmTime);

        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            PageHelper.startPage(Integer.parseInt(page),Integer.parseInt(rows));
        }

        List<Map> doorExceptions = iegs.queryDoorExceptionRecord(doorExceptionCondition);

        if(page!=null&&!page.isEmpty()&&rows!=null&&!rows.isEmpty()){
            //进行分页操作
            doorExceptions = PageUtils.doSplitPage(doorExceptions);
        }

        return JSONArray.toJSONString(doorExceptions);
    }

    //TODO 门禁管理------------门禁系统设置下发

    /**
     * 门禁高级设置
     * @param doorFeaturesSetup
     */
    @PostMapping("/doorFeaturesSetup")
    public void doorFeaturesSetup(@RequestBody String doorFeaturesSetup){

        /**测试数据
         *
         {
         "doorId": "001",
         "countLimitAuthenticationFailed": "5",
         "enableAlarm": "0",
         "alarmTimeLength": "60",
         "publicPassword1": "110",
         "publicPassword2": "120",
         "threatenPassword": "130",
         "deviceManagePassword": "18649866",
         "enableDoorOpenRecord": "0",
         "enableDoorKeepOpen": "1",
         "enableFirstCardKeepOpen": "1",
         "enableDoorCalendar": "1",
         "employeeIdList": [
         "897020EA96214392B28369F2B421E319",
         "9C305EC5587745FF9F0D8198512264D6"
         ],
         "oneWeekTimeDoorKeepList": [
         {
         "weekType": "3",
         "startTime": "08:00",
         "endTime": "12:02"
         },
         {
         "weekType": "3",
         "startTime": "14:00",
         "endTime": "18:10"
         }
         ],
         "oneWeekTimeFirstCardList": [
         {
         "weekType": "4",
         "startTime": "08:00",
         "endTime": "12:02",
         "doorOpenType": "234"
         },
         {
         "weekType": "4",
         "startTime": "14:00",
         "endTime": "18:10",
         "doorOpenType": "234"
         }
         ],
         "accessCalendar": [
         {
         "deviceCalendarDate": "2017-10-10",
         "enableDoorOpenGlobal": "0"
         },
         {
         "deviceCalendarDate": "2017-10-12",
         "enableDoorOpenGlobal": "1"
         }
         ]
         }
         */

        //解析数据
        Map<String, Object> setupMap = (Map<String, Object>)net.sf.json.JSONObject.fromObject(doorFeaturesSetup);
        String doorId = (String) setupMap.get("doorId");
        String countLimitAuthenticationFailed = (String)setupMap.get("countLimitAuthenticationFailed");
        String enableAlarm = (String)setupMap.get("enableAlarm");
        String alarmTimeLength = (String)setupMap.get("alarmTimeLength");
        String publicPassword1 = (String)setupMap.get("publicPassword1");
        String publicPassword2 = (String)setupMap.get("publicPassword2");
        String threatenPassword = (String)setupMap.get("threatenPassword");
        String deviceManagePassword = (String)setupMap.get("deviceManagePassword");
        String enableDoorOpenRecord = (String)setupMap.get("enableDoorOpenRecord");
        String enableDoorKeepOpen = (String)setupMap.get("enableDoorKeepOpen");
        String enableFirstCardKeepOpen = (String)setupMap.get("enableFirstCardKeepOpen");
        String enableDoorCalendar = (String)setupMap.get("enableDoorCalendar");
        List oneWeekTimeDoorKeepList = new ArrayList();
        List oneWeekTimeFirstCardList = new ArrayList();
        List accessCalendar = new ArrayList();
        List<String> employeeIdList = (List<String>) setupMap.get("employeeIdList");

        //判断门定时常开是否开启
        if (enableDoorKeepOpen.equals("1")){
            oneWeekTimeDoorKeepList = (List)setupMap.get("oneWeekTimeDoorKeepList");
        }else {
            oneWeekTimeDoorKeepList = new ArrayList<>();
        }

        //判断门定时常开是否开启
        if (enableFirstCardKeepOpen.equals("1")){
            oneWeekTimeFirstCardList = (List)setupMap.get("oneWeekTimeFirstCardList");
        }else {
            oneWeekTimeFirstCardList = new ArrayList<>();
        }

        //判断门定时常开是否开启
        if (enableDoorCalendar.equals("1")){
            accessCalendar = (List)setupMap.get("accessCalendar");
        }else {
            accessCalendar = new ArrayList<>();
        }

        //下发门禁配置---功能配置（密码、开门事件记录）
        iegs.doorCommonSetupAdditional(doorId, countLimitAuthenticationFailed, enableAlarm,
                alarmTimeLength, publicPassword1, publicPassword2, threatenPassword,
                deviceManagePassword, enableDoorOpenRecord, oneWeekTimeDoorKeepList,
                enableDoorKeepOpen, enableFirstCardKeepOpen, enableDoorCalendar);

        //下发门禁配置---功能配置（首卡常开权限）
        iegs.handOutFirstCard(doorId, enableFirstCardKeepOpen, employeeIdList, oneWeekTimeFirstCardList);

        //下发门禁配置---功能配置（门禁日历）
        iegs.handOutDoorCalendar(doorId, enableDoorCalendar, accessCalendar);

        //记录操作人和操作时间，更新到设备日志表里
        Door doorExist = doorMapper.selectByPrimaryKey(doorId);
        if (doorExist == null){
            System.out.println("门信息不存在");
        }else {
            Door door = new Door();
            door.setDoorId(doorId);
            door.setOperateTime(DateUtils.getDateTime());
            door.setOperateEmployee("");
            doorMapper.updateByPrimaryKeySelective(door);
        }

    }

    /**
     * 门禁警报记录上传及警报消息实时推送（HTTP POST）
     * @param message
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/doorAlarmRealTimePushMessage", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> doorAlarmRealTimePushMessage(@RequestBody String message){

        //解析json数据
        Map<String, Object> mapResult = (Map<String, Object>) net.sf.json.JSONObject.fromObject(message);
        String deviceId = (String) mapResult.get("deviceId");

        //回复设备
        Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        String resultCode = "";
        String resultMessage = "";

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
        }else {
            System.out.println("MD5校验失败，数据已被修改");
        }

        System.out.println("收到实时报警记录："+message);

        //******************************************
        //*
        //*
        //*此处以后加上消息推送，如推送报警消息到app上
        //*
        //*
        //******************************************

        //回复设备
        resultCode = "0";
        resultMessage = "执行成功";
        resultData.put("resultCode", resultCode);
        resultData.put("resultMessage", resultMessage);
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
        doorCmdRecord.setAction("UPLOAD_ACCESS_ALARM");
        doorCmdRecord.setActionCode("3007");
        doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdRecord.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
        doorCmdRecord.setData(JSON.toJSONString(resultMap));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorRecordAll =  rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
        //命令状态设置为: 已回复
        doorCmdRecord.setStatus("5");
        doorCmdRecord.setResultCode(resultCode);
        doorCmdRecord.setResultMessage(resultMessage);
        //设置md5校验值
        doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
        //命令数据存入数据库
        iegs.insertCommand(doorCmdRecord);

        return doorRecordAll;
    }



    /**
     * 获取一个人在一段时间内的最早和最晚打卡时间
     */
    @RequestMapping("/record/getPunchCardRecord")
    public String getPunchCardRecord(@RequestBody String requestParam){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        List<String> strings = iegs.queryPunchCardTime(jsonObject.get("empId").toString(),jsonObject.get("companyId").toString(),jsonObject.get("startTime").toString(),jsonObject.get("endTime").toString());
        String result = "";
        if(strings!=null&&strings.size()>0){
            if(strings.size()<2){
                result = strings.get(0);
            }
            if(strings.size()>1){
                result = strings.get(strings.size()-1)+","+strings.get(0);
            }
        }
        return result;
    }
}




