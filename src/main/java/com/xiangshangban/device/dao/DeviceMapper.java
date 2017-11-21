package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceMapper {
    int deleteByPrimaryKey(String deviceId);

    int insert(Device record);

    int insertSelective(Device record);

    Device selectByPrimaryKey(String deviceId);

    int updateByPrimaryKeySelective(Device record);

    int updateByPrimaryKey(Device record);

    /**
     * 非自动生成
     */

    List<Map<String, String>> findByCondition(Device device);

    List<String> findDeviceIdByCompanyId(String companyId);

    /**
     * 查询所有的设备信息
     */
    List<Map> selectAllDeviceInfo(@Param("companyId") String companyId);
}