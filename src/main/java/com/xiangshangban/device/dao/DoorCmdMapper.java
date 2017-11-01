package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorCmd;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface DoorCmdMapper {
    int deleteByPrimaryKey(String serverId);

    int insert(DoorCmd record);

    int insertSelective(DoorCmd record);

    DoorCmd selectByPrimaryKey(String serverId);

    int updateBySuperCmdIdSelective(DoorCmd record);

    int updateByPrimaryKey(DoorCmd record);

    DoorCmd selectBySuperCmdId(String superCmdId);

    int updateBySuperCmdId(DoorCmd doorCmd);

    List<DoorCmd> selectByStatus(String status);
}