package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.ReturnCodeUtil;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.common.utils.PageUtils;
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
     *"doorIdList": [
    001,------->门的ID
    002
    ]
     */
    @PostMapping("/basic/delDoor")
    public String delDoor(@RequestBody String requestParam) {
        JSONArray objects = JSONArray.parseArray(JSONObject.parseObject(requestParam).get("doorIdList").toString());
        List list = new ArrayList();
        for(int i=0;i<objects.size();i++){
            list.add(JSONObject.parseObject(objects.get(i).toString()).get("door_id"));
        }
        boolean result = iegs.delDoorInfoByBatch(list);
        return JSONArray.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * 添加门信息
     * {
     *     "doorName":"超级大门",
     *     "deviceId":"1"
     * }
     */
    @PostMapping(value = "/basic/addDoor")
    public String addDoor(@RequestBody String requestParam) {
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object deviceId = jsonObject.get("deviceId");

        Door door = new Door();
        door.setDoorName(doorName!=null?doorName.toString():null);
        door.setDeviceId(deviceId!=null?deviceId.toString():null);
        //查询主键的最大值
        door.setDoorId(String.valueOf(iegs.queryPrimaryKeyFromDoor()+1));
        door.setOperateTime(DateUtils.getDateTime());
        door.setOperateEmployee("1");//获取当前的登录人员ID
        boolean result = iegs.addDoorInfo(door);
        return JSONArray.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * 修改门信息（更改门关联的设备）
     {
     *     "doorName":"超级大门",
     *     "deviceId":"1"
     *     "doorId":""
     * }
     */
    @PostMapping(value = "/basic/updateDoor")
    public String updateDoor(@RequestBody String requestParam) {
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object deviceId = jsonObject.get("deviceId");
        Object doorId = jsonObject.get("doorId");

        Door door = new Door();
        door.setDoorName(doorName!=null?doorName.toString():null);
        door.setDeviceId(deviceId!=null?deviceId.toString():null);
        door.setDoorId(doorId!=null?doorId.toString():null);
        door.setOperateEmployee("1");//当前登录的人员的ID

        boolean result = iegs.updateDoorInfo(door);
        return JSONArray.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * 查询门信息（根据门名称）
     * {
     *     "page":"",----->当前页码
     *     "rows":"",----->每一页要显示的行数
     *     "doorName":"" ------>搜索时的门名称
     *     "companyId":""
     * }
     */
    @PostMapping(value = "/basic/selectDoor")
    public String selectDoor(@RequestBody String requestParam) {
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        Object companyId = jsonObject.get("companyId");

        Map doorMap = new HashMap();
        doorMap.put("doorName",jsonObject.get("doorName")==null?null:"%"+jsonObject.get("doorName").toString()+"%");
        doorMap.put("companyId",jsonObject.get("companyId")==null?null:jsonObject.get("companyId").toString());
        Page pageObj = null;
        if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
            pageObj = PageHelper.startPage(Integer.parseInt(page.toString()),Integer.parseInt(rows.toString()));
        }

        List<Map> maps = iegs.queryAllDoorInfo(doorMap);
        Map result =  result  = PageUtils.doSplitPage(null,maps,page,rows,pageObj);
        return JSONArray.toJSONString(result);
    }

    //TODO 门禁管理------“日志管理”

    /**
     * 查看日志
     * @param requestParam
     * @return
     * {
     *     "companyName":"无敌的公司",----->企业名称
     *     "deviceName":"无敌的设备",----------------------------------->设备名称
     *     "operateType":"1",----------------->操作类型ID
     *     "time":"2017-11-20 18:00",--------->时间
     *     "page":"1",------------->当前页码
     *     "rows":"10"------------->每一页显示的行数
     * }
     */
    @PostMapping("/log/getLogCommand")
    public String getLogoCommand(@RequestBody String requestParam){
        Map logs = iegs.queryLogCommand(requestParam);
        return JSONObject.toJSONString(logs);
    }

    /**
     * 批量删除日志
         {
         "logIdList":[
         {"log_id":"26C8746239514090926B68CE0A07AA60"},
         {"log_id":"725E7BD1B1F44DFAB4C64CA09D790E4A"}
         ]
         }
     */
    @PostMapping("/log/delLogCommand")
    public String delLogCommand(@RequestBody String requestParam){
        boolean result = iegs.clearLogCommand(requestParam);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    //TODO 门禁管理------“授权中心”
    /**

     * 获取门信息（包括关联的人员信息）
     * @param requestParam
     * @return
     * {
     *     "page":"1",
     *     "rows":"10",
     *     "doorName":"超级大门"
     *     "companyId":""----->公司Id
     * }
     */
    @PostMapping("/autho/queryDoor")
    public  String queryDoor(@RequestBody String requestParam) {

        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object companyId = jsonObject.get("companyId");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        DoorEmployee doorEmployee = new DoorEmployee();

        Map doorEmployeeMap = new HashMap();

        doorEmployeeMap.put("doorName",doorName != null ? doorName.toString() : null);
        doorEmployeeMap.put("companyId",companyId != null ? companyId.toString() : null);

        //查询门关联的数据
        List<Map> maps = iegs.authoQueryAllDoor(doorEmployeeMap);

        Map dataItem;
        List<Map> innerList;
        Map innerMap;
        Map<String, List<Map>> resultMap = new HashMap<String, List<Map>>();
        Map<String, Map<String, String>> realMap = new HashMap<String, Map<String, String>>();
        List<String> keyList = new ArrayList<String>();

        //根据门名称对数据进行分组
        for (int i = 0; i < maps.size(); i++) {
            //maps集合中的每一个map
            dataItem = maps.get(i);
            //如果拥有对应的key的话，去取出该key对应的List<Map>
            if (resultMap.containsKey(dataItem.get("door_name"))) {
                //获取该List<Map>然后向其中添加一个map
                resultMap.get(dataItem.get("door_name")).add(dataItem);
            } else {
                List<Map> list = new ArrayList<Map>();
                list.add(dataItem);
                resultMap.put(dataItem.get("door_name").toString(), list);
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
        for (int j = 0; j < newList.size(); j++) {

            //关联信息的数量
            int relatePhone = 0;
            int relateFace = 0;
            int relateNFC = 0;
            String doorId = "";
            innerList = resultMap.get(newList.get(j));

            for (int k = 0; k < innerList.size(); k++) {
                /**
                 * innerMap数据样式：{employee_nfc=32rrwef, door_name=一号门, door_id=1, employee_face=fsdfsdsfs, employee_phone=12121}
                 */
                innerMap = innerList.get(k);
                //关联手机号数量
                if (innerMap.get("employee_phone") != null && !innerMap.get("employee_phone").toString().isEmpty()) {
                    relatePhone++;
                }
                //关联人脸数量
                if (innerMap.get("employee_face") != null && !innerMap.get("employee_face").toString().isEmpty()) {
                    relateFace++;
                }
                //关联NFC数量
                if (innerMap.get("employee_nfc") != null && !innerMap.get("employee_nfc").toString().isEmpty()) {
                    relateNFC++;
                }
                if (innerMap.get("door_id") != null && !innerMap.get("door_id").toString().isEmpty()) {
                    doorId = innerMap.get("door_id").toString();
                }
            }

            //包装数据
            if (realMap.containsKey(newList.get(j).toString())) {
                if (realMap.get(newList.get(j)).get("relatePhone") != null) {
                    realMap.get(newList.get(j)).put("relatePhone", String.valueOf(relatePhone));
                }
                if (realMap.get(newList.get(j)).get("relateFace") != null) {
                    realMap.get(newList.get(j)).put("relateFace", String.valueOf(relateFace));
                }
                if (realMap.get(newList.get(j)).get("relateNFC") != null) {
                    realMap.get(newList.get(j)).put("relateNFC",String.valueOf(relateNFC));
                }
            } else {
                Map<String, String> map = new HashMap<String, String>();
                map.put("doorId", doorId);
                map.put("relatePhone", String.valueOf(relatePhone));
                map.put("relateFace", String.valueOf(relateFace));
                map.put("relateNFC", String.valueOf(relateNFC));
                realMap.put(newList.get(j).toString(), map);
            }
        }

        /**
         * 格式化数据的样式
         * {"一号门":{"relateNFC":1,"relateFace":1,"relatePhone":1,door_id=1},"二号门":{"relateNFC":2,"relateFace":1,"relatePhone":2,door_id=2}}
         */
        List<Map> outterList = new ArrayList<Map>();

        //拼接最后下发时间
        String timeDoorId;
        String outterDoorId;
        //查询当前门下发人员权限信息的最后下发时间
        List<Map> sentTimes = iegs.querySendTime(doorName != null ? doorName.toString() : null);

        for (int h = 0; h < newList.size(); h++) {
            Map outterMap = new HashedMap();

            outterMap.put("doorName", newList.get(h));
            outterMap.put("relateInfo", realMap.get(newList.get(h)));

            outterDoorId = realMap.get(newList.get(h)).get("doorId");
            for (int j = 0; j < sentTimes.size(); j++) {
                timeDoorId = sentTimes.get(j).get("doorId").toString();
                if (timeDoorId.equals(outterDoorId)) {
                    outterMap.put("sendTime", sentTimes.get(j).get("sendTime"));
                }else{
                    outterMap.put("sendTime","");
                }
            }
            outterList.add(outterMap);
        }

        List<Map> newInfo = new ArrayList<Map>();

        if(outterList!=null&&outterList.size()>0){
            //进行分页操作
            if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
                int pageIndex = Integer.parseInt(page.toString());
                int pageSize = Integer.parseInt(rows.toString());

                for (int i = ((pageIndex - 1) * pageSize); i < (pageSize * pageIndex); i++) {
                    if(i==outterList.size()){
                        break;
                    }
                    newInfo.add(outterList.get(i));
                }
            }
        }
        Map result  = PageUtils.doSplitPage(outterList,newInfo,page,rows,null);
        return JSONArray.toJSONString(result);
    }

    /**
     * 查询门关联的用户的权限信息（员工姓名、部门、开门方式，开门时间、设备指令下发时间、状态）
     * @return  String doorId,String empName,String deptName,String openType,String issueState,String page,String rows
     * {
     *     "doorId":"001",
     *     "empName":"xx人员",
     *     "deptName":"xx部门",
     *     "openTime":"19:00-22:00",
     *     "openType":"0",
     *     "page":"1",
     *     "rows":"10"
     * }
     */
    @PostMapping("/autho/getRelateEmpPermissionInfo")
    public String getRelateEmpPermissionInfo(@RequestBody String requestParam){
        //定义指令的状态
        String[] cmdStatusStr = {"待发送","下发中","下发成功","下发失败","删除人员权限","已回复"};
        //定义开门方式
        String[] openTypeStr = {"卡","个人密码","卡+个人密码","指纹","人脸","手机蓝牙","手机NFC"};

        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorId = jsonObject.get("doorId");
        Object empName = jsonObject.get("empName");
        Object deptName = jsonObject.get("deptName");
        Object openTime = jsonObject.get("openTime");
        Object openType = jsonObject.get("openType");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");

        Map relateEmpPermissionCondition = new HashMap();
        relateEmpPermissionCondition.put("doorId",doorId!=null?doorId.toString():null);
        relateEmpPermissionCondition.put("empName",empName!=null?empName.toString():null);
        relateEmpPermissionCondition.put("deptName",deptName!=null?deptName.toString():null);
        relateEmpPermissionCondition.put("openTime",openTime!=null?openTime.toString():null);
        relateEmpPermissionCondition.put("openType",openType!=null?openType.toString():null);
        Page pageObj = null;
        //分页
        if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
            pageObj = PageHelper.startPage(Integer.parseInt(page.toString()),Integer.parseInt(rows.toString()));
        }
        //门关联的《员工姓名、部门、开门方式，开门时间》
        List<Map> maps = iegs.queryRelateEmpPermissionInfo(relateEmpPermissionCondition);
        //将开门方式和命令状态由数字更改为文字信息
        for(int i=0;i<maps.size();i++){
            String statusStr = maps.get(i).get("status").toString();
            String openDoorType = maps.get(i).get("range_door_open_type").toString();
           for(int s=0;s<cmdStatusStr.length;s++){
                if(statusStr.equals(String.valueOf(s))){
                    maps.get(i).put("status",cmdStatusStr[i].toString());
                }
           }
           for(int t=0;t<openTypeStr.length;t++){
               if(openDoorType.equals(String.valueOf(t))){
                   maps.get(i).put("range_door_open_type",openTypeStr[t].toString());
               }
           }
        }
        Map result = PageUtils.doSplitPage(null,maps,page,rows,pageObj);
        return JSONArray.toJSONString(result);
    }

    /**
     * 查询具有门禁权限的人员一周的开门时间
     * @param requestParam
     * @return
     * {
     *     "empId":"0F87BE675874465AA64D366EBCA0CF9A"-------------->人员ID
     * }
     */
    @PostMapping("/autho/getAWeekOpenTime")
    public String getAWeekOpenTime(@RequestBody String requestParam){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        List<Map> aWeekTimeList = iegs.queryAWeekOpenTime(jsonObject == null ? null : jsonObject.get("empId").toString());

        //将数据根据星期进行分组
        Map<String,List<Map>> listMap = new HashMap();
        for(int i=0;i<aWeekTimeList.size();i++){

            if(listMap.containsKey(aWeekTimeList.get(i).get("day_of_week"))){//存在该map
                Map map = new HashMap();
                map.put("startTime",aWeekTimeList.get(i).get("range_start_time"));
                map.put("endTime",aWeekTimeList.get(i).get("range_end_time"));
                //获取到该list，向其中添加map数据
                listMap.get(aWeekTimeList.get(i).get("day_of_week").toString()).add(map);
            }else{
                Map map = new HashMap();
                map.put("startTime",aWeekTimeList.get(i).get("range_start_time"));
                map.put("endTime",aWeekTimeList.get(i).get("range_end_time"));

                List list = new ArrayList();
                list.add(map);

                listMap.put(aWeekTimeList.get(i).get("day_of_week").toString(),list);
            }
        }
       Map result =  ReturnCodeUtil.addReturnCode(listMap);
       return JSONObject.toJSONString(result);
    }

    /**
     * 高级设置（功能模块默认信息查询）
     * {
     *     "doorId":"1"
     * }
     */
    @PostMapping ("/autho/getHighSettingForFunction")
    public String getHighSettingForFunction(@RequestBody String requestParam){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorId = jsonObject.get("doorId");
        //获取该门的设置信息
        List<Map> doorSetting = iegs.queryDoorSettingInfo(doorId!=null?doorId.toString():null);
        //移除报警时长
        doorSetting.get(0).remove("alarm_time_length_trespass");
        //移除身份认证上限次数
        doorSetting.get(0).remove("fault_count_authentication");

        //定时常开信息
        List<DoorTimingKeepOpen> doorTimingKeepOpens = iegs.queryKeepOpenInfo(doorId!=null?doorId.toString():null);
        //获取该门上具有首卡常开权限的人员信息
        List<Map> firstCardKeepOpen = iegs.queryFirstCardKeepOpenInfo(doorId!=null?doorId.toString():null);

       /* Map<String,List> firstOpenEmp = new HashMap<String,List>();
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
        firstCardKeepOpen.add(firstOpenEmp);*/

        //门禁日历信息
        List<DoorCalendar> doorCalendars = iegs.queryDoorCalendarInfo(doorId!=null?doorId.toString():null);

        Map map = new HashMap();

        map.put("doorSetting",doorSetting.get(0));
        map.put("timingKeepOpen",doorTimingKeepOpens);
        map.put("firstCardKeepOpen",firstCardKeepOpen);
        map.put("doorCanlendar",doorCalendars);

        return JSONArray.toJSONString(ReturnCodeUtil.addReturnCode(map));
    }

    /**
     * 高级设置（常规模块默认信息查询）
     * @param requestParam
     * @return
     * {
     *     "doorId":"1"
     * }
     */
    @PostMapping("/autho/getHighSettingForConvention")
    public String getHighSettingForConvention(@RequestBody String requestParam){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorId = jsonObject.get("doorId");
        List<Map> maps = iegs.queryDoorSettingInfo(doorId!=null?doorId.toString():null);
        //完善数据
        for(int i=0;i<maps.size();i++){
            if(maps.get(i).get("alarm_time_length_trespass")!=null){
                maps.get(i).put("alarmFlag","1");//报警
            }else{
                maps.get(i).put("alarmFlag","0");//不报警
            }
        }

        //挑选出常规模块需要的数据
        Map conventionInfo = new HashMap();
        conventionInfo.put("alarm_time_length_trespass",maps.get(0).get("alarm_time_length_trespass")==null?"":maps.get(0).get("alarm_time_length_trespass").toString());
        conventionInfo.put("fault_count_authentication",maps.get(0).get("fault_count_authentication")==null?"":maps.get(0).get("fault_count_authentication").toString());
        conventionInfo.put("alarmFlag",maps.get(0).get("alarmFlag")==null?"":maps.get(0).get("alarmFlag").toString());

        return JSONArray.toJSONString(ReturnCodeUtil.addReturnCode(conventionInfo));
    }

    //TODO 门禁管理------------门禁记录
    //1)出入记录
    /**
     * {
     *     "empName":"",
     *     "companyId":"",----->当前登陆的管理员的所属公司ID
     *     "dept":"",
     *     "recordType":"",
     *     "recordTime":"",
     *     "page":"",
     *     "rows":"",
     * }
     *
     */
    @PostMapping("/record/getInOutRecord")
    public String getInOutRecord(@RequestBody String requestParam){
        Map doorRecordAndException = getDoorRecordAndException(requestParam,0);
        return JSONArray.toJSONString(doorRecordAndException);
    }

    // 2)门禁异常

    /**
     *{
     *     "empName":"",
     *     "companyId":"",----->当前登陆的管理员的所属公司ID
     *     "dept":"",
     *     "alarmType":"",
     *     "alarmTime":"",
     *     "page":"",
     *     "rows":"",
     *}
     */
    @PostMapping("/record/getDoorException")
    public String getDoorException(@RequestBody String requestParam){
        Map doorRecordAndException = getDoorRecordAndException(requestParam, 1);
        return JSONArray.toJSONString(doorRecordAndException);
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
    @PostMapping ("/record/getPunchCardRecord")
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

    //TODO 获取门禁记录和门禁异常
    public Map getDoorRecordAndException(String requestParam,int flag) {
        //定义记录类型
        String[] recordTypeName = {"公共密码开锁", "胁迫密码开锁", "个人密码开锁", "卡开锁", "卡+个人密码开锁", "指纹开锁", "人脸开锁", "手机蓝牙开锁",
                "手机NFC开锁", "延时开锁", "定时常开开锁", "定时常开关锁", "首卡常开开锁", "首开常开到时关锁", "首开常开首卡关锁", "远程限时开锁",
                "远程限时关锁", "远程定时开锁", "远程定时关锁", "电子钥匙（访客）开锁", "消防联动触发开锁", "消防联动解除关锁", "消防联动撤防关锁", "门开", "门关",
                "开门超时报警触发", "开门超时报警解除", "身份认证失败报警", "非法入侵报警触发", "非法入侵报警解除", "防拆报警触发", "防拆报警解除", "消防联动报警触发", "消防联动报警解除"};

            JSONObject jsonObject = JSONObject.parseObject(requestParam);

            Object recordType = jsonObject.get(flag==0?"recordType":"alarmType");
            Object recordTime = jsonObject.get(flag==0?"recordTime":"alarmTime");
            Object empName = jsonObject.get("empName");
            Object companyId = jsonObject.get("companyId");
            Object dept = jsonObject.get("dept");
            Object page = jsonObject.get("page");
            Object rows = jsonObject.get("rows");

            DoorRecordCondition doorRecordCondition = new DoorRecordCondition();

            doorRecordCondition.setName(empName != null ? empName.toString() : null);
            doorRecordCondition.setCompanyId(companyId != null ? companyId.toString() : null);
            doorRecordCondition.setDepartment(dept != null ? dept.toString() : null);
            doorRecordCondition.setPunchCardType(recordType != null ? recordType.toString() : null);
            doorRecordCondition.setPunchCardTime(recordTime != null ? recordTime.toString() : null);

            Page pageObj = null;
            if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
                pageObj = PageHelper.startPage(Integer.parseInt(page.toString()), Integer.parseInt(rows.toString()));
            }

            //查询打卡记录和门禁异常
            List<Map> doorRecords = iegs.queryPunchCardRecord(doorRecordCondition,flag);

            Map result = PageUtils.doSplitPage(null, doorRecords, page, rows, pageObj);

            if (doorRecords != null && doorRecords.size() > 0) {
                //完善记录类型名称信息
                JSONArray dataArray = JSONArray.parseArray(JSONObject.toJSONString(result.get("data")));
                List recordList = new ArrayList();
                for (int i = 0; i < dataArray.size(); i++) {
                    JSONObject jSObject = JSONObject.parseObject(dataArray.get(i).toString());
                    String record_type = jSObject.get("record_type").toString();
                    Map recordMap = new HashMap();

                    //遍历所有的记录类型
                    for (int r = 0; r < recordTypeName.length; r++) {
                        if (record_type.equals(String.valueOf(r))) {
                            recordMap.put(flag==0?"record_type":"alarm_type",String.valueOf(r));
                            recordMap.put(flag==0?"record_type_name":"alarm_type_name",recordTypeName[r]);
                        }
                    }

                    recordMap.put(flag==0?"record_date":"alarm_date",jSObject.get("record_date"));
                    recordMap.put("employee_department_name", jSObject.get("employee_department_name"));
                    recordMap.put("employee_name", jSObject.get("employee_name"));

                    recordList.add(recordMap);
                }

                //移除旧的data，添加新的data
                result.remove("data");
                result.put("data", recordList);
            }
           return result;
    }
}




