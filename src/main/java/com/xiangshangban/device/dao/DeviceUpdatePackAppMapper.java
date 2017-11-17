package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceUpdatePackApp;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeviceUpdatePackAppMapper {
    int deleteByPrimaryKey(String appName);

    int insert(DeviceUpdatePackApp record);

    int insertSelective(DeviceUpdatePackApp record);

    DeviceUpdatePackApp selectByPrimaryKey(String appName);

    int updateByPrimaryKeySelective(DeviceUpdatePackApp record);

    int updateByPrimaryKey(DeviceUpdatePackApp record);
}