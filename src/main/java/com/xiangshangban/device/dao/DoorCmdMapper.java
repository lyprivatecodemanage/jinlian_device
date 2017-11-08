package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorCmd;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoorCmdMapper {
    int deleteByPrimaryKey(String superCmdId);

    int insert(DoorCmd record);

    int insertSelective(DoorCmd record);

    DoorCmd selectByPrimaryKey(String superCmdId);

    int updateByPrimaryKeySelective(DoorCmd record);

    int updateByPrimaryKey(DoorCmd record);

    //非自动生成

    int updateBySuperCmdIdSelective(DoorCmd record);

    DoorCmd selectBySuperCmdId(String superCmdId);

    int updateBySuperCmdId(DoorCmd doorCmd);

    List<DoorCmd> selectByStatus(String status);

    List<DoorCmd> selectEmployeeDraftByDeviceId(String deviceId);

    List<DoorCmd> selectCmdByEmployeeIdSendTimeDesc(@Param("employeeId") String employeeId,
                                                    @Param("action") String action);

}