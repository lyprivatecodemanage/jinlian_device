package com.xiangshangban.device.dao;

import com.xiangshangban.device.bean.Template;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface TemplateMapper {

    int deleteByPrimaryKey(String templateId);

    int insert(Template record);

    int insertSelective(Template record);

    Template selectByPrimaryKey(String templateId);

    int updateByPrimaryKeySelective(Template record);

    int updateByPrimaryKey(Template record);

    //TODO 更新部分
    /**
     * 更新设备的模板
     */
    int updateDeviceTemplate(Map map);

    //TODO 添加部分
    /**
     * 给设备添加新的模板
     */
    int insertDeviceTemplate(Map map);

    //TODO 查询部分
    /**
     * 查询所有设备当前的模板信息
     * (根据设备的名称和编号进行条件搜索)
     */
    List<Map> selectDeviceTemplateInfo(Map map);

    /**
     * 查询模板的详细信息(用于封装下发指令)
     */
    List<Map> selectTemplateDetailInfo(@Param("templateId") String templateId);

    /**
     * 根据模板的ID查询模板items
     */
    List<Map> selectTemplateItems(@Param("templateId") String templateId);

    /**
     * 查询模板的items的详细信息（用于封装下发指令）
     */
    List<Map> selectTemplateItemsDetail(@Param("templateId") String templateId);

    /**
     * 根据模板的ID查询模板相关的图片
     */
    List<Map> selectTemplateImages(@Param("templateId") String templateId);

    /**
     * 查询模板相关图片的详细信息(用于封装下发指令)
     */
    List<Map> selectTemplateImagesDetail(@Param("templateId") String templateId);

    //TODO 删除部分
    int deleteDeviceTemplate(@Param("templateIds") List<String> templateIds);
}