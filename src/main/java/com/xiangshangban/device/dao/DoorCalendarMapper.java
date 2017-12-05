package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorCalendar;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoorCalendarMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorCalendar record);

    int insertSelective(DoorCalendar record);

    List<DoorCalendar> selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(DoorCalendar record);

    int updateByPrimaryKey(DoorCalendar record);

    /**
     * 根据门的id查询门禁信息
     * @param doorId
     * @return
     */
    List<DoorCalendar> selectDoorCalendarInfo(@Param("doorId") String doorId);

    /**
     * 查询是否有这一条门禁日历数据
     * @param doorCalendar
     * @return
     */
    DoorCalendar selectDoorCalendarExist(DoorCalendar doorCalendar);
}