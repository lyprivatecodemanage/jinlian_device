package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.Employee;
import com.xiangshangban.device.bean.Font;
import com.xiangshangban.device.common.utils.PageUtils;
import com.xiangshangban.device.common.utils.ReturnCodeUtil;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * author : Administrator
 * date: 2017/11/4 10:04
 * describe: TODO
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ITemplateService iTemplateService;

    @Autowired
    private IEmployeeService iEmployeeService;

    /**
     * TODO 查询所有设备自定义模板信息
     * 请求参数
     * {
     *     "deviceId":"",
     *     "deviceName":"",
     *     "page":"",
     *     "rows":""
     * }
     */
    @PostMapping("/getDeviceTemplate")
    public String getDeviceTemplateInfo(@RequestBody String requestParam ){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        Object deviceId = jsonObject.get("deviceId");
        Object deviceName = jsonObject.get("deviceName");

        List<Map> templates = iTemplateService.queryDeviceTemplateInfo(deviceId!=null?deviceId.toString():null,deviceName!=null?deviceName.toString():null);

        //封装完整信息
        List<Map> fullInfo = new ArrayList<Map>();

        //判断该设备是否有主题模板，有的话查询出该模板的详细信息
        for(int i=0;i<templates.size();i++){
            //创建Map封装数据
            Map map = new HashMap();

            if(templates.get(i).get("template_id")!=null&&!templates.get(i).get("template_id").toString().isEmpty()){
                //获取主题操作人名称
                Employee operate_emp = iEmployeeService.findEmployeeById(templates.get(i).get("operate_emp").toString());
                //模板的item信息
                List<Map> templateItems = iTemplateService.queryTemplateItems(templates.get(i).get("template_id").toString());

                //TODO 数据处理，按照时间将问候语进行分组
                String itemType = "";
                String timeScope = "";
                Map<String,Map<String,String>> resultMap = new HashMap<String,Map<String,String>>();
                List<String> timeKey = new ArrayList<String>();
                Map logoMap = new HashMap();

                for(int j=0;j<templateItems.size();j++){
                    itemType = templateItems.get(j).get("item_type").toString();
                    //type==0表明是文字信息
                    if(itemType.equals("0")) {
                        timeScope = templateItems.get(j).get("item_start_date").toString() + "~" + templateItems.get(j).get("item_end_date").toString();
                        //对时间进行判断
                        if (resultMap.containsKey(timeScope)) {
                            //获取单个字的id、内容、X坐标和Y坐标
                            resultMap.get(timeScope).put(templateItems.get(j).get("item_id").toString(),templateItems.get(j).get("item_font_content").toString()+"-"+templateItems.get(j).get("item_top_x").toString()+"-"+templateItems.get(j).get("item_top_y").toString());
                            //一句问候语中的字的方向都是一致的
                            resultMap.get(timeScope).put("fontOrient",templateItems.get(j).get("item_font_orient").toString());
                            timeKey.add(timeScope);
                        } else {
                            Map innerMap = new HashMap();
                            innerMap.put(templateItems.get(j).get("item_id").toString(),templateItems.get(j).get("item_font_content").toString()+"-"+templateItems.get(j).get("item_top_x").toString()+"-"+templateItems.get(j).get("item_top_y").toString());
                            innerMap.put("fontOrient",templateItems.get(j).get("item_font_orient").toString());
                            resultMap.put(timeScope,innerMap);
                        }
                    }else{
                        logoMap.put("itemType",templateItems.get(j).get("item_type").toString());
                        logoMap.put("itemId",templateItems.get(j).get("item_id").toString());
                        logoMap.put("imgUrl",templateItems.get(j).get("img_url").toString());
                        logoMap.put("imgName",templateItems.get(j).get("img_name").toString());
                    }
                }

                //TODO 拼接完整的问候语
                List<Map> listMap = new ArrayList<>();
                StringBuffer content = new StringBuffer();
                String fontOrient = "";
                List<String> keyList = new ArrayList<>();
                List<Font> fontList = new ArrayList<Font>();
                for(int k=0;k<timeKey.size();k++){//key是一个时间区间（处理的内容是该时间区间中的问候语）

                    Map myMap = new HashMap();
                    //获取字的方向
                    fontOrient = resultMap.get(timeKey.get(k)).get("fontOrient");
                    for(String key:  resultMap.get(timeKey.get(k)).keySet()){
                        keyList.add(key);
                    }
                    //去除指定的键fontOrient
                    keyList.remove("fontOrient");

                    //TODO 更正字的顺序
                    for(int s = 0;s<keyList.size();s++){
                        //获取每一个字
                        String[] outterFontInfo =  resultMap.get(timeKey.get(k)).get(keyList.get(s)).split("-");// 逝
                        //获取字的内容
                        String outterFontContent = outterFontInfo[0];
                        //获取字的X坐标
                        String outterFontX = outterFontInfo[1];
                        //获取字的Y坐标
                        String outterFontY = outterFontInfo[2];
                        //创建Font对象
                        Font font = new Font();
                        font.setContent(outterFontContent);
                        font.setCoordinateX(outterFontX);
                        font.setCoordinateY(outterFontY);

                        //保存文字到集合中
                        fontList.add(font);
                    }

                    //对文字进行排序
                    if(fontOrient.equals("0")){//横向，根据X坐标进行排序
                        Collections.sort(fontList, new Comparator<Font>() {
                            public int compare(Font o1, Font o2) {
                                return o1.getCoordinateX().compareTo(o2.getCoordinateX());
                            }
                        });
                    }

                    if(fontOrient.equals("1")){
                        Collections.sort(fontList, new Comparator<Font>() {
                            public int compare(Font o1, Font o2) {
                                return o1.getCoordinateY().compareTo(o2.getCoordinateY());
                            }
                        });
                    }

                    //输出问候语
                    for (Font font: fontList
                         ) {
                        content.append(font.getContent());
                    }

                    String[] times = timeKey.get(k).split("~");
                    myMap.put("startTime",times[0]);
                    myMap.put("endTime",times[1]);
                    myMap.put("content",content);
                    myMap.put("itemType",0);
                    listMap.add(myMap);
                }

                listMap.add(logoMap);

                //模板的背景图以及展示时间
                List<Map> templateImages = iTemplateService.queryTemplateImages(templates.get(i).get("template_id").toString());

                map.put("deviceId",templates.get(i).get("device_id"));
                map.put("deviceName",templates.get(i).get("device_name"));
                map.put("templateId",templates.get(i).get("template_id"));
                map.put("templateStyle",templates.get(i).get("template_style"));

                //是否有公司Logo
                if(templates.get(i).get("logo_flag")!=null&&!templates.get(i).get("logo_flag").toString().isEmpty()){
                    if(Integer.parseInt(templates.get(i).get("logo_flag").toString())==0){
                        map.put("companyLogo","没有");
                    }else{
                        map.put("companyLogo","有");
                    }
                }

                //是否自定义问候语
                if(templates.get(i).get("solutation_flag")!=null&&!templates.get(i).get("solutation_flag").toString().isEmpty()){
                    if(Integer.parseInt(templates.get(i).get("solutation_flag").toString())==0){
                        map.put("salutation","未设置");
                    }else{
                        map.put("salutation","已设置");
                    }
                }

                map.put("operateEmployee",operate_emp.getEmployeeName());
                map.put("operateTime",templates.get(i).get("operate_time"));

                map.put("items",listMap);

                map.put("images",templateImages);

            }else{ //还没有设置模板
                map.put("deviceId",templates.get(i).get("device_id"));
                map.put("deviceName",templates.get(i).get("device_name"));
            }
            fullInfo.add(map);
        }
        List<Map> newInfo = new ArrayList<>();
        //进行分页操作
        if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
            int pageIndex = Integer.parseInt(page.toString());
            int pageSize = Integer.parseInt(rows.toString());

            for(int i=((pageIndex-1)*pageSize);i<(pageSize*pageIndex);i++){
                    if(i==fullInfo.size()){
                        break;
                    }
                    newInfo.add(fullInfo.get(i));
                }
            }

        Map result = PageUtils.doSplitPage(fullInfo,newInfo,page,rows,null,1);
        return JSONArray.toJSONString(result);
    }

    /**
     * TODO 查询所有的背景图（根据类别进行分组）
     */
    @GetMapping("/getAllBackground")
    public String getAllBackGround(){

        List<Map> maps = iTemplateService.queryAllBackGround();
        Map<String,List<Map>> map = new HashMap<String,List<Map>>();
        List back_show = new ArrayList();
        List back_visit = new ArrayList();
        List back_festival = new ArrayList();
        //对图片进行分类
        for(int i=0;i<maps.size();i++){
            if(maps.get(i).get("img_type").toString().equals("back_show")){
                back_show.add(maps.get(i));
            }
            if(maps.get(i).get("img_type").toString().equals("back_visit")){
                back_visit.add(maps.get(i));
            }
            if(maps.get(i).get("img_type").toString().equals("back_festival")){
                back_festival.add(maps.get(i));
            }
        }
        map.put("back_show",back_show);
        map.put("back_visit",back_visit);
        map.put("back_festival",back_festival);

        return JSONObject.toJSONString(map);

    }
    /**
     * TODO 查询所有标准模板的详细信息(关于坐标的问题，可以进行等比缩小。)
     */
    @GetMapping("/getStandardTemplateInfo")
    public String getStandardTemplateInfo(){
        List<Map> templateInfo = iTemplateService.queryStandardTemplateInfo();
        return JSONObject.toJSONString(templateInfo);
    }

    //TODO ==================<2017-11-22>====================

    /**
     * TODO 添加自定义的模板
     *
     {
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
     */
    @PostMapping ("/addDeviceTemplate")
    public String addDeviceTemplate(@RequestParam("templateInfo") String templateInfo, @RequestParam("file")MultipartFile file){
        Map result = iTemplateService.addDeviceTemplate(templateInfo,file);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * TODO 更新设备自定义模板信息
     *
     "salutationList":[ 请求参数数据格式：
         *{
         "deviceId":"1",
         "templateId":"2",
         "backImgList":[

         {"imgId":"1","startTime":"2017-11-04 08:00","endTime":"2017-11-04 12:00"},
         {"imgId":"2","startTime":"2017-11-04 12:00","endTime":"2017-11-04 18:00"}

         ],


         {"content":"上午","startTime":"2017-11-04 08:00","endTime":"2017-11-04 12:00"},
         {"content":"下午","startTime":"2017-11-04 12:00","endTime":"2017-11-04 18:00"}

         ],
         "companyLogoName":"xxx"
         }
     *
     */
    @PostMapping("/refreshDeviceTemplate")
    public String refreshDeviceTemplate(@RequestParam("templateInfo") String templateInfo, @RequestParam("file")MultipartFile file){
        //更新模板信息
        Map result= iTemplateService.modifyDeviceTemplateInfo(templateInfo,file);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * TODO 重置设备的模板（恢复成默认的主题：删除该设备所有的自定义模板？）
         {
         "deviceId":"1"
         }
     */
    @PostMapping ("/resetDeviceTemplate")
    public String resetDeviceTemplate(@RequestBody String delTemplateInfo){
        return null;
    }
}
