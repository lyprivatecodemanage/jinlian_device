package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.Employee;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
             "operateTime":"2017-11-03 16:23",
             "deviceName":"设备1",
             "items":[
             {
             "content":"上午",
             "startTime":"2017-11-03 08:00",
             "itemType":0,
             "itemId":"16,26",
             "endTime":"2017-11-03 10:00"
             },
             {
             "content":"中午",
             "startTime":"2017-11-03 10:00",
             "itemType":0,
             "itemId":"17,27",
             "endTime":"2017-11-03 12:00"
             },
             {
             "content":"下午",
             "startTime":"2017-11-03 12:00",
             "itemType":0,
             "itemId":"18,28",
             "endTime":"2017-11-03 18:00"
             },
             {
             "imgUrl":"http://xsxasa",
             "imgName":"logo",
             "itemType":"8",
             "itemId":"15"
             }
             ],
             "templateStyle":"v1",
             "images":[
             {
             "img_url":"http://dasdasa",
             "id":"17",
             "img_name":"ccc"
             },
             {
             "img_url":"http://ascascas",
             "id":"18",
             "img_name":"xxx"
             },
             {
             "img_url":"http://ggfdgd",
             "id":"16",
             "img_name":"aaa"
             }
             ],
             "companyLogo":"有",
             "salutation":"未设置",
             "operateEmployee":"员工1",
             "deviceId":"1"
             }
             ]
     */
    @RequestMapping("/getDeviceTemplate")
    public String getDeviceTemplateInfo(String deviceId,String deviceName){

        List<Map> templates = iTemplateService.queryDeviceTemplateInfo(deviceId,deviceName);

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
                List<String> idKey = new ArrayList<String>();
                Map logoMap = new HashMap();
                for(int j=0;j<templateItems.size();j++){
                    itemType = templateItems.get(j).get("item_type").toString();
                    //type==0表明是文字信息
                    if(itemType.equals("0")) {
                        timeScope = templateItems.get(j).get("item_start_date").toString() + "~" + templateItems.get(j).get("item_end_date").toString();
                        //对时间进行判断
                        if (resultMap.containsKey(timeScope)) {
                            resultMap.get(timeScope).put(templateItems.get(j).get("item_id").toString(), templateItems.get(j).get("item_font_content").toString());
                            timeKey.add(timeScope);
                        } else {
                            Map innerMap = new HashMap();
                            innerMap.put(templateItems.get(j).get("item_id").toString(), templateItems.get(j).get("item_font_content").toString());
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
                StringBuffer ids = new StringBuffer();

                for(int k=0;k<timeKey.size();k++){

                    Map myMap = new HashMap();
                    for(String key:  resultMap.get(timeKey.get(k)).keySet()){
                        content.append(resultMap.get(timeKey.get(k)).get(key));
                        ids.append(key+",");
                    }

                    String[] times = timeKey.get(k).split("~");
                    myMap.put("startTime",times[0]);
                    myMap.put("endTime",times[1]);
                    myMap.put("content",content.toString());
                    myMap.put("itemId",ids.toString().substring(0,ids.toString().length()-1));
                    myMap.put("itemType",0);
                    listMap.add(myMap);

                    //清空buffer
                    content.delete(0,content.length());
                    ids.delete(0,ids.length());
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
        return JSONArray.toJSONString(fullInfo);
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
        int i = iTemplateService.modifyDeviceTemplateInfo(templateInfo);
        if(i>0){
            return "true";
        }else{
            return "false";
        }
    }

    /**
     * 给设备添加模板
     */
    @RequestMapping("/increaseDeviceTemplate")
    public String increaseDeviceTemplate(String templateInfo){

        return "";
    }


    /**
     * 查询所有的背景图（根据类别进行分组）
     *
     *
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
    @RequestMapping("/getAllBackground")
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
     * 删除设备的主题模板
             {
                 "deviceId":"1",
                 "templateIds":[
                         1,
                         2
                 ]
             }
     *
     */
    @RequestMapping("/clearDeviceTemplate")
    public String clearDeviceTemplate(@RequestBody String delTemplateInfo){
        JSONObject jsonObject = JSONObject.parseObject(delTemplateInfo);
        String deviceId = jsonObject.get("deviceId").toString();
        JSONArray templateIds = JSONArray.parseArray(jsonObject.get("templateIds").toString());

        List<String> list = new ArrayList<>();
        for(int i=0;i<templateIds.size();i++){
            list.add(templateIds.get(i).toString());
        }
        int delResult = iTemplateService.removeDeviceTemplate(deviceId, list);
        if(delResult>0){
            return "true";
        }else{
            return "false";
        }
    }

    /**
     * 添加设备模板
     * {
             "deviceId":"1",
             "templateId":"2",-------->选择的标准模板的ID
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
    @RequestMapping("/addDeviceTemplate")
    public String addDeviceTemplate(@RequestBody String addTemplateInfo){

        int addResult = iTemplateService.addDeviceTemplate(addTemplateInfo);
        if(addResult>0){
            return "true";
        }else{
            return "false";
        }
    }
}
