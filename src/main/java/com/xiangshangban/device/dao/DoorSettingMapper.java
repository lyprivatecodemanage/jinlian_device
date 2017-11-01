package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorSetting;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface DoorSettingMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorSetting record);

    int insertSelective(DoorSetting record);

    DoorSetting selectByPrimaryKey(@Param("doorId") String doorId);

    int updateByPrimaryKeySelective(DoorSetting record);

    int updateByPrimaryKey(DoorSetting record);

    /**
     * 根据门的id查询门的设置信息
     */
    List<Map> selectDoorSettingInfo(@Param("doorId") String doorId);
}