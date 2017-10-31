package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
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
    @RequestMapping("/basic/delDoor.do")
    public String delDoor(List<String> list) {
        boolean b = iegs.delDoorInfoByBatch(list);
        return JSONArray.toJSONString(b);
    }

    /**
     * 添加门信息
     */
    @PostMapping(value = "/basic/addDoor.do")
    public String addDoor(Door door) {
        boolean b = iegs.addDoorInfo(door);
        return JSONArray.toJSONString(b);
    }

    /**
     * 修改门信息（更改门关联的设备）
     */
    @PostMapping(value = "/basic/updateDoor.do")
    public String updateDoor(Door door) {
        boolean b = iegs.updateDoorInfo(door);
        return JSONArray.toJSONString(b);
    }

    /**
     * 查询门信息（根据门名称）
     */
    @GetMapping(value = "/basci/selectDoor.do")
    public String selectDoor(Door door) {
        List<Map> maps = iegs.queryAllDoorInfo(door);
        return JSONArray.toJSONString(maps);
    }

    //TODO 门禁管理------“授权中心”

    /**
     * 获取门信息（包括关联的人员信息）
     * @param doorName
     * @return
     */
    @RequestMapping("/autho/queryDoor.do")
    public  String queryDoor(String doorName){
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

           //计算关联数据的数量
           for(int k=0;k<innerList.size();k++){
               /**
                * innerMap数据样式：{employee_nfc=32rrwef, door_name=一号门, door_id=1, employee_face=fsdfsdsfs, employee_phone=12121}
                */
               innerMap = innerList.get(k);
               //关联手机号数量
               if(!innerMap.get("employee_phone").toString().isEmpty()){
                   relatePhone++;
               }
               //关联人脸数量
               if(!innerMap.get("employee_face").toString().isEmpty()){
                   relateFace++;
               }
               //关联NFC数量
               if(!innerMap.get("employee_nfc").toString().isEmpty()){
                   relateNFC++;
               }
               if(!innerMap.get("door_id").toString().isEmpty()){
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
        return JSONArray.toJSONString(outterList);
    }

    /**
     * 查询门关联的用户的权限信息（员工姓名、部门、开门方式，开门时间、设备指令下发时间、状态、数据）
     * @return
     */
    @RequestMapping("/autho/getRelateEmpPermissionInfo.do")
    public String getRelateEmpPermissionInfo(String doorId,String empName,String deptName,String openType,String issueState){

        RelateEmpPermissionCondition relateEmpPermissionCondition = new RelateEmpPermissionCondition();
        relateEmpPermissionCondition.setDoorId(doorId);
        relateEmpPermissionCondition.setEmpName(empName);
        relateEmpPermissionCondition.setDeptName(deptName);
        relateEmpPermissionCondition.setOpenType(openType);
        relateEmpPermissionCondition.setIssueState(issueState);

        //门关联的《员工姓名、部门、开门方式，开门时间》
        List<Map> maps = iegs.queryRelateEmpPermissionInfo(relateEmpPermissionCondition);
        List<String> deviceIds = new ArrayList<String>();
        for (int i=0;i<maps.size();i++){
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

            //匹配员工和对应的指令
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
            listMaps.add(map);
        }

        //组拼数据（将用户的：姓名、部门、开门时间、开门方式、和最后下发时间、下发状态进行组拼）
        String aEmpId = "0";
        for(int a=0;a<maps.size();a++){//长度为9
            aEmpId = maps.get(a).get("employee_id").toString();
            for(int b=0;b<listMaps.size();b++){
                if(listMaps.get(b).containsKey(aEmpId)){
                    //将该key对应的value拼接到maps中
                    maps.get(a).put("cmdInfo", listMaps.get(b).get(aEmpId));
                }
            }
        }

        return JSONArray.toJSONString(maps);
    }

    //TODO 门禁管理------------门禁记录

    //1)出入记录

    @RequestMapping("/record/getInOutRecord.do")
    public String getInOutRecord(String empName,String dept,String recordType,String recordTime){
        DoorRecordCondition doorRecordCondition = new DoorRecordCondition();

        doorRecordCondition.setName(empName);
        doorRecordCondition.setDepartment(dept);
        doorRecordCondition.setPunchCardType(recordType);
        doorRecordCondition.setPunchCardTime(recordTime);

        List<DoorRecord> doorRecords = iegs.queryPunchCardRecord(doorRecordCondition);
        return JSONArray.toJSONString(doorRecords);
    }

    // 2)门禁异常

    @RequestMapping("/record/getDoorException.do")
    public String getDoorException(String empName,String dept,String alarmType,String alarmTime){
        DoorExceptionCondition doorExceptionCondition = new DoorExceptionCondition();

        doorExceptionCondition.setName(empName);
        doorExceptionCondition.setDepartment(dept);
        doorExceptionCondition.setAlarmType(alarmType);
        doorExceptionCondition.setAlarmTime(alarmTime);

        List<DoorException> doorExceptions = iegs.queryDoorExceptionRecord(doorExceptionCondition);
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

    }

    //TODO 门禁记录上传存储
}
