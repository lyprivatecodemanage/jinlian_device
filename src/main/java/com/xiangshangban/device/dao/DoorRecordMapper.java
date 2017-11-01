package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorRecord;
import com.xiangshangban.device.bean.DoorRecordCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface DoorRecordMapper {
    int deleteByPrimaryKey(String doorPermissionRecordId);

    int insert(DoorRecord record);

    int insertSelective(DoorRecord record);

    DoorRecord selectByPrimaryKey(String doorPermissionRecordId);

    int updateByPrimaryKeySelective(DoorRecord record);

    int updateByPrimaryKey(DoorRecord record);

    /**
     * 分条件，查询所有的打卡记录
     */
    List<Map> selectPunchCardRecord(@Param("doorRecordCondition") DoorRecordCondition doorRecordCondition);

    /**
     * 查询一段时间内，一个人的最早最晚打卡时间
     */
    List<String> selectPunchCardTime(@Param("map")Map map);
}