package com.xiangshangban.device.controller;

import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.ReturnData;
import com.xiangshangban.device.service.IOperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @Author Wangyonghui
 * @Date 2018/1/4 ~ 16:20
 * TODO
 */
@RestController
@RequestMapping("/operateLog")
public class OperateLogController {

    @Autowired
    private IOperateLogService iOperateLogService;


    /**
     * 添加操作日志(--测试--)
     * {
     *     "id":"",------>主键uuid
     *     "operateempid":"", --------->操作人员
     *     "operatetype":"",------->操作类型
     *     "operatecontent":"",------->操作内容
     *     "operatedate":""----->操作时间
     * }
     */
    @PostMapping("/addOperateLog")
    public String additionOperateLog(@RequestBody String requestParam, HttpServletRequest request){
        boolean result = iOperateLogService.addOperateLog(request.getHeader("accessUserId"),
                request.getHeader("companyId"),"1","补2018-01-02日的考勤");
        if(result){
            return "true";
        }else {
            return "false";
        }
    }

    /**
     * 查询操作日志(以及日志操作次数)
     * 请求参数：
     * {
     *         "searchTime":"2018-01-02~2018-01-03",（搜索时间）
     *         "operateType":"查看/补勤",(操作类型)
     *         "empDept":"部门",
     *         "empName":"人员名称",
     *         "sortFlag":"0",--------->排序的标志位（0：按照时间正序排列  1：按照时间倒序排列）
     *         "page":"",--------->当前页码
     *         "rows":""----------->每一页要显示的行数
     * }
     *
     * 返回数据格式：
     * {
     "data": {
     "operateLog": [
     {
     "operate_type": "补勤",
     "operate_content": "补2018-01-02日的考勤",
     "operate_date": "2018-01-04 17:02:41",
     "employee_department_name": "研发宝宝部",
     "employee_name": "刚宝宝"
     },
     {
     "operate_type": "补勤",
     "operate_content": "补2018-01-02日的考勤",
     "operate_date": "2018-01-04 17:02:02",
     "employee_department_name": "研发宝宝部",
     "employee_name": "航宝宝"
     },
     {
     "operate_type": "补勤",
     "operate_content": "补2018-01-02日的考勤",
     "operate_date": "2018-01-04 16:58:15",
     "employee_department_name": "研发宝宝部",
     "employee_name": "辉宝宝"
     }
     ],
     "operateCount": {
     "yesterday_count": 3,
     "thismonth_count": 6,
     "total_count": 6
     }
     },
     "message": "请求数据成功",
     "returnCode": "3000",
     "pagecountNum": "2",
     "totalPages": "6",
     "bluetoothId": null
     }
     *
     */
    @PostMapping("/getOperateLog")
    public ReturnData getOperateLog(@RequestBody String requestParam, HttpServletRequest request){
        //获取公司ID
        String companyId = request.getHeader("companyId");
        //返回数据
        ReturnData returnData = new ReturnData();
        if(companyId!=null && !companyId.isEmpty()){
            returnData = iOperateLogService.queryOperateLog(requestParam,companyId);
        }else{
            returnData.setReturnCode("4205");
            returnData.setMessage("未知的登录人（公司）ID");
        }
        return returnData;
    }

    /**
     * 分条件导出操作日志
     * 请求参数：
     * {
     *         "searchTime":"2018-01-02~2018-01-03",（搜索时间）
     *         "operateType":"查看/补勤",(操作类型)
     *         "empDept":"部门",
     *         "empName":"人员名称",
     *         "sortFlag":"0",--------->排序的标志位（0：按照时间正序排列  1：按照时间倒序排列）
     *         "flag":"3"----->导出操作日志
     * }
     */
    @PostMapping(value = "/exportOperateLog", produces = "application/json;charset=UTF-8")
    public void exportOperateLog(@RequestBody String requestParam, HttpServletRequest request, HttpServletResponse response){
        try {
            response.setContentType("application/octet-stream ");
            String agent = request.getHeader("USER-AGENT");
            String excelName = "unknown.xls";
            //解析请求的数据
            JSONObject jsonObject = JSONObject.parseObject(requestParam);
            //获取请求的标志
            Object flag = jsonObject.get("flag");

            if (flag != null && !flag.toString().trim().isEmpty()) {
                String status = flag.toString().trim();
                if (status.equals("0")) {
                    excelName = "inOutRecord.xls";
                }
                if (status.equals("1")) {
                    excelName = "doorExceptionRecord.xls";
                }
                if (status.equals("2")) {
                    excelName = "signInAndOutRecord.xls";
                }
                if(status.equals("3")){
                    excelName = "operateLog.xls";
                }
            }
            if (agent != null && agent.indexOf("MSIE") == -1 && agent.indexOf("rv:11") == -1 &&
                    agent.indexOf("Edge") == -1 && agent.indexOf("Apache-HttpClient") == -1) {//非IE
                excelName = new String(excelName.getBytes("UTF-8"), "ISO-8859-1");
                response.addHeader("Content-Disposition", "attachment;filename=" + excelName);
            } else {
                response.addHeader("Content-Disposition", "attachment;filename=" + java.net.URLEncoder.encode(excelName, "UTF-8"));
            }
            response.addHeader("excelName", java.net.URLEncoder.encode(excelName, "UTF-8"));
            //获取输出流
            OutputStream out = response.getOutputStream();
            // 获取公司ID
            String companyId = request.getHeader("companyId");
            //获取操作人ID
            String accessUserId = request.getHeader("accessUserId");
            if ((companyId != null && !companyId.isEmpty()) && (accessUserId != null && !accessUserId.isEmpty())) {
                iOperateLogService.exportRecordToExcel(requestParam, excelName, out, companyId);
                out.flush();
                //添加操作日志
                iOperateLogService.addOperateLog(accessUserId,companyId,"4","导出操作日志");
            } else {
                System.out.println("未知的登录人（公司）ID");
            }
        } catch (IOException e) {
            System.out.println("导出文件输出流出错了！" + e);
        }
    }
}
