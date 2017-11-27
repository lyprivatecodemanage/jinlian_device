package com.xiangshangban.device.service;


import com.xiangshangban.device.bean.Festival;
import org.springframework.web.multipart.MultipartFile;

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
     */
     Map modifyDeviceTemplateInfo(String templateInfo,MultipartFile file);

     //TODO 添加
    /**
     * 给设备添加模板
     * （模板信息，背景图以及展示时间、问候语以及展示时间，公司Logo）
     */
    Map addDeviceTemplate(String templateInfo,MultipartFile file);

    /**
     * 下发节日节气模板
     */
    boolean addFestivalTemplate(String festivalName);

    //TODO 查询
    /**
     * 查询所有设备的自定义模板信息
     */
    List<Map> queryDeviceTemplateInfo(String deviceId,String deviceName);

    /**
     * 查询标准模板的信息
     */
    List<Map> queryStandardTemplateInfo();

    /**
     * 根据模板的ID查询模板相关items
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

    /**
     * 判断当天日期是不是一个节气或者节日
     */
    Festival verifyCurrentDate(String date);

}
