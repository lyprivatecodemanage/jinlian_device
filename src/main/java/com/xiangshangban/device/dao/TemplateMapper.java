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
     * 查询所有设备当前的模板信息
     * (根据设备的名称和编号进行条件搜索)
     */
    List<Map> selectDeviceTemplateInfo(Map map);

    /**
     * 根据模板的ID查询模板items
     */
    List<Map> selectTemplateItems(@Param("templateId") String templateId);

    /**
     * 根据模板的ID查询模板相关的图片
     */
    List<Map> selectTemplateImages(@Param("templateId") String templateId);

    /**
     * 查询标准模板的详细信息（首页选择模板的时候使用）
     * item的大致位置、背景图片
     */
    List<Map> selectStandardTemplateDetailImageInfo();

    /**
     * 查询标准模板的item详细信息（首页选择模板的时候使用）
     */
    List<Map> selectStandardTemplateDetailItemInfo();


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
     * 确认指定设备是否具有自定义模板
     */
    Template confirmPersonalTemplate(@Param("deviceId") String deviceId);

    /**
     * 查询template表中主键的最大值
     * @return
     */
    int selectTemplateMaxPrimaryKey();

    /**
     * 向template_表中添加自定义模板
     */
    int insertIntoPersonalTemplate(Map map);

    /**
     * 查询background_image_template中添加数据主键的最大值
     */
    int selectBackgroundImageTemplatePrimaryKey();

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
     * 查询所有节假日模板信息（待写）
     */
    List<Map> selectFestivalTemplateInfo();

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