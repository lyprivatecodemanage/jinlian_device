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
    int updateTemplateTable(Map map);

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

    /**
     * 查询标准模板的信息
     * @param templateId
     * @return
     */
    List<Map> selectStandardTemplateInfo(@Param("templateId") String templateId);

    /**
     * 查询标准模板的详细信息（首页选择模板的时候使用）
     * item的大致位置、背景图片
     */
    List<Map> selectStandardTemplateDetailImageInfo();

    /**
     * 查询标准模板的item详细信息（首页选择模板的时候使用）
     */
    List<Map> selectStandardTemplateDetailItemInfo();

    /**
     * 查询标准模板相关的items信息
     * @param templateId
     * @return
     */
    List<Map> selectStandardTemplateItems(@Param("templateId") String templateId);

    /**
     * 查询标准模板相关联的背景图信息
     * @param templateId
     * @return
     */
    List<Map> selectStandardTemplateImages(@Param("templateId") String templateId);

    /**
     * 查询所有节假日模板信息
     */
    List<Map> selectFestivalTemplateInfo();

    /**
     * 查询当前设备中是否有自定义的模板
     * @param deviceId
     * @return
     */
    List<Map> selectTemplateByLevel(String deviceId);

    /**
     * 查询template表中主键的最大值
     * @return
     */
    int selectTemplateMaxPrimaryKey();

    //TODO 删除部分
    int deleteDeviceTemplate(@Param("templateIds") List<String> templateIds);




    //TODO =============<2017-11-21>================

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
}