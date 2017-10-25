package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorEmployeePermission;
import org.springframework.stereotype.Component;

@Component
public interface DoorEmployeePermissionMapper {
    int deleteByPrimaryKey(String employeeId);

    int insert(DoorEmployeePermission record);

    int insertSelective(DoorEmployeePermission record);

    DoorEmployeePermission selectByPrimaryKey(String employeeId);

    int updateByPrimaryKeySelective(DoorEmployeePermission record);

    int updateByPrimaryKey(DoorEmployeePermission record);
}