package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceUpdatePackSys;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DeviceUpdatePackSysMapper {
    int deleteByPrimaryKey(String newSysVerion);

    int insert(DeviceUpdatePackSys record);

    int insertSelective(DeviceUpdatePackSys record);

    DeviceUpdatePackSys selectByPrimaryKey(String newSysVerion);

    int updateByPrimaryKeySelective(DeviceUpdatePackSys record);

    int updateByPrimaryKey(DeviceUpdatePackSys record);

    /**
     * 非自动生成方法
     */
    List<DeviceUpdatePackSys> selectAllByLatestTime();
}