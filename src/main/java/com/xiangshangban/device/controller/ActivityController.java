package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.xiangshangban.device.bean.Employee;
import com.xiangshangban.device.bean.Font;
import com.xiangshangban.device.common.utils.PageUtils;
import com.xiangshangban.device.common.utils.ReturnCodeUtil;
import com.xiangshangban.device.service.IEmployeeService;
import com.xiangshangban.device.service.ITemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * author : Administrator
 * date: 2017/11/4 10:04
 * describe: TODO   主题部分（牵涉到上传图片的地方，提交的时候要采用form表单的形式提交:提交一个JSON样式的数据，外带上传的文件file）
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {

    @Autowired
    private ITemplateService iTemplateService;

    @Autowired
    private IEmployeeService iEmployeeService;


    /*******************************************************************************************
     *                                                  TODO web端接口
     *****************************************************************************************/

    /**
     * TODO 查询"当前公司“所有（未解绑设备自定义）模板信息(首页展示)
     * 请求参数
     * {
     *      "companyId":"",------------>公司名称
     *      "deviceId":"",---------->搜索条件：设备编号
     *      "deviceName":"",------>搜索条件：设备名称
     *      "page":"",
     *      "rows":""
     * }
     *
     * 返回数据格式：
     {
         "message":"数据请求成功/请求数据不存在",
         "returnCode":"3000/4203",
         "data":[
<<<<<<< HEAD
             {
                 "template_id":"3",
                 "device_name":"设备1.1",
                 "device_id":"1",
                 "template_style":"vx",
                 "logo_flag":"有",
                 "salutation_flag":"已设置",
                 "operate_time":"2017-11-28 16:00",
                 "operate_emp":"员工1",
                 "status":"设置状态"(设置成功，设置失败，更换中..)
             }
         ]
     }*/
    @PostMapping("/getDeviceTemplate")
    public String getDeviceTemplateInfo(@RequestBody String requestParam,HttpServletRequest request ){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        Object deviceId = jsonObject.get("deviceId");
        Object deviceName = jsonObject.get("deviceName");
        //当前登录人公司ID
        String companyId = request.getHeader("companyId");
        //返回的数据
        Map resultMap  = new HashMap();
        //定义指令执行状态
        String[] commandStatus = {"待发送","设置中","设置成功","设置失败"};

        if(companyId!=null && !companyId.isEmpty()){
            Page pageObj = null;
            if(page!=null&&!page.toString().isEmpty()&&rows!=null&&!rows.toString().isEmpty()){
                pageObj = PageHelper.startPage(Integer.parseInt(page.toString()),Integer.parseInt(rows.toString()));
            }

            //TODO 查询操作
            List<Map> templates = iTemplateService.queryDeviceTemplateInfo(companyId,deviceId,deviceName);

            if(templates!=null && templates.size()>0){
                //数据处理（自定义文案、公司logo、相关的指令的执行状态更改为文字信息）
                for(int i=0;i<templates.size();i++){
                    templates.get(i).put("salutation_flag",templates.get(i).get("salutation_flag").toString().equals("1")?"已设置":"未设置");
                    templates.get(i).put("logo_flag",templates.get(i).get("logo_flag").toString().equals("1")?"有":"无");
                    //处理执行状态
                    for(int s=0;s<commandStatus.length;s++){
                        String status = templates.get(i).get("status").toString().trim();
                        if(status.equals(commandStatus[s])){
                            templates.get(i).put("status",commandStatus[s]);
                        }
                    }
                }
            }
            resultMap =  PageUtils.doSplitPage(null,templates,page,rows,pageObj,1);
        }else{
            resultMap =   ReturnCodeUtil.addReturnCode(3);
        }
        return JSONArray.toJSONString(resultMap);
    }

    /**
     * TODO 查询所有(指定)标准模板的预览图
     * 疑问？？？
     * ==========①：修改主题的时候，传递的是自定义模板的ID,要先获取自定义模板使用的标准模板的ID，然后查询===============
     * ==========②：修改主题的时候，传递的是自定义模板的ID,直接查询当前设备正在使用的背景图（预览图）===============
     *请求参数：（可有可无>没有的时候获取所有的标准模板的预览图）
     * {
     *      "templateId":"xxx"
     * }
     *
     * 返回的数据类型:
         * [
                 {
                     "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",----->预览图路径
                     "img_id":"27",
                     "template_id":"1",
                     "img_name":"back.png" ------->预览图名称
                 }
         ]
     */
    @GetMapping("/getTemplatePreview")
    public String getTemplatePreview(@RequestBody String requestParam){
        List<Map> templatePreview = iTemplateService.queryStandardTemplatePreview(requestParam);
        //返回给前端的数据
        Map resultMap = new HashMap();
        if(templatePreview!=null && templatePreview.size()>0){
            resultMap = ReturnCodeUtil.addReturnCode(templatePreview);
        }else{
            resultMap = ReturnCodeUtil.addReturnCode(null);
        }
        return JSONObject.toJSONString(resultMap);
    }

    /**
     * TODO 查询所有的背景图（根据类别进行分组）
     * 返回的参数
     * {
         "back_festival":[
             {
                 "img_type":"back_festival_节气",
                 "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
                 "id":"31",
                 "img_name":"festival.png"
             }
         ],
         "back_visit":[
             {
                 "img_type":"back_visit",
                 "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
                 "id":"34",
                 "img_name":"unknownBack.png"
             }
         ],
         "back_show":[
             {
                 "img_type":"back_show",
                 "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
                 "id":"33",
                 "img_name":"wooback.png"
             }
         ]
     }
     */
    @GetMapping("/getAllBackground")
    public String getAllBackGround(){

        List<Map> maps = iTemplateService.queryAllBackGround();
        //返回给前端的数据
        Map resultMap = new HashMap();
        if(maps!=null && maps.size()>0){
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
                if(maps.get(i).get("img_type").toString().contains("back_festival")){
                    back_festival.add(maps.get(i));
                }
            }
            map.put("back_show",back_show);
            map.put("back_visit",back_visit);
            map.put("back_festival",back_festival);

            resultMap = ReturnCodeUtil.addReturnCode(map);
        }else{
            resultMap = ReturnCodeUtil.addReturnCode(null);
        }
        return JSONObject.toJSONString(resultMap);
    }

    /**
     * TODO 根据模板的ID，查询模板的背景图以及显示时间（标准模板没有背景图，无需显示），以及自定义问候语的展示
     *  ①：请求参数
     *          {
     *              "templateId":"3"
     *          }
     *  ②：返回的数据格式
     *
     *   {
             "message":"数据请求成功",
             "returnCode":"3000",
             "data":{
             "background":[
                 {
                     "img_url":"http://xiangshangban.oss-cn-hangzhou.aliyuncs.com/test%2Fsys%2Fdevice%2Ftemplate%2Ftemplate1%2F",
                     "img_id":"34",
                     "broad_end_time":"2017-11-28 10:00",
                     "img_name":"unknownBack.png",
                     "broad_start_time":"2017-11-28 08:00"
                 }
             ],
             "salutation":[
                     {
                         "content":"快乐",------------------>问候语1
                         "startDate":"2017-11-29 10:00",
                         "endDate":"2017-11-29 12:00"
                     },
                     {
                         "content":"冬至",------------------>问候语2
                         "startDate":"2017-11-29 06:00",
                         "endDate":"2017-11-29 10:00"
                     }
             ]
            }
     }
     *
     */
    @PostMapping("/getTemplateBackAndTime")
    public String getTemplateBackAndTime(@RequestBody String requestParam){
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        //获取模板的ID
        Object templateId = jsonObject.get("templateId");
        //返回给前端的数据
        Map result = new HashMap();

        if(templateId!=null && !templateId.toString().isEmpty()){
            //查询背景图
            List<Map> backgroundAndTime = iTemplateService.queryTemplateBackAndTime(templateId.toString());
            //查询该自定义模板的问候语
            List<Map> templateSalutation = iTemplateService.queryTemplateSalutation(templateId.toString());

            //TODO 处理背景图
            Map background = null;
            if(backgroundAndTime!=null && backgroundAndTime.size()>0){
                //整理背景图的时间
                for(int i=0;i<backgroundAndTime.size();i++){
                    background = backgroundAndTime.get(i);
                    backgroundAndTime.get(i).put("broad_start_time",background.get("broad_start_date").toString()+" "+background.get("broad_start_time"));
                    backgroundAndTime.get(i).put("broad_end_time",background.get("broad_end_date").toString()+" "+background.get("broad_end_time"));

                    //移除broad_start_date和broad_end_date
                    backgroundAndTime.get(i).remove("broad_start_date");
                    backgroundAndTime.get(i).remove("broad_end_date");
                }
            }

            //TODO 处理问候语（按照时间对单个字进行整句话整合）
            Map<String,List<Map>> fontListMap = new HashMap<>();
            if(templateSalutation!=null && templateSalutation.size()>0){
                //判断问候语的内容和时间是否为null或者‘’，如果是的话表明该问候语所依附的模板是：标准模板和节假日专用模板。
                Object salutationStartTime = templateSalutation.get(0).get("item_start_date");
                Object salutationEndTime = templateSalutation.get(0).get("item_end_date");
                Object salutationContent = templateSalutation.get(0).get("item_font_content");

                if((salutationStartTime!=null && !salutationStartTime.toString().isEmpty())
                        && (salutationEndTime!=null && !salutationEndTime.toString().isEmpty())
                        &&(salutationContent!=null && !salutationContent.toString().isEmpty())){

                    for(int k=0;k<templateSalutation.size();k++){
                        String key =  templateSalutation.get(k).get("item_start_date")+"~"+templateSalutation.get(k).get("item_end_date");
                        if(fontListMap.containsKey(key)){
                            Map font = new HashMap();
                            font.put("item_top_x",templateSalutation.get(k).get("item_top_x"));
                            font.put("item_top_y",templateSalutation.get(k).get("item_top_y"));
                            font.put("item_font_orient",templateSalutation.get(k).get("item_font_orient"));
                            font.put("item_font_content",templateSalutation.get(k).get("item_font_content"));

                            fontListMap.get(key).add(font);
                        }else{
                            List<Map> fontList = new ArrayList<>();

                            Map font = new HashMap();

                            font.put("item_top_x",templateSalutation.get(k).get("item_top_x"));
                            font.put("item_top_y",templateSalutation.get(k).get("item_top_y"));
                            font.put("item_font_orient",templateSalutation.get(k).get("item_font_orient"));
                            font.put("item_font_content",templateSalutation.get(k).get("item_font_content"));

                            fontList.add(font);

                            fontListMap.put(key,fontList);
                        }
                    }
                    //保存所有的问候语
                    Map<String,List<Font>> salutationMap = new HashMap<>();
                    //TODO 根据文字的方向，以及坐标判断文字的先后顺序
                    for (String key: fontListMap.keySet()      //遍历所有的问候语（包含多句话）
                            ) {
                        //保存一句问候语的字
                        List<Font> fontList = new ArrayList<>();

                        List<Map> fontStyle = fontListMap.get(key);
                        //获取文字的方向
                        String fontOrient = fontStyle.get(0).get("item_font_orient").toString();
                        for (int f = 0; f < fontStyle.size(); f++) {
                            //将文字信息封装到font类中
                            Font font = new Font();
                            font.setCoordinateX(fontStyle.get(f).get("item_top_x").toString());
                            font.setCoordinateY(fontStyle.get(f).get("item_top_y").toString());
                            font.setFontOrient(fontOrient);
                            font.setContent(fontStyle.get(f).get("item_font_content").toString());

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
                        salutationMap.put(key,fontList);
                    }

                    //遍历问候语集合构建问候语内容
                    List<Map> salutationList = new ArrayList<>();
                    for(String key:salutationMap.keySet()){
                        Map map = new HashMap();
                        map.put("startDate",key.toString().split("~")[0].toString());
                        map.put("endDate",key.toString().split("~")[1].toString());
                        //问候语内容
                        String content = "";
                        //组拼单个字
                        for(int f = 0;f<salutationMap.get(key).size();f++){
                            content += salutationMap.get(key).get(f).getContent();
                        }
                        map.put("content",content);

                        salutationList.add(map);
                    }
                    //TODO 将背景图数据和问候语数据进行组拼
                    Map backgroundAndSalutation = new HashMap();
                    backgroundAndSalutation.put("background",backgroundAndTime);
                    backgroundAndSalutation.put("salutation",salutationList);

                    result = ReturnCodeUtil.addReturnCode(backgroundAndSalutation);
                }else{
                    result = ReturnCodeUtil.addReturnCode(false,"标准模板（节日标准模板）没有问候语和图片集");
                }
            }
        }else{
            //参数格式错误
            result = ReturnCodeUtil.addReturnCode(1);
        }
        return JSONObject.toJSONString(result);
    }

    //TODO &&&&&&&&&&&&&<更改进度>&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&

    /******************************************************************************************
     *                                                  TODO 设备端相关接口
     *****************************************************************************************/

    /**
     * TODO 添加自定义的模板（Logo（上传）/背景图/问候语）
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

      ]
     }
     */
    @PostMapping ("/addDeviceTemplate")
    public String addDeviceTemplate(HttpServletRequest request,
                                    @RequestParam("templateInfo") String templateInfo,
                                    @RequestParam("file")MultipartFile file){
        Map result = iTemplateService.addDeviceTemplate(request,templateInfo,file);
        return JSONObject.toJSONString(ReturnCodeUtil.addReturnCode(result));
    }

    /**
     * TODO 更新设备自定义模板信息
     *
     "salutationList":[ 请求参数数据格式：
         *{
         "deviceId":"1",
         "templateId":"2",------->首页展示的自定义模板ID
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
    public String refreshDeviceTemplate(@RequestParam("request") HttpServletRequest request,
                                        @RequestParam("templateInfo") String templateInfo,
                                        @RequestParam("file")MultipartFile file){
        //更新模板信息
        Map result= iTemplateService.modifyDeviceTemplateInfo(request,templateInfo,file);
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
