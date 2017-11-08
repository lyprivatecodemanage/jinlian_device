package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.dao.ImagesMapper;
import com.xiangshangban.device.dao.TemplateItemsMapper;
import com.xiangshangban.device.dao.TemplateMapper;
import com.xiangshangban.device.service.IEntranceGuardService;
import com.xiangshangban.device.service.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * author : Administrator
 * date: 2017/11/3 10:57
 * describe: TODO
 */
@Service
public class TemplateServiceImpl implements ITemplateService{

    @Autowired
    private TemplateMapper templateMapper;

    @Autowired
    private ImagesMapper imagesMapper;

    @Autowired
    private TemplateItemsMapper templateItemsMapper;

    @Autowired
    private IEntranceGuardService entranceGuardService;

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    /**
     *  //更新的数据
     * {
             "deviceId":"1",
             "templateId":"2",
             "backImgList":[

             {"imgId":"1","startTime":"2017-11-04 08:00","endTime":"2017-11-04 12:00"},
             {"imgId":"2","startTime":"2017-11-04 12:00","endTime":"2017-11-04 18:00"}

             ],

             "salutationList":[

             {"content":"上午","startTime":"2017-11-04 08:00","endTime":"2017-11-04 12:00"},
             {"content":"下午","startTime":"2017-11-04 12:00","endTime":"2017-11-04 18:00"}

             ],
             "companyLogoName":"xxx"
     }
    /**
     * 更新模板信息(背景图、问候语、logo)
     * @param templateInfo 新数据
     * @return
     */

    @Override
    public int modifyDeviceTemplateInfo(String templateInfo) {

        JSONObject jsonObject = JSONObject.parseObject(templateInfo);

        //要进行更新的设备ID
        String deviceId =  jsonObject.get("deviceId").toString();
        //要进行更新的模板ID（或者是选择的标准模板）
        String templateId = jsonObject.get("templateId").toString();

        //公司logo名称
        String logoName = jsonObject.get("companyLogoName").toString();
        //TODO 更新公司logo
        Map logoMap = new HashMap();
        logoMap.put("imgName",logoName);
        logoMap.put("templateId",templateId);
        int logoResult = templateItemsMapper.updateTemplateLogo(logoMap);

        //背景图信息
        JSONArray backImgList = JSONArray.parseArray(jsonObject.get("backImgList").toString());
        //TODO 更新背景图
        int delBackResult = templateItemsMapper.deleteTemplateBackground(templateId);
        //旧的背景图删除成功的时候，进行新增操作
        if(delBackResult>0){
            JSONObject backInfo;
            String[] startTime;
            String[] endTime;
            for(int i=0;i<backImgList.size();i++){
                backInfo = JSONObject.parseObject(backImgList.get(i).toString());
                //获取背景图设置的显示时间
                startTime = backInfo.get("startTime").toString().split(" ");
                endTime = backInfo.get("endTime").toString().split(" ");
                Map map = new HashMap();
                //查询当前表中主键的最大值
                int id = templateItemsMapper.selectMaxId();
                map.put("id",id+1);
                map.put("imgId",backInfo.get("imgId"));
                map.put("templateId",templateId);
                map.put("startDate",startTime[0]);
                map.put("startTime",startTime[1]);
                map.put("endDate",endTime[0]);
                map.put("endTime",endTime[1]);
                //添加新的背景图
                templateItemsMapper.insertTemplateBackground(map);
            }
        }
        //获取更新的问候语数据
        JSONArray salutationList = JSONArray.parseArray(jsonObject.get("salutationList").toString());
        //TODO 更新问候语
        //查询当前模板的文字排版
        List<Map> fontStyle = templateItemsMapper.selectSalutationStyle(templateId);
        //创建List集合保存文字坐标信息
        List<Map> fontCoord = new ArrayList<Map>();
        //删除旧的问候语
        int delSalutationResult = templateItemsMapper.deleteTemplateSalutation(templateId);
        //当旧的问候语删除成功的时候进行添加新的问候语的
        if(delSalutationResult>0){
            JSONObject salutationInfo;
            String content;
            String startDate;
            String endDate;
           for(int k=0;k<salutationList.size();k++){
               salutationInfo = JSONObject.parseObject(salutationList.get(k).toString());
               content = salutationInfo.get("content").toString();
               startDate = salutationInfo.get("startTime").toString();
               endDate = salutationInfo.get("endTime").toString();

               //TODO 准备文字的坐标
               //根据(问候语字的个数和方向)获取问候语字的坐标
             /*  if(fontStyle.get(0).get("item_font_orient")==1){//纵向
                   if(content.length()==1){
                       Map fontMap = new HashMap();
                       fontMap.put("itemTopX",111);
                       fontMap.put("itemTopY",222);
                       fontCoord.add(fontMap);
                   }

                   if(content.length()==2){
                       for(int f = 0;f<content.length();f++){
                           Map fontMap = new HashMap();
                           fontMap.put("itemTopX",111);
                           fontMap.put("itemTopY",222);
                           fontCoord.add(fontMap);
                       }
                   }
               }

               if(fontStyle.get(0).get("item_font_orient")==0){//横向

               }*/

               for(int x = 0;x<content.length();x++){
                   //查询当前表中主键的最大值
                   int id = templateItemsMapper.selectMaxItemId();

                   Map map = new HashMap();
                   map.put("itemId",id+1);
                   map.put("itemTopX","111");
                   map.put("itemTopY","222");
                   map.put("itemFontOrient",fontStyle.get(0).get("item_font_orient"));
                   map.put("itemFontSize",fontStyle.get(0).get("item_font_size"));
                   map.put("itemFontBold",fontStyle.get(0).get("item_font_bold"));
                   map.put("itemFontColor",fontStyle.get(0).get("item_font_color"));
                   map.put("templateId",templateId);
                   map.put("startTime",startDate);
                   map.put("endTime",endDate);
                   map.put("itemFontContent",content.substring(x,x+1));

                   templateItemsMapper.insertTemplateSalutation(map);
               }
           }
        }

        //更改模板的一些基础信息
        Map templateMap = new HashMap();
        templateMap.put("templateLevel","2");
        templateMap.put("templateId",templateId);
        templateMapper.updateDeviceTemplate(templateMap);

        //查询当前模板的详细信息，下发到设备中
     /*   List<Map> currTemplateInfo = templateMapper.selectTemplateDetailInfo(templateId);
        //查询模板的items信息
        List<Map> itemsInfoForCommand = templateMapper.selectTemplateItemsDetail(templateId);
        //查询模板关联的图片
        List<Map> imagesInfoForCommand = templateMapper.selectTemplateImagesDetail(templateId);

        //保存item的类型
        int itemType;
        List<Map> templates = new ArrayList<>();
        //遍历图片信息，每一行图片都有其展示时间，所以一张图片对应一个template
        for(int t = 0;t<imagesInfoForCommand.size();t++){
            Map templateInfoMap = new HashMap();
            templateInfoMap.put("templateId",templateId);
            templateInfoMap.put("templateType",currTemplateInfo.get(0).get("template_type"));
            templateInfoMap.put("templateLevel",currTemplateInfo.get(0).get("template_level"));
            templateInfoMap.put("imageName",imagesInfoForCommand.get(t).get("img_name"));
            templateInfoMap.put("imagePath",imagesInfoForCommand.get(t).get("img_url"));
            templateInfoMap.put("alertButtonColor",imagesInfoForCommand.get(t).get("ripple_color"));
            templateInfoMap.put("alertImgName",imagesInfoForCommand.get(t).get("relate_img_name"));
            templateInfoMap.put("alertImgPath",imagesInfoForCommand.get(t).get("relate_img_url"));
            templateInfoMap.put("broadStartDate",imagesInfoForCommand.get(t).get("broad_start_date"));
            templateInfoMap.put("broadEndDate",imagesInfoForCommand.get(t).get("broad_end_date"));
            templateInfoMap.put("broadStartTime",imagesInfoForCommand.get(t).get("broad_start_time"));
            templateInfoMap.put("broadEndTime",imagesInfoForCommand.get(t).get("broad_end_time"));

            //创建List封装items信息
            List<Map> itemsInfo = new ArrayList<>();
            //遍历items信息
            for(int f = 0;f<itemsInfoForCommand.size();f++){
                Map itemMap = new HashMap();
                //获取item的类型
                itemType = Integer.parseInt(itemsInfoForCommand.get(f).get("item_type").toString());
                itemMap.put("templateId",templateId);
                itemMap.put("itemId",itemsInfoForCommand.get(f).get("item_id"));
                itemMap.put("itemType",itemsInfoForCommand.get(f).get("item_type"));
                //判断item的类型
                if(itemType==0||itemType==9){//文字（问候语）<默认、自定义>
                    itemMap.put("itemTopX",itemsInfoForCommand.get(f).get("item_top_x"));
                    itemMap.put("itemTopY",itemsInfoForCommand.get(f).get("item_top_y"));
                    itemMap.put("itemFontOrient",itemsInfoForCommand.get(f).get("item_font_orient"));
                    itemMap.put("itemFontSize",itemsInfoForCommand.get(f).get("item_font_size"));
                    itemMap.put("itemFontBold",itemsInfoForCommand.get(f).get("item_font_bold"));
                    itemMap.put("itemFontColor",itemsInfoForCommand.get(f).get("item_font_color"));
                    itemMap.put("itemFontContent",itemsInfoForCommand.get(f).get("item_font_content"));
                    itemMap.put("itemStartDate",itemsInfoForCommand.get(f).get("item_start_date"));
                    itemMap.put("itemEndDate",itemsInfoForCommand.get(f).get("item_end_date"));
                }

                if(itemType==1||itemType==2){ //1:星期  2:时分
                    itemMap.put("itemTopX",itemsInfoForCommand.get(f).get("item_top_x"));
                    itemMap.put("itemTopY",itemsInfoForCommand.get(f).get("item_top_y"));
                    itemMap.put("itemFontOrient",itemsInfoForCommand.get(f).get("item_font_orient"));
                }

                if(itemType==3||itemType==6||itemType==7||itemType==8){ //3:文字修饰框 6:铃铛背景图 7:二维码 8:logo
                    itemMap.put("itemTopX",itemsInfoForCommand.get(f).get("item_top_x"));
                    itemMap.put("itemTopY",itemsInfoForCommand.get(f).get("item_top_y"));
                    itemMap.put("itemImgName",itemsInfoForCommand.get(f).get("img_name"));
                    itemMap.put("itemImgPath",itemsInfoForCommand.get(f).get("img_url"));
                }

                if(itemType==4||itemType==5){// 4:人脸按钮 5:密码按钮
                    itemMap.put("itemTopX",itemsInfoForCommand.get(f).get("item_top_x"));
                    itemMap.put("itemTopY",itemsInfoForCommand.get(f).get("item_top_y"));
                }

                itemsInfo.add(itemMap);
            }
            templateInfoMap.put("items",itemsInfo);
            templates.add(templateInfoMap);
        }

        //构造命令格式
        DoorCmd doorCmdUpdateDeviceTemplate = new DoorCmd();
        doorCmdUpdateDeviceTemplate.setServerId("001");
        doorCmdUpdateDeviceTemplate.setDeviceId(deviceId);
        doorCmdUpdateDeviceTemplate.setFileEdition("v1.3");
        doorCmdUpdateDeviceTemplate.setCommandMode("C");
        doorCmdUpdateDeviceTemplate.setCommandType("single");
        doorCmdUpdateDeviceTemplate.setCommandTotal("1");
        doorCmdUpdateDeviceTemplate.setCommandIndex("1");
        doorCmdUpdateDeviceTemplate.setSubCmdId("");
        doorCmdUpdateDeviceTemplate.setAction("UPDATE_DEVICE_TEMPLATE");
        doorCmdUpdateDeviceTemplate.setActionCode("4001");
        doorCmdUpdateDeviceTemplate.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdUpdateDeviceTemplate.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdUpdateDeviceTemplate.setSuperCmdId(FormatUtil.createUuid());
        doorCmdUpdateDeviceTemplate.setData(JSON.toJSONString(templates));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdUpdateDeviceTemplate, "templates",templates , "C");
        //命令状态设置为: 发送中
        doorCmdUpdateDeviceTemplate.setStatus("1");
        //设置md5校验值
        doorCmdUpdateDeviceTemplate.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdUpdateDeviceTemplate.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdUpdateDeviceTemplate);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);*/

        return 0;
    }

    /**
     * 设备添加模板
     * @param  templateInfo 添加的信息
     * @return
     */
    @Override
    public int addDeviceTemplate(String templateInfo){
        JSONObject jsonObject = JSONObject.parseObject(templateInfo);

        //要添加模板的设备ID
        String deviceId =  jsonObject.get("deviceId").toString();
        //要进行更新的模板ID（或者是选择的标准模板）
        String templateId = jsonObject.get("templateId").toString();

        //查询标准模板的信息


        return 0;
    }

    /**
     * 查询所有的设备主题信息
     * @param deviceId
     * @param deviceName
     * @return
     */
    @Override
    public List<Map> queryDeviceTemplateInfo(String deviceId,String deviceName) {
        String deviceTitle = "";
        if(deviceName!=null&&!deviceName.isEmpty()){
            deviceTitle = "%"+deviceName+"%";
        }
        Map map = new HashMap();
        map.put("deviceId",deviceId);
        map.put("deviceName",deviceTitle);
        List<Map> maps = templateMapper.selectDeviceTemplateInfo(map);
        return maps;
    }

    /**
     * 根据模板ID查询模板items
     * @param templateId
     * @return
     */
    @Override
    public List<Map> queryTemplateItems(String templateId) {
        List<Map> maps = templateMapper.selectTemplateItems(templateId);
        return maps;
    }

    /**
     * 根据模板id查找模板关联的图片
     */
    @Override
    public List<Map> queryTemplateImages(String templateId){
        List<Map> maps = templateMapper.selectTemplateImages(templateId);
        return maps;
    }

    /**
     * 获取所有的背景图
     * @return
     */
    @Override
    public List<Map> queryAllBackGround() {
        List<Map> maps = imagesMapper.selectAllBackGround();
        return maps;
    }

    /**
     * 删除模板
     * @param deviceId
     * @param templateIds
     * @return
     */
    @Override
    public int removeDeviceTemplate(String deviceId, List<String> templateIds) {
        int delResult = templateMapper.deleteDeviceTemplate(templateIds);

        //构造命令格式
        DoorCmd doorCmdDeleteDeviceTemplate = new DoorCmd();
        doorCmdDeleteDeviceTemplate.setServerId("001");
        doorCmdDeleteDeviceTemplate.setDeviceId(deviceId);
        doorCmdDeleteDeviceTemplate.setFileEdition("v1.3");
        doorCmdDeleteDeviceTemplate.setCommandMode("C");
        doorCmdDeleteDeviceTemplate.setCommandType("single");
        doorCmdDeleteDeviceTemplate.setCommandTotal("1");
        doorCmdDeleteDeviceTemplate.setCommandIndex("1");
        doorCmdDeleteDeviceTemplate.setSubCmdId("");
        doorCmdDeleteDeviceTemplate.setAction("DELETE_ DEVICE_TEMPLATE");
        doorCmdDeleteDeviceTemplate.setActionCode("4002");
        doorCmdDeleteDeviceTemplate.setSendTime(CalendarUtil.getCurrentTime());
        doorCmdDeleteDeviceTemplate.setOutOfTime(DateUtils.addDaysOfDateFormatterString(new Date(),3));
        doorCmdDeleteDeviceTemplate.setSuperCmdId(FormatUtil.createUuid());
        doorCmdDeleteDeviceTemplate.setData(JSON.toJSONString(templateIds));

        //获取完整的数据加协议封装格式
        RabbitMQSender rabbitMQSender = new RabbitMQSender();
        Map<String, Object> doorCmdPackageAll =  rabbitMQSender.messagePackaging(doorCmdDeleteDeviceTemplate, "templateList",templateIds ,"C");
        //命令状态设置为: 发送中
        doorCmdDeleteDeviceTemplate.setStatus("1");
        //设置md5校验值
        doorCmdDeleteDeviceTemplate.setMd5Check((String) doorCmdPackageAll.get("MD5Check"));
        //设置数据库的data字段
        doorCmdDeleteDeviceTemplate.setData(JSON.toJSONString(doorCmdPackageAll.get("data")));
        //命令数据存入数据库
        entranceGuardService.insertCommand(doorCmdDeleteDeviceTemplate);
        //立即下发数据到MQ
        rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);

        return delResult;
    }
}
