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


    /*****************************************
     * TODO web端接口
     ******************************************/

    /**
     * TODO ①：首页列表展示
     * 查询所有设备当前的模板信息
     * (根据设备的名称和编号进行条件搜索)
     */
    List<Map> selectDeviceTemplateInfo(Map map);

    /**
     * TODO ②：新增主题、编辑主题部分界面默认显示信息
     * 查询标准模板的预览图
     */
    List<Map> selectStandardTemplatePreview(@Param("templateId") String templateId);

    /**
     * 根据模板的ID查询模板的背景图以及展示时间段
     */
    List<Map> selectTemplateBackAndTime(@Param("templateId") String templateId);

    /**
     * 查询当前自定义模板的问候语
     */
    List<Map> selectTemplateSalutation(@Param(("templateId")) String templateId);


    /*********************************************
     * TODO 设备端接口
     *********************************************/

    /**
     * 查询标准模板的item信息
     * @param templateId
     * @return
     */
    List<Map> selectStandardItemInfo(@Param("templateId") String templateId);

    /**
     * 查询节日模板的item信息
     */
    List<Map> selectFestivalItemInfo();

    /**
     * 确认指定设备是否具有自定义模板
     */
    Template confirmPersonalTemplate(@Param("deviceId") String deviceId);

    /**
     * 查询template表中主键的最大值
     * @return
     */
    String selectTemplateMaxPrimaryKey();

    /**
     * 向template_表中添加自定义模板
     */
    int insertIntoPersonalTemplate(Map map);

    /**
     * 查询background_image_template中添加数据主键的最大值
     */
    String selectBackgroundImageTemplatePrimaryKey();

    /**
     * 向background_image_template中添加数据（设置自定义模板的背景图）
     */
    int insertIntoBackImage(Map map);

    /**
     * 删除template_表中的数据
     */
    int deletePersonalTemplate(@Param("templateId") String templateId);

    /**
     * 删除background_image_template表中的数据
     */
    int deleteBackgroundImage(@Param("templateId") String templateId);

    /**
     * 根据自定义模板的template_style查询其使用的标准模板的ID
     */
    String selectStandardTemplateIdByStyle(@Param("templateStyle") String templateStyle);

    /**
     * 查询模板相关图片的详细信息(用于封装下发指令)
     */
    List<Map> selectTemplateImagesDetail(@Param("templateId") String templateId);

    /**
     * 查询模板的items的详细信息（用于封装下发指令）
     */
    List<Map> selectTemplateItemsDetail(@Param("templateId") String templateId);

    /**
     * 查询模板的详细信息(用于封装下发指令)
     */
    List<Map> selectTemplateDetailInfo(@Param("templateId") String templateId);
}