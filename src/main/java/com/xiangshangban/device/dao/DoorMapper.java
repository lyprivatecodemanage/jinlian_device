package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Door;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface DoorMapper {

    int insertSelective(Door record);

    Door selectByPrimaryKey(@Param("doorId") String doorId);

    int updateByPrimaryKeySelective(Door record);

    Door findDoorIdByDeviceId(String deviceId);

    //通过doorId查询所有的信息
    Door findAllByDoorId(String doorId);

    //通过deviceId查询所有的信息
    Door findAllByDeviceId(String deviceId);

    //通过companyId查询所有的门信息
    List<Door> selectAllDoorByCompanyId(String companyId);

    //TODO 基础信息部分
    /**
     * 删除门信息
     * @param doorId
     * @return
     */
    int deleteByPrimaryKey(@Param("doorId")String doorId);

    /**
     * 新增门信息
     * @param record
     * @return
     */
    int insert(Door record);

    /**
     * 通过主键更新门信息
     * @param door
     * @return
     */
    int updateByPrimaryKey(Door door);

    /**
     * 分条件查询门信息
     */
    List<Map> getDoorInfo(Map map);

    /**
     * 批量删除门信息
     * @param doorList
     * @return
     */
    int delDoorBatch(List<String> doorList);

    /**
     * 查询door表主键的最大值
     */
    int selectPrimaryKeyFromDoor();
}