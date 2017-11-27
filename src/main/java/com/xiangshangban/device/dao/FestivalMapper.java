package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Festival;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

@Component
public interface FestivalMapper {
    int deleteByPrimaryKey(String id);

    int insert(Festival record);

    int insertSelective(Festival record);

    Festival selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Festival record);

    int updateByPrimaryKey(Festival record);

    /**
     * 根据当天日期查询当天是否是节日
     */
    Festival selectFestivalByDate(@Param("date") String date);
}