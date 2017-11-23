package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.TemplateItems;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

@Mapper
public interface TemplateItemsMapper {
    int deleteByPrimaryKey(String itemId);

    int insert(TemplateItems record);

    int insertSelective(TemplateItems record);

    TemplateItems selectByPrimaryKey(String itemId);

    int updateByPrimaryKeySelective(TemplateItems record);

    int updateByPrimaryKey(TemplateItems record);



    //TODO =============<2017-11-22>================

    /**
     * 更新模板的logo
     */
    int updateTemplateLogo(Map map);

    /**
     * 查询模板logo的id
     */
    String selectTemplateLogoId(@Param("templateId") String templateId);

    /**
     * 查询(template_items表中)item_id的最大值
     */
    int selectMaxItemId();

    /**
     * 添加自定义模板的item信息
     */
    int insertPersonalTemplateItemInfo(Map map);

    /**
     * 删除自定义模板的item信息
     */
    int deletePersonalTemplateItemInfo(@Param("templateId") String templateId);

}