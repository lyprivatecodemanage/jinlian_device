package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.common.encode.MD5Util;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.*;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.dao.DoorRecordMapper;
import com.xiangshangban.device.dao.EmployeeMapper;
import com.xiangshangban.device.dao.OSSFileMapper;
import com.xiangshangban.device.service.IEntranceGuardService;
import org.apache.commons.collections.map.HashedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * 控制层：门禁操作
 */
@RestController
@RequestMapping("/door")
public class EntranceGuardController {

    @Value("${command.timeout.seconds}")
    String commandTimeoutSeconds;

    @Autowired
    private IEntranceGuardService iEntranceGuardService;

    @Autowired
    private DoorMapper doorMapper;

    @Autowired
    private OSSController ossController;

    @Autowired
    private DoorRecordMapper doorRecordMapper;

    @Autowired
    private EmployeeMapper employeeMapper;

    @Autowired
    private OSSFileMapper ossFileMapper;

    //TODO 门禁管理-------“基础信息”

    /**
     * 删除门信息（解除门和设备的绑定关系）
     *
     * @return "doorIdList": [
     * 001,------->门的ID
     * 002
     * ]
     */
    @PostMapping("/basic/delDoor")
    public String delDoor(@RequestBody String requestParam) {
        JSONArray objects = JSONArray.parseArray(JSONObject.parseObject(requestParam).get("doorIdList").toString());
        List list = new ArrayList();
        for (int i = 0; i < objects.size(); i++) {
            list.add(JSONObject.parseObject(objects.get(i).toString()).get("door_id"));
        }
        boolean result = iEntranceGuardService.delDoorInfoByBatch(list);
        return JSONArray.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * ----已修改----
     * 添加门信息
     * {
     *   "doorName":"超级大门",
     *   "deviceId":"1"
     * }
     */
    @PostMapping(value = "/basic/addDoor")
    public String addDoor(@RequestBody String requestParam,HttpServletRequest request){

        //绑定门的时候，同一个门只能绑定一个设备

        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object deviceId = jsonObject.get("deviceId");
        //获取当前登录人的ID
        String operateUserId = request.getHeader("accessUserId");

        Map resultMap = new HashMap();
        if(deviceId!=null){
            Door door = new Door();
            door.setDoorName(doorName != null ? doorName.toString() : null);
            door.setDeviceId(deviceId != null ? deviceId.toString() : null);
            //查询主键的最大值
            String doorPrimary = iEntranceGuardService.queryPrimaryKeyFromDoor();
            door.setDoorId((doorPrimary ==null || doorPrimary.isEmpty())?"1":String.valueOf(Integer.parseInt(doorPrimary)+1));
            door.setOperateTime(DateUtils.getDateTime());
            if(operateUserId==null|| operateUserId.isEmpty()){
                //未知的登录人ID
                resultMap = ReturnCodeUtil.addReturnCode(3);
            }else{
                door.setOperateEmployee(operateUserId);//设置操作人
                boolean result = iEntranceGuardService.addDoorInfo(door);
                resultMap = ReturnCodeUtil.addReturnCode(result);
            }
        }else{
            //该门未绑定设备
           resultMap = ReturnCodeUtil.addReturnCode(2);
        }
        return JSONObject.toJSONString( resultMap);
    }

    /**
     * 修改门信息（更改门关联的设备）
     * {
     * "doorName":"超级大门",
     * "deviceId":"1"
     * "doorId":""
     * }
     */
    @PostMapping(value = "/basic/updateDoor")
    public String updateDoor(@RequestBody String requestParam,HttpServletRequest request) {
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object deviceId = jsonObject.get("deviceId");
        Object doorId = jsonObject.get("doorId");
        //获取当前登陆人的ID
        String operateUserId = request.getHeader("accessUserId");
        Map resultMap;

        if(deviceId!=null){
            Door door = new Door();
            door.setDoorName(doorName != null ? doorName.toString() : null);
            door.setDeviceId(deviceId != null ? deviceId.toString() : null);
            door.setDoorId(doorId != null ? doorId.toString() : null);
            if(operateUserId==null|| operateUserId.isEmpty()){
                //未知的登录人ID
                resultMap = ReturnCodeUtil.addReturnCode(3);
            }else{
                door.setOperateEmployee(operateUserId);//当前登录的人员的ID
                boolean result = iEntranceGuardService.updateDoorInfo(door);
                resultMap = ReturnCodeUtil.addReturnCode(result);
            }
        }else{
            //该门未绑定设备
            resultMap = ReturnCodeUtil.addReturnCode(2);
        }
        return JSONArray.toJSONString(resultMap);
    }

    /**
     * ----已修改----（关联的人员需要添加公司的ID限制）
     * 查询门信息（根据门名称）
     * {
     * "page":"",----->当前页码
     * "rows":"",----->每一页要显示的行数
     * "doorName":"" ------>搜索时的门名称
     * "companyId":""
     * }
     */
    @PostMapping(value = "/basic/selectDoor")
    public String selectDoor(@RequestBody String requestParam,HttpServletRequest request) {
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorName = jsonObject.get("doorName");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        Object companyId = jsonObject.get("companyId");
        Map result;

        if(companyId!=null){
            Map doorMap = new HashMap();
            doorMap.put("doorName", (doorName == null || doorName.toString().isEmpty())? null : "%" + doorName.toString() + "%");
            doorMap.put("companyId", (companyId == null || companyId.toString().isEmpty())? null : companyId.toString());
            Page pageObj = null;
            if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
                pageObj = PageHelper.startPage(Integer.parseInt(page.toString()), Integer.parseInt(rows.toString()));
            }
            List<Map> maps = iEntranceGuardService.queryAllDoorInfo(doorMap);
            result = PageUtils.doSplitPage(null, maps, page, rows, pageObj,2);
        }else{
            result = ReturnCodeUtil.addReturnCode(1);
        }
        return JSONObject.toJSONString(result);
    }

    //TODO 门禁管理-------“日志管理”
    /**
     * 查看各个企业的日志
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
        Map logs = iEntranceGuardService.queryLogCommand(requestParam);
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
        boolean result = iEntranceGuardService.clearLogCommand(requestParam);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    //TODO 门禁管理--------“授权中心”
    /**
     * ----已修改----
     * 获取当前公司的门信息（包括关联的人员信息）
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

        Map doorEmployeeMap = new HashMap();

        doorEmployeeMap.put("doorName",doorName != null ? doorName.toString() : null);
        doorEmployeeMap.put("companyId",companyId != null ? companyId.toString() : null);

        //查询门关联的数据
        List<Map> maps = iEntranceGuardService.authoQueryAllDoor(doorEmployeeMap);
        /**
         * 格式化数据的样式
         * {"一号门":{"relateNFC":1,"relateFace":1,"relatePhone":1,door_id=1},"二号门":{"relateNFC":2,"relateFace":1,"relatePhone":2,door_id=2}}
         */
        List<Map> outterList = new ArrayList<Map>();
        List<Map> newInfo = new ArrayList<Map>();
        //最后返回给前端展示的结果
        Map result  = null;

        if(maps!=null && maps.size()>0){
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

                //遍历当前门关联的所有人员信息
                for (int k = 0; k < innerList.size(); k++) {
                    /**
                     * innerMap数据样式：{employee_nfc=32rrwef, door_name=一号门, door_id=1, employee_face=fsdfsdsfs, employee_phone=12121}
                     * ---->获取其中的一个人
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
                    if(realMap.get(newList.get(j)).get("doorId") != null){
                        realMap.get(newList.get(j)).put("doorId",doorId);
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
            //拼接每一扇门的人员指令下发时间
            for(String key:realMap.keySet()){
                String sentTimes = iEntranceGuardService.querySendTime(realMap.get(key).get("doorId"));

                Map outterMap = new HashedMap();
                outterMap.put("doorName",key);
                outterMap.put("relateInfo", realMap.get(key));
                outterMap.put("sendTime",sentTimes==null?"":sentTimes);

                outterList.add(outterMap);
            }
            //进行分页操作
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
        }
        result  = PageUtils.doSplitPage(outterList,newInfo,page,rows,null,1);
        return JSONArray.toJSONString(result);
    }

    /**
     * ----已修改----
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
    public String getRelateEmpPermissionInfo(@RequestBody String requestParam,HttpServletRequest request){

        //定义指令的状态
        String[] cmdStatusStr = {"待发送","下发中","下发成功","下发失败","删除人员权限","已回复"};
        //(自定义)删除指令的状态
        String[] delStatusStr = {"待发送","删除中","删除成功","删除失败"};
        //定义开门方式
        String[] openTypeStr = {"卡","个人密码","卡+个人密码","指纹","人脸","手机蓝牙","手机NFC"};

        //定义最终返回的数据结果集
        Map result = null;

        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorId = jsonObject.get("doorId");
        Object empName = jsonObject.get("empName");
        Object deptName = jsonObject.get("deptName");
        Object openTime = jsonObject.get("openTime");
        Object openType = jsonObject.get("openType");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        //公司ID
        String companyId = request.getHeader("companyId");
        //--打印测试--
        System.out.println("getRelateEmpPermissionInfo(查询的门的Id)==============="+doorId+"==================");

        Map relateEmpPermissionCondition = new HashMap();
        relateEmpPermissionCondition.put("doorId",doorId!=null?doorId.toString():null);
        relateEmpPermissionCondition.put("empName",empName!=null?empName.toString():null);
        relateEmpPermissionCondition.put("deptName",deptName!=null?deptName.toString():null);
        relateEmpPermissionCondition.put("openTime",openTime!=null?openTime.toString():null);
        relateEmpPermissionCondition.put("openType",openType!=null?openType.toString():null);

        //门关联的《员工姓名、部门、开门方式，开门时间，下发状态，下发时间》
        List<Map> maps = iEntranceGuardService.queryRelateEmpPermissionInfo(relateEmpPermissionCondition,companyId);
        //对查询出的数据根据最后下发时间进行排序
        List<DoorPermissionEmp> permissionEmps = new ArrayList<>();
        if(maps!=null && maps.size()>0){
           //将开门方式和命令状态由数字更改为文字信息
           for(int i=0;i<maps.size();i++){
               //先判断是哪种指令(下发、删除) TODO    ========此处要更改（可能获取不到isDelCommand报NULLPointerException）==========
               String commandType = maps.get(i).get("isDelCommand").toString();
               Object openDoorType = maps.get(i).get("range_door_open_type");

               if(commandType.equals("1")){ //删除指令（使用删除指令的状态）
                   Object statusStr = maps.get(i).get("status");
                   for(int s=0;s<delStatusStr.length;s++){
                       if(statusStr==null){
                           maps.get(i).put("status","");
                           continue;
                       }
                       if(statusStr.toString().equals(String.valueOf(s))){
                           maps.get(i).put("status",delStatusStr[s].toString());
                       }
                   }
               }
               if(commandType.equals("0")){ //下发指令（使用下发指令的状态）
                   Object statusStr = maps.get(i).get("status");
                   for(int s=0;s<cmdStatusStr.length;s++){
                       if(statusStr==null){
                           maps.get(i).put("status","");
                           continue;
                       }
                       if(statusStr.toString().equals(String.valueOf(s))){
                           maps.get(i).put("status",cmdStatusStr[s].toString());
                       }
                   }
               }
               //TODO 转换打卡方式（包含组合打卡方式）
               if(openDoorType!=null&&!"".equals(openDoorType)){
                   StringBuffer buffer = new StringBuffer();
                   //遍历打卡方式
                   for(int d = 0;d<openDoorType.toString().trim().length();d++){
                       //判断每一个字符的数值
                       Character c = openDoorType.toString().trim().charAt(d);


                       for(int t=0;t<openTypeStr.length;t++){
                           if(c.toString().equals(String.valueOf(t))){
                               buffer.append(openTypeStr[t].toString()+"-");
                           }
                       }
                   }
                   System.out.println("-----------------"+buffer.toString()+"------------------");
                   maps.get(i).put("range_door_open_type",buffer.toString().substring(0,buffer.toString().length()-1));
               }else{
                   maps.get(i).put("range_door_open_type","");
               }
               maps.get(i).put("day_of_week","1");
           }

            //对查询出的数据根据最后下发时间进行排序
           for(int x = 0;x<maps.size();x++){
               DoorPermissionEmp doorPermissionEmp = new DoorPermissionEmp();
               doorPermissionEmp.setDay_of_week(maps.get(x).get("day_of_week") != null?maps.get(x).get("day_of_week").toString():"");
               doorPermissionEmp.setRange_door_open_type(maps.get(x).get("range_door_open_type")!= null?maps.get(x).get("range_door_open_type").toString():"");
               doorPermissionEmp.setStatus(maps.get(x).get("status")!= null?maps.get(x).get("status").toString():"");
               doorPermissionEmp.setLasttime(maps.get(x).get("lasttime")!= null?maps.get(x).get("lasttime").toString():"");
               doorPermissionEmp.setRange_end_time(maps.get(x).get("range_end_time")!= null?maps.get(x).get("range_end_time").toString():"");
               doorPermissionEmp.setRange_start_time(maps.get(x).get("range_start_time")!= null?maps.get(x).get("range_start_time").toString():"");
               doorPermissionEmp.setEmployee_department_name(maps.get(x).get("employee_department_name")!= null?maps.get(x).get("employee_department_name").toString():"");
               doorPermissionEmp.setEmployee_id(maps.get(x).get("employee_id")!= null?maps.get(x).get("employee_id").toString():"");
               doorPermissionEmp.setEmployee_name(maps.get(x).get("employee_name")!= null?maps.get(x).get("employee_name").toString():"");

               permissionEmps.add(doorPermissionEmp);
           }

           Collections.sort(permissionEmps, new Comparator<DoorPermissionEmp>() {
               @Override
               public int compare(DoorPermissionEmp o1, DoorPermissionEmp o2) {
                   return (o2.getLasttime()+o2.getEmployee_name()).compareTo((o1.getLasttime()+o1.getEmployee_name()));
               }
           });
       }
        List<DoorPermissionEmp> newPermissionEmps = new ArrayList<>();
        //进行分页操作
        if(permissionEmps!=null&&permissionEmps.size()>0){
            //进行分页操作
            if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
                int pageIndex = Integer.parseInt(page.toString());
                int pageSize = Integer.parseInt(rows.toString());

                for (int i = ((pageIndex - 1) * pageSize); i < (pageSize * pageIndex); i++) {
                    if(i==permissionEmps.size()){
                        break;
                    }
                    newPermissionEmps.add(permissionEmps.get(i));
                }
            }
        }
        if((permissionEmps == null || permissionEmps.size()== 0) && (newPermissionEmps == null || newPermissionEmps.size()== 0)){ //无数据
            result.put("returnCode","4203");
            result.put("message","当前列表为空");
        }else{
            result = PageUtils.doSplitPage(permissionEmps,newPermissionEmps,page,rows,null,1);
        }
        if(doorId!=null){
            //根据门的id查询门的名称
            Door door = doorMapper.selectByPrimaryKey(doorId.toString());
            if(door!=null){
                result.put("doorId",doorId);
                result.put("doorName",door.getDoorName());
            }else{
                result.put("doorId","");
                result.put("doorName","");
            }
        }
        return JSONArray.toJSONString(result);
    }

    /**
     *
     * ----已修改----
     * 查询具有门禁权限的人员一周的开门时间
     * @param requestParam
     * @return
     * {
     *     "doorId":"123",----------->新添加的字段（需要前端进行传递）
     *     "empId":"400A6B8A0717481FB1B27B235F2ECBEB"--------------->人员ID
     * }
     */
    @PostMapping("/autho/getAWeekOpenTime")
    public String getAWeekOpenTime(@RequestBody String requestParam,HttpServletRequest request){
        //定义开门方式
      /*  String[] openTypeStr = {"卡","个人密码","卡+个人密码","指纹","人脸","手机蓝牙","手机NFC"};*/
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object empId = jsonObject.get("empId");
        Object doorId = jsonObject.get("doorId");
        String companyId = request.getHeader("companyId");
        Map result = null;
        if(jsonObject!=null && jsonObject.size()>0){
            List<Map> aWeekTimeList = iEntranceGuardService.queryAWeekOpenTime(empId.toString().trim(),(doorId!=null && !doorId.toString().isEmpty())?doorId.toString().trim():null);

            if(aWeekTimeList!=null&&aWeekTimeList.size()>0){
                //获取该人员的有效的开门时间
                Object door_open_start_time = aWeekTimeList.get(0).get("door_open_start_time");
                Object door_open_end_time = aWeekTimeList.get(0).get("door_open_end_time");
                //获取开门方式(一周的开门方式都是一样的，获取其中一天的即可)
                String openType = aWeekTimeList.get(0).get("range_door_open_type").toString();
                //将数据根据星期进行分组
                Map<String,List<Map>> listMap = new HashMap();
                for(int i=0;i<aWeekTimeList.size();i++){

                    if(listMap.containsKey(aWeekTimeList.get(i).get("day_of_week"))){//存在该map
                        Map map = new HashMap();
                        map.put("startTime",aWeekTimeList.get(i).get("range_start_time"));
                        map.put("endTime",aWeekTimeList.get(i).get("range_end_time"));
                        map.put("isDitto",aWeekTimeList.get(i).get("is_ditto"));
                        map.put("isAllDay",aWeekTimeList.get(i).get("is_all_day"));
                        //获取到该list，向其中添加map数据
                        listMap.get(aWeekTimeList.get(i).get("day_of_week").toString()).add(map);
                    }else{
                        Map map = new HashMap();
                        map.put("startTime",aWeekTimeList.get(i).get("range_start_time"));
                        map.put("endTime",aWeekTimeList.get(i).get("range_end_time"));
                        map.put("isDitto",aWeekTimeList.get(i).get("is_ditto"));
                        map.put("isAllDay",aWeekTimeList.get(i).get("is_all_day"));
                        List list = new ArrayList();
                        list.add(map);

                        listMap.put(aWeekTimeList.get(i).get("day_of_week").toString(),list);
                    }
                }
                List<Map> weekInfo = new ArrayList<>();
                //处理数据
                for (String key:listMap.keySet()
                        ) {
                    String isDitto = listMap.get(key).get(0).get("isDitto").toString();
                    String isAllDay = listMap.get(key).get(0).get("isAllDay").toString();

                    //移除isDitto和isAllDay
                    for(int j = 0;j<listMap.get(key).size();j++){
                        listMap.get(key).get(j).remove("isDitto");
                        listMap.get(key).get(j).remove("isAllDay");
                    }
                    Map map = new HashMap();
                    map.put("week",key);
                    map.put("isDitto",isDitto);
                    map.put("isAllDay",isAllDay);
                    map.put("timeRange",listMap.get(key));

                    weekInfo.add(map);
                }

                result =  ReturnCodeUtil.addReturnCode(weekInfo);
                /**
                 * ----已修改-----
                 *通过人员的ID和公司的ID查询人员的名称
                 */
                if(companyId!=null && !companyId.isEmpty()){
                    Map empParam = new HashMap();
                    empParam.put("empId",jsonObject.get("empId").toString().trim());
                    empParam.put("companyId",companyId);
                    String employeeName = employeeMapper.selectEmpNameByComIdAndEmpId(empParam);

                    result.put("openType",openType);
                    result.put("employeeName",employeeName);
                    result.put("door_open_start_time",door_open_start_time);
                    result.put("door_open_end_time",door_open_end_time);
                }else{
                    //未知的人员和公司Id
                    result = ReturnCodeUtil.addReturnCode(3);
                }
            }else{
                result =  ReturnCodeUtil.addReturnCode(aWeekTimeList);
            }
        }else{
            //请求参数异常
            result = ReturnCodeUtil.addReturnCode(1);
        }
        return JSONObject.toJSONString(result);
    }

    /**
     * 授权中心高级设置（默认信息查询）
     * {
     *     "doorId":"17"
     * }
     */
    @PostMapping ("/autho/getHighSettingForFunction")
    public String getHighSettingForFunction(@RequestBody String requestParam,HttpServletRequest request){

        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object doorId = jsonObject.get("doorId");
        String companyId = request.getHeader("companyId");
        Map result;

        if(doorId!=null && !doorId.toString().isEmpty()){
            //获取该门的设置信息
            List<Map> doorSetting = iEntranceGuardService.queryDoorSettingInfo(doorId!=null?doorId.toString():null);
            //声明定时常开标志
            String keepOpenFlag = "";
            //声明首卡常开标志
            String firstCardOpenFlag = "";
            //声明定时常开数据
            List<DoorTimingKeepOpen> doorTimingKeepOpens = new ArrayList<>();
            //声明首卡常开数据
            List<Map> firstCardKeepOpen = new ArrayList<>();


            if(doorSetting!=null && doorSetting.size()>0){
                //完善数据
                for(int i=0;i<doorSetting.size();i++){
                    if(doorSetting.get(i).get("alarm_time_length_trespass")!=null && !doorSetting.get(i).get("alarm_time_length_trespass").equals("0")){
                        doorSetting.get(i).put("alarmFlag","1");//报警
                    }else{
                        doorSetting.get(i).put("alarmFlag","0");//不报警
                    }
                }
                //查看该门的定时常开和首卡常开的标志
                keepOpenFlag = doorSetting.get(0).get("enable_door_keep_open").toString();
                firstCardOpenFlag = doorSetting.get(0).get("enable_first_card_keep_open").toString();
            }

            //开启定时常开的时候再进行查询
            if(!keepOpenFlag.isEmpty() && Integer.parseInt(keepOpenFlag)==1){
                //查询门定时常开信息(每一天分为四个时间段)
                doorTimingKeepOpens = iEntranceGuardService.queryKeepOpenInfo(doorId!=null?doorId.toString():null);
            }

            //开启首开常开的时候再进行查询
            if(!firstCardOpenFlag.isEmpty() && Integer.parseInt(firstCardOpenFlag)==1){
                /**
                 * ----已修改----（查询人员的时候要根据门所在公司的ID进行人员信息的查询）
                 *获取该门首卡常开信息
                 */
                if(companyId!=null && !companyId.isEmpty()){
                    firstCardKeepOpen = iEntranceGuardService.queryFirstCardKeepOpenInfo(doorId!=null?doorId.toString():null,companyId);
                }else{
                    //未知的登录人员Id和公司Id
                    return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(3));
                }
            }
            //整理定时常开数据(根据星期进行分组)
            Map<String,List<Map>> keepOpenMap = new HashMap<>();

            //最终的定时常开数据
            List<Map> timingKeepOpenList = new ArrayList<>();
            if(doorTimingKeepOpens!= null && doorTimingKeepOpens.size()>0){

                for(int k = 0;k<doorTimingKeepOpens.size();k++){
                    String key = doorTimingKeepOpens.get(k).getDayOfWeek();
                    if(keepOpenMap.containsKey(key)){
                        Map weekMap = new HashMap();
                        weekMap.put("startTime",doorTimingKeepOpens.get(k).getTimingOpenStartTime());
                        weekMap.put("endTime",doorTimingKeepOpens.get(k).getTimingOpenEndTime());
                        weekMap.put("isAllDay",doorTimingKeepOpens.get(k).getIsAllDay());
                        weekMap.put("isDitto",doorTimingKeepOpens.get(k).getIsDitto());

                        keepOpenMap.get(key).add(weekMap);
                    }else{
                        List<Map> weekList = new ArrayList<>();
                        Map weekMap = new HashMap();
                        weekMap.put("startTime",doorTimingKeepOpens.get(k).getTimingOpenStartTime());
                        weekMap.put("endTime",doorTimingKeepOpens.get(k).getTimingOpenEndTime());
                        weekMap.put("isAllDay",doorTimingKeepOpens.get(k).getIsAllDay());
                        weekMap.put("isDitto",doorTimingKeepOpens.get(k).getIsDitto());

                        weekList.add(weekMap);

                        keepOpenMap.put(key,weekList);
                    }
                }

                //TODO 更改数据的结构
                for(String realKey:keepOpenMap.keySet()){
                    Object isAllDay = keepOpenMap.get(realKey).get(0).get("isAllDay");
                    Object isDitto = keepOpenMap.get(realKey).get(0).get("isDitto");

                    //移除isDitto和isAllDay
                    for(int j = 0;j<keepOpenMap.get(realKey).size();j++){
                        keepOpenMap.get(realKey).get(j).remove("isDitto");
                        keepOpenMap.get(realKey).get(j).remove("isAllDay");
                    }
                    Map timingKeepOpenMap = new HashMap();

                    timingKeepOpenMap.put("week",realKey);
                    timingKeepOpenMap.put("isAllDay",isAllDay.toString());
                    timingKeepOpenMap.put("isDitto",isDitto.toString());
                    timingKeepOpenMap.put("timeRange",keepOpenMap.get(realKey));

                    timingKeepOpenList.add(timingKeepOpenMap);
                }
            }

            //声明首卡常开最终数据
            Map firstCardKeepOpenMap = null;
            if(firstCardKeepOpen!=null && firstCardKeepOpen.size()>0){
                firstCardKeepOpenMap  = new HashMap();
                firstCardKeepOpenMap.put("startWeekNumber",firstCardKeepOpen.get(0).get("start_week_number").toString());
                firstCardKeepOpenMap.put("endWeekNumber",firstCardKeepOpen.get(0).get("end_week_number").toString());

                Set<FirstCardOpen> firstCardKeepOpenEmpList = new TreeSet<>(new Comparator<FirstCardOpen>() {
                    @Override
                    public int compare(FirstCardOpen o1, FirstCardOpen o2) {
                        return (o1.getEmployeeId()+o1.getEmployeeName()).compareTo(o2.getEmployeeId()+o2.getEmployeeName());
                    }
                });
                Set<FirstCardOpen> firstCardKeepOpenTimeRange = new TreeSet<>(new Comparator<FirstCardOpen>() {
                    @Override
                    public int compare(FirstCardOpen o1, FirstCardOpen o2) {
                        return (o1.getStartTime()+o1.getEndTime()).compareTo(o2.getStartTime()+o2.getEndTime());
                    }
                });

                //整理首卡常开人员
                for(int s = 0;s<firstCardKeepOpen.size();s++){

                    FirstCardOpen firstCardOpenEmp = new FirstCardOpen();
                    FirstCardOpen firstCardOpenTime = new FirstCardOpen();

                    firstCardOpenEmp.setEmployeeId(firstCardKeepOpen.get(s).get("employee_id").toString());
                    firstCardOpenEmp.setEmployeeName(firstCardKeepOpen.get(s).get("employee_name").toString());

                    firstCardOpenTime.setStartTime(firstCardKeepOpen.get(s).get("range_start_time").toString());
                    firstCardOpenTime.setEndTime(firstCardKeepOpen.get(s).get("range_end_time").toString());

                    firstCardKeepOpenEmpList.add(firstCardOpenEmp);
                    firstCardKeepOpenTimeRange.add(firstCardOpenTime);
                }

                firstCardKeepOpenMap.put("employeeIdList",firstCardKeepOpenEmpList);
                firstCardKeepOpenMap.put("timeList",firstCardKeepOpenTimeRange);
            }

            //门禁日历信息
            List<DoorCalendar> doorCalendars = iEntranceGuardService.queryDoorCalendarInfo(doorId!=null?doorId.toString():null);
            Map map = new HashMap();

            map.put("doorSetting",(doorSetting!=null && doorSetting.size()>0)?doorSetting.get(0):"");
            map.put("timingKeepOpen",timingKeepOpenList);
            map.put("firstCardKeepOpen",firstCardKeepOpenMap);
            map.put("doorCalendar",doorCalendars);

            result = ReturnCodeUtil.addReturnCode(map);
        }else{
            result = ReturnCodeUtil.addReturnCode(1);
        }

        return JSONArray.toJSONString(result);
    }

    //TODO 门禁管理-------------门禁记录
    //1)出入记录
    /**
     * ----已修改----
     * {
     *     "empName":"",
     *     "companyId":"",------>当前登陆的企业管理员的所属公司ID
     *     "deviceName":"",
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
     * ----已修改----
     *{
     *     "empName":"",
     *     "companyId":"",----->当前登陆的管理员的所属公司ID
     *     "deviceName":"",
     *     "dept":"",
     *     "alarmType":"",
     *     "alarmTime":"",
     *     "page":"",
     *     "rows":"",
     *}
     */
    @PostMapping("/record/getDoorException")
    public String getDoorException(@RequestBody String requestParam){
        Map doorRecordAndException = getDoorRecordAndException(requestParam,1);
        return JSONArray.toJSONString(doorRecordAndException);
    }

    //TODO 门禁管理------------门禁系统设置下发

    /**
     * 门禁高级设置
     * @param doorFeaturesSetup
     */
    @PostMapping("/handOutDoorFeaturesSetup")
    public ReturnData handOutDoorFeaturesSetup(@RequestBody String doorFeaturesSetup, HttpServletRequest request){

         /**测试数据
         *
          {
          "doorId": "001",
          "loginEmployeeId": "897020EA96214392B28369F2B421E319",
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
          "isAllDay": "1",
          "weekType": "3",
          "startTime": "08:00",
          "endTime": "12:02",
          "isDitto": "1"
          },
          {
          "isAllDay": "1",
          "weekType": "3",
          "startTime": "14:00",
          "endTime": "18:10",
          "isDitto": "1"
          }
          ],
          "oneWeekTimeFirstCardList": [
          {
          "startTime": "08:00",
          "endTime": "12:02",
          "startWeekNumber": "1",
          "endWeekNumber": "7"
          },
          {
          "startTime": "13:00",
          "endTime": "16:00",
          "startWeekNumber": "1",
          "endWeekNumber": "7"
          }
          ],
          "accessCalendarList": [
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

        String operatorEmployeeId = request.getHeader("accessUserId");

        System.out.println("doorFeaturesSetup: "+doorFeaturesSetup);

        //解析数据
        Map<String, Object> setupMap = (Map<String, Object>)net.sf.json.JSONObject.fromObject(doorFeaturesSetup);

        //返回给前端的数据
        ReturnData returnData = new ReturnData();

        String doorId = "";
        String loginEmployeeId = "";
        String countLimitAuthenticationFailed = "";
        String enableAlarm = "";
        String alarmTimeLength = "";
        String publicPassword1 = "";
        String publicPassword2 = "";
        String threatenPassword = "";
        String deviceManagePassword = "";
        String enableDoorOpenRecord = "";
        String enableDoorKeepOpen = "";
        String enableFirstCardKeepOpen = "";
        String enableDoorCalendar = "";
        List<String> employeeIdList = new ArrayList<String>();
        List oneWeekTimeDoorKeepList = new ArrayList();
        List oneWeekTimeFirstCardList = new ArrayList();
        List accessCalendarList = new ArrayList();

        try {
            doorId = (String) setupMap.get("doorId");
            loginEmployeeId = (String) setupMap.get("loginEmployeeId");
            countLimitAuthenticationFailed = (String)setupMap.get("countLimitAuthenticationFailed");
            enableAlarm = (String)setupMap.get("enableAlarm");
            alarmTimeLength = (String)setupMap.get("alarmTimeLength");
            publicPassword1 = (String)setupMap.get("publicPassword1");
            publicPassword2 = (String)setupMap.get("publicPassword2");
            threatenPassword = (String)setupMap.get("threatenPassword");
            deviceManagePassword = (String)setupMap.get("deviceManagePassword");
            enableDoorOpenRecord = (String)setupMap.get("enableDoorOpenRecord");
            enableDoorKeepOpen = (String)setupMap.get("enableDoorKeepOpen");
            enableFirstCardKeepOpen = (String)setupMap.get("enableFirstCardKeepOpen");
            enableDoorCalendar = (String)setupMap.get("enableDoorCalendar");
            employeeIdList = (List<String>) setupMap.get("employeeIdList");
        }catch (Exception e){

            System.out.println("必传参数字段为null");
            returnData.setMessage("必传参数字段为null");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (doorId == null
                || loginEmployeeId == null
                || countLimitAuthenticationFailed == null
                || enableAlarm == null
                || alarmTimeLength == null
                || publicPassword1 == null
                || publicPassword2 == null
                || threatenPassword == null
                || deviceManagePassword == null
                || enableDoorOpenRecord == null
                || enableDoorKeepOpen == null
                || enableFirstCardKeepOpen == null
                || enableDoorCalendar == null
                || employeeIdList == null
                || oneWeekTimeDoorKeepList == null
                || oneWeekTimeFirstCardList == null
                || accessCalendarList == null){
            System.out.println("必传参数字段不存在");
            returnData.setMessage("必传参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        //判断门定时常开是否开启
        if (enableDoorKeepOpen.equals("1")){
            try {
                oneWeekTimeDoorKeepList = (List)setupMap.get("oneWeekTimeDoorKeepList");
            }catch (Exception e){

                System.out.println("必传参数字段为null");
                returnData.setMessage("必传参数字段为null");
                returnData.setReturnCode("3006");
                return returnData;
            }
        }else {
            oneWeekTimeDoorKeepList = new ArrayList<>();
        }

        //判断门定时常开是否开启
        if (enableFirstCardKeepOpen.equals("1")){
            try {
                oneWeekTimeFirstCardList = (List)setupMap.get("oneWeekTimeFirstCardList");
            }catch (Exception e){

                System.out.println("必传参数字段为null");
                returnData.setMessage("必传参数字段为null");
                returnData.setReturnCode("3006");
                return returnData;
            }
        }else {
            oneWeekTimeFirstCardList = new ArrayList<>();
        }

        //判断门定时常开是否开启
        if (enableDoorCalendar.equals("1")){
            try {
                accessCalendarList = (List)setupMap.get("accessCalendarList");
            }catch (Exception e){

                System.out.println("必传参数字段为null");
                returnData.setMessage("必传参数字段为null");
                returnData.setReturnCode("3006");
                return returnData;
            }
        }else {
            accessCalendarList = new ArrayList<>();
        }

        try {
            //下发门禁配置---功能配置（密码、开门事件记录，定时常开）
            iEntranceGuardService.doorCommonSetupAdditional(doorId, countLimitAuthenticationFailed, enableAlarm,
                    alarmTimeLength, publicPassword1, publicPassword2, threatenPassword,
                    deviceManagePassword, enableDoorOpenRecord, oneWeekTimeDoorKeepList,
                    enableDoorKeepOpen, enableFirstCardKeepOpen, enableDoorCalendar, operatorEmployeeId);

            //下发门禁配置---功能配置（首卡常开权限）
            iEntranceGuardService.handOutFirstCard(doorId, enableFirstCardKeepOpen, employeeIdList, oneWeekTimeFirstCardList,
                    operatorEmployeeId);

            //下发门禁配置---功能配置（门禁日历）
            iEntranceGuardService.handOutDoorCalendar(doorId, enableDoorCalendar, accessCalendarList, operatorEmployeeId);

            //记录操作人和操作时间，更新到设备日志表里
            Door doorExist = doorMapper.selectByPrimaryKey(doorId);
            if (doorExist == null){
                System.out.println("门信息不存在");
                returnData.setMessage("门信息不存在");
                returnData.setReturnCode("4007");
                return returnData;
            }else {
                Door door = new Door();
                door.setDoorId(doorId);
                door.setOperateTime(DateUtils.getDateTime());
                door.setOperateEmployee(loginEmployeeId);
                try {
                    doorMapper.updateByPrimaryKeySelective(door);
                }catch (Exception e){
                    returnData.setMessage("没有查到此操作人【"+loginEmployeeId+"】的信息");
                    returnData.setReturnCode("4007");
                    return returnData;
                }
            }

            returnData.setMessage("已执行下发门禁设置操作");
            returnData.setReturnCode("3000");
            return returnData;

        }catch (Exception e){
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 门禁警报记录上传及警报消息实时推送（HTTP POST）
     * @param jsonString
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/doorAlarmRealTimePushMessage", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> doorAlarmRealTimePushMessage(@RequestBody String jsonString){

        /**
         *测试数据
         {
         "serverId": "001",
         "deviceId": "0f1a21d4e6fd3cb8",
         "fileEdition": "v1.3",
         "commandMode": "C",
         "commandType": "single",
         "commandTotal": "1",
         "commandIndex": "1",
         "sendTime": "2017-10-31 19:12:25",
         "outOfTime": "2017-11-03 19:12",
         "MD5Check": "5EA3A2027684295414D4AC2696A8C1CF",
         "command": {
         "superCMDID": "49641B5A57474BE2B5E4BE126AC63C49",
         "subCMDID": "",
         "ACTION": "UPLOAD_ACCESS_ALARM",
         "ACTIONCode": "3007"
         },
         "data": {
         "attData": {
         "attType": "0",
         "deviceId": "111",
         "deviceName": "张三|金念大门",
         "attTime": "2017-10-10 10:06:00",
         "week": "0",
         "eventPhotoCombinationId": "111|1504825665941"
         }
         }
         }
         */

        System.out.println("--------------"+jsonString);
        String jsonUrlDecoderString = UrlUtil.getURLDecoderString(jsonString);
        System.out.println(jsonUrlDecoderString);
        //去除数据的前缀名称
        jsonUrlDecoderString = jsonUrlDecoderString.replace("alarmData=", "");
//        System.out.println(jsonUrlDecoderString);

        //解析json数据
        Map<String, Object> mapResult = (Map<String, Object>) net.sf.json.JSONObject.fromObject(jsonUrlDecoderString);
        String deviceId = (String) mapResult.get("deviceId");

        //回复设备
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
        System.out.println("myMd5="+myMd5);
        //双方的md5比较判断
        if (myMd5.equals(otherMd5)){
            System.out.println("MD5校验成功，数据完好无损");
        }else {
            System.out.println("MD5校验失败，数据已被修改");
        }

        System.out.println("收到实时报警记录："+jsonUrlDecoderString);

        //******************************************
        //*
        //*
        //*此处以后加上消息推送，如推送报警消息到app上
        //*
        //*
        //*******************************************

        //回复设备
        resultCode = "0";
        resultMessage = "执行成功";
        resultData.put("resultCode", resultCode);
        resultData.put("resultMessage", resultMessage);
        resultData.put("returnObj", new ArrayList<>());

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
        doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
        doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
        doorCmdRecord.setData(JSON.toJSONString(resultData));

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
        iEntranceGuardService.insertCommand(doorCmdRecord);

        return doorRecordAll;
    }

    /**
     * 保存设备上传的异常记录图片
     * @param deviceId
     * @param id
     * @param eventPhotoCombinationId
     * @param file
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveUploadAccessAlarmImg", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public Map<String, Object> saveUploadAccessAlarmImg(@RequestParam(value = "deviceId") String deviceId,
                                                        @RequestParam(value = "id") String id,
                                                        @RequestParam(value = "eventPhotoCombinationId") String eventPhotoCombinationId,
                                                        @RequestParam(value="file") MultipartFile file){

        System.out.println("门禁记录id = "+id);
        System.out.println("fileName******: "+file.getOriginalFilename());

        //回复设备
        Map<String, Object> resultData = new LinkedHashMap<String, Object>();
        String resultCode = "";
        String resultMessage = "";

        //根据文件名称查询是否有对应的记录，有则不再上传此文件
        OSSFile ossFile = ossFileMapper.selectByFileName(file.getOriginalFilename());

        if (ossFile == null){
            //上传照片到oss服务器
            String fileJsonString = ossController.deviceOssUpdate(file, "deviceRecordImg", "", deviceId);

            //获取文件上传返回的标识文件的唯一值key
            String key = ((Map<String, String>)net.sf.json.JSONObject.fromObject(fileJsonString)).get("key");

            //将这个文件标识存到门禁记录表里
            DoorRecord doorRecord = new DoorRecord();
            doorRecord.setDoorPermissionRecordId(id);
            doorRecord.setBackKey(key);
            doorRecord.setEventPhotoGroupId(eventPhotoCombinationId);
            DoorRecord doorRecordExist = doorRecordMapper.selectByPrimaryKey(id);
            if (doorRecordExist != null){
                System.out.println("上传的警报记录图片【"+key+"】已成功");
                doorRecordMapper.updateByPrimaryKeySelective(doorRecord);

                //回复设备
                resultCode = "0";
                resultMessage = "执行成功";
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
                Map<String, String> keyMap = new HashMap<String, String>();
                keyMap.put("imgKey", key);
                List<Map<String, String>> returnList = new ArrayList<Map<String, String>>();
                returnList.add(keyMap);
                resultData.put("returnObj", returnList);
            }else {
                System.out.println("上传的警报记录图片【"+key+"】没有与之匹配的记录id");
                //回复设备
                resultCode = "999";
                resultMessage = "上传的警报记录图片【"+key+"】没有与之匹配的记录id";
                resultData.put("resultCode", resultCode);
                resultData.put("resultMessage", resultMessage);
                resultData.put("returnObj", new ArrayList<>());
            }

        }else {
            System.out.println("上传的警报记录图片【"+file.getOriginalFilename()+"】已存在");
            //回复设备
            resultCode = "999";
            resultMessage = "上传的警报记录图片【"+file.getOriginalFilename()+"】已存在";
            resultData.put("resultCode", resultCode);
            resultData.put("resultMessage", resultMessage);
            resultData.put("returnObj", new ArrayList<>());
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
        doorCmdRecord.setAction("UPLOAD_ACCESS_ALARM_IMG");
        doorCmdRecord.setActionCode("3008");
        doorCmdRecord.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdRecord.setOutOfTime(DateUtils.addSecondsConvertToYMDHM(new Date(), commandTimeoutSeconds));
        doorCmdRecord.setSuperCmdId(FormatUtil.createUuid());
        doorCmdRecord.setData(JSON.toJSONString(resultData));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorRecordAll = rabbitMQSender.messagePackaging(doorCmdRecord, "", resultData, "R");
        //命令状态设置为: 已回复
        doorCmdRecord.setStatus("5");
        doorCmdRecord.setResultCode(resultCode);
        doorCmdRecord.setResultMessage(resultMessage);
        //设置md5校验值
        doorCmdRecord.setMd5Check((String) doorRecordAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdRecord.setData(JSON.toJSONString(doorRecordAll.get("result")));
        //命令数据存入数据库
        iEntranceGuardService.insertCommand(doorCmdRecord);

        return doorRecordAll;

    }

    /**
     * 根据公司id查询门列表
     * @param jsonString
     * @return
     */
    @ResponseBody
    @Transactional
    @RequestMapping(value = "/findDoorIdByCompanyId", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public ReturnData findDoorIdByCompanyId(@RequestBody String jsonString){

        /**
         * 测试数据
         {
         "employeeId": "9C305EC5587745FF9F0D8198512264D6"
         }
         */

        Map<String, String> employeeIdMap = (Map<String, String>) net.sf.json.JSONObject.fromObject(jsonString);

        ReturnData returnData = new ReturnData();
        String employeeId = "";
        String companyId = "";

        try {
            employeeId = employeeIdMap.get("employeeId");
        }catch (Exception e){

            System.out.println("人员id参数字段为null");
            returnData.setMessage("人员id字段为null，请登录后再操作");
            returnData.setReturnCode("3006");
            return returnData;
        }

        if (employeeId == null){
            System.out.println("人员id参数字段不存在");
            returnData.setMessage("人员id参数字段不存在");
            returnData.setReturnCode("3006");
            return returnData;
        }

        //根据人员id查询公司id
        try {
            Employee employee = employeeMapper.selectByPrimaryKey(employeeId);
            if (employee != null){
                companyId = employee.getEmployeeCompanyId();
                List<Door> doorList = iEntranceGuardService.findDoorIdByCompanyId(companyId);
                returnData.setData(doorList);
                returnData.setMessage("数据请求成功");
                returnData.setReturnCode("3000");
                return returnData;
            }else {
                returnData.setMessage("没有查到该人员的信息");
                returnData.setReturnCode("4007");
                return returnData;
            }
        }catch (Exception e){
            e.printStackTrace();
            returnData.setMessage("服务器错误");
            returnData.setReturnCode("3001");
            return returnData;
        }
    }

    /**
     * 获取一个人在一段时间内的最早和最晚打卡时间
     */
    @PostMapping ("/record/getPunchCardRecord")
    public String getPunchCardRecord(@RequestBody String requestParam){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        List<String> strings = iEntranceGuardService.queryPunchCardTime(jsonObject.get("empId").toString(),jsonObject.get("companyId").toString(),jsonObject.get("startTime").toString(),jsonObject.get("endTime").toString());
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
                "远程限时关锁", "远程定时开锁", "远程定时关锁", "电子钥匙（访客）开锁", "消防联动报警", "消防联动解除关锁", "消防联动撤防关锁", "门开", "门关",
                "开门超时报警触发", "开门超时报警解除", "身份认证失败报警", "非法入侵报警触发", "非法入侵报警解除", "防拆报警触发", "防拆报警解除", "消防联动报警触发", "消防联动报警解除"};

            JSONObject jsonObject = JSONObject.parseObject(requestParam);

            Object recordType = jsonObject.get(flag==0?"recordType":"alarmType");
            Object recordTime = jsonObject.get(flag==0?"recordTime":"alarmTime");
            Object deviceName = jsonObject.get("deviceName");
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
            doorRecordCondition.setDeviceName(deviceName != null ? deviceName.toString() : null);

            Page pageObj = null;
            if (page != null && !page.toString().isEmpty() && rows != null && !rows.toString().isEmpty()) {
                pageObj = PageHelper.startPage(Integer.parseInt(page.toString()), Integer.parseInt(rows.toString()));
            }
            //查询打卡记录和门禁异常
            List<Map> doorRecords = iEntranceGuardService.queryPunchCardRecord(doorRecordCondition,flag);

            Map result = PageUtils.doSplitPage(null, doorRecords, page, rows, pageObj,2);

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
                    recordMap.put("deviceName",jSObject.get("device_name"));

                    recordList.add(recordMap);
                }

                //移除旧的data，添加新的data
                result.remove("data");
                result.put("data", recordList);
            }
           return result;
    }

    /****************************************************
     * TODO APP接口（获取员工的打卡记录）
     * 请求参数：
         {
         "empId":"897020EA96214392B28369F2B421E319",----->员工ID
         "searchTime":"2017-11-21"--------->搜索时间(不传的时候查询当天)
         "companyId":"xafaf13232"---------->公司ID（备用字段，添加公司切换的时候使用）
         }
     ****************************************************/

    @PostMapping("/app/getEmpPunchCardRecord")
    public String getEmployeePunchCardRecord(@RequestBody String requestParam){
        //定义打卡方式
        String[] punchCardType = {"","","","卡","","指纹","人脸","蓝牙","NFC"};

        List<Map> empPunchCardRecord = iEntranceGuardService.queryEmpPunchCardRecord(requestParam);
        //将数据封装成对象，方便进行排序
        List<PersonalRecordForApp> personalRecordForAppsList = new ArrayList<>();
        if(empPunchCardRecord !=null && empPunchCardRecord.size()>0){
            //将打卡方式由数字装换为文字
            for(int i=0;i<empPunchCardRecord.size();i++){
                String openType = empPunchCardRecord.get(i).get("record_type").toString();
                for(int s = 0;s<punchCardType.length;s++){
                    if(openType.equals(String.valueOf(s))){
                        empPunchCardRecord.get(i).put("openType",punchCardType[s].toString());
                    }
                }
                //移除所有的record_type数字
                empPunchCardRecord.get(i).remove("record_type");
            }

            //根据时间进行排序
            for(int k=0;k<empPunchCardRecord.size();k++){
                PersonalRecordForApp personalRecordForApp = new PersonalRecordForApp();
                personalRecordForApp.setDoorName(empPunchCardRecord.get(k).get("door_name").toString().trim());
                personalRecordForApp.setDoorId(empPunchCardRecord.get(k).get("door_id").toString().trim());
                personalRecordForApp.setRecordDate(empPunchCardRecord.get(k).get("record_date").toString().trim().split(" ")[1]);
                personalRecordForApp.setOpenType(empPunchCardRecord.get(k).get("openType").toString().trim());

                personalRecordForAppsList.add(personalRecordForApp);
            }

            //根据时间进行排序
            Collections.sort(personalRecordForAppsList, new Comparator<PersonalRecordForApp>() {
                @Override
                public int compare(PersonalRecordForApp o1, PersonalRecordForApp o2) {
                    return o1.getRecordDate().compareTo(o2.getRecordDate());
                }
            });

            //将第一个打卡时间和，最后的一个打卡时间的打卡方式更改为（“签到、签退”）
            if(personalRecordForAppsList.size()>0){
                if(personalRecordForAppsList.size()>1){
                    personalRecordForAppsList.get(0).setOpenType(personalRecordForAppsList.get(0).getOpenType()+"(签到)");
                    personalRecordForAppsList.get(personalRecordForAppsList.size()-1).setOpenType(personalRecordForAppsList.get(personalRecordForAppsList.size()-1).getOpenType()+"(签退)");
                }else{
                    personalRecordForAppsList.get(0).setOpenType("签到");
                }
            }
        }
        //添加返回码
        Map map = ReturnCodeUtil.addReturnCode(personalRecordForAppsList!=null && personalRecordForAppsList.size()>0?personalRecordForAppsList:null);
        return JSONObject.toJSONString(map);
    }
}


