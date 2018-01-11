package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.EmployeeBluetoothCount;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmployeeBluetoothCountMapper {
    int deleteByPrimaryKey(String id);

    int insert(EmployeeBluetoothCount record);

    int insertSelective(EmployeeBluetoothCount record);

    EmployeeBluetoothCount selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(EmployeeBluetoothCount record);

    int updateByPrimaryKey(EmployeeBluetoothCount record);

}