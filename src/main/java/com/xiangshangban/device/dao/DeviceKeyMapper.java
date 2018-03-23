package com.xiangshangban.device.dao;

import org.apache.ibatis.annotations.Mapper;

import com.xiangshangban.device.bean.DeviceKey;

@Mapper
public interface DeviceKeyMapper {
	
	DeviceKey selectDeviceKey();
}
