package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorRecord;
import com.xiangshangban.device.bean.DoorRecordCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

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
    List<DoorRecord> selectPunchCardRecord(@Param("doorRecordCondition") DoorRecordCondition doorRecordCondition);
}