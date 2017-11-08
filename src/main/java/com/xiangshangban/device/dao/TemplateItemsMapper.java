package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.TemplateItems;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TemplateItemsMapper {
    int deleteByPrimaryKey(String itemId);

    int insert(TemplateItems record);

    int insertSelective(TemplateItems record);

    TemplateItems selectByPrimaryKey(String itemId);

    int updateByPrimaryKeySelective(TemplateItems record);

    int updateByPrimaryKey(TemplateItems record);

    /**
     * 更新模板的logo
     */
    int updateTemplateLogo(Map map);

    /**
     * 更新模板的问候语（单字）
     */
    int updateTemplateSalutation(Map map);

    /**
     * 删除模板原来的问候语
     */
    int deleteTemplateSalutation(@Param("templateId") String templateId);

    /**
     * 添加新的问候语（单字）
     */
    int insertTemplateSalutation(Map map);

    /**
     * 查询当前模板的问候语样式
     */
    List<Map> selectSalutationStyle(@Param("templateId") String templateId);

    /**
     * 查询(template_items表中)item_id的最大值
     */
    int selectMaxItemId();

    /**
     * 查询（background_image_template）表中的id最大值
     */
    int selectMaxId();

    /**
     * 删除模板旧的背景图
     */
    int deleteTemplateBackground(@Param("templateId") String templateId);

    /**
     * 插入新的背景图
     */
    int insertTemplateBackground(Map map);

}