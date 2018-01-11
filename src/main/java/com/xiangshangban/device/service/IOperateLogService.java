package com.xiangshangban.device.service;

import com.xiangshangban.device.bean.OperateLog;
import com.xiangshangban.device.bean.ReturnData;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * @Author Wangyonghui
 * @Date 2018/1/4 ~ 11:14
 * TODO
 */
public interface IOperateLogService {

    /**
     * 添加操作日志
     * @param operateEmp 操作人
     * @param operateEmpCompany 操作人所在公司
     * @param operateType 操作类型 （0:查看、1:补勤、2:年假调整、3:调休调整、4:报表导出）
     * @param operateContent 操作内容
     * @return
     */
    boolean addOperateLog(String operateEmp,String operateEmpCompany,String operateType,String operateContent);

    /**
     * 查询操作日志
     */
    ReturnData queryOperateLog(String requestParam, String companyId);

    /**
     * 导出操作日志
     */
    void exportRecordToExcel(String requestParam, String excelName, OutputStream out,String companyId);

}
