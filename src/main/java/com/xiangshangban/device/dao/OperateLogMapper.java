package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.OperateLog;
import com.xiangshangban.device.bean.OperateLogExample;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface OperateLogMapper {

    int countByExample(OperateLogExample example);

    int deleteByExample(OperateLogExample example);

    int deleteByPrimaryKey(String id);

    int insert(OperateLog record);

    List<OperateLog> selectByExample(OperateLogExample example);

    OperateLog selectByPrimaryKey(String id);

    int updateByExampleSelective(@Param("record") OperateLog record, @Param("example") OperateLogExample example);

    int updateByExample(@Param("record") OperateLog record, @Param("example") OperateLogExample example);

    int updateByPrimaryKeySelective(OperateLog record);

    int updateByPrimaryKey(OperateLog record);

    /**
     * 新增操作日志
     * @param record
     * @return
     */
    int insertSelective(OperateLog record);

    /**
     * 查询条件查询数据总行数
     */
    int selectCountByCondition(Map map);

    /**
     * 查询操作日志
     */
    List<Map> selectOperateLog(Map map);

    /**
     * 查询日志操作次数
     */
    Map selectOperateCount(Map map);
}