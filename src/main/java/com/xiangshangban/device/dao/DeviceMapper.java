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
     * 查询所有的没有关联门的设备信息
     */
    List<Map> selectAllDeviceInfo(@Param("companyId") String companyId);

    /**
     * 查询所有的设备的信息
     * @param companyId
     * @return
     */
    List<Map> selectAllDevice(@Param("companyId") String companyId);

    /**
     * 根据设备id查询该公司下的设备id的集合
     * @param deviceId
     * @return
     */
    List<Map<String, String>> selectAllDeviceIdOfCompanyByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据设备id查询该设备上传的最新的一条重启记录带上来的所有版本信息
     * @param deviceId
     * @return
     */
    List<Map<String, String>> selectAllVersionInfoByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 查出所有的设备的信息
     * @return
     */
    List<Device> selectAllDeviceInfoByNone();

    /**
     * 查询跟当前公司已经解绑的设备列表
     */
    List<String> selectUnBindDeviceByCompanyId(@Param("companyId") String companyId);
}