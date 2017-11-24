package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.DoorCmd;
import com.xiangshangban.device.bean.Font;
import com.xiangshangban.device.bean.Template;
import com.xiangshangban.device.common.rmq.RabbitMQSender;
import com.xiangshangban.device.common.utils.CalendarUtil;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.common.utils.ReturnCodeUtil;
import com.xiangshangban.device.dao.DoorCmdMapper;
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

    @Autowired
    private DoorCmdMapper doorCmdMapper;

    @Value("${rabbitmq.download.queue.name}")
    String downloadQueueName;

    /************************************************************
     * TODO 设备端接口
     ************************************************************/
    /**
     * TODO 更新自定义模板信息(背景图、问候语、logo)
     * @param templateInfo 新数据
     * @return
             {
            "deviceId":"1",
            "templateId":"2",-------->要进行更新的模板的ID
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
     */
    @Override
    public Map modifyDeviceTemplateInfo(String templateInfo) {

        JSONObject jsonObject = JSONObject.parseObject(templateInfo);
        Object deviceId = jsonObject.get("deviceId");
        Object templateId = jsonObject.get("templateId");
        Object backImgList = jsonObject.get("backImgList");
        Object salutationList = jsonObject.get("salutationList");
        Object companyLogoName = jsonObject.get("companyLogoName");

        //返回给Controller层的数据
        Map result = null;
        //数据完善的时候进行添加自定义模板操作
        if(deviceId!=null&&templateId!=null&&backImgList!=null&&salutationList!=null&&companyLogoName!=null){
            //设备ID
            String rdeviceId =  jsonObject.get("deviceId").toString();
            //用户要更新的自定义模板的ID
            String selectTemplateId = jsonObject.get("templateId").toString();
            //用户设置的背景图信息
            JSONArray personalBackImgList =JSONArray.parseArray(jsonObject.get("backImgList").toString());
            //用户设置的问候语数据
            JSONArray personalSalutationList =JSONArray.parseArray(jsonObject.get("salutationList").toString());
            //公司logo名称
            String logoName = jsonObject.get("companyLogoName").toString();

            //查询该自定义模板使用的标准模板ID
            String standardTemplateId = templateMapper.selectStandardTemplateIdByStyle(selectTemplateId);
            //查询自定义模板使用的标准模板的板式信息(包含各个item的坐标以及关联的图片<文字修饰框、铃铛背景图>)
            List<Map> templateItems = templateMapper.selectStandardItemInfo(standardTemplateId);

            //删除原本的自定义模板（模板item、模板图片)，然后然后将用户更改的新的数据，和使用的标准模板信息整合成新的自定义模板，仍然使用原本自定义模板的Id
            int tresult = templateMapper.deletePersonalTemplate(selectTemplateId);
            int bresult = templateMapper.deleteBackgroundImage(selectTemplateId);
            int iresult = templateItemsMapper.deletePersonalTemplateItemInfo(selectTemplateId);

            if(tresult>0&&bresult>0&&iresult>0){
                //添加新的自定义模板，代替删除的自定义模板
                boolean operateResult = addNewPersonalTemplate(rdeviceId, templateItems, personalBackImgList, personalSalutationList, logoName);
                //下发到设备
                issueUpdateLaterTemplateInfo(String.valueOf(templateMapper.selectTemplateMaxPrimaryKey()),rdeviceId);
                //查询设备那边的执行情况(睡眠一段时间,等待回复)
                try {
                    Thread.sleep(3000);
                }catch(Exception e){
                    e.printStackTrace();
                }
                //查询该指令的执行情况
                String commandResult = doorCmdMapper.selectDoorCmdResultCode();
                if(commandResult.equals("0")&&operateResult){//执行成功
                    ReturnCodeUtil.addReturnCode(true);
                }else{
                    ReturnCodeUtil.addReturnCode(false);
                }
            }
        }else{
            //返回“参数错误”提示
            result = ReturnCodeUtil.addReturnCode(1);
        }
        return result;
    }

     /**
     * TODO 添加自定义模板
     * @param templateInfo
     * @return
      * 参数：
              *{
                 "deviceId":"1", ------->要进行模板添加的设备
                 "templateId":"",------>选择的标准模板的ID
                 "backImgList":[--------用户设置的背景图信息
                     {"imgId":"1","startTime":"2017-11-04 08:00","endTime":"2017-11-04 12:00"},
                     {"imgId":"2","startTime":"2017-11-04 12:00","endTime":"2017-11-04 18:00"}
                 ],
                 "salutationList":[
                     {"content":"上午","startTime":"2017-11-04 08:00","endTime":"2017-11-04 12:00"},
                     {"content":"下午","startTime":"2017-11-04 12:00","endTime":"2017-11-04 18:00"}
                 ],
                 "companyLogoName":"logoName"--------------->用户上传的公司Logo名称
             }
      *
      *
     */
    public Map addDeviceTemplate(String templateInfo){

        JSONObject jsonObject = JSONObject.parseObject(templateInfo);
        Object deviceId = jsonObject.get("deviceId");
        Object templateId = jsonObject.get("templateId");
        Object backImgList = jsonObject.get("backImgList");
        Object salutationList = jsonObject.get("salutationList");
        Object companyLogoName = jsonObject.get("companyLogoName");

        //返回给Controller层的数据
        Map result = null;
        //数据完善的时候进行添加自定义模板操作
        if(deviceId!=null&&templateId!=null&&backImgList!=null&&salutationList!=null&&companyLogoName!=null){
            //设备ID
            String rdeviceId =  jsonObject.get("deviceId").toString();
            //用户选择的标准模板的Id
            String selectTemplateId = jsonObject.get("templateId").toString();
            //用户设置的背景图信息
            JSONArray personalBackImgList =JSONArray.parseArray(jsonObject.get("backImgList").toString());
            //用户设置的问候语数据
            JSONArray personalSalutationList =JSONArray.parseArray(jsonObject.get("salutationList").toString());
            //公司logo名称
            String logoName = jsonObject.get("companyLogoName").toString();

            //查询选择的标准模板的板式信息(包含各个item的坐标以及关联的图片<文字修饰框、铃铛背景图>)
            List<Map> templateItems = templateMapper.selectStandardItemInfo(selectTemplateId);
            //根据设备的ID查询该设备上是否有自定义的模板(有的话，更新该自定义模板，没有的话添加新的自定义模板)
            Template resultTemplate = templateMapper.confirmPersonalTemplate(rdeviceId);

            if(resultTemplate==null){
                addNewPersonalTemplate(rdeviceId,templateItems,personalBackImgList,personalSalutationList,logoName);
            }else{
                //删除拥有的自定义模板（模板item、模板图片)，然后添加新的自定义模板
                int tresult = templateMapper.deletePersonalTemplate(resultTemplate.getTemplateId());
                int bresult = templateMapper.deleteBackgroundImage(resultTemplate.getTemplateId());
                int iresult = templateItemsMapper.deletePersonalTemplateItemInfo(resultTemplate.getTemplateId());

                if(tresult>0&&bresult>0&&iresult>0){
                    //添加新的自定义模板，代替删除的自定义模板
                    boolean operateResult =  addNewPersonalTemplate(rdeviceId,templateItems,personalBackImgList,personalSalutationList,logoName);
                    //下发到设备
                    issueUpdateLaterTemplateInfo(String.valueOf(templateMapper.selectTemplateMaxPrimaryKey()),rdeviceId);
                    //查询设备那边的执行情况
                    try {
                        Thread.sleep(3000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    //查询该指令的执行情况
                    String commandResult = doorCmdMapper.selectDoorCmdResultCode();
                    if(commandResult.equals("0")&&operateResult){//执行成功
                        ReturnCodeUtil.addReturnCode(true);
                    }else{
                        ReturnCodeUtil.addReturnCode(false);
                    }
                    result = ReturnCodeUtil.addReturnCode(operateResult);
                }
            }
        }else{
            //返回参数错误提示
            result = ReturnCodeUtil.addReturnCode(1);
        }
        return result;
    }

    /**
     * TODO 下发节日节气模板（后台判断日期下发相对应的模板）
     * @param
     * @return
     */
    @Override
    public boolean addFestivalTemplate() {
        //查询所有节假日模板的信息
        List<Map> festivalTemplateInfo = templateMapper.selectFestivalTemplateInfo();
        return false;
    }


    /**********************************************************
     * TODO web端接口
     *********************************************************/

    /**
     * TODO 查询所有的设备自定义模板信息
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
     * TODO 查询所有标准模板的详细信息
     * @return
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
     * TODO 根据模板ID查询模板items
     * @param templateId
     * @return
     */
    @Override
    public List<Map> queryTemplateItems(String templateId) {
        List<Map> maps = templateMapper.selectTemplateItems(templateId);
        return maps;
    }

    /**
     * TODO 根据模板id查找模板关联的图片
     */
    @Override
    public List<Map> queryTemplateImages(String templateId){
        List<Map> maps = templateMapper.selectTemplateImages(templateId);
        return maps;
    }

    /**
     * TODO 获取所有的背景图
     * @return
     */
    @Override
    public List<Map> queryAllBackGround() {
        List<Map> maps = imagesMapper.selectAllBackGround();
        return maps;
    }


    /*********************************************************
     *   TODO 公共方法区
     *********************************************************/
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
     * TODO 添加（替换）自定义模板(公共方法)
     */
    public boolean addNewPersonalTemplate(String rdeviceId,List<Map> templateItems,JSONArray personalBackImgList,JSONArray personalSalutationList,String logoName) {
        //添加新的自定义模板
        Map templateMap = new HashMap();

        templateMap.put("template_id", templateMapper.selectTemplateMaxPrimaryKey() + 1);
        templateMap.put("template_type", "0");
        templateMap.put("template_level", "2");
        templateMap.put("operate_time", DateUtils.getDateTime());
        templateMap.put("operate_emp", "88F332DF5D2243BCB69B1CB14D3473CA");//后期要更改为当前登录人的ID
        templateMap.put("device_id", rdeviceId);
        templateMap.put("roasting_time", "");
        templateMap.put("logo_flag", "1");
        templateMap.put("solutation_flag", "1");
        templateMap.put("festival_name", "");
        templateMap.put("template_style", templateItems.get(0).get("template_style"));
        templateMap.put("is_use", "1");
        templateMap.put("is_standard", "0");

        //TODO ①：向template_表中添加数据
        int insertTemplateResult = templateMapper.insertIntoPersonalTemplate(templateMap);

        //TODO ②：向background_image_template表中添加数据（设置背景图）
        int insertBackImageResult = 0;
        for (int i = 0; i < personalBackImgList.size(); i++) {
            JSONObject backJSONobj = JSONObject.parseObject(personalBackImgList.get(i).toString());
            Map backgroundImageMap = new HashMap();
            backgroundImageMap.put("id", templateMapper.selectBackgroundImageTemplatePrimaryKey() + 1);
            backgroundImageMap.put("img_id", backJSONobj.get("imgId"));
            backgroundImageMap.put("template_id", templateMapper.selectTemplateMaxPrimaryKey());
            backgroundImageMap.put("broad_start_date", backJSONobj.get("startTime").toString().split(" ")[0]);
            backgroundImageMap.put("broad_start_time", backJSONobj.get("startTime").toString().split(" ")[1]);
            backgroundImageMap.put("broad_end_date", backJSONobj.get("endTime").toString().split(" ")[0]);
            backgroundImageMap.put("broad_end_time", backJSONobj.get("endTime").toString().split(" ")[1]);

            insertBackImageResult =  templateMapper.insertIntoBackImage(backgroundImageMap);
        }

        //TODO ③：向template_item表中添加数据
        int insertItemResult = 0;
        //保存标准模板文字板式
        List<Map> fontStyle = new ArrayList<>();
        //添加除问候语外的部分(itemType:1-8   <0：问候语 7：二维码，8：公司logo >)
        for (int i = 0; i < templateItems.size(); i++) {
            //获取item_type
            String itemType = templateItems.get(i).get("item_type").toString();

            if (itemType.equals("0")) {//跳过问候语的添加(添加问候语item的条数是根据用户上传的问候语的来决定的)
                //储存标准模板每一个字的板式信息
                fontStyle.add(templateItems.get(i));
                continue;
            }
            Map map = new HashMap();
            map.put("item_id", templateItemsMapper.selectMaxItemId() + 1);
            map.put("item_type", itemType);
            map.put("item_top_x", templateItems.get(i).get("item_top_x") == null ? "" : templateItems.get(i).get("item_top_x").toString());
            map.put("item_top_y", templateItems.get(i).get("item_top_y") == null ? "" : templateItems.get(i).get("item_top_y").toString());
            map.put("item_font_orient", templateItems.get(i).get("item_font_orient") == null ? "" : templateItems.get(i).get("item_font_orient").toString());
            map.put("item_font_size", templateItems.get(i).get("item_font_size") == null ? "" : templateItems.get(i).get("item_font_size").toString());
            map.put("item_font_bold", templateItems.get(i).get("item_font_bold") == null ? "" : templateItems.get(i).get("item_font_bold").toString());
            map.put("item_font_color", templateItems.get(i).get("item_font_color") == null ? "" : templateItems.get(i).get("item_font_color").toString());
            map.put("template_id", templateMapper.selectTemplateMaxPrimaryKey());
            if (itemType.equals("7") || itemType.equals("8")) {//根据用户选择的设备信息生成二维码,上传之后，该item要使用二维码图片的id
                if (itemType.equals("7")) {//用户上传二维码
                    map.put("item_img__id", "222");
                }
                if (itemType.equals("8")) {//用户上传logo后，该item要使用logo图片的id
                    map.put("item_img__id", "666");
                }
            } else {
                //使用模板默认的图片
                map.put("item_img_id", templateItems.get(i).get("item_img_id") == null ? "" : templateItems.get(i).get("item_img_id").toString());
            }
            map.put("item_start_date", templateItems.get(i).get("item_start_date") == null ? "" : templateItems.get(i).get("item_start_date").toString());
            map.put("item_end_date", templateItems.get(i).get("item_end_date") == null ? "" : templateItems.get(i).get("item_end_date").toString());
            map.put("item_font_content", templateItems.get(i).get("item_font_content") == null ? "" : templateItems.get(i).get("item_font_content").toString());

            insertItemResult = templateItemsMapper.insertPersonalTemplateItemInfo(map);
        }

        //TODO 整理fontStyle
        int insertFontResult = 0;
        List<Font> fontList = new ArrayList<>();
        //获取文字的方向
        String fontOrient = fontStyle.get(0).get("item_font_orient").toString();
        for (int f = 0; f < fontStyle.size(); f++) {
            //将文字信息封装到font类中
            Font font = new Font();
            font.setCoordinateX(fontStyle.get(f).get("item_top_x").toString());
            font.setCoordinateY(fontStyle.get(f).get("item_top_y").toString());
            font.setFontBold(fontStyle.get(f).get("item_font_bold").toString());
            font.setFontColor(fontStyle.get(f).get("item_font_color").toString());
            font.setFontSize(fontStyle.get(f).get("item_font_size").toString());
            font.setFontOrient(fontOrient);

            fontList.add(font);
        }

        //TODO 对文字进行排序
        //对文字进行排序
        if (fontOrient.equals("0")) {//横向，根据X坐标进行排序
            Collections.sort(fontList, new Comparator<Font>() {
                public int compare(Font o1, Font o2) {
                    return o1.getCoordinateX().compareTo(o2.getCoordinateX());
                }
            });
        }
        if (fontOrient.equals("1")) {//纵向，根据Y坐标进行排序
            Collections.sort(fontList, new Comparator<Font>() {
                public int compare(Font o1, Font o2) {
                    return o1.getCoordinateY().compareTo(o2.getCoordinateY());
                }
            });
        }
        //向template_item表中添加自定义问候语
        for (int k = 0; k < personalSalutationList.size(); k++) {
            JSONObject salutationObj = JSONObject.parseObject(personalSalutationList.get(k).toString());
            //获取文本内容(其中的一句)
            String content = salutationObj.get("content").toString();

            //一次添加一个字
            for (int s = 0; s < content.length(); s++) {
                Map map = new HashMap();
                map.put("item_id", templateItemsMapper.selectMaxItemId() + 1);
                map.put("item_type", "9");
                map.put("item_top_x", fontList.get(s).getCoordinateX());
                map.put("item_top_y", fontList.get(s).getCoordinateY());
                map.put("item_font_orient", fontList.get(s).getFontOrient());
                map.put("item_font_size", fontList.get(s).getFontSize());
                map.put("item_font_bold", fontList.get(s).getFontBold());
                map.put("item_font_color", fontList.get(s).getFontColor());
                map.put("template_id", templateMapper.selectTemplateMaxPrimaryKey());
                map.put("item_img__id", "");
                map.put("item_start_date", salutationObj.get("startTime").toString());
                map.put("item_end_date", salutationObj.get("endTime").toString());
                map.put("item_font_content", content.substring(s, s + 1));

                insertFontResult =  templateItemsMapper.insertPersonalTemplateItemInfo(map);
            }
        }

        //判断执行结果是否成功
        if(insertTemplateResult>0&&insertBackImageResult>0&&insertItemResult>0&&insertFontResult>0){
            return false;
        }else{
            return true;
        }
    }

    /**
     * TODO 查询新增（替换、更新）后的自定义模板信息，下发到设备
     */
    public void issueUpdateLaterTemplateInfo(String templateId,String deviceId){

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
        rabbitMQSender.sendMessage(deviceId, doorCmdPackageAll);
    }
}
