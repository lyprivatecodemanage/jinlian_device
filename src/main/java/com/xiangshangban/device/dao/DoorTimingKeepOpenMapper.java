package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorTimingKeepOpen;

import java.util.List;

public interface DoorTimingKeepOpenMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorTimingKeepOpen record);

    int insertSelective(DoorTimingKeepOpen record);

    DoorTimingKeepOpen selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(DoorTimingKeepOpen record);

    int updateByPrimaryKey(DoorTimingKeepOpen record);

    List<DoorTimingKeepOpen> selectExistByDoorId(String doorId);
}