package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Festival;

public interface FestivalMapper {
    int deleteByPrimaryKey(String id);

    int insert(Festival record);

    int insertSelective(Festival record);

    Festival selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Festival record);

    int updateByPrimaryKey(Festival record);
}