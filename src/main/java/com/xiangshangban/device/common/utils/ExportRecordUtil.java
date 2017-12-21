package com.xiangshangban.device.common.utils;

import com.xiangshangban.device.bean.SignInAndOut;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.HSSFColor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 导出记录(出入记录、门禁异常、签到签退情况)到Excel
 */
public class ExportRecordUtil {

    /**
     * @param exportData 要导出的数据
     * @param excelName 导出后的表格的名称
     * @param headers 表头
     * @param out 输出流
     * @param flag 标志位：
     *             0 ---->出入记录
     *             1----->门禁异常
     *             2----->签到签退记录
     */
    public static void exportAnyRecordToExcel(List<?> exportData, String excelName, String[] headers,OutputStream out,int flag){
        if(exportData!=null && exportData.size()>0){
            // 第一步，创建一个webbook，对应一个Excel文件
            HSSFWorkbook workbook = new HSSFWorkbook();
            //生成一个表格
            HSSFSheet sheet = workbook.createSheet(excelName);
            //设置表格默认列宽度为15个字符
            sheet.setDefaultColumnWidth(15);
            //生成一个样式，用来设置标题样式
            HSSFCellStyle style = workbook.createCellStyle();
            //设置这些样式
            style.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
            style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            style.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            style.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            style.setBorderRight(HSSFCellStyle.BORDER_THIN);
            style.setBorderTop(HSSFCellStyle.BORDER_THIN);
            style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            //生成一个字体
            HSSFFont font = workbook.createFont();
            font.setColor(HSSFColor.VIOLET.index);
            font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
            //把字体应用到当前的样式
            style.setFont(font);

            // 生成并设置另一个样式,用于设置内容样式
            HSSFCellStyle contentStyle = workbook.createCellStyle();
            contentStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
            contentStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
            contentStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
            contentStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
            contentStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
            contentStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
            contentStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
            contentStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
            // 生成另一个字体
            HSSFFont font2 = workbook.createFont();
            font2.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
            // 把字体应用到当前的样式
            contentStyle.setFont(font2);

            //TODO ==========产生表格标题行===========
            HSSFRow row = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                //创建一个个的表格
                HSSFCell cell = row.createCell(i);
                //设置标题表格的样式
                cell.setCellStyle(style);
                HSSFRichTextString text = new HSSFRichTextString(headers[i]);
                //设置表格内容
                cell.setCellValue(text);
            }

            //TODO  =========保存数据部分============
            switch (flag){
                case 0: //TODO 出入记录
                    System.out.println("-------《导出出入记录》------");
                    for(int a = 0;a<exportData.size();a++){
                        Map inOutMap = (Map)exportData.get(a);
                        //从表格的第二行开始
                        row = sheet.createRow(a + 1);
                        //创建字符串数组，保存每一行表格中的内容
                        String[] doorCellContent = {inOutMap.get("employee_name").toString().trim(),
                                inOutMap.get("employee_department_name").toString().trim(),
                                inOutMap.get("deviceName").toString().trim(),
                                inOutMap.get("record_type_name").toString().trim(),
                                inOutMap.get("record_date").toString().trim()};

                        for(int x=0;x<headers.length;x++){
                            HSSFCell cell = row.createCell(x);
                            //设置内容表格的样式
                          /*  cell.setCellStyle(contentStyle);*/
                            //设置显示的内容
                            cell.setCellValue(doorCellContent[x]);
                        }
                    }
                    break;
                case 1://TODO 门禁异常
                    System.out.println("-------《导出门禁异常》------");
                    for(int b=0;b<exportData.size();b++){
                        Map exceptionMap = (Map)exportData.get(b);
                        //从表格的第二行开始
                        row = sheet.createRow(b+ 1);
                        //创建字符串数组，保存每一行表格中的内容
                        String[] exceptionCellContent = {exceptionMap.get("employee_name").toString().trim(),
                                exceptionMap.get("employee_department_name").toString().trim(),
                                exceptionMap.get("deviceName").toString().trim(),
                                exceptionMap.get("alarm_date").toString().trim(),
                                exceptionMap.get("alarm_type_name").toString().trim()};

                        for(int y=0;y<headers.length;y++){
                            HSSFCell cell = row.createCell(y);
                            //设置内容表格的样式
                          /*  cell.setCellStyle(contentStyle);*/
                            //设置显示的内容
                            cell.setCellValue(exceptionCellContent[y]);
                        }
                    }
                    break;
                case 2://TODO 签到签退
                    System.out.println("-------《导出签到签退记录》------");
                    for (int c = 0; c < exportData.size(); c++) {
                        SignInAndOut sign = (SignInAndOut)exportData.get(c);
                        System.out.println("************************签到签退内容:"+sign.toString()+"*****************************");
                        //从表格的第二行开始
                        row = sheet.createRow(c + 1);
                        //分割时间(日期+时间)
                        String[] signInArr = sign.getSignIn().trim().split(" "); //签到
                        String[] signOutArr = sign.getSignOut().trim().split(" ");//签退

                        //创建字符串数组，保存每一行表格中的内容
                        String[] cellContent = {sign.getEmpId(),sign.getEmpName(),sign.getEmpDept(),signInArr[0],signInArr[1]+"/"+signOutArr[1]};

                        for(int z=0;z<headers.length;z++){
                            HSSFCell cell = row.createCell(z);
                            //设置内容表格的样式
                         /*   cell.setCellStyle(contentStyle);*/
                            //设置显示的内容
                            cell.setCellValue(cellContent[z]);
                        }
                    }
                    break;
                    default:
                        break;
            }
            try {
                //将数据写入输出流
                workbook.write(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
