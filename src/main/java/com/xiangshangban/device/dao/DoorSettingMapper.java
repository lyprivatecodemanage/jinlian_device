package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorSetting;

public interface DoorSettingMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorSetting record);

    int insertSelective(DoorSetting record);

    DoorSetting selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(DoorSetting record);

    int updateByPrimaryKey(DoorSetting record);
}