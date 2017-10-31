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

    int updateBySubCmdIdSelective(DoorCmd record);

    int updateByPrimaryKey(DoorCmd record);

    DoorCmd selectBySubCmdId(DoorCmd doorCmd);

    int updateBySubCmdId(DoorCmd doorCmd);

    List<DoorCmd> selectByStatus(String status);
}