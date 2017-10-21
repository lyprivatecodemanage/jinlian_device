package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorCmd;
import org.springframework.stereotype.Component;

@Component
public interface DoorCmdMapper {
    int deleteByPrimaryKey(String serverId);

    int insert(DoorCmd record);

    int insertSelective(DoorCmd record);

    DoorCmd selectByPrimaryKey(String serverId);

    int updateByPrimaryKeySelective(DoorCmd record);

    int updateByPrimaryKey(DoorCmd record);
}