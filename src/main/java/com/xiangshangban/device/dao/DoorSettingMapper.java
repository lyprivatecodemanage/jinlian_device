package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorSetting;
import org.springframework.stereotype.Component;

@Component
public interface DoorSettingMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorSetting record);

    int insertSelective(DoorSetting record);

    DoorSetting selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(DoorSetting record);

    int updateByPrimaryKey(DoorSetting record);
}