package com.xiangshangban.device.service.impl;

import com.xiangshangban.device.service.IEntranceGuardService;
import com.xiangshangban.device.bean.*;
import com.xiangshangban.device.dao.DoorEmployeeMapper;
import com.xiangshangban.device.dao.DoorExceptionMapper;
import com.xiangshangban.device.dao.DoorMapper;
import com.xiangshangban.device.dao.DoorRecordMapper;
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
}
