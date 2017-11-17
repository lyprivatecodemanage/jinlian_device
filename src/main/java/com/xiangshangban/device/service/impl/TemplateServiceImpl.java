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

             {"imgId":"1","startTime":"2017-11-10 08:00","endTime":"2017-11-10 12:00"},
             {"imgId":"2","startTime":"2017-11-10 12:00","endTime":"2017-11-10 18:00"}

             ],

             "salutationList":[

             {"content":"上午","startTime":"2017-11-010 08:00","endTime":"2017-11-10 12:00"},
             {"content":"下午","startTime":"2017-11-010 12:00","endTime":"2017-11-1018:00"}

             ],
             "companyLogoName":"20171110Logo"
     }

    /**
     * 更新自定义模板信息(背景图、问候语、logo)
     * @param templateInfo 新数据
     * @return
     */

    @Override
    public boolean modifyDeviceTemplateInfo(String templateInfo) {

        JSONObject jsonObject = JSONObject.parseObject(templateInfo);

        //要进行更新的设备ID
        String deviceId =  jsonObject.get("deviceId").toString();
        //要进行更新的模板ID（或者是选择的标准模板）
        String templateId = jsonObject.get("templateId").toString();

        //公司logo名称
        String logoName = jsonObject.get("companyLogoName").toString();
        //TODO 更新公司logo
        Map logoMap = new HashMap();
        logoMap.put("imgName",logoName+".png");
        logoMap.put("templateId",templateId);
        int logoResult = templateItemsMapper.updateTemplateLogo(logoMap);

        //背景图信息
        JSONArray backImgList = JSONArray.parseArray(jsonObject.get("backImgList").toString());
        //TODO 更新背景图
        int delBackResult = templateItemsMapper.deleteTemplateBackground(templateId);
        int updateBackResult = 0;
        //旧的背景图删除成功的时候，进行新增操作
        updateBackResult = updatePersonalTemplateImage(delBackResult, backImgList, templateId);

        //获取更新的问候语数据
        JSONArray salutationList = JSONArray.parseArray(jsonObject.get("salutationList").toString());
        //TODO 更新问候语
        //查询当前模板的文字排版
        List<Map> fontStyle = templateItemsMapper.selectSalutationStyle(templateId);
        //创建List集合保存文字坐标信息
        List<Map> fontCoord = new ArrayList<Map>();
        //删除旧的问候语
        int delSalutationResult = templateItemsMapper.deleteTemplateSalutation(templateId);
        int updateSalutationResult = 0;
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

                updateSalutationResult = templateItemsMapper.insertTemplateSalutation(map);
            }
        }
        }

        //更改模板的一些基础信息
        Map templateMap = new HashMap();
        templateMap.put("templateLevel","2");
        templateMap.put("templateId",templateId);
        int updateTemplateStateResult = templateMapper.updateDeviceTemplate(templateMap);

        //查询当前模板的详细信息，下发到设备中
        getUpdateLaterTemplateInfo(templateId,deviceId);

        if(logoResult>0&&updateBackResult>0&&updateSalutationResult>0&&updateTemplateStateResult>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * {
     "deviceId":"1", ------->要进行模板添加的设备
     "templateId":{
        "current":"2",
        "select":"3"
     },
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
     * 添加自定义模板（必须设置问候语和背景图的时候才会畸形应用）
     * @param  templateInfo 添加的信息
     * @return
     *
     * 前提：默认的模板只有三套（每一个模板的背景图是三张）
     * 默认的问候语都是：（上午、中午、下午）
     *
     * 新增：
     *     ①：没有默认模板  ----->添加默认的模板和自定义的模板
     *     ②：拥有了默认的模板----->用户又选择了一个默认的模板，那么要删除该设备之前的默认的模板，使用又选择的默认模板进行替换。
     *     ③：自定义模板（用户在选择的默认的模板上更改了问候语和背景图，此时的默认模板变为自定义模板？）
     *
     * 修改：
     *      修改的是设备当前正在使用的模板信息，模板不变，改变的只是问候语和背景图
     *
     * 问题：
     * ①：自定义问候语的时候、问候语的文字个数，超出模板默认的问候语的长度，超出部分的坐标如何确定。
     * ②：节假日模板发送过去的是一个模板、还是仅仅发送图片
     * ④：首页显示当前公司的设备正在使用的模板信息？
     *
     */
    @Override
    public boolean addDeviceTemplate(String templateInfo){

        JSONObject jsonObject = JSONObject.parseObject(templateInfo);

        //要添加自定义模板的设备ID
        String deviceId =  jsonObject.get("deviceId").toString();

        //当前正在使用的模板id
        String currentTemplateId = JSONObject.parseObject(jsonObject.get("templateId").toString()).get("current").toString();

        //用户选择的标准模板的Id
        String selectTemplateId = JSONObject.parseObject(jsonObject.get("templateId").toString()).get("select").toString();
        //公司logo名称
        Object logoName = jsonObject.get("companyLogoName");

        //用户设置的背景图信息
        JSONArray personalBackImgList = null;
        //用户设置的问候语数据
        JSONArray personalSalutationList = null;

        if(jsonObject.get("backImgList")!=null){
            //用户设置的背景图信息
            personalBackImgList = JSONArray.parseArray(jsonObject.get("backImgList").toString());
        }
        if(jsonObject.get("salutationList")!=null){
            //用户设置的问候语数据
            personalSalutationList = JSONArray.parseArray(jsonObject.get("salutationList").toString());
        }

        //查询选择的标准模板的板式信息（问候语、背景图）
        List<Map> templateSateInfo = templateMapper.selectStandardTemplateInfo(selectTemplateId);
        //查询该标准模板的items
        List<Map> templateItems = templateMapper.selectStandardTemplateItems(selectTemplateId);
        //查询标准模板的图片信息
        List<Map> templateImages = templateMapper.selectStandardTemplateImages(selectTemplateId);


        List<Map> templateBackImgList = new ArrayList<Map>();
        //整理模板的背景图信息
        for(int i=0;i<templateImages.size();i++){
            //查询background_image_template表主键的最大值
            int templateImagePrimaryKey = templateItemsMapper.selectMaxId();
            Map map = new HashMap();
            map.put("id",templateImagePrimaryKey+i+1);
            map.put("imgId",  templateImages.get(i).get("id"));
            map.put("templateId",currentTemplateId);
            templateBackImgList.add(map);
        }

        //整理标准模板items的信息
        Map<String,Map> standardItems = new HashMap<String,Map>();
        String item_type;
        for(int y = 0;y<templateItems.size();y++){
            item_type = templateItems.get(y).get("item_type").toString();
            if(item_type.equals("0")){ //文字
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));
                innerMap.put("itemFontOrient",templateItems.get(y).get("item_font_orient"));
                innerMap.put("itemFontSize",templateItems.get(y).get("item_font_size"));
                innerMap.put("itemFontBold",templateItems.get(y).get("item_font_bold"));
                innerMap.put("itemFontColor",templateItems.get(y).get("item_font_color"));
                innerMap.put("itemStartDate",templateItems.get(y).get("item_start_date"));
                innerMap.put("itemEndDate",templateItems.get(y).get("item_end_date"));
                innerMap.put("itemFontContent",templateItems.get(y).get("item_font_content"));

                standardItems.put("type0",innerMap);
            }
            if(item_type.equals("1")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));
                innerMap.put("itemFontOrient",templateItems.get(y).get("item_font_orient"));
                innerMap.put("itemFontSize",templateItems.get(y).get("item_font_size"));
                innerMap.put("itemFontColor",templateItems.get(y).get("item_font_color"));

                standardItems.put("type1",innerMap);
            }
            if(item_type.equals("2")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));
                innerMap.put("itemFontOrient",templateItems.get(y).get("item_font_orient"));
                innerMap.put("itemFontSize",templateItems.get(y).get("item_font_size"));
                innerMap.put("itemFontColor",templateItems.get(y).get("item_font_color"));

                standardItems.put("type2",innerMap);
            }
            if(item_type.equals("3")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));
                innerMap.put("itemImgId",templateItems.get(y).get("item_img_id"));

                standardItems.put("type3",innerMap);
            }
            if(item_type.equals("4")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));

                standardItems.put("type4",innerMap);
            }
            if(item_type.equals("5")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));

                standardItems.put("type5",innerMap);
            }
            if(item_type.equals("6")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));
                innerMap.put("itemImgId",templateItems.get(y).get("item_img_id"));


                standardItems.put("type6",innerMap);
            }
            if(item_type.equals("7")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));

                standardItems.put("type7",innerMap);
            }
            if(item_type.equals("8")){
                Map innerMap = new HashMap();
                innerMap.put("itemTopX",templateItems.get(y).get("item_top_x"));
                innerMap.put("itemTopY",templateItems.get(y).get("item_top_y"));

                standardItems.put("type8",innerMap);
            }
        }

        //更新主表和items表的参数
        Map templateStateMap = new HashMap();
        Map itemsInfoMap = new HashMap();

        templateStateMap.put("templateId",currentTemplateId);
        templateStateMap.put("operateEmp","1");//用该是当前登陆的用户token
        templateStateMap.put("deviceId",deviceId);
        templateStateMap.put("operateTime",DateUtils.getDateminutes());
        templateStateMap.put("templateLevel","2");
        templateStateMap.put("roastingTime","");

        //判断用户是否输入的有新的问候语和更改背景图片
     /*   if((personalBackImgList!=null&&JSONObject.parseObject(personalBackImgList.get(0).toString()).get("startTime")!=null)||(personalSalutationList!=null&&personalSalutationList.size()>0)){
            //存在自定义问候语和背景图展示时间------->自定义的模板
            templateStateMap.put("templateLevel","2");
            //取消轮播周期
            templateStateMap.put("roastingTime","");
        }else{
            templateStateMap.put("templateLevel","0");
            templateStateMap.put("roastingTime",templateSateInfo.get(0).get("roasting_time"));
        }*/

        //TODO 更新公司Logo
        int updateLogoFlag = 0;
        if(logoName!=null&&!logoName.equals("")){
            templateStateMap.put("logoFlag",'1');
            //更新当前模板的logo
            Map logoMap = new HashMap();
            logoMap.put("imgName",logoName+".png");
            logoMap.put("templateId",currentTemplateId);
            updateLogoFlag = templateItemsMapper.updateTemplateLogo(logoMap);
        }/*else{
            templateStateMap.put("logoFlag",'0');
        }*/


        //TODO 更新模板的 items
        int insertItemFlag = 0;
        //查询当前模板的使用的logo的id
        String currentTemplateLogoId = templateItemsMapper.selectTemplateLogoId(currentTemplateId);
        //删除模板原来的items
        int delItemResult = templateItemsMapper.deleteTemplateItem(currentTemplateId);

        if(delItemResult>0){
            //添加新的模板items信息（除了问候语）
            for(int s = 1;s<9;s++){
                //查询template_items表主键的最大值
                int templateItemPrimaryKey = templateItemsMapper.selectMaxItemId();
                itemsInfoMap.put("itemId",templateItemPrimaryKey+1);
                itemsInfoMap.put("itemType",String.valueOf(s));
                itemsInfoMap.put("itemTopX",standardItems.get("type"+s).get("itemTopX"));
                itemsInfoMap.put("itemTopY",standardItems.get("type"+s).get("itemTopY"));
                itemsInfoMap.put("itemFontOrient",standardItems.get("type"+s).get("itemFontOrient"));
                itemsInfoMap.put("itemFontSize",standardItems.get("type"+s).get("itemFontSize"));
                itemsInfoMap.put("itemFontBold",standardItems.get("type"+s).get("itemFontBold"));
                itemsInfoMap.put("itemFontColor",standardItems.get("type"+s).get("itemFontColor"));
                itemsInfoMap.put("templateId",currentTemplateId);
                if(s==8){
                    if(logoName!=null&&!logoName.equals("")){
                        itemsInfoMap.put("itemImgId",currentTemplateLogoId);
                    }else{
                        itemsInfoMap.put("itemImgId",standardItems.get("type"+s).get("itemImgId"));
                    }
                }else{
                    itemsInfoMap.put("itemImgId",standardItems.get("type"+s).get("itemImgId"));
                }
                itemsInfoMap.put("itemStartDate",standardItems.get("type"+s).get("itemStartDate"));
                itemsInfoMap.put("itemEndDate",standardItems.get("type"+s).get("itemEndDate"));
                itemsInfoMap.put("itemFontContent",standardItems.get("type"+s).get("itemFontContent"));

                insertItemFlag = templateItemsMapper.insertTemplateItems(itemsInfoMap);
            }

            if(personalSalutationList!=null&&personalSalutationList.size()>0){
                //设置自定义文案标志位为1
                templateStateMap.put("salutationFlag","1");
                //添加自定义问候语
                if(delItemResult>0){
                    JSONObject salutationInfo;
                    String content;
                    String startDate;
                    String endDate;
                    for(int k=0;k<personalSalutationList.size();k++){
                        salutationInfo = JSONObject.parseObject(personalSalutationList.get(k).toString());
                        content = salutationInfo.get("content").toString();
                        startDate = salutationInfo.get("startTime").toString();
                        endDate = salutationInfo.get("endTime").toString();

                        for(int x = 0;x<content.length();x++){
                            //查询template_items表主键的最大值
                            int templateItemPrimaryKey = templateItemsMapper.selectMaxItemId();
                            Map map = new HashMap();
                            map.put("itemId",templateItemPrimaryKey+1);
                            map.put("itemType","0");
                            map.put("itemTopX","111");
                            map.put("itemTopY","222");
                            map.put("itemFontOrient",standardItems.get("type0").get("itemFontOrient"));
                            map.put("itemFontSize",standardItems.get("type0").get("itemFontSize"));
                            map.put("itemFontBold",standardItems.get("type0").get("itemFontBold"));
                            map.put("itemFontColor",standardItems.get("type0").get("itemFontColor"));
                            map.put("templateId",currentTemplateId);
                            map.put("itemImgId",null);
                            map.put("itemStartDate",startDate);
                            map.put("itemEndDate",endDate);
                            map.put("itemFontContent",content.substring(x,x+1));

                            insertItemFlag =  templateItemsMapper.insertTemplateItems(map);
                        }
                    }
                }
            }/*else{
                //采用模板默认的问候语
                templateStateMap.put("salutationFlag","0");
                for(int y = 0;y<templateItems.size();y++) {

                    if (templateItems.get(y).get("item_type").toString().equals("0")) { //文字
                        //查询template_items表主键的最大值
                        int templateItemPrimaryKey = templateItemsMapper.selectMaxItemId();
                        Map innerMap = new HashMap();

                        innerMap.put("itemId",templateItemPrimaryKey+1);
                        innerMap.put("itemType","0");
                        innerMap.put("itemTopX", templateItems.get(y).get("item_top_x"));
                        innerMap.put("itemTopY", templateItems.get(y).get("item_top_y"));
                        innerMap.put("itemFontOrient", templateItems.get(y).get("item_font_orient"));
                        innerMap.put("itemFontSize", templateItems.get(y).get("item_font_size"));
                        innerMap.put("itemFontBold", templateItems.get(y).get("item_font_bold"));
                        innerMap.put("itemFontColor", templateItems.get(y).get("item_font_color"));
                        innerMap.put("templateId",currentTemplateId);
                        innerMap.put("itemImgId",null);
                        innerMap.put("itemStartDate", templateItems.get(y).get("item_start_date"));
                        innerMap.put("itemEndDate", templateItems.get(y).get("item_end_date"));
                        innerMap.put("itemFontContent", templateItems.get(y).get("item_font_content"));

                        insertItemFlag = templateItemsMapper.insertTemplateItems(innerMap);
                    }
                }
            }*/
        }

        templateStateMap.put("templateStyle",templateSateInfo.get(0).get("template_style"));
        //TODO 更新template_主表信息
        int templateState = templateMapper.updateTemplateTable(templateStateMap);


        //TODO 更新背景图
        //删除当前模板使用的背景图
        int delBackResult = templateItemsMapper.deleteTemplateBackground(currentTemplateId);
        int updateBackResult = 0;
        //添加新的背景图
        if(personalBackImgList!=null&&JSONObject.parseObject(personalBackImgList.get(0).toString()).get("startTime")!=null){
            //使用用户自定义的背景图
            updateBackResult = updatePersonalTemplateImage(delBackResult, personalBackImgList, currentTemplateId);
        }else{
            //使用模板自带的背景图
            for(int j = 0;j<templateBackImgList.size();j++){
                updateBackResult = templateItemsMapper.insertTemplateBackground(templateBackImgList.get(j));
            }
        }

        //查询更新后的数据下发到设备
        getUpdateLaterTemplateInfo(currentTemplateId,deviceId);

        if(updateLogoFlag>0&&insertItemFlag>0&&templateState>0&&updateBackResult>0){
           return true;
        }else{
            return false;
        }
    }

    /**
     * 下发节日节气模板（每发送一次接收到响应的时候，再发下一个）
     * @param templateInfo
     * @return
     */
    @Override
    public boolean addFestivalTemplate(String templateInfo) {
        //查询所有节假日模板的信息
        List<Map> festivalTemplateInfo = templateMapper.selectFestivalTemplateInfo();

        return false;
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
     * 查询所有标准模板的详细信息
     * @return
     * 返回的数据格式模板
     *
             *[
            {
            "templateId":"3",
            "items":[
            {
            "template_id":"3",
            "item_top_x":"660",
            "item_font_orient":"0",
            "item_top_y":"88",
            "item_type":"0"
            },
            {
            "template_id":"3",
            "item_top_x":"580",
            "item_font_orient":"",
            "item_top_y":"180",
            "item_type":"3"
            },
            {
            "template_id":"3",
            "item_top_x":"30",
            "item_font_orient":"",
            "item_top_y":"802",
            "item_type":"4"
            },
            {
            "template_id":"3",
            "item_top_x":"30",
            "item_font_orient":"",
            "item_top_y":"846",
            "item_type":"5"
            },
            {
            "template_id":"3",
            "item_top_x":"628",
            "item_font_orient":"",
            "item_top_y":"1168",
            "item_type":"8"
            },
            {
            "template_id":"3",
            "item_top_x":"",
            "item_font_orient":"",
            "item_top_y":"",
            "item_type":"6"
            },
            {
            "template_id":"3",
            "item_top_x":"24",
            "item_font_orient":"",
            "item_top_y":"1128",
            "item_type":"7"
            },
            {
            "template_id":"3",
            "item_top_x":"661",
            "item_font_orient":"1",
            "item_top_y":"370",
            "item_type":"1"
            },
            {
            "template_id":"3",
            "item_top_x":"570",
            "item_font_orient":"0",
            "item_top_y":"70",
            "item_type":"0"
            },
            {
            "template_id":"3",
            "item_top_x":"596",
            "item_font_orient":"1",
            "item_top_y":"212",
            "item_type":"2"
            }
            ],
            "backgrounds":[
            {
            "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
            "id":"27",
            "template_id":"3",
            "img_name":"back"
            }
            ]
            }
            ]
     */
    @Override
    public List<Map> queryStandardTemplateInfo() {
        //查询背景图片信息
        List<Map> standardImage = templateMapper.selectStandardTemplateDetailImageInfo();
        //查询item信息
        List<Map> standardItems = templateMapper.selectStandardTemplateDetailItemInfo();
        //TODO 整合数据
       //将图片按照模板的Id进行分组
        Map<String, List<Map>> imgListMap = groupByTemplateId(standardImage);
        //将item按照模板的id进行分组
        Map<String, List<Map>> itemListMap = groupByTemplateId(standardItems);

        List<Map> outterFrame = new ArrayList<Map>();
        for(String myKey:imgListMap.keySet()){
            Map innerFrame = new HashMap();
            innerFrame.put("templateId",myKey);
            innerFrame.put("backgrounds",imgListMap.get(myKey));
            innerFrame.put("items",itemListMap.get(myKey));

            outterFrame.add(innerFrame);
        }
        return outterFrame;
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
    public boolean removeDeviceTemplate(String deviceId, List<String> templateIds) {
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

        if(delResult>0){
            return true;
        }else{
            return false;
        }
    }


    /**
     * TODO 根据模板的ID对数据进行分组
     */
    public  Map<String,List<Map>> groupByTemplateId(List<Map> standardInfo){
        Map<String,List<Map>> mapList = new HashMap<String,List<Map>>();
        String itemKey = "";
        //将item按照模板的id进行分组
        for(int j=0;j<standardInfo.size();j++){
            itemKey = standardInfo.get(j).get("template_id").toString();
            if(mapList.containsKey(itemKey)){
                mapList.get(itemKey).add(standardInfo.get(j));
            }else{
                List<Map> innerListMap = new ArrayList<>();
                innerListMap.add(standardInfo.get(j));
                mapList.put(itemKey,innerListMap);
            }
        }
        return mapList;
    }

    /**
     * TODO 更新模板背景图（用户自定义背景图）
     */
    public int  updatePersonalTemplateImage(int delBackResult,JSONArray backImgList,String currentTemplateId){
        int updateBackResult = 0;
        if(delBackResult>0) {
            JSONObject backInfo;
            String[] startTime;
            String[] endTime;
            for (int i = 0; i < backImgList.size(); i++) {
                backInfo = JSONObject.parseObject(backImgList.get(i).toString());
                //获取背景图设置的显示时间
                startTime = backInfo.get("startTime").toString().split(" ");
                endTime = backInfo.get("endTime").toString().split(" ");
                Map map = new HashMap();
                //查询background_image_template表主键的最大值
                int templateImagePrimaryKey = templateItemsMapper.selectMaxId();
                map.put("id", templateImagePrimaryKey + 1);
                map.put("imgId", backInfo.get("imgId"));
                map.put("templateId", currentTemplateId);
                map.put("startDate", startTime[0]);
                map.put("startTime", startTime[1]);
                map.put("endDate", endTime[0]);
                map.put("endTime", endTime[1]);
                //添加新的背景图
                updateBackResult = templateItemsMapper.insertTemplateBackground(map);
            }
        }
        return updateBackResult;
    }

    /**
     * TODO 查询更新后的模板信息，下发到设备
     */
    public void getUpdateLaterTemplateInfo(String templateId,String deviceId){


        //查询当前模板的详细信息，下发到设备中
        List<Map> currTemplateInfo = templateMapper.selectTemplateDetailInfo(templateId);
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
            templateInfoMap.put("templateType",currTemplateInfo.get(0).get("template_type")==null?"":currTemplateInfo.get(0).get("template_type"));
            templateInfoMap.put("templateLevel",currTemplateInfo.get(0).get("template_level")==null?"":currTemplateInfo.get(0).get("template_level"));
            templateInfoMap.put("imageName",imagesInfoForCommand.get(t).get("img_name")==null?"":imagesInfoForCommand.get(t).get("img_name"));
            templateInfoMap.put("imagePath",imagesInfoForCommand.get(t).get("img_url")==null?"":imagesInfoForCommand.get(t).get("img_url"));
            templateInfoMap.put("alertButtonColor",imagesInfoForCommand.get(t).get("ripple_color")==null?"":imagesInfoForCommand.get(t).get("ripple_color"));
            templateInfoMap.put("alertImgName",imagesInfoForCommand.get(t).get("relate_img_name")==null?"":imagesInfoForCommand.get(t).get("relate_img_name"));
            templateInfoMap.put("alertImgPath",imagesInfoForCommand.get(t).get("relate_img_url")==null?"":imagesInfoForCommand.get(t).get("relate_img_url"));
            templateInfoMap.put("broadStartDate",imagesInfoForCommand.get(t).get("broad_start_date")==null?"":imagesInfoForCommand.get(t).get("broad_start_date"));
            templateInfoMap.put("broadEndDate",imagesInfoForCommand.get(t).get("broad_end_date")==null?"":imagesInfoForCommand.get(t).get("broad_end_date"));
            templateInfoMap.put("broadStartTime",imagesInfoForCommand.get(t).get("broad_start_time")==null?"":imagesInfoForCommand.get(t).get("broad_start_time"));
            templateInfoMap.put("broadEndTime",imagesInfoForCommand.get(t).get("broad_end_time")==null?"":imagesInfoForCommand.get(t).get("broad_end_time"));
            templateInfoMap.put("roastingTime",currTemplateInfo.get(0).get("roasting_time")==null?"":currTemplateInfo.get(0).get("roasting_time"));

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


                //刚添加的数据
                itemMap.put("itemTopX",itemsInfoForCommand.get(f).get("item_top_x")==null?"":itemsInfoForCommand.get(f).get("item_top_x"));
                itemMap.put("itemTopY",itemsInfoForCommand.get(f).get("item_top_y")==null?"":itemsInfoForCommand.get(f).get("item_top_y"));
                itemMap.put("itemFontOrient",itemsInfoForCommand.get(f).get("item_font_orient")==null?"":itemsInfoForCommand.get(f).get("item_font_orient"));
                itemMap.put("itemFontSize",itemsInfoForCommand.get(f).get("item_font_size")==null?"":itemsInfoForCommand.get(f).get("item_font_size"));
                itemMap.put("itemFontBold",itemsInfoForCommand.get(f).get("item_font_bold")==null?"":itemsInfoForCommand.get(f).get("item_font_bold"));
                itemMap.put("itemFontColor",itemsInfoForCommand.get(f).get("item_font_color")==null?"":itemsInfoForCommand.get(f).get("item_font_color"));
                itemMap.put("itemFontContent",itemsInfoForCommand.get(f).get("item_font_content")==null?"":itemsInfoForCommand.get(f).get("item_font_content"));
                itemMap.put("itemStartDate",itemsInfoForCommand.get(f).get("item_start_date")==null?"":itemsInfoForCommand.get(f).get("item_start_date"));
                itemMap.put("itemEndDate",itemsInfoForCommand.get(f).get("item_end_date")==null?"":itemsInfoForCommand.get(f).get("item_end_date"));
                itemMap.put("itemImgName",itemsInfoForCommand.get(f).get("img_name")==null?"":itemsInfoForCommand.get(f).get("img_name"));
                itemMap.put("itemImgPath",itemsInfoForCommand.get(f).get("img_url")==null?"":itemsInfoForCommand.get(f).get("img_url"));

            /*    //判断item的类型
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
*/
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
        rabbitMQSender.sendMessage(downloadQueueName, doorCmdPackageAll);
    }
}
