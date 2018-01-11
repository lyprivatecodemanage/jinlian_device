package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DoorCmd;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

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

    String selectStatusBySuperCmdId(String superCmdId);

    /**
     * 查询授权中心高级设置部分的命令（作为日志:按照条件查询企业日志）
     */
    List<Map> selectLogCommand(Map map);

    /**
     * 批量删除日志信息(web端)
     */
    int removeLogCommand(@Param("logList") List logList);

    int removeDeviceCommand(@Param("deviceId") String deviceId);

    /**
     * 删除门关联的设备的日志信息(指令：3003、3004、3005)
     */
    int deleteDoorRelateDeviceLog(@Param("deviceId") String deviceId);

    /**
     * 查询更新模板指令的执行情况(result_code)
     */
    String selectDoorCmdResultCode();

    /**
     * 查询某个人最新的一条下发命令（下发命令同一时间会有两条，删除命令同一时间一条）
     */
    List<DoorCmd> selectDoorCmdLatestByEmployeeId(String employeeId);
}