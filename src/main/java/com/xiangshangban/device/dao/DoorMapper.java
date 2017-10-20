package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Door;

public interface DoorMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(Door record);

    int insertSelective(Door record);

    Door selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(Door record);

    int updateByPrimaryKey(Door record);
}