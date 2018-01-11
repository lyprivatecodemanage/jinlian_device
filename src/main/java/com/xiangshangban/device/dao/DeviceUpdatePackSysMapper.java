package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.DeviceUpdatePackSys;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface DeviceUpdatePackSysMapper {
    int deleteByPrimaryKey(String newSysVerion);

    int insert(DeviceUpdatePackSys record);

    int insertSelective(DeviceUpdatePackSys record);

    DeviceUpdatePackSys selectByPrimaryKey(String newSysVerion);

    int updateByPrimaryKeySelective(DeviceUpdatePackSys record);

    int updateByPrimaryKey(DeviceUpdatePackSys record);

    /**
     * 非自动生成方法
     */
    List<DeviceUpdatePackSys> selectAllByLatestTime();

    /**
     * 根据版本号和路径查询当前本地数据库中是否已经存在
     */
    String verifyWhetherExistsResource(@Param("path") String path);

    /**
     * 更新操作时间
     */
    int updateOperateTime(Map map);
}