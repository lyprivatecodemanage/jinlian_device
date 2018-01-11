package com.xiangshangban.device.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.xiangshangban.device.bean.OperateLog;
import com.xiangshangban.device.bean.ReturnData;
import com.xiangshangban.device.bean.SignInAndOut;
import com.xiangshangban.device.common.utils.DateUtils;
import com.xiangshangban.device.common.utils.ExportRecordUtil;
import com.xiangshangban.device.common.utils.FormatUtil;
import com.xiangshangban.device.dao.OperateLogMapper;
import com.xiangshangban.device.service.IOperateLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author Wangyonghui
 * @Date 2018/1/4 ~ 11:19
 * TODO   操作日志业务类
 */
@Service
public class OperateLogServiceImpl implements IOperateLogService{

    @Autowired
    private OperateLogMapper operateLogMapper;

    /**
     * 添加操作日志
     * @param operateEmp 操作人
     * @param operateEmpCompany 操作人所在公司
     * @param operateType 操作类型 （0:查看、1:补勤、2:年假调整、3:调休调整、4:报表导出）
     * @param operateContent 操作内容
     * @return
     */
    @Override
    public boolean addOperateLog(String operateEmp,String operateEmpCompany,String operateType,String operateContent) {
        //创建OperateLog对象
        OperateLog operateLog = new OperateLog();
        operateLog.setId(FormatUtil.createUuid());
        operateLog.setOperateEmpId(operateEmp);
        operateLog.setOperateEmpCompany(operateEmpCompany);
        operateLog.setOperateType(operateType);
        operateLog.setOperateContent(operateContent);
        operateLog.setOperateDate(DateUtils.getDateTime());

        int insertResult = operateLogMapper.insertSelective(operateLog);
        if(insertResult>0){
            return true;
        }else {
            return false;
        }
    }

    /**
     *查询操作日志
     * @param requestParam
     * 请求参数：
     * {
     *         "searchTime":"2018-01-02~2018-01-03",（搜索时间）
     *         "operateType":"0",(操作类型)
     *         "empDept":"部门",
     *         "empName":"人员名称",
     *         "sortFlag":"0",--------->排序的标志位（0：按照时间正序排列  1：按照时间倒序排列）
     *         "page":"",--------->当前页码
     *         "rows":""----------->每一页要显示的行数
     * }
     * @return
     */
    @Override
    public ReturnData queryOperateLog(String requestParam, String companyId) {
        //解析请求参数
        JSONObject jsonObject = JSONObject.parseObject(requestParam);
        Object searchTime = jsonObject.get("searchTime");
        Object operateType = jsonObject.get("operateType");
        Object empDept = jsonObject.get("empDept");
        Object empName = jsonObject.get("empName");
        Object sortFlag = jsonObject.get("sortFlag");
        Object page = jsonObject.get("page");
        Object rows = jsonObject.get("rows");
        //设置返回的数据
        ReturnData returnData = new ReturnData();
        //初始化(数据总行数/数据总页数)
        int totalRows = 0;
        int totalPage = 0;
        //请求参数处理
        Map param = new HashMap<>();
        if(searchTime!=null && !searchTime.toString().trim().isEmpty()){
            param.put("startDate",searchTime.toString().trim().split("~")[0].trim());
            param.put("endDate",searchTime.toString().trim().split("~")[1].trim());
        }else{
            param.put("startDate",null);
            param.put("endDate",null);
        }

        param.put("operateType",(operateType!=null && !operateType.toString().trim().isEmpty())?operateType.toString().trim():null);
        param.put("empDept",(empDept!=null && ! empDept.toString().trim().isEmpty())?empDept.toString().trim():null);
        param.put("empName",(empName!=null && ! empName.toString().trim().isEmpty())?"%"+empName.toString().trim()+"%":null);
        param.put("sortFlag",(sortFlag!=null && !sortFlag.toString().trim().isEmpty())?sortFlag.toString().trim():null);
        param.put("companyId",companyId);

        if ((rows!=null && !rows.toString().trim().isEmpty()) && (page!=null && !page.toString().trim().isEmpty())){
            param.put("rows",rows.toString().trim());
            param.put("offset",(Integer.parseInt(page.toString().trim())-1)*Integer.parseInt(rows.toString().trim()));
            //获取数据的总行数
            totalRows = operateLogMapper.selectCountByCondition(param);
            //设置总页数
            totalPage = totalRows%Integer.parseInt(rows.toString().trim())==0?totalRows/Integer.parseInt(rows.toString().trim()):totalRows/Integer.parseInt(rows.toString().trim())+1;
        }else{
            param.put("rows",null);
            param.put("offset",null);
        }
        List<Map> operateLogs = operateLogMapper.selectOperateLog(param);
        //查询日志操作次数
        Map getCountParam = new HashMap();
        getCountParam.put("yesterday", "%"+DateUtils.getYesterdayDate()+"%"); //昨天
        getCountParam.put("thisMonth", "%"+DateUtils.getDate().split("-")[0].trim()+"-"+DateUtils.getDate().split("-")[1].trim()+"%");//本月
        getCountParam.put("companyId", companyId);//公司ID

        Map operateCount = operateLogMapper.selectOperateCount(getCountParam);
        //封装数据
        Map outterMap = new HashMap();
        outterMap.put("operateLog",operateLogs);
        outterMap.put("operateCount",operateCount);

        returnData.setData(outterMap);
        returnData.setReturnCode("3000");
        returnData.setMessage("请求数据成功");
        returnData.setTotalPages(String.valueOf(totalRows));
        returnData.setPagecountNum(String.valueOf(totalPage));

        return returnData;
    }

    /**
     * 导出操作日志
     * @param requestParam 请求参数
     * @param excelName 导出的文件名称
     * @param out 输出流
     * @param companyId 公司ID
     */
    @Override
    public void exportRecordToExcel(String requestParam, String excelName, OutputStream out, String companyId) {
        //解析请求参数
        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(requestParam);
        Object flag = jsonObject.get("flag");

        if(flag!=null && !flag.toString().trim().isEmpty()){
            int value =  Integer.parseInt(flag.toString().trim());
            switch (value){
                case 0:  //条件查询出入记录
                   /* Map doorRecord = entranceGuardController.getDoorRecordAndException(requestParam, companyId, 0);
                    Object doorData = doorRecord.get("data");
                    if(doorData!=null && !doorData.toString().isEmpty()){
                        //要导出的出入记录数据
                        List<Map> doorResource = (List<Map>)doorData;
                        String[] inOutHeaders = new String[]{"姓名", "所属部门", "设备名称", "打卡方式","打卡时间"};
                        //导出出入记录
                        ExportRecordUtil.exportAnyRecordToExcel(doorResource,excelName,inOutHeaders,out,value);
                    }*/
                    break;
                case 1:  //条件查询门禁异常
                    /*Map exceptionRecord = entranceGuardController.getDoorRecordAndException(requestParam, companyId, 1);
                    Object exceptionData = exceptionRecord.get("data");
                    if(exceptionData!=null && !exceptionData.toString().isEmpty()){
                        //要导出的门禁异常记录数据
                        List<Map> doorExceptionResource = (List<Map>)exceptionData;
                        String[] exceptionHeaders = new String[]{"设备名称", "日期","报警记录"};
                        //导出门禁异常记录
                        ExportRecordUtil.exportAnyRecordToExcel(doorExceptionResource,excelName,exceptionHeaders,out,value);
                    }*/
                    break;
                case 2:  //条件查询签到签退记录
                 /*   Object empName = jsonObject.get("empName");
                    Object deptName = jsonObject.get("deptName");
                    Object recordTime = jsonObject.get("recordTime");
                    //获取签到签退记录
                    Map param = new HashMap();
                    param.put("companyId",companyId);
                    param.put("empName",(empName!=null && !empName.toString().isEmpty())?"%"+empName.toString()+"%":null);
                    param.put("deptName",(deptName!=null && !deptName.toString().isEmpty())?"%"+deptName.toString()+"%":null);
                    if(recordTime!=null && !recordTime.toString().trim().isEmpty()){
                        param.put("recordStartTime",recordTime.toString().split("~")[0]);
                        param.put("recordEndTime",recordTime.toString().split("~")[1]);
                    }else{
                        param.put("recordStartTime",null);
                        param.put("recordEndTime",null);
                    }
                    //查询签到记录
                    List<SignInAndOut> signInAndOutRecord = doorRecordMapper.selectSignInAndOutRecord(param);

                    String[] signInAndOutHeaders = new String[]{"ID","姓名", "所属部门", "打卡日期", "签到/签退"};
                    //导出签到签退记录
                    ExportRecordUtil.exportAnyRecordToExcel(signInAndOutRecord,excelName,signInAndOutHeaders,out,value);*/
                    break;
                case 3://条件查询操作日志
                    //解析请求参数
                    Object searchTime = jsonObject.get("searchTime");
                    Object operateType = jsonObject.get("operateType");
                    Object empDept = jsonObject.get("empDept");
                    Object empName = jsonObject.get("empName");
                    Object sortFlag = jsonObject.get("sortFlag");
                    //设置返回的数据
                    ReturnData returnData = new ReturnData();
                    //请求参数处理
                    Map param = new HashMap<>();
                    if(searchTime!=null && !searchTime.toString().trim().isEmpty()){
                        param.put("startDate",searchTime.toString().trim().split("~")[0].trim());
                        param.put("endDate",searchTime.toString().trim().split("~")[1].trim());
                    }else{
                        param.put("startDate",null);
                        param.put("endDate",null);
                    }

                    param.put("operateType",(operateType!=null && !operateType.toString().trim().isEmpty())?operateType.toString().trim():null);
                    param.put("empDept",(empDept!=null && ! empDept.toString().trim().isEmpty())?empDept.toString().trim():null);
                    param.put("empName",(empName!=null && ! empName.toString().trim().isEmpty())?"%"+empName.toString().trim()+"%":null);
                    param.put("sortFlag",(sortFlag!=null && !sortFlag.toString().trim().isEmpty())?sortFlag.toString().trim():null);
                    param.put("companyId",companyId);
                    param.put("rows",null);
                    param.put("offset",null);
                    //操作日志记录
                    List<Map> operateLogs = operateLogMapper.selectOperateLog(param);
                    //定义表格头
                    String[] operateLogsHeaders = new String[]{"部门","操作人", "操作类型", "操作内容", "操作时间"};
                    //导出操作日志
                    ExportRecordUtil.exportAnyRecordToExcel(operateLogs,excelName,operateLogsHeaders,out,value);
                    break;
                default:
                    break;
            }
        }
    }
}
