package com.xiangshangban.device.service;


import com.xiangshangban.device.bean.Festival;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
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
     Map modifyDeviceTemplateInfo(HttpServletRequest request,String templateInfo,MultipartFile file);

     //TODO 添加
    /**
     * 给设备添加模板
     * （模板信息，背景图以及展示时间、问候语以及展示时间，公司Logo）
     */
    Map addDeviceTemplate(HttpServletRequest request,String templateInfo, MultipartFile file);

    /**
     * 下发节日节气模板
     */
    boolean addFestivalTemplate(String festivalName);

    //TODO 查询
    /**
     * 查询所有设备的自定义模板信息
     */
    List<Map> queryDeviceTemplateInfo(String companyId,String deviceId,String deviceName);

    /**
     * 查询标准模板预览图
     */
    List<Map> queryStandardTemplatePreview();

    /**
     * 获取所有的背景图
     */
    List<Map> queryAllBackGround();

    /**
     * 根据模板的ID查询模板的背景图以及展示时间
     */
    List<Map> queryTemplateBackAndTime(String templateId);

    /**
     * 查询当前自定义模板的问候语
     */
    List<Map> queryTemplateSalutation(String templateId);

    /**
     * 判断当天日期是不是一个节气或者节日
     */
    Festival verifyCurrentDate(String date);
}
