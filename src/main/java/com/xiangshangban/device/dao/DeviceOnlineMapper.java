package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceOnline;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceOnlineMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(DeviceOnline record);

    int insertSelective(DeviceOnline record);

    List<DeviceOnline> selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(DeviceOnline record);

    int updateByPrimaryKey(DeviceOnline record);

    //非自动生成

    /**
     * 查找这台设备在线状态时间区间的最大开始时间
     * @param deviceId
     * @return
     */
    String selectMaxStartTimeByDeviceId(String deviceId);

    /**
     * 查找这台设备在线状态时间区间的最大结束时间
     * @param deviceId
     * @return
     */
    String selectMaxEndTimeByDeviceId(String deviceId);

    /**
     * 根据设备id和开始时间查询
     * @param deviceId
     * @param startTime
     * @return
     */
    List<DeviceOnline> selectByDeviceIdAndStartTime(@Param("deviceId") String deviceId,
                                              @Param("startTime") String startTime);

    /**
     * 根据设备id和结束时间查询
     * @param deviceId
     * @param endTime
     * @return
     */
    DeviceOnline selectByDeviceIdAndEndTime(@Param("deviceId") String deviceId,
                                            @Param("endTime") String endTime);

    /**
     * 根据设备id和开始时间update
     * @param deviceOnline
     * @return
     */
    int updateByDeviceIdAndStartTimeSelective(DeviceOnline deviceOnline);

    /**
     * 根据设备id和结束时间update
     * @param deviceOnline
     * @return
     */
    int updateByDeviceIdAndEndTimeSelective(DeviceOnline deviceOnline);
}