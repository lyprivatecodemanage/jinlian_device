package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorEmployee;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DoorEmployeeMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(DoorEmployee record);

    int insertSelective(DoorEmployee record);

    DoorEmployee selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(DoorEmployee record);

    int updateByPrimaryKey(DoorEmployee record);
}