package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorException;
import com.xiangshangban.device.bean.DoorExceptionCondition;
import com.xiangshangban.device.bean.DoorRecordCondition;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface DoorExceptionMapper {
    int deleteByPrimaryKey(String doorExceptionId);

    int insert(DoorException record);

    int insertSelective(DoorException record);

    DoorException selectByPrimaryKey(String doorExceptionId);

    int updateByPrimaryKeySelective(DoorException record);

    int updateByPrimaryKey(DoorException record);

    /**
     * 多条件查询所有的门禁异常信息
     */
    List<Map> selectDoorExceptionRecord(@Param("doorRecordCondition") DoorRecordCondition doorRecordCondition);
}