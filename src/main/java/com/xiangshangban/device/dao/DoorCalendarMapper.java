package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorCalendar;

import java.util.List;

public interface DoorCalendarMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorCalendar record);

    int insertSelective(DoorCalendar record);

    List<DoorCalendar> selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(DoorCalendar record);

    int updateByPrimaryKey(DoorCalendar record);
}