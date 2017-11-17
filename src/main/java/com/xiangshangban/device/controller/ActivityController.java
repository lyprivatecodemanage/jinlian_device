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
     * 查询所有设备使用的模板信息
         [
         {
         "templateId":"2",
         "operateTime":"2017-11-11 14:50",
         "deviceName":"设备1",
         "items":[
         {
         "content":"秋逝",
         "startTime":"2017-11-10 08:00",
         "itemType":0,
         "endTime":"2017-11-10 18:00"
         },
         {
         "imgUrl":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
         "imgName":"logo",
         "itemType":"8",
         "itemId":"51"
         }
         ],
         "templateStyle":"vx",
         "images":[
         {
         "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
         "id":"27",
         "img_name":"back"
         }
         ],
         "companyLogo":"有",
         "salutation":"未设置",
         "operateEmployee":"员工1",
         "deviceId":"1"
         },
         {
         "totalCount":"1",
         "totalPage":"1"
         }
         ]
    /**
     * 请求参数
     * {
     *     "deviceId":"",
     *     "deviceName":"",
     *     "page":"",
     *     "rows":""
     * }
     *
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
                newInfo.add(fullInfo.get(i));
            }
        }

        Map result = PageUtils.doSplitPage(newInfo,page,rows);
        return JSONArray.toJSONString(result);
    }

    /**
     * 查询所有的背景图（根据类别进行分组）
     *
     *
             * {
             "back_festival":[
             {
             "img_type":"back_festival",
             "img_url":"http://vfdvfdv",
             "id":"22",
             "img_name":"dfdfdsf"
             }
             ],
             "back_visit":[
             {
             "img_type":"back_visit",
             "img_url":"http://fbdbfdb",
             "id":"21",
             "img_name":"sdsdsd"
             }
             ],
             "back_show":[
             {
             "img_type":"back_show",
             "img_url":"http://csdcsdc",
             "id":"23",
             "img_name":"ffasdfsadf"
             },
             {
             "img_type":"back_show",
             "img_url":"http://dasdasa",
             "id":"17",
             "img_name":"ccc"
             },
             {
             "img_type":"back_show",
             "img_url":"http://ascascas",
             "id":"18",
             "img_name":"xxx"
             },
             {
             "img_type":"back_show",
             "img_url":"http://ggfdgd",
             "id":"16",
             "img_name":"aaa"
             },
             {
             "img_type":"back_show",
             "img_url":"http://ggfdgd",
             "id":"3",
             "img_name":"aaa"
             },
             {
             "img_type":"back_show",
             "img_url":"http://dasdasa",
             "id":"2",
             "img_name":"ccc"
             },
             {
             "img_type":"back_show",
             "img_url":"http://ascascas",
             "id":"1",
             "img_name":"xxx"
             }
             ]
             }
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
     * 更新设备当前使用的模板的信息
     * 请求参数数据格式：
     *{
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
     *
     */
    @PostMapping("/refreshDeviceTemplate")
    public String refreshDeviceTemplate(@RequestBody String templateInfo){
        //更新模板信息
        boolean result= iTemplateService.modifyDeviceTemplateInfo(templateInfo);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }


    /**
     * 重置设备的模板（恢复成默认的主题）
             {
                 "deviceId":"1",
                 "templateIds":[
                         1,
                         2
                 ]
             }
     *
     */
    @PostMapping ("/clearDeviceTemplate")
    public String clearDeviceTemplate(@RequestBody String delTemplateInfo){

        JSONObject jsonObject = JSONObject.parseObject(delTemplateInfo);
        String deviceId = jsonObject.get("deviceId").toString();
        JSONArray templateIds = JSONArray.parseArray(jsonObject.get("templateIds").toString());

        List<String> list = new ArrayList<>();
        for(int i=0;i<templateIds.size();i++){
            list.add(templateIds.get(i).toString());
        }
        boolean result = iTemplateService.removeDeviceTemplate(deviceId, list);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * 添加自动义的模板
     *
     {
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
     */
    @PostMapping ("/addDeviceTemplate")
    public String addDeviceTemplate(@RequestBody String addTemplateInfo){

        boolean result = iTemplateService.addDeviceTemplate(addTemplateInfo);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * 查询所有标准模板的详细信息(关于坐标的问题，可以进行等比缩小。)
     */
    @GetMapping("/getStandardTemplateInfo")
    public String getStandardTemplateInfo(){
        List<Map> templateInfo = iTemplateService.queryStandardTemplateInfo();
        return JSONObject.toJSONString(templateInfo);
    }

    /**
     * 下发节日节气模板
     */
    public String issueFestivalTemplate(){

        return  null;
    }
}
