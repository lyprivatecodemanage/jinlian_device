package com.xiangshangban.device.dao;
import com.xiangshangban.device.bean.DoorTimingKeepOpen;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface DoorTimingKeepOpenMapper {
    int deleteByPrimaryKey(String doorId);

    int insert(DoorTimingKeepOpen record);

    int insertSelective(DoorTimingKeepOpen record);

    DoorTimingKeepOpen selectByPrimaryKey(String doorId);

    int updateByPrimaryKeySelective(DoorTimingKeepOpen record);

    int updateByPrimaryKey(DoorTimingKeepOpen record);

    /**
     * 非自动生成
     */

    List<DoorTimingKeepOpen> selectExistByDoorId(String doorId);

    /**
     * 根据门的id，查询门的定时常开信息
     */
    List<DoorTimingKeepOpen> selectKeepOpenInfo(@Param("doorId") String doorId);
}