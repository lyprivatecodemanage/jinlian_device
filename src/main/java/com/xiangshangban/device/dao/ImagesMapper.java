package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Images;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ImagesMapper {

    int deleteByPrimaryKey(String id);

    int insert(Images record);

    int insertSelective(Images record);

    Images selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Images record);

    int updateByPrimaryKey(Images record);

    /**
     * 查询所有的背景图信息（根据类型进行分组）
     */
    List<Map> selectAllBackGround();

    /**
     * 根据图片的类型查询图片信息
     */
    List<Map> selectBackGroundByType(@Param("imgType") String imgType);

    /**
     * 查询image_表主键的最大值
     */
    int selectImagePrimaryKey();

    /**
     * 添加图片信息
     */
    int insertIntoImageInfo(Map map);
}