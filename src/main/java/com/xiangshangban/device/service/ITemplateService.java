package com.xiangshangban.device.service;


import java.util.List;
import java.util.Map;

/**
 * author : Administrator
 * date: 2017/11/3 10:56
 * describe: TODO
 */

public interface ITemplateService {

    //TODO 更新
    /**
     * 更新设备的模板信息(背景图<以及展示的时间>、问候语以及展示的时间、公司Logo)
     * ###更新xx设备的xx模板
     */
     int modifyDeviceTemplateInfo(String templateInfo);

     //TODO 添加
    /**
     * 给设备添加模板
     * （模板信息，背景图以及展示时间、问候语以及展示时间，公司Logo）
     */
    int addDeviceTemplate(String templateInfo);

    //TODO 查询
    /**
     * 查询所有设备当前使用的模板信息
     */
    List<Map> queryDeviceTemplateInfo(String deviceId,String deviceName);

    /**
     * 根据模板的ID查询模板相关信息（背景图、铃铛按钮图、items）
     */
    List<Map> queryTemplateItems(String templateId);

    /**
     * 根据模板id查找模板关联的图片
     */
    List<Map> queryTemplateImages(String templateId);

    /**
     * 获取所有的背景图
     */
    List<Map> queryAllBackGround();


    //TODO 删除
    int removeDeviceTemplate(String deviceId,List<String> templateIds);

}
